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
        lookups = JsonUtil.getStaticDataLookups(JsonUtil.pvUrl + "/lookups");
        features = JsonUtil.getStaticDataFeatures(JsonUtil.pvUrl + "/features");
        groups = JsonUtil.getGroups(JsonUtil.pvUrl + "/groups");
        roles = JsonUtil.getRoles(JsonUtil.pvUrl + "/roles");
    }


    public void migrate() {
        createSpecialties();
        createGroups();
    }

    public void createSpecialties() {

        Lookup groupType = getLookupByName("SPECIALTY");

        renal = new Group();
        renal.setName("Renal");
        renal.setCode("RENAL");
        renal.setDescription("The renal specialty");
        renal.setId(1L);
        renal.setGroupType(groupType);

        try {
            JsonUtil.gsonPost(JsonUtil.pvUrl + "/group", renal);
        } catch (Exception e) {
            LOG.error("Error saving group: " + e.getCause());
        }

        diabetes = new Group();
        diabetes.setName("Diabetes");
        diabetes.setCode("DIABETES");
        diabetes.setDescription("The diabetes specialty");
        diabetes.setId(2L);
        diabetes.setGroupType(groupType);

        try {
            LOG.info("Specialty creation response: {}", JsonUtil.gsonPost(JsonUtil.pvUrl + "/group", diabetes).toString());
        } catch (Exception e) {
            LOG.error("Error saving group: " + e.getCause());
        }

        ibd = new Group();
        ibd.setName("IBD");
        ibd.setCode("IBD");
        ibd.setDescription("The IBD specialty");
        ibd.setId(2L);
        ibd.setGroupType(groupType);


        try {
            JsonUtil.gsonPost(JsonUtil.pvUrl + "/group", ibd);
        } catch (Exception e) {
            LOG.error("Error saving group: " + e.getCause());
        }

    }

    public void createGroups() {

        for (Unit unit : unitDao.getAll(false)) {

            Set<Feature> unitFeatures = getUnitFeatures(unit);

            LOG.info("Got unit: {}", unit.getUnitcode());
            Group group = PvUtil.createGroup(unit);
            group.setGroupType(getLookupByName("UNIT"));

            group = JsonUtil.jsonRequest(JsonUtil.pvUrl + "/group", Group.class, group, HttpPost.class);

            if (group == null) {
                LOG.error("Could not create group");
                continue;
            }

            if (CollectionUtils.isNotEmpty(unitFeatures)) {

                for (Feature feature : unitFeatures) {
                    String featureUrl = JsonUtil.pvUrl + "/group/" + group.getId() + "/feature/" + feature.getId();
                    GroupFeature groupFeature = JsonUtil.jsonRequest(featureUrl, GroupFeature.class, null, HttpPut.class);

                    if (groupFeature != null) {
                        LOG.info("Feature created for group");
                    }

                }
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
