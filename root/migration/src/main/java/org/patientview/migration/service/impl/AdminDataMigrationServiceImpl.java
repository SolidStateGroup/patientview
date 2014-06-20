package org.patientview.migration.service.impl;

import org.apache.commons.collections.CollectionUtils;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.patientview.Feature;
import org.patientview.Group;
import org.patientview.GroupFeature;
import org.patientview.Lookup;
import org.patientview.Role;
import org.patientview.migration.service.AdminDataMigrationService;
import org.patientview.migration.util.JsonUtil;
import org.patientview.migration.util.PvUtil;
import org.patientview.migration.util.exception.JsonMigrationException;
import org.patientview.migration.util.exception.JsonMigrationExistsException;
import org.patientview.model.Unit;
import org.patientview.repository.FeatureDao;
import org.patientview.repository.UnitDao;
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
    }

    public void createGroups() {

        // Export a dummy group to test hibernate
        sendDummyUnit();

        for (Unit unit : unitDao.getAll(false)) {

            Set<Feature> unitFeatures = getUnitFeatures(unit);

            LOG.info("Got unit: {}", unit.getUnitcode());
            Group group = PvUtil.createGroup(unit);
            group.setGroupType(getLookupByName("UNIT"));

            //TODO refactor continues into exceptions
            try {
                group = JsonUtil.jsonRequest(JsonUtil.pvUrl + "/group", Group.class, group, HttpPost.class);
            } catch (JsonMigrationException jme) {
                LOG.error("Unable to create group: ", jme.getMessage());
                continue;
            } catch (JsonMigrationExistsException jee) {
                LOG.info("Group {} already exists", unit.getName());
                continue;
            }

            LOG.info("Success: created group");

            if (CollectionUtils.isNotEmpty(unitFeatures)) {

                for (Feature feature : unitFeatures) {
                    String featureUrl = JsonUtil.pvUrl + "/group/" + group.getId() + "/feature/" + feature.getId();

                    try {
                        GroupFeature groupFeature = JsonUtil.jsonRequest(featureUrl, GroupFeature.class, null, HttpPut.class);
                    } catch (JsonMigrationException jme) {
                        LOG.error("Unable to create group: ", jme.getMessage());
                        continue;
                    }catch (JsonMigrationExistsException jee) {
                        LOG.info("Could not update group {} already exists", unit.getName());
                    }

                    LOG.info("Success: feature created for group");

                }
            }

        }

    }

    public void sendDummyUnit() {

        // Export a dummy group to test hibernate until one works
        // FIXME need hibernate to read the table indexes on startup
        Group group = new Group();
        group.setName("TEST_GROUP");
        group.setCode("TEST");
        group.setGroupType(getLookupByName("UNIT"));

        while (true) {
            try {
                group = JsonUtil.jsonRequest(JsonUtil.pvUrl + "/group", Group.class, group, HttpPost.class);
                break;
            } catch (JsonMigrationException jme) {
                LOG.error("Unable to create group: ", jme.getMessage());
            } catch (JsonMigrationExistsException jee) {
                LOG.info("Group {} already exists", group.getName());
            }
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

    public void setUnitDao(final UnitDao unitDao) {
        this.unitDao = unitDao;
    }
}
