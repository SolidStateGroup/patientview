package org.patientview.migration.service.impl;

import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.patientview.migration.service.AdminDataMigrationService;
import org.patientview.migration.util.JsonUtil;
import org.patientview.migration.util.PvUtil;
import org.patientview.migration.util.exception.JsonMigrationException;
import org.patientview.migration.util.exception.JsonMigrationExistsException;
import org.patientview.model.Specialty;
import org.patientview.patientview.model.EdtaCode;
import org.patientview.patientview.model.ResultHeading;
import org.patientview.patientview.model.SpecialtyResultHeading;
import org.patientview.persistence.model.Code;
import org.patientview.persistence.model.ContactPointType;
import org.patientview.persistence.model.Feature;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.Lookup;
import org.patientview.persistence.model.ObservationHeading;
import org.patientview.persistence.model.ObservationHeadingGroup;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.repository.EdtaCodeDao;
import org.patientview.repository.ResultHeadingDao;
import org.patientview.repository.SpecialtyDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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
    private EdtaCodeDao edtaCodeDao;

    @Inject
    private ResultHeadingDao resultHeadingDao;

    @Inject
    private SpecialtyDao specialtyDao;

    private List<Group> groups;
    private List<Role> roles;
    private List<Lookup> lookups;
    private List<Feature> features;

    private @Value("${migration.username}") String migrationUsername;
    private @Value("${migration.password}") String migrationPassword;
    private @Value("${patientview.api.url}") String patientviewApiUrl;

    public void init() throws JsonMigrationException {
        try {
            JsonUtil.setPatientviewApiUrl(patientviewApiUrl);
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

    public void migrate() throws JsonMigrationException {
        groups = JsonUtil.getGroups(JsonUtil.pvUrl + "/group");
        createCodes(getLookupByName("DIAGNOSIS"), "edtaCode");
        createCodes(getLookupByName("TREATMENT"), "treatment");
        createObservationHeadings();
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

    private ContactPointType callApiGetType(String type) {
        ContactPointType newLink = null;
        String url = JsonUtil.pvUrl + "/contactpoint/type/" + type;
        try {
            newLink = JsonUtil.jsonRequest(url, ContactPointType.class, null , HttpGet.class, true);
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
}
