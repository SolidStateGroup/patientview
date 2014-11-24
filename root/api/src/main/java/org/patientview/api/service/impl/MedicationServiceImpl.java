package org.patientview.api.service.impl;

import org.apache.commons.lang.StringUtils;
import org.hl7.fhir.instance.model.CodeableConcept;
import org.hl7.fhir.instance.model.DateAndTime;
import org.hl7.fhir.instance.model.Medication;
import org.hl7.fhir.instance.model.MedicationStatement;
import org.hl7.fhir.instance.model.Period;
import org.hl7.fhir.instance.model.ResourceType;
import org.json.JSONObject;
import org.patientview.api.controller.BaseController;
import org.patientview.api.model.FhirMedicationStatement;
import org.patientview.api.model.GpMedicationStatus;
import org.patientview.api.service.GroupService;
import org.patientview.api.service.MedicationService;
import org.patientview.api.service.RoleService;
import org.patientview.api.util.Util;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.Feature;
import org.patientview.persistence.model.FhirLink;
import org.patientview.persistence.model.GroupFeature;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.UserFeature;
import org.patientview.persistence.model.enums.FeatureType;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.persistence.model.enums.RoleType;
import org.patientview.persistence.repository.FeatureRepository;
import org.patientview.persistence.repository.GroupRoleRepository;
import org.patientview.persistence.repository.UserFeatureRepository;
import org.patientview.persistence.repository.UserRepository;
import org.patientview.persistence.resource.FhirResource;
import org.patientview.persistence.util.DataUtils;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 29/09/2014
 */
@Service
public class MedicationServiceImpl extends BaseController<MedicationServiceImpl> implements MedicationService {

    @Inject
    private FhirResource fhirResource;

    @Inject
    private UserRepository userRepository;

    @Inject
    private FeatureRepository featureRepository;

    @Inject
    private UserFeatureRepository userFeatureRepository;

    @Inject
    private RoleService roleService;

    @Inject
    private GroupService groupService;

    @Inject
    private GroupRoleRepository groupRoleRepository;

    @Override
    public void addMedicationStatement(
            org.patientview.persistence.model.FhirMedicationStatement fhirMedicationStatement, FhirLink fhirLink)
            throws FhirResourceException {

        // Medication, stores name
        Medication medication = new Medication();
        CodeableConcept code = new CodeableConcept();
        code.setTextSimple(fhirMedicationStatement.getName());
        medication.setCode(code);

        UUID medicationUuid = FhirResource.getLogicalId(fhirResource.create(medication));

        // Medication statement, stores date, dose
        MedicationStatement medicationStatement = new MedicationStatement();

        if (fhirMedicationStatement.getStartDate() != null) {
            DateAndTime dateAndTime = new DateAndTime(fhirMedicationStatement.getStartDate());
            Period period = new Period();
            period.setStartSimple(dateAndTime);
            period.setEndSimple(dateAndTime);
            medicationStatement.setWhenGiven(period);
        }

        if (StringUtils.isNotEmpty(fhirMedicationStatement.getDose())) {
            MedicationStatement.MedicationStatementDosageComponent dosageComponent
                    = new MedicationStatement.MedicationStatementDosageComponent();
            CodeableConcept concept = new CodeableConcept();
            concept.setTextSimple(fhirMedicationStatement.getDose());
            dosageComponent.setRoute(concept);
            medicationStatement.getDosage().add(dosageComponent);
        }

        medicationStatement.setPatient(Util.createFhirResourceReference(fhirLink.getResourceId()));
        medicationStatement.setMedication(Util.createFhirResourceReference(medicationUuid));

        fhirResource.create(medicationStatement);
    }

    @Override
    public List<FhirMedicationStatement> getByUserId(final Long userId)
            throws ResourceNotFoundException, FhirResourceException {

        User user = userRepository.findOne(userId);
        if (user == null) {
            throw new ResourceNotFoundException("Could not find user");
        }

        List<FhirMedicationStatement> fhirMedications = new ArrayList<>();

        for (FhirLink fhirLink : user.getFhirLinks()) {
            if (fhirLink.getActive()) {
                StringBuilder query = new StringBuilder();
                query.append("SELECT  content::varchar ");
                query.append("FROM    medicationstatement ");
                query.append("WHERE   content->> 'patient' = '{\"display\": \"");
                query.append(fhirLink.getResourceId().toString());
                query.append("\", \"reference\": \"uuid\"}'");

                // get list of medication statements
                List<MedicationStatement> medicationStatements
                    = fhirResource.findResourceByQuery(query.toString(), MedicationStatement.class);

                // for each, create new transport object with medication found from resource reference
                for (MedicationStatement medicationStatement : medicationStatements) {

                    try {
                        JSONObject medicationJson = fhirResource.getResource(
                            UUID.fromString(medicationStatement.getMedication().getDisplaySimple()),
                            ResourceType.Medication);

                        Medication medication = (Medication) DataUtils.getResource(medicationJson);

                        org.patientview.persistence.model.FhirMedicationStatement fhirMedicationStatement =
                            new org.patientview.persistence.model.FhirMedicationStatement(
                                medicationStatement, medication, fhirLink.getGroup());

                        fhirMedications.add(new FhirMedicationStatement(fhirMedicationStatement));

                    } catch (Exception e) {
                        throw new FhirResourceException(e.getMessage());
                    }
                }
            }
        }

        return fhirMedications;
    }

    @Override
    public GpMedicationStatus getGpMedicationStatus(final Long userId) throws ResourceNotFoundException {

        User user = userRepository.findOne(userId);
        if (user == null) {
            throw new ResourceNotFoundException("Could not find user");
        }

        GpMedicationStatus gpMedicationStatus = new GpMedicationStatus();

        // if user has GP_MEDICATION feature (has either opted in or out) then return GP medication status (transport)
        for (UserFeature userFeature : user.getUserFeatures()) {
            if (userFeature.getFeature().getName().equals(FeatureType.GP_MEDICATION.toString())) {
                gpMedicationStatus = new GpMedicationStatus(userFeature);
            }
        }

        // set if available for user based on group features
        gpMedicationStatus.setAvailable(userGroupsHaveGpMedicationFeature(user));

        return gpMedicationStatus;
    }

    @Override
    public void saveGpMedicationStatus(final Long userId, GpMedicationStatus gpMedicationStatus)
            throws ResourceNotFoundException {

        User user = userRepository.findOne(userId);
        Role patientRole = roleService.findByRoleTypeAndName(RoleType.PATIENT, RoleName.PATIENT);

        if (user == null) {
            throw new ResourceNotFoundException("Could not find user");
        }

        Feature gpMedicationFeature = featureRepository.findByName(FeatureType.GP_MEDICATION.toString());
        UserFeature userFeature = userFeatureRepository.findByUserAndFeature(user, gpMedicationFeature);

        if (userFeature == null) {
            UserFeature newUserFeature = new UserFeature(gpMedicationFeature);
            newUserFeature.setFeature(gpMedicationFeature);
            newUserFeature.setUser(user);
            newUserFeature.setOptInStatus(gpMedicationStatus.getOptInStatus());
            newUserFeature.setOptInHidden(gpMedicationStatus.getOptInHidden());
            newUserFeature.setOptOutHidden(gpMedicationStatus.getOptOutHidden());
            newUserFeature.setCreator(user);
            if (gpMedicationStatus.getOptInDate() != null) {
                newUserFeature.setOptInDate(new Date(gpMedicationStatus.getOptInDate()));
            }
            user.getUserFeatures().add(newUserFeature);
            userRepository.save(user);
        } else {
            userFeature.setOptInStatus(gpMedicationStatus.getOptInStatus());
            userFeature.setOptInHidden(gpMedicationStatus.getOptInHidden());
            userFeature.setOptOutHidden(gpMedicationStatus.getOptOutHidden());
            userFeature.setLastUpdate(new Date());
            userFeature.setLastUpdater(user);
            if (gpMedicationStatus.getOptInDate() != null) {
                userFeature.setOptInDate(new Date(gpMedicationStatus.getOptInDate()));
            } else {
                userFeature.setOptInDate(null);
            }
            userFeatureRepository.save(userFeature);
        }

        if (gpMedicationStatus.getOptInStatus() && !userHasGpMedicationGroupRole(user)) {
            // add to GP_MEDICATION group
            GroupRole groupRole = new GroupRole();
            groupRole.setRole(patientRole);
            groupRole.setGroup(groupService.findByCode(FeatureType.GP_MEDICATION.toString()));
            groupRole.setUser(user);
            groupRole.setCreator(user);
            user.getGroupRoles().add(groupRole);
            userRepository.save(user);
        }

        if (!gpMedicationStatus.getOptInStatus() && userHasGpMedicationGroupRole(user)) {
            // remove from GP_MEDICATION group
            GroupRole groupRole = null;
            for (GroupRole entityGroupRole : user.getGroupRoles()) {
                if (entityGroupRole.getGroup().getCode().equals(FeatureType.GP_MEDICATION.toString())) {
                    groupRole = entityGroupRole;
                }
            }

            if (groupRole != null) {
                groupRoleRepository.delete(groupRole);
                user.getGroupRoles().remove(groupRole);
                userRepository.save(user);
            }
        }
    }

    // check if user's groupRoles include medication groupRole
    private boolean userHasGpMedicationGroupRole(User user) {
        User entityUser = userRepository.findOne(user.getId());

        for (GroupRole groupRole : entityUser.getGroupRoles()) {
            if (groupRole.getGroup().getCode().equals(FeatureType.GP_MEDICATION.toString())) {
                return true;
            }
        }

        return false;
    }

    // verify at least one of the user's groups has GP medication feature enabled
    private boolean userGroupsHaveGpMedicationFeature(User user) {

        User entityUser = userRepository.findOne(user.getId());

        for (GroupRole groupRole : entityUser.getGroupRoles()) {
            for (GroupFeature groupFeature : groupRole.getGroup().getGroupFeatures()) {
                if (groupFeature.getFeature().getName().equals(FeatureType.GP_MEDICATION.toString())) {
                    return true;
                }
            }
        }

        return false;
    }
}
