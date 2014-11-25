package org.patientview.migration.service.impl;

import org.apache.commons.collections.CollectionUtils;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.patientview.migration.service.AdminDataMigrationService;
import org.patientview.migration.util.JsonUtil;
import org.patientview.migration.util.PvUtil;
import org.patientview.migration.util.exception.JsonMigrationException;
import org.patientview.migration.util.exception.JsonMigrationExistsException;
import org.patientview.model.Specialty;
import org.patientview.model.Unit;
import org.patientview.patientview.model.EdtaCode;
import org.patientview.patientview.model.ResultHeading;
import org.patientview.patientview.model.SpecialtyResultHeading;
import org.patientview.persistence.model.Code;
import org.patientview.persistence.model.ContactPoint;
import org.patientview.persistence.model.ContactPointType;
import org.patientview.persistence.model.Feature;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupFeature;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.Link;
import org.patientview.persistence.model.Lookup;
import org.patientview.persistence.model.ObservationHeading;
import org.patientview.persistence.model.ObservationHeadingGroup;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.enums.ContactPointTypes;
import org.patientview.persistence.model.enums.FeatureType;
import org.patientview.persistence.model.enums.GroupTypes;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.repository.EdtaCodeDao;
import org.patientview.repository.FeatureDao;
import org.patientview.repository.ResultHeadingDao;
import org.patientview.repository.SpecialtyDao;
import org.patientview.repository.UnitDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
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

    @Inject
    private SpecialtyDao specialtyDao;

    private List<Group> groups;
    private List<Role> roles;
    private List<Lookup> lookups;
    private List<Feature> features;

    private Group renal;
    private Group diabetes;
    private Group ibd;

    private @Value("${migration.username}") String migrationUsername;
    private @Value("${migration.password}") String migrationPassword;

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
    public void init() throws JsonMigrationException {
        try {
            JsonUtil.token = JsonUtil.authenticate(migrationUsername, migrationPassword);
            lookups = JsonUtil.getStaticDataLookups(JsonUtil.pvUrl + "/lookup");
            features = JsonUtil.getStaticDataFeatures(JsonUtil.pvUrl + "/feature");
            roles = JsonUtil.getRoles(JsonUtil.pvUrl + "/role");
            groups = JsonUtil.getGroups(JsonUtil.pvUrl + "/group");
        } catch (JsonMigrationException e) {
            LOG.error("Could not authenticate {} ", e.getCause());
            throw new JsonMigrationException(e.getMessage());
        } catch (JsonMigrationExistsException e) {
            LOG.error("Could not authenticate {} ", e.getCause());
        }
    }

    public void migrate() {
        createGroups();
        groups = JsonUtil.getGroups(JsonUtil.pvUrl + "/group");
        createCodes(getLookupByName("DIAGNOSIS"), "edtaCode");
        createCodes(getLookupByName("TREATMENT"), "treatment");
        createObservationHeadings();
    }

    public void createGroups() {
        // Export a dummy group to test hibernate
        //sendDummyUnit();

        for (Unit unit : unitDao.getAll(false)) {

            Set<Feature> unitFeatures = getUnitFeatures(unit);

            LOG.info("Got unit from PatientView 1: {}", unit.getUnitcode());

            // Create the unit
            Group group = createGroup(unit);

            Long groupId = callApiCreateGroup(group);

            if (groupId != null) {
                group = callApiGetGroup(groupId);

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
    }

    private void callApiCreateParentGroup(Group group, Group parentGroup) {
        String featureUrl = JsonUtil.pvUrl + "/group/" + group.getId() + "/parent/" + parentGroup.getId();

        try {
            JsonUtil.jsonRequest(featureUrl, GroupRole.class, null, HttpPut.class, true);
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

        String featureUrl = JsonUtil.pvUrl + "/group/" + group.getId() + "/features/" + feature.getId();

        try {
            return JsonUtil.jsonRequest(featureUrl, GroupFeature.class, null, HttpPut.class, true);
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
            return JsonUtil.jsonRequest(featureUrl, ContactPoint.class, contactPoint, HttpPost.class, true);
        } catch (JsonMigrationException jme) {
            LOG.error("Unable to create contact point: ", jme.getMessage());
        } catch (JsonMigrationExistsException jee) {
            LOG.info("Could not create contact point: {}", group.getName());
        } catch (Exception jee) {
            LOG.info("Could not create contact point: {}", group.getName());
        }

        return null;
    }


    private Long callApiCreateGroup(Group group) {
        Long newGroupId = null;
        try {
            newGroupId = JsonUtil.jsonRequest(JsonUtil.pvUrl + "/group", Long.class, group, HttpPost.class, true);
            LOG.info("Created group");
        } catch (JsonMigrationException jme) {
            LOG.error("Unable to create group: ", jme.getMessage());
        } catch (JsonMigrationExistsException jee) {
            LOG.info("Group {} already exists", group.getName());
        }

        return newGroupId;
    }

    private Group callApiGetGroup(Long groupId) {
        Group newGroup = null;
        try {
            newGroup = JsonUtil.jsonRequest(JsonUtil.pvUrl + "/group/" + groupId, Group.class, null, HttpGet.class, true);
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
            newCode = JsonUtil.jsonRequest(JsonUtil.pvUrl + "/code", Code.class, code, HttpPost.class, true);
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
                    ObservationHeading.class, observationHeading, HttpPost.class, true);
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
            newLink = JsonUtil.jsonRequest(JsonUtil.pvUrl + "/link", Link.class, link, HttpPost.class, true);
            //LOG.info("Created link");
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
            newLink = JsonUtil.jsonRequest(url, ContactPointType.class, null , HttpGet.class, true);
            //LOG.info("Got Contact Point Type");
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
                group = JsonUtil.jsonRequest(JsonUtil.pvUrl + "/group", Group.class, group, HttpPost.class, true);
                // Delete the test group once we have successfully created one
                group = JsonUtil.jsonRequest(JsonUtil.pvUrl + "/group/" + group.getId(), null, null, HttpDelete.class, true);
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

        /*if (unit.isSharedThoughtEnabled()) {
            unitFeatures.add(getFeatureByName(FeatureType.SHARING_THOUGHTS.toString()));
        }*/

        if (unit.isFeedbackEnabled()) {
            unitFeatures.add(getFeatureByName(FeatureType.FEEDBACK.toString()));
        }

        if (featureDao.getUnitsForFeature("messaging").contains(unit)) {
            unitFeatures.add(getFeatureByName(FeatureType.MESSAGING.toString()));
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
        for (ResultHeading resultHeading : resultHeadingDao.getAll(null)) {
            ObservationHeading observationHeading = new ObservationHeading();
            observationHeading.setCode(resultHeading.getHeadingcode());
            observationHeading.setHeading(resultHeading.getHeading());
            observationHeading.setName(resultHeading.getRollover());
            observationHeading.setInfoLink(resultHeading.getLink());
            observationHeading.setDefaultPanel(
                    (long) resultHeading.getSpecialtyResultHeadings().iterator().next().getPanel());
            observationHeading.setDefaultPanelOrder(
                    (long) resultHeading.getSpecialtyResultHeadings().iterator().next().getPanelOrder());
            observationHeading.setMinGraph(resultHeading.getMinRangeValue());
            observationHeading.setMaxGraph(resultHeading.getMaxRangeValue());
            observationHeading.setUnits(resultHeading.getUnits());

            // create specialty specific
            Set<SpecialtyResultHeading> specialtyResultHeadings = resultHeading.getSpecialtyResultHeadings();
            observationHeading.setObservationHeadingGroups(new HashSet<ObservationHeadingGroup>());

            for (SpecialtyResultHeading specialtyResultHeading : specialtyResultHeadings) {
                ObservationHeadingGroup observationHeadingGroup = new ObservationHeadingGroup();
                observationHeadingGroup.setPanel((long) specialtyResultHeading.getPanel());
                observationHeadingGroup.setPanelOrder((long) specialtyResultHeading.getPanelOrder());

                Specialty specialty = specialtyDao.get((long) specialtyResultHeading.getSpecialtyId());
                Group group = getGroupByCode(specialty.getContext());
                observationHeadingGroup.setGroup(group);

                observationHeading.getObservationHeadingGroups().add(observationHeadingGroup);
            }

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

    public Role getRoleByName(RoleName name) {
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

    private List<ContactPoint> createGroupContactPoints(Unit unit) {
        List<ContactPoint> contactPoints = new ArrayList<ContactPoint>();

        if (unit.getAppointmentphone() != null) {
            ContactPoint contactPoint = new ContactPoint();
            contactPoint.setContactPointType(callApiGetType(ContactPointTypes.APPOINTMENT_PHONE.toString()));
            contactPoint.setContent(unit.getAppointmentphone());
            contactPoints.add(contactPoint);
        }

        if (unit.getAppointmentemail() != null) {
            ContactPoint contactPoint = new ContactPoint();
            contactPoint.setContactPointType(callApiGetType(ContactPointTypes.APPOINTMENT_EMAIL.toString()));
            contactPoint.setContent(unit.getAppointmentemail());
            contactPoints.add(contactPoint);
        }

        if (unit.getUnitenquiriesemail() != null) {
            ContactPoint contactPoint = new ContactPoint();
            contactPoint.setContactPointType(callApiGetType(ContactPointTypes.UNIT_ENQUIRIES_EMAIL.toString()));
            contactPoint.setContent(unit.getUnitenquiriesemail());
            contactPoints.add(contactPoint);
        }

        if (unit.getOutofhours() != null) {
            ContactPoint contactPoint = new ContactPoint();
            contactPoint.setContactPointType(callApiGetType(ContactPointTypes.OUT_OF_HOURS_INFO.toString()));
            contactPoint.setContent(unit.getOutofhours());
            contactPoints.add(contactPoint);
        }

        return contactPoints;
    }

    private Group createGroup(Unit unit) {
        Group group = new Group();
        group.setName(unit.getName());
        group.setShortName(unit.getShortname());
        group.setCode(unit.getUnitcode());
        group.setVisibleToJoin(unit.isVisible());
        group.setVisible(true);

        if (unit.getSourceType().equalsIgnoreCase("renalunit")) {
            group.setGroupType(getLookupByName(GroupTypes.UNIT.toString()));
        } else {
            group.setGroupType(getLookupByName(GroupTypes.DISEASE_GROUP.toString()));
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
