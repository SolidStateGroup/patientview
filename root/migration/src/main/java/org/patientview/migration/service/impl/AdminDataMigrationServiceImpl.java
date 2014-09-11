package org.patientview.migration.service.impl;

import org.apache.commons.collections.CollectionUtils;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.patientview.Code;
import org.patientview.ContactPoint;
import org.patientview.ContactPointType;
import org.patientview.Feature;
import org.patientview.Group;
import org.patientview.GroupFeature;
import org.patientview.GroupRole;
import org.patientview.Link;
import org.patientview.Lookup;
import org.patientview.ObservationHeading;
import org.patientview.Role;
import org.patientview.enums.Roles;
import org.patientview.migration.service.AdminDataMigrationService;
import org.patientview.migration.util.JsonUtil;
import org.patientview.migration.util.PvUtil;
import org.patientview.migration.util.exception.JsonMigrationException;
import org.patientview.migration.util.exception.JsonMigrationExistsException;
import org.patientview.model.Unit;
import org.patientview.patientview.model.EdtaCode;
import org.patientview.patientview.model.ResultHeading;
import org.patientview.repository.EdtaCodeDao;
import org.patientview.repository.FeatureDao;
import org.patientview.repository.ResultHeadingDao;
import org.patientview.repository.UnitDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by james@solidstategroup.com
 * Created on 05/06/2014
 */
@Service
public class AdminDataMigrationServiceImpl implements AdminDataMigrationService {

    private static final Logger LOG = LoggerFactory.getLogger(AdminDataMigrationServiceImpl.class);

    @Inject
    private UnitDao unitDao;

    @Inject
    private EdtaCodeDao edtaCodeDao;

    @Inject
    private ResultHeadingDao resultHeadingDao;

    @Inject
    private FeatureDao featureDao;

    private List<Group> groups;
    private List<Role> roles;
    private List<Lookup> lookups;
    private List<Feature> features;

    private Group renal;
    private Group diabetes;
    private Group ibd;

    @Override
    public Group getRenal() {
        return renal;
    }

    @Override
    public Group getDiabetes() {
        return diabetes;
    }

    @Override
    public Group getIbd() {
        return ibd;
    }

    @PostConstruct
    public void init() {
        lookups = JsonUtil.getStaticDataLookups(JsonUtil.pvUrl + "/lookup");
        features = JsonUtil.getStaticDataFeatures(JsonUtil.pvUrl + "/feature");
        groups = JsonUtil.getGroups(JsonUtil.pvUrl + "/group");
        roles = JsonUtil.getRoles(JsonUtil.pvUrl + "/role");
    }

    public void migrate() {
        createGroups();
        createCodes(getLookupByName("DIAGNOSIS"), "edtaCode");
        createCodes(getLookupByName("TREATMENT"), "treatment");
        createObservationHeadings();
    }

    public void createGroups() {
        // Export a dummy group to test hibernate
        sendDummyUnit();

        for (Unit unit : unitDao.getAll(false)) {

            Set<Feature> unitFeatures = getUnitFeatures(unit);

            LOG.info("Got unit: {}", unit.getUnitcode());

            // Create the unit
            Group group = createGroup(unit);
            group = callApiCreateGroup(group);

            // Create the features
            if (CollectionUtils.isNotEmpty(unitFeatures)) {
                for (Feature feature : unitFeatures) {
                    if (group != null) {
                        callApiCreateGroupFeature(group, feature);
                    }
                }
            }

            // Assign a specialty
            Group parentGroup = getGroupParent(unit);
            if (parentGroup != null && group != null) {
                callApiCreateParentGroup(group, parentGroup);
            } else {
                LOG.error("Unable to find parent group");
            }

            // Add the contact points
            if (group != null) {
                for (ContactPoint contactPoint : createGroupContactPoints(unit)) {
                    callApiCreateContactPoint(group, contactPoint);
                }
            }
        }
    }

    private void callApiCreateParentGroup(Group group, Group parentGroup) {
        String featureUrl = JsonUtil.pvUrl + "/group/" + group.getId() + "/parent/" + parentGroup.getId();

        try {
            JsonUtil.jsonRequest(featureUrl, GroupRole.class, null, HttpPut.class);
            LOG.info("Success: feature created for group");
        } catch (JsonMigrationException jme) {
            LOG.error("Unable to parent group: ", jme.getMessage());

        } catch (JsonMigrationExistsException jee) {
            LOG.error("Unable to parent group: ", jee.getMessage());
        } catch (Exception e) {
            LOG.error("Unable to parent group: ", e.getMessage());
        }
    }

    private GroupFeature callApiCreateGroupFeature(Group group, Feature feature) {

        String featureUrl = JsonUtil.pvUrl + "/group/" + group.getId() + "/feature/" + feature.getId();

        try {
            return JsonUtil.jsonRequest(featureUrl, GroupFeature.class, null, HttpPut.class);
        } catch (JsonMigrationException jme) {
            LOG.error("Unable to create group feature: ", jme.getMessage());
        } catch (JsonMigrationExistsException jee) {
            LOG.info("Could not create group feature for {}", group.getName());
        } catch (Exception jee) {
            LOG.info("Could not create group feature for {}", group.getName());
        }

        LOG.info("Success: feature created for group");

        return null;
    }

    private ContactPoint callApiCreateContactPoint(Group group, ContactPoint contactPoint) {

        String featureUrl = JsonUtil.pvUrl + "/group/" + group.getId() + "/contactpoints";

        try {
            return JsonUtil.jsonRequest(featureUrl, ContactPoint.class, contactPoint, HttpPost.class);
        } catch (JsonMigrationException jme) {
            LOG.error("Unable to create contact point: ", jme.getMessage());
        } catch (JsonMigrationExistsException jee) {
            LOG.info("Could not create contact point: {}", group.getName());
        } catch (Exception jee) {
            LOG.info("Could not create contact point: {}", group.getName());
        }

        return null;
    }


    private Group callApiCreateGroup(Group group) {
        Group newGroup = null;
        try {
            newGroup = JsonUtil.jsonRequest(JsonUtil.pvUrl + "/group", Group.class, group, HttpPost.class);
            LOG.info("Success: created group");
        } catch (JsonMigrationException jme) {
            LOG.error("Unable to create group: ", jme.getMessage());
        } catch (JsonMigrationExistsException jee) {
            LOG.info("Group {} already exists", group.getName());
        }

        return newGroup;
    }

    private Group callApiGetGroup(Long groupId) {
        Group newGroup = null;
        try {
            newGroup = JsonUtil.jsonRequest(JsonUtil.pvUrl + "/group/" + groupId, Group.class, null, HttpGet.class);
        } catch (JsonMigrationException jme) {
            LOG.error("Unable to get group: ", jme.getMessage());
        } catch (JsonMigrationExistsException jee) {
            LOG.error("Unable to get group: ", jee.getMessage());
        }

        return newGroup;
    }

    private Code callApiCreateCode(Code code) {
        Code newCode = null;
        try {
            newCode = JsonUtil.jsonRequest(JsonUtil.pvUrl + "/code", Code.class, code, HttpPost.class);
            LOG.info("Created code");
        } catch (JsonMigrationException jme) {
            LOG.error("Unable to create code: ", jme.getMessage());
        } catch (JsonMigrationExistsException jee) {
            LOG.error("Unable to create code: ", jee.getMessage());
        }

        return newCode;
    }

    private ObservationHeading callApiCreateObservationHeading(ObservationHeading observationHeading) {
        ObservationHeading newObservationHeading = null;
        try {
            newObservationHeading = JsonUtil.jsonRequest(JsonUtil.pvUrl + "/observationheading",
                    ObservationHeading.class, observationHeading, HttpPost.class);
            LOG.info("Created observation heading");
        } catch (JsonMigrationException jme) {
            LOG.error("Unable to create observation heading: ", jme.getMessage());
        } catch (JsonMigrationExistsException jee) {
            LOG.error("Unable to create observation heading: ", jee.getMessage());
        }

        return newObservationHeading;
    }

    private Link callApiCreateLink(Link link) {
        Link newLink = null;
        try {
            newLink = JsonUtil.jsonRequest(JsonUtil.pvUrl + "/link", Link.class, link, HttpPost.class);
            LOG.info("Created link");
        } catch (JsonMigrationException jme) {
            LOG.error("Unable to create link: ", jme.getMessage());
        } catch (JsonMigrationExistsException jee) {
            LOG.error("Unable to create link: ", jee.getMessage());
        }

        return newLink;
    }

    private ContactPointType callApiGetType(String type) {
        ContactPointType newLink = null;
        String url = JsonUtil.pvUrl + "/contactpoint/type/" + type;
        try {
            newLink = JsonUtil.jsonRequest(url, ContactPointType.class, null , HttpGet.class);
            LOG.info("Got Contact Point Type");
        } catch (JsonMigrationException jme) {
            LOG.error("Unable to get contact point: ", jme.getMessage());
        } catch (JsonMigrationExistsException jee) {
            LOG.error("Unable to get contact point: ", jee.getMessage());
        }

        return newLink;
    }

    private void sendDummyUnit() {

        // Export a dummy group to test hibernate until one works
        // FIXME need hibernate to read the table indexes on startup
        Group group = new Group();
        group.setName("TEST_GROUP");
        group.setCode("TEST");
        group.setGroupType(getLookupByName("UNIT"));

        int i = 0;
        while (i < 10) {
            try {
                group = JsonUtil.jsonRequest(JsonUtil.pvUrl + "/group", Group.class, group, HttpPost.class);
                // Delete the test group once we have successfully created one
                group = JsonUtil.jsonRequest(JsonUtil.pvUrl + "/group/" + group.getId(), null, null, HttpDelete.class);
                break;
            } catch (JsonMigrationException jme) {
                LOG.trace("Unable to create group: ", jme.getMessage());
            } catch (JsonMigrationExistsException jee) {
                LOG.trace("Group {} already exists", group.getName());
            }
            i++;
        }
    }

    public Set<Feature> getUnitFeatures(Unit unit) {
        Set<Feature> unitFeatures = new HashSet<Feature>();

        if (unit.isSharedThoughtEnabled()) {
            unitFeatures.add(getFeatureByName("SHARING_THOUGHTS"));
        }

        if (unit.isFeedbackEnabled()) {
            unitFeatures.add(getFeatureByName("FEEDBACK"));
        }

        if (featureDao.getUnitsForFeature("messaging").contains(unit)) {
            unitFeatures.add(getFeatureByName("MESSAGING"));
        }

        return  unitFeatures;
    }

    public void createCodes(Lookup codeType, String codeTypeName) {

        int i = 1;
        for (EdtaCode edtaCode : edtaCodeDao.get(codeTypeName, null)) {
            Code code = new Code();
            code.setDisplayOrder(i++);
            code.setStandardType(getLookupByName("EDTA"));
            code.setCodeType(codeType);
            code.setDescription(edtaCode.getDescription());
            code.setCode(edtaCode.getEdtaCode());
            code.setLinks(PvUtil.getLinks(edtaCode));
            callApiCreateCode(code);
        }
    }

    public void createObservationHeadings() {

        // note: gets defaults from first instance of specialty result headings
        // todo: create specialty specific panel ordering based on existing

        for (ResultHeading resultHeading : resultHeadingDao.getAll(null)) {
            ObservationHeading observationHeading = new ObservationHeading();
            observationHeading.setCode(resultHeading.getHeadingcode());
            observationHeading.setHeading(resultHeading.getHeading());
            observationHeading.setName(resultHeading.getRollover());
            observationHeading.setInfoLink(resultHeading.getLink());
            observationHeading.setDefaultPanel(
                    (long)resultHeading.getSpecialtyResultHeadings().iterator().next().getPanel());
            observationHeading.setDefaultPanelOrder(
                    (long)resultHeading.getSpecialtyResultHeadings().iterator().next().getPanelOrder());
            observationHeading.setMinGraph(resultHeading.getMinRangeValue());
            observationHeading.setMaxGraph(resultHeading.getMaxRangeValue());
            observationHeading.setUnits(resultHeading.getUnits());
            callApiCreateObservationHeading(observationHeading);
        }
    }

    @Override
    public Lookup getLookupByName(String value) {
        for (Lookup lookup : lookups) {
            if (lookup.getValue().equalsIgnoreCase(value)) {
                return  lookup;
            }
        }
        return null;
    }

    public Group getGroupByCode(String code) {
        for (Group group : groups) {
            if (group.getCode().equalsIgnoreCase(code)) {
                return group;
            }
        }
        return null;
    }

    public Group getGroupByName(String name) {
        for (Group group : groups) {
            if (group.getName().equalsIgnoreCase(name)) {
                return group;
            }
        }
        return null;
    }

    public Role getRoleByName(Roles name) {
        for (Role role : roles) {
            if (role.getName().equals(name)) {
                return role;
            }
        }
        return null;
    }

    @Override
    public Feature getFeatureByName(String value) {
        for (Feature feature : features) {
            if (feature.getName().equalsIgnoreCase(value)) {
                return feature;
            }
        }
        return null;
    }

            /*INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id) VALUES (28, now(), 'UNIT_ENQUIRIES_PHONE','Unit Enquiries Phone','1','9');
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id) VALUES (29, now(), 'UNIT_ENQUIRIES_EMAIL','Unit Enquiries Email','1','9');
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id) VALUES (30, now(), 'APPOINTMENT_PHONE','Appointment Phone','1','9');
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id) VALUES (31, now(), 'APPOINTMENT_EMAIL','Appointment Email','1','9');
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id) VALUES (32, now(), 'OUT_OF_HOURS_INFO','Out of Hours Information','1','9');(*/


    private List<ContactPoint> createGroupContactPoints(Unit unit) {
        List<ContactPoint> contactPoints = new ArrayList<ContactPoint>();

        if (unit.getAppointmentphone() != null) {
            ContactPoint contactPoint = new ContactPoint();
            contactPoint.setContactPointType(callApiGetType("APPOINTMENT_PHONE"));
            contactPoint.setContent(unit.getAppointmentphone());
            contactPoints.add(contactPoint);
        }

        if (unit.getAppointmentemail() != null) {
            ContactPoint contactPoint = new ContactPoint();
            contactPoint.setContactPointType(callApiGetType("APPOINTMENT_EMAIL"));
            contactPoint.setContent(unit.getAppointmentemail());
            contactPoints.add(contactPoint);
        }

        if (unit.getUnitenquiriesemail() != null) {
            ContactPoint contactPoint = new ContactPoint();
            contactPoint.setContactPointType(callApiGetType("UNIT_ENQUIRIES_EMAIL"));
            contactPoint.setContent(unit.getUnitenquiriesemail());
            contactPoints.add(contactPoint);
        }

        if (unit.getOutofhours() != null) {
            ContactPoint contactPoint = new ContactPoint();
            contactPoint.setContactPointType(callApiGetType("OUT_OF_HOURS_INFO"));
            contactPoint.setContent(unit.getOutofhours());
            contactPoints.add(contactPoint);
        }

        return contactPoints;
    }

    private Group createGroup(Unit unit) {
        Group group = new Group();
        group.setName(unit.getShortname());
        group.setCode(unit.getUnitcode());
        group.setVisibleToJoin(unit.isVisible());
        group.setVisible(true);
        //group.setSftpUser(unit.ge);

        if (unit.getSourceType().equalsIgnoreCase("renalunit")) {
            group.setGroupType(getLookupByName("UNIT"));
        } else {
            group.setGroupType(getLookupByName("DISEASE_GROUP"));
        }

        return group;
    }

    private Group getGroupParent(Unit unit) {
        if (unit.getSpecialty().getContext().equalsIgnoreCase("renal")) {
            return getGroupByName("RENAL");
        }
        if (unit.getSpecialty().getContext().equalsIgnoreCase("diabetes")) {
            return getGroupByName("DIABETES");
        }

        return null;
    }

    public void setUnitDao(final UnitDao unitDao) {
        this.unitDao = unitDao;
    }
}
