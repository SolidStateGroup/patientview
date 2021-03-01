package org.patientview.api.service.impl;

import org.apache.commons.lang.StringUtils;
import org.hl7.fhir.instance.model.Condition;
import org.hl7.fhir.instance.model.Encounter;
import org.hl7.fhir.instance.model.Patient;
import org.hl7.fhir.instance.model.Practitioner;
import org.hl7.fhir.instance.model.ResourceReference;
import org.hl7.fhir.instance.model.ResourceType;
import org.patientview.api.builder.PatientBuilder;
import org.patientview.api.service.ApiConditionService;
import org.patientview.api.service.ApiObservationService;
import org.patientview.api.service.ApiPatientService;
import org.patientview.api.service.CodeService;
import org.patientview.api.service.LookupService;
import org.patientview.api.service.UserService;
import org.patientview.api.util.ApiUtil;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.Code;
import org.patientview.persistence.model.FhirCondition;
import org.patientview.persistence.model.FhirDatabaseEntity;
import org.patientview.persistence.model.FhirEncounter;
import org.patientview.persistence.model.FhirLink;
import org.patientview.persistence.model.FhirPatient;
import org.patientview.persistence.model.FhirPractitioner;
import org.patientview.persistence.model.GpLetter;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.Identifier;
import org.patientview.persistence.model.ServerResponse;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.CodeTypes;
import org.patientview.persistence.model.enums.DiagnosisTypes;
import org.patientview.persistence.model.enums.EncounterTypes;
import org.patientview.persistence.model.enums.GroupTypes;
import org.patientview.persistence.model.enums.HiddenGroupCodes;
import org.patientview.persistence.model.enums.LookupTypes;
import org.patientview.persistence.model.enums.NonTestObservationTypes;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.persistence.model.enums.TransplantStatus;
import org.patientview.persistence.repository.FhirLinkRepository;
import org.patientview.persistence.repository.GpLetterRepository;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.IdentifierRepository;
import org.patientview.persistence.repository.UserRepository;
import org.patientview.persistence.resource.FhirResource;
import org.patientview.persistence.util.DataUtils;
import org.patientview.service.AllergyService;
import org.patientview.service.ConditionService;
import org.patientview.service.EncounterService;
import org.patientview.service.GpLetterService;
import org.patientview.util.Util;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

/**
 * Patient service, for managing the patient records associated with a User, retrieved from FHIR.
 *
 * Created by james@solidstategroup.com
 * Created on 02/09/2014
 */
@Service
public class ApiPatientServiceImpl extends AbstractServiceImpl<ApiPatientServiceImpl> implements ApiPatientService {

    @Inject
    private AllergyService allergyService;

    @Inject
    private ApiConditionService apiConditionService;

    @Inject
    private ApiObservationService apiObservationService;

    @Inject
    private CodeService codeService;

    @Inject
    private ConditionService conditionService;

    @Inject
    private EncounterService encounterService;

    @Inject
    private FhirLinkRepository fhirLinkRepository;

    @Inject
    private FhirResource fhirResource;

    @Inject
    private GpLetterService gpLetterService;

    @Inject
    private GpLetterRepository gpLetterRepository;

    @Inject
    private GroupRepository groupRepository;

    @Inject
    private IdentifierRepository identifierRepository;

    @Inject
    private LookupService lookupService;

    @Inject
    private UserService userService;

    @Inject
    private UserRepository userRepository;

    private static final String GEN_CODE = "GEN";

    /**
     * Get a list of User patient records, as stored in FHIR and associated with Groups that have imported patient
     * data.
     * Produces a larger object containing all the properties required to populate My Details and My Conditions pages.
     *
     * @param userId   ID of User to retrieve patient record for
     * @param groupIds IDs of Groups to retrieve patient records from
     * @return List of Patient objects containing patient encounters, conditions etc
     * @throws FhirResourceException
     * @throws ResourceNotFoundException
     */
    @Override
    public List<org.patientview.api.model.Patient> get(final Long userId, final List<Long> groupIds)
            throws FhirResourceException, ResourceNotFoundException, ResourceForbiddenException {

        // check User exists
        User user = userService.get(userId);
        if (user == null) {
            throw new ResourceNotFoundException("Could not find user");
        }

        // restrict Groups if passed in, only include Groups in the User's current GroupRoles (even if they have
        // FhirLinks from previous Groups)
        boolean restrictGroups = false;
        List<Long> filteredGroupIds = new ArrayList<>();

        if (!CollectionUtils.isEmpty(groupIds)) {
            restrictGroups = true;
            for (GroupRole groupRole : user.getGroupRoles()) {
                Long groupId = groupRole.getGroup().getId();
                if (groupIds.contains(groupId)) {
                    filteredGroupIds.add(groupId);
                }
            }
        }

        List<org.patientview.api.model.Patient> patients = new ArrayList<>();
        List<FhirLink> fhirLinks = new ArrayList<>();
        fhirLinks.addAll(user.getFhirLinks());

        // sort fhirLinks by id
        Collections.sort(fhirLinks, new Comparator<FhirLink>() {
            public int compare(FhirLink f1, FhirLink f2) {
                return f2.getCreated().compareTo(f1.getCreated());
            }
        });

        List<Long> foundFhirLinkGroupIds = new ArrayList<>();

        // get data from FHIR from each unit, ignoring multiple FHIR records per unit (versions)
        for (FhirLink fhirLink : fhirLinks) {
            if ((restrictGroups && filteredGroupIds.contains(fhirLink.getGroup().getId())) || (!restrictGroups)) {
                if (fhirLink.getActive() && !ApiUtil.isInEnum(fhirLink.getGroup().getCode(), HiddenGroupCodes.class)) {
                    Patient fhirPatient = get(fhirLink.getResourceId());
                    foundFhirLinkGroupIds.add(fhirLink.getGroup().getId());

                    if (fhirPatient != null) {

                        // create basic patient with group
                        org.patientview.api.model.Patient patient
                                = new org.patientview.api.model.Patient(fhirPatient, fhirLink.getGroup());

                        // set practitioners
                        patient = setPractitioners(patient, fhirPatient);

                        // set conditions
                        patient = setConditions(patient, conditionService.get(fhirLink.getResourceId()));

                        // set staff entered conditions
                        patient = setStaffEnteredConditions(patient, user.getId());

                        // set encounters (treatment)
                        patient = setEncounters(
                                patient, encounterService.get(fhirLink.getResourceId()), user, fhirLink.getGroup());

                        // set edta diagnosis if present based on available codes in conditions
                        // BORKED on LAZY Init
                        patient = setDiagnosisCodes(patient);

                        // set non test observations
                        patient = setNonTestObservations(patient, fhirLink);

                        // set allergies
                        patient = setAllergies(patient, fhirLink);

                        patients.add(patient);
                    }
                }
            }
        }

        // make sure group links are displayed on screen, even if no fhirlink exists for the patient's groups, ignore
        // specialty groups and general practices
        for (GroupRole groupRole : user.getGroupRoles()) {
            Group group = groupRole.getGroup();
            if ((restrictGroups && filteredGroupIds.contains(group.getId())) || (!restrictGroups)) {
                if (!foundFhirLinkGroupIds.contains(group.getId())
                        && group.getVisible()
                        && !group.getGroupType().getValue().equals(GroupTypes.SPECIALTY.toString())
                        && !group.getGroupType().getValue().equals(HiddenGroupCodes.GENERAL_PRACTICE.toString())) {
                    org.patientview.api.model.Patient p = new org.patientview.api.model.Patient();
                    p.setFhirPatient(new FhirPatient());
                    p.setGroup(new org.patientview.api.model.Group(group));
                    patients.add(p);
                }
            }
        }

        return patients;
    }

    /**
     * Get a FHIR Patient record given the UUID associated with the Patient in FHIR.
     *
     * @param uuid UUID of Patient in FHIR to retrieve
     * @return FHIR Patient
     * @throws FhirResourceException
     */
    @Override
    public Patient get(final UUID uuid) throws FhirResourceException {
        try {
            return (Patient) DataUtils.getResource(fhirResource.getResource(uuid, ResourceType.Patient));
        } catch (Exception e) {
            throw new FhirResourceException(e);
        }
    }

    @Override
    public List<org.patientview.api.model.Patient> getBasic(Long userId)
            throws FhirResourceException, ResourceNotFoundException {
        // check User exists
        User user = userService.get(userId);
        if (user == null) {
            throw new ResourceNotFoundException("Could not find user");
        }

        List<org.patientview.api.model.Patient> patients = new ArrayList<>();
        List<FhirLink> fhirLinks = new ArrayList<>();
        fhirLinks.addAll(user.getFhirLinks());

        // sort fhirLinks by id
        Collections.sort(fhirLinks, new Comparator<FhirLink>() {
            public int compare(FhirLink f1, FhirLink f2) {
                return f2.getCreated().compareTo(f1.getCreated());
            }
        });

        // get data from FHIR from each unit, ignoring multiple FHIR records per unit (versions)
        for (FhirLink fhirLink : fhirLinks) {
            if (fhirLink.getActive() && !ApiUtil.isInEnum(fhirLink.getGroup().getCode(), HiddenGroupCodes.class)) {
                Patient fhirPatient = get(fhirLink.getResourceId());

                if (fhirPatient != null) {
                    // create basic patient with group
                    org.patientview.api.model.Patient patient
                            = new org.patientview.api.model.Patient(fhirPatient, fhirLink.getGroup());

                    // set conditions
                    patient = setConditions(patient, conditionService.get(fhirLink.getResourceId()));

                    // set encounters (treatment)
                    patient = setEncounters(
                            patient, encounterService.get(fhirLink.getResourceId()), user, fhirLink.getGroup());

                    patients.add(patient);
                }
            }
        }

        return patients;
    }


    @Override
    public List<org.patientview.api.model.Patient> getPatientResearchStudyCriteria(Long userId)
            throws ResourceNotFoundException, FhirResourceException, ResourceForbiddenException {
        // check User exists
        User user = userService.get(userId);
        if (user == null) {
            throw new ResourceNotFoundException("Could not find user");
        }

        List<org.patientview.api.model.Patient> patients = new ArrayList<>();
        List<FhirLink> fhirLinks = new ArrayList<>();
        fhirLinks.addAll(user.getFhirLinks());

        // sort fhirLinks by id
        Collections.sort(fhirLinks, new Comparator<FhirLink>() {
            public int compare(FhirLink f1, FhirLink f2) {
                return f2.getCreated().compareTo(f1.getCreated());
            }
        });

        // get data from FHIR from each unit, ignoring multiple FHIR records per unit (versions)
        for (FhirLink fhirLink : fhirLinks) {
            if (fhirLink.getActive() && !ApiUtil.isInEnum(fhirLink.getGroup().getCode(), HiddenGroupCodes.class)) {
                Patient fhirPatient = get(fhirLink.getResourceId());

                if (fhirPatient != null) {
                    // create basic patient with group
                    org.patientview.api.model.Patient patient
                            = new org.patientview.api.model.Patient(fhirPatient, fhirLink.getGroup());

                    // set practitioners
                    patient = setPractitioners(patient, fhirPatient);

                    // set edta diagnosis if present based on available codes in conditions
                    patient = setDiagnosisCodes(patient);

                    // set staff entered conditions
                    patient = setStaffEnteredConditions(patient, user.getId());

                    // set patient entered conditions
                    patient = setPatientEnteredConditions(patient, user.getId());

                    // set conditions
                    patient = setConditions(patient, conditionService.get(fhirLink.getResourceId()));

                    // set encounters (treatment)
                    List<Encounter> encounters = encounterService.get(fhirLink.getResourceId());

                    // replace fhirEncounter type field with a more useful description if it exists in codes
                    for (Encounter encounter : encounters) {
                        FhirEncounter fhirEncounter = new FhirEncounter(encounter);
                        patient.getFhirEncounters().add(fhirEncounter);
                    }

                    patients.add(patient);
                }
            }
        }

        return patients;
    }

    @Override
    @Transactional
    public ServerResponse importPatient(FhirPatient fhirPatient) {
        if (StringUtils.isEmpty(fhirPatient.getGroupCode())) {
            return new ServerResponse("group code not set");
        }
        if (StringUtils.isEmpty(fhirPatient.getIdentifier())) {
            return new ServerResponse("identifier not set");
        }

        Group group = groupRepository.findByCode(fhirPatient.getGroupCode());

        if (group == null) {
            return new ServerResponse("group not found");
        }

        // check current logged in user has rights to this group
        if (!(ApiUtil.currentUserHasRole(RoleName.GLOBAL_ADMIN)
                || ApiUtil.doesContainGroupAndRole(group.getId(), RoleName.IMPORTER))) {
            return new ServerResponse("failed group and role validation");
        }

        List<Identifier> identifiers = identifierRepository.findByValue(fhirPatient.getIdentifier());

        if (CollectionUtils.isEmpty(identifiers)) {
            return new ServerResponse("identifier not found");
        }
        if (identifiers.size() > 1) {
            return new ServerResponse("identifier not unique");
        }

        Identifier identifier = identifiers.get(0);
        User user = identifier.getUser();

        if (user == null) {
            return new ServerResponse("user not found");
        }

        // make sure importer and patient from the same group
        if (!userService.currentUserSameUnitGroup(user, RoleName.IMPORTER)) {
            LOG.error("Importer trying to import medication for patient outside his group");
            return new ServerResponse("Forbidden");
        }

        // make sure patient is a member of the imported group
        if (!ApiUtil.userHasGroup(user, group.getId())) {
            return new ServerResponse("patient not a member of imported group");
        }

        // get FhirLink
        FhirLink fhirLink = Util.getFhirLink(group, fhirPatient.getIdentifier(), user.getFhirLinks());

        if (fhirLink == null) {
            // no FhirLink exists
            FhirDatabaseEntity entity;

            // build patient
            PatientBuilder patientBuilder = new PatientBuilder(null, fhirPatient);
            Patient builtPatient = patientBuilder.build();

            // store new patient in FHIR
            try {
                entity = fhirResource.createEntity(builtPatient, ResourceType.Patient.name(), "patient");
            } catch (FhirResourceException fre) {
                return new ServerResponse("error creating FHIR patient");
            }

            // create FhirLink
            fhirLink = new FhirLink();
            fhirLink.setUser(user);
            fhirLink.setIdentifier(identifier);
            fhirLink.setGroup(group);
            fhirLink.setResourceId(entity.getLogicalId());
            fhirLink.setVersionId(entity.getVersionId());
            fhirLink.setResourceType(ResourceType.Patient.name());
            fhirLink.setActive(true);

            if (CollectionUtils.isEmpty(user.getFhirLinks())) {
                user.setFhirLinks(new HashSet<FhirLink>());
            }

            user.getFhirLinks().add(fhirLink);
            userRepository.save(user);

            return new ServerResponse(null, "created", true);
        } else {
            // FhirLink exists, check patient exists
            Patient currentPatient;
            FhirDatabaseEntity entity;

            if (fhirLink.getResourceId() == null) {
                return new ServerResponse("error retrieving FHIR patient, no UUID");
            }

            try {
                currentPatient = get(fhirLink.getResourceId());
            } catch (FhirResourceException fre) {
                return new ServerResponse("error retrieving FHIR patient");
            }

            // build patient
            PatientBuilder patientBuilder = new PatientBuilder(currentPatient, fhirPatient);
            Patient builtPatient = patientBuilder.build();

            if (currentPatient == null) {
                // create patient in FHIR, update FhirLink with newly created resource ID
                try {
                    entity = fhirResource.createEntity(builtPatient, ResourceType.Patient.name(), "patient");
                    fhirLink.setResourceId(entity.getLogicalId());
                    fhirLink.setResourceType(ResourceType.Patient.name());
                    fhirLink.setActive(true);
                } catch (FhirResourceException fre) {
                    return new ServerResponse("error creating FHIR patient");
                }
            } else {
                // store updated patient in FHIR
                try {
                    entity = fhirResource.updateEntity(
                            builtPatient, ResourceType.Patient.name(), "patient", fhirLink.getResourceId());
                } catch (FhirResourceException fre) {
                    return new ServerResponse("error updating FHIR patient");
                }
            }

            if (entity == null) {
                return new ServerResponse("error storing FHIR patient");
            }

            // update FhirLink and save
            fhirLink.setVersionId(entity.getVersionId());
            fhirLink.setUpdated(entity.getUpdated());
            fhirLinkRepository.save(fhirLink);

            return new ServerResponse(null, "updated", true);
        }
    }

    private Practitioner getPractitioner(final UUID uuid) throws FhirResourceException {
        try {
            return (Practitioner) DataUtils.getResource(fhirResource.getResource(uuid, ResourceType.Practitioner));
        } catch (Exception e) {
            // gracefully throw exception and continue (ignore bad data from migration)
            LOG.error("Get Practitioner error: ", e);
            return null;
        }
    }

    private org.patientview.api.model.Patient setAllergies(
            org.patientview.api.model.Patient patient, FhirLink fhirLink) throws FhirResourceException {
        patient.setFhirAllergies(allergyService.getBySubject(fhirLink.getResourceId()));
        return patient;
    }

    private org.patientview.api.model.Patient setConditions(org.patientview.api.model.Patient patient,
                                                            List<Condition> conditions) {
        for (Condition condition : conditions) {
            FhirCondition fhirCondition = new FhirCondition(condition);

            // try and set links based on diagnosis code (used by my IBD)
            List<Code> codes = codeService.findAllByCodeAndType(fhirCondition.getCode(),
                    lookupService.findByTypeAndValue(LookupTypes.CODE_TYPE, CodeTypes.DIAGNOSIS.toString()));
            if (!codes.isEmpty()) {
                Code code = codes.get(0);

                fhirCondition.setDescription(code.getDescription());

                if (!CollectionUtils.isEmpty(code.getLinks())) {
                    fhirCondition.setLinks(codes.get(0).getLinks());
                }
            }
            patient.getFhirConditions().add(fhirCondition);
        }

        return patient;
    }

    private org.patientview.api.model.Patient setPatientEnteredConditions(
            org.patientview.api.model.Patient patient, Long userId)
            throws FhirResourceException, ResourceForbiddenException, ResourceNotFoundException {
        patient.getFhirConditions().addAll(
                apiConditionService.getUserEntered(userId, DiagnosisTypes.DIAGNOSIS_PATIENT_ENTERED, false));
        return patient;
    }


    private org.patientview.api.model.Patient setStaffEnteredConditions(
            org.patientview.api.model.Patient patient, Long userId)
            throws FhirResourceException, ResourceForbiddenException, ResourceNotFoundException {
        patient.getFhirConditions().addAll(
                apiConditionService.getUserEntered(userId, DiagnosisTypes.DIAGNOSIS_STAFF_ENTERED, false));
        return patient;
    }

    private org.patientview.api.model.Patient setDiagnosisCodes(org.patientview.api.model.Patient patient) {
        for (FhirCondition condition : patient.getFhirConditions()) {
            if (StringUtils.isNotEmpty(condition.getCategory())
                    && condition.getCategory().equals(DiagnosisTypes.DIAGNOSIS_EDTA.toString())) {

                // RPV-768 we are having issue with some patients to load diagnosis codes
                // org.hibernate.LazyInitializationException: failed to lazily initialize a collection of
                // role: org.patientview.persistence.model.Code.codeCategories, could not initialize proxy - no Session
                //List<Code> codes = codeService.findAllByCodeAndType(condition.getCode(),
                //      lookupService.findByTypeAndValue(LookupTypes.CODE_TYPE, CodeTypes.DIAGNOSIS.toString()));

                Code code = codeService.findOneByCodeAndType(condition.getCode(),
                        lookupService.findByTypeAndValue(LookupTypes.CODE_TYPE, CodeTypes.DIAGNOSIS.toString()));
                if (code != null) {
                    patient.getDiagnosisCodes().add(code);
                }
            }
        }

        return patient;
    }

    private org.patientview.api.model.Patient setEncounters(org.patientview.api.model.Patient patient,
                                                            List<Encounter> encounters, User user, Group group)
            throws FhirResourceException {

        boolean hasTreatment = false;
        patient.setFhirEncounters(new ArrayList<FhirEncounter>());

        // replace fhirEncounter type field with a more useful description if it exists in codes
        for (Encounter encounter : encounters) {
            FhirEncounter fhirEncounter = new FhirEncounter(encounter);

            List<Code> codes = codeService.findAllByCodeAndType(fhirEncounter.getStatus(),
                    lookupService.findByTypeAndValue(LookupTypes.CODE_TYPE, CodeTypes.TREATMENT.toString()));

            if (!codes.isEmpty()) {
                fhirEncounter.setStatus(codes.get(0).getDescription());
                fhirEncounter.setLinks(codes.get(0).getLinks());
            }

            if (fhirEncounter.getEncounterType().contains("TRANSPLANT")) {
                for (TransplantStatus transplantStatus
                        : TransplantStatus.class.getEnumConstants()) {
                    if (fhirEncounter.getStatus().equals(transplantStatus.toString())) {
                        fhirEncounter.setStatus(transplantStatus.getName());
                    }
                }
            }

            patient.getFhirEncounters().add(fhirEncounter);

            if (fhirEncounter.getEncounterType().equals(EncounterTypes.TREATMENT.toString())) {
                hasTreatment = true;
            }
        }

        // Card #314 if no treatment type encounter exists and patient is member of renal specialty, add GEN
        if (!hasTreatment) {
            boolean isMemberOfRenal = false;
            for (GroupRole groupRole : user.getGroupRoles()) {
                if (groupRole.getGroup().getCode().equals(RENAL_GROUP_CODE)) {
                    isMemberOfRenal = true;
                }
            }
            if (isMemberOfRenal && groupIsRenalChild(group)) {
                List<Code> codes = codeService.findAllByCodeAndType(GEN_CODE,
                        lookupService.findByTypeAndValue(LookupTypes.CODE_TYPE, CodeTypes.TREATMENT.toString()));
                if (!CollectionUtils.isEmpty(codes)) {
                    FhirEncounter fhirEncounter = new FhirEncounter();
                    fhirEncounter.setEncounterType(EncounterTypes.TREATMENT.toString());
                    fhirEncounter.setLinks(codes.get(0).getLinks());
                    fhirEncounter.setStatus(codes.get(0).getDescription());
                    patient.getFhirEncounters().add(fhirEncounter);
                }
            }
        }

        return patient;
    }

    private org.patientview.api.model.Patient setNonTestObservations(org.patientview.api.model.Patient patient,
                                                                     FhirLink fhirLink) {
        try {
            List<String> nonTestTypes = new ArrayList<>();
            for (NonTestObservationTypes observationType : NonTestObservationTypes.class.getEnumConstants()) {
                nonTestTypes.add(observationType.toString());
            }
            patient.getFhirObservations().addAll(apiObservationService.getByFhirLinkAndCodes(fhirLink, nonTestTypes));

        } catch (ResourceNotFoundException | FhirResourceException e) {
            LOG.error("Error setting non test observations: " + e.getMessage());
        }

        return patient;
    }

    private org.patientview.api.model.Patient setPractitioners(
            org.patientview.api.model.Patient patient, Patient fhirPatient) throws FhirResourceException {

        if (fhirPatient.getCareProvider() != null && !fhirPatient.getCareProvider().isEmpty()) {
            for (ResourceReference practitionerReference : fhirPatient.getCareProvider()) {
                Practitioner practitioner = getPractitioner(UUID.fromString(practitionerReference.getDisplaySimple()));
                if (practitioner != null) {
                    FhirPractitioner fhirPractitioner = new FhirPractitioner(practitioner);

                    // check if practitioner has been claimed already using gp letter table
                    GpLetter gpLetter = new GpLetter();
                    gpLetter.setGpName(fhirPractitioner.getName());
                    gpLetter.setGpAddress1(fhirPractitioner.getAddress1());
                    gpLetter.setGpAddress2(fhirPractitioner.getAddress2());
                    gpLetter.setGpAddress3(fhirPractitioner.getAddress3());
                    gpLetter.setGpAddress4(fhirPractitioner.getAddress4());
                    gpLetter.setGpPostcode(fhirPractitioner.getPostcode());

                    // check existing gp letter does not exist for postcode and gp name (claimed or unclaimed)
                    // handle entries with and without spaces in postcode
                    List<GpLetter> gpLetters = new ArrayList<>();

                    if (StringUtils.isNotEmpty(gpLetter.getGpPostcode())
                            && StringUtils.isNotEmpty(gpLetter.getGpName())) {
                        gpLetters.addAll(gpLetterRepository.findByPostcodeAndName(
                                gpLetter.getGpPostcode(), gpLetter.getGpName()));
                        gpLetters.addAll(gpLetterRepository.findByPostcodeAndName(
                                gpLetter.getGpPostcode().replace(" ", ""), gpLetter.getGpName()));
                    }

                    if (gpLetters.isEmpty()) {
                        // no current gp letter, check gp details are suitable for creating gp letter
                        if (gpLetterService.hasValidPracticeDetails(gpLetter)
                                || gpLetterService.hasValidPracticeDetailsSingleMaster(gpLetter)) {
                            fhirPractitioner.setAllowInviteGp(true);
                        }
                    } else {
                        // get correct gp letter associated with this patient's identifier
                        if (!CollectionUtils.isEmpty(fhirPatient.getIdentifier())) {
                            for (org.hl7.fhir.instance.model.Identifier identifier : fhirPatient.getIdentifier()) {
                                for (GpLetter gpLetter1 : gpLetters) {
                                    if (gpLetter1.getPatientIdentifier().equals(identifier.getValueSimple())) {
                                        fhirPractitioner.setInviteDate(gpLetter1.getCreated());
                                    }
                                }
                            }
                        }
                    }

                    patient.getFhirPractitioners().add(fhirPractitioner);
                }
            }
        }

        return patient;
    }

    // API
    @Override
    public void update(Long userId, Long groupId, FhirPatient fhirPatient)
            throws ResourceNotFoundException, FhirResourceException, ResourceForbiddenException {

        // check current logged in user has rights to this group
        if (!(ApiUtil.currentUserHasRole(RoleName.GLOBAL_ADMIN)
                || ApiUtil.doesContainGroupAndRole(groupId, RoleName.UNIT_ADMIN_API))) {
            throw new ResourceForbiddenException("Failed group and role validation");
        }

        // check User exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // check user has fhirLink associated with this group
        FhirLink foundFhirLink = null;
        for (FhirLink fhirLink : user.getFhirLinks()) {
            if (fhirLink.getGroup().getId().equals(groupId)) {
                foundFhirLink = fhirLink;
            }
        }
        if (foundFhirLink == null) {
            throw new ResourceForbiddenException("Failed fhirLink validation");
        }

        // check patient exists
        Patient currentPatient = get(foundFhirLink.getResourceId());
        if (currentPatient == null) {
            throw new ResourceNotFoundException("Patient not found");
        }

        // build updated patient
        PatientBuilder patientBuilder = new PatientBuilder(currentPatient, fhirPatient);
        Patient updatedPatient = patientBuilder.build();

        // store updated patient in FHIR
        FhirDatabaseEntity entity
                = fhirResource.updateEntity(
                updatedPatient, ResourceType.Patient.name(), "patient", foundFhirLink.getResourceId());

        // update FhirLink
        foundFhirLink.setVersionId(entity.getVersionId());
        foundFhirLink.setUpdated(entity.getUpdated());
        fhirLinkRepository.save(foundFhirLink);
    }
}
