package org.patientview.migration.service.impl;

import org.apache.commons.collections.CollectionUtils;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.patientview.Code;
import org.patientview.Feature;
import org.patientview.Group;
import org.patientview.GroupFeature;
import org.patientview.GroupRole;
import org.patientview.Lookup;
import org.patientview.Role;
import org.patientview.migration.service.AdminDataMigrationService;
import org.patientview.migration.util.JsonUtil;
import org.patientview.migration.util.PvUtil;
import org.patientview.migration.util.exception.JsonMigrationException;
import org.patientview.migration.util.exception.JsonMigrationExistsException;
import org.patientview.model.Unit;
import org.patientview.patientview.model.EdtaCode;
import org.patientview.repository.EdtaCodeDao;
import org.patientview.repository.FeatureDao;
import org.patientview.repository.UnitDao;
import org.patientview.service.EdtaCodeManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
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
        createCodes();
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

            LOG.info("Success: created group");

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

        }

    }

    private void callApiCreateParentGroup(Group group, Group parentGroup) {
        String featureUrl = JsonUtil.pvUrl + "/group/" + group.getId() + "/parent/" + parentGroup.getId();


        try {
            GroupRole groupRole = JsonUtil.jsonRequest(featureUrl, GroupRole.class, null, HttpPut.class);
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


    private Group callApiCreateGroup(Group group) {
        Group newGroup = null;
        try {
            newGroup = JsonUtil.jsonRequest(JsonUtil.pvUrl + "/group", Group.class, group, HttpPost.class);
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



    public void createCodes() {

        int i = 1;
        for (EdtaCode edtaCode : edtaCodeDao.get("edtaCode", null)) {
            Code code = new Code();
            code.setDisplayOrder(i++);
            code.setStandardType(getLookupByName("EDTA"));
            code.setCodeType(getLookupByName("DIAGNOSIS"));
            code.setLinks(PvUtil.getLinks(edtaCode));
            code.setDescription(edtaCode.getDescription());
            code.setCode(edtaCode.getEdtaCode());
            callApiCreateCode(code);
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

    public Role getRoleByName(String name) {
        for (Role role : roles) {
            if (role.getName().equalsIgnoreCase(name)) {
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
