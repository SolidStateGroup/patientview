package org.patientview.api.service.impl;

import org.hl7.fhir.instance.model.Encounter;
import org.hl7.fhir.instance.model.Enumeration;
import org.hl7.fhir.instance.model.HumanName;
import org.hl7.fhir.instance.model.Patient;
import org.hl7.fhir.instance.model.Practitioner;
import org.hl7.fhir.instance.model.ResourceType;
import org.patientview.api.model.FhirCondition;
import org.patientview.api.model.FhirEncounter;
import org.patientview.api.service.CodeService;
import org.patientview.api.service.ConditionService;
import org.patientview.api.service.EncounterService;
import org.patientview.api.service.LookupService;
import org.patientview.api.service.PatientService;
import org.patientview.api.util.Util;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.exception.FhirResourceException;
import org.patientview.persistence.model.Code;
import org.patientview.persistence.model.FhirLink;
import org.patientview.persistence.model.Identifier;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.CodeTypes;
import org.patientview.persistence.model.enums.DiagnosisTypes;
import org.patientview.persistence.model.enums.HiddenGroupCodes;
import org.patientview.persistence.model.enums.LookupTypes;
import org.patientview.persistence.repository.UserRepository;
import org.patientview.persistence.resource.FhirResource;
import org.patientview.persistence.util.DataUtils;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Created by james@solidstategroup.com
 * Created on 02/09/2014
 */
@Service
public class PatientServiceImpl extends AbstractServiceImpl<PatientServiceImpl> implements PatientService {

    @Inject
    private FhirResource fhirResource;

    @Inject
    private UserRepository userRepository;

    @Inject
    private CodeService codeService;

    @Inject
    private ConditionService conditionService;

    @Inject
    private EncounterService encounterService;

    @Inject
    private LookupService lookupService;

    @Override
    public List<org.patientview.api.model.Patient> get(final Long userId, final List<Long> groupIds)
            throws FhirResourceException, ResourceNotFoundException {

        boolean restrictGroups = !(groupIds == null || groupIds.isEmpty());
        User user = userRepository.findOne(userId);

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
            if ((restrictGroups && groupIds.contains(fhirLink.getGroup().getId())) || (!restrictGroups)) {
                if (fhirLink.getActive() && !Util.isInEnum(fhirLink.getGroup().getCode(), HiddenGroupCodes.class)) {
                    Patient fhirPatient = get(fhirLink.getResourceId());

                    Practitioner fhirPractitioner = null;
                    if (!fhirPatient.getCareProvider().isEmpty()) {
                        fhirPractitioner
                            = getPractitioner(UUID.fromString(fhirPatient.getCareProvider().get(0).getDisplaySimple()));
                    }

                    org.patientview.api.model.Patient patient = new org.patientview.api.model.Patient(fhirPatient,
                            fhirPractitioner, fhirLink.getGroup(), conditionService.get(fhirLink.getResourceId()));

                    // set encounters
                    patient.getFhirEncounters().addAll(setEncounters(fhirLink.getResourceId()));

                    // set edta diagnosis if present based on available codes
                    patients.add(setDiagnosisCodes(patient));
                }
            }
        }

        return patients;
    }

    @Override
    public Patient get(final UUID uuid) throws FhirResourceException {
        try {
            return (Patient) DataUtils.getResource(fhirResource.getResource(uuid, ResourceType.Patient));
        } catch (Exception e) {
            throw new FhirResourceException(e);
        }
    }

    public Practitioner getPractitioner(final UUID uuid) throws FhirResourceException {
        try {
            return (Practitioner) DataUtils.getResource(fhirResource.getResource(uuid, ResourceType.Practitioner));
        } catch (Exception e) {
            throw new FhirResourceException(e);
        }
    }

    private org.patientview.api.model.Patient setDiagnosisCodes(org.patientview.api.model.Patient patient) {
        for (FhirCondition condition : patient.getFhirConditions()) {
            if (condition.getCategory().equals(DiagnosisTypes.DIAGNOSIS_EDTA.toString())) {

                List<Code> codes = codeService.findAllByCodeAndType(condition.getCode(),
                        lookupService.findByTypeAndValue(LookupTypes.CODE_TYPE, CodeTypes.DIAGNOSIS.toString()));
                if (!codes.isEmpty()) {
                    patient.getDiagnosisCodes().add(codes.get(0));
                }
            }
        }

        return patient;
    }

    private List<FhirEncounter> setEncounters(UUID patientUuid) throws FhirResourceException {
        List<FhirEncounter> fhirEncounters = new ArrayList<>();

        // replace fhirEncounter type field with a more useful description if it exists in codes
        for (Encounter encounter : encounterService.get(patientUuid)) {
            FhirEncounter fhirEncounter = new FhirEncounter(encounter);

            List<Code> codes = codeService.findAllByCodeAndType(fhirEncounter.getType(),
                    lookupService.findByTypeAndValue(LookupTypes.CODE_TYPE, CodeTypes.TREATMENT.toString()));
            if (!codes.isEmpty()) {
                fhirEncounter.setType(codes.get(0).getDescription());
            }

            fhirEncounters.add(fhirEncounter);
        }

        return fhirEncounters;
    }

    @Override
    public Patient buildPatient(User user, Identifier identifier) {
        Patient patient = new Patient();

        patient = createHumanName(patient, user);
        patient = addIdentifier(patient, identifier);

        return patient;
    }

    private Patient createHumanName(Patient patient, User user) {
        HumanName humanName = patient.addName();
        humanName.addFamilySimple(user.getSurname());
        humanName.addGivenSimple(user.getForename());
        Enumeration<HumanName.NameUse> nameUse = new Enumeration(HumanName.NameUse.usual);
        humanName.setUse(nameUse);
        return patient;
    }

    private Patient addIdentifier(Patient patient, Identifier identifier) {
        org.hl7.fhir.instance.model.Identifier fhirIdentifier = patient.addIdentifier();
        fhirIdentifier.setLabelSimple(identifier.getIdentifierType().getValue());
        fhirIdentifier.setValueSimple(identifier.getIdentifier());
        return patient;
    }
}
