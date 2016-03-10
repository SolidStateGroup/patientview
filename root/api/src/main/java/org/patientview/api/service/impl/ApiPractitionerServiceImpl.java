package org.patientview.api.service.impl;

import org.apache.commons.lang.StringUtils;
import org.hl7.fhir.instance.model.Patient;
import org.hl7.fhir.instance.model.Practitioner;
import org.hl7.fhir.instance.model.ResourceReference;
import org.hl7.fhir.instance.model.ResourceType;
import org.patientview.api.builder.PatientBuilder;
import org.patientview.api.builder.PractitionerBuilder;
import org.patientview.api.service.ApiPatientService;
import org.patientview.api.service.ApiPractitionerService;
import org.patientview.api.util.ApiUtil;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.persistence.model.FhirDatabaseEntity;
import org.patientview.persistence.model.FhirLink;
import org.patientview.persistence.model.FhirPatient;
import org.patientview.persistence.model.FhirPractitioner;
import org.patientview.persistence.model.GpLetter;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.Identifier;
import org.patientview.persistence.model.ServerResponse;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.PractitionerRoles;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.persistence.repository.FhirLinkRepository;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.IdentifierRepository;
import org.patientview.persistence.repository.UserRepository;
import org.patientview.persistence.resource.FhirResource;
import org.patientview.service.GpLetterService;
import org.patientview.service.PractitionerService;
import org.patientview.util.Util;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.inject.Inject;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

/**
 * Practitioner service, for handling updates to a patient's practitioner by API importer
 *
 * Created by james@solidstategroup.com
 * Created on 04/03/2016
 */
@Service
public class ApiPractitionerServiceImpl extends AbstractServiceImpl<ApiPractitionerServiceImpl>
        implements ApiPractitionerService {

    @Inject
    private ApiPatientService apiPatientService;

    @Inject
    private FhirLinkRepository fhirLinkRepository;

    @Inject
    private FhirResource fhirResource;

    @Inject
    private GpLetterService gpLetterService;

    @Inject
    private GroupRepository groupRepository;

    @Inject
    private IdentifierRepository identifierRepository;

    @Inject
    private PractitionerService practitionerService;

    @Inject
    private UserRepository userRepository;

    @Override
    @Transactional
    public ServerResponse importPractitioner(FhirPractitioner fhirPractitioner) {
        if (StringUtils.isEmpty(fhirPractitioner.getGroupCode())) {
            return new ServerResponse("group code not set");
        }
        if (StringUtils.isEmpty(fhirPractitioner.getIdentifier())) {
            return new ServerResponse("identifier not set");
        }

        Group group = groupRepository.findByCode(fhirPractitioner.getGroupCode());

        if (group == null) {
            return new ServerResponse("group not found");
        }

        // check current logged in user has rights to this group
        if (!(ApiUtil.currentUserHasRole(RoleName.GLOBAL_ADMIN)
                || ApiUtil.doesContainGroupAndRole(group.getId(), RoleName.IMPORTER))) {
            return new ServerResponse("failed group and role validation");
        }

        List<Identifier> identifiers = identifierRepository.findByValue(fhirPractitioner.getIdentifier());

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

        // get FhirLink
        FhirLink fhirLink = Util.getFhirLink(group, fhirPractitioner.getIdentifier(), user.getFhirLinks());

        // FHIR patient object
        Patient patient;

        // handle if no fhirlink or patient record associated with fhirlink
        if (fhirLink == null) {
            // no FhirLink exists, create one, build basic patient
            FhirPatient fhirPatient = new FhirPatient();
            fhirPatient.setForename(user.getForename());
            fhirPatient.setSurname(user.getSurname());
            fhirPatient.setDateOfBirth(user.getDateOfBirth());

            PatientBuilder patientBuilder = new PatientBuilder(null, fhirPatient);
            patient = patientBuilder.build();
            FhirDatabaseEntity patientEntity;

            // store new patient in FHIR
            try {
                patientEntity = fhirResource.createEntity(patient, ResourceType.Patient.name(), "patient");
            } catch (FhirResourceException fre) {
                return new ServerResponse("error creating patient");
            }

            // create FhirLink
            fhirLink = new FhirLink();
            fhirLink.setUser(user);
            fhirLink.setIdentifier(identifier);
            fhirLink.setGroup(group);
            fhirLink.setResourceId(patientEntity.getLogicalId());
            fhirLink.setVersionId(patientEntity.getVersionId());
            fhirLink.setResourceType(ResourceType.Patient.name());
            fhirLink.setActive(true);
            fhirLink.setIsNew(true);

            if (CollectionUtils.isEmpty(user.getFhirLinks())) {
                user.setFhirLinks(new HashSet<FhirLink>());
            }

            user.getFhirLinks().add(fhirLink);
            userRepository.save(user);
        } else {
            FhirDatabaseEntity patientEntity;

            // FhirLink exists, check patient exists
            if (fhirLink.getResourceId() == null) {
                return new ServerResponse("error retrieving patient, no UUID");
            }

            try {
                patient = apiPatientService.get(fhirLink.getResourceId());
            } catch (FhirResourceException fre) {
                return new ServerResponse("error retrieving patient");
            }

            if (patient == null) {
                // no patient exists, build basic
                FhirPatient fhirPatient = new FhirPatient();
                fhirPatient.setForename(user.getForename());
                fhirPatient.setSurname(user.getSurname());
                fhirPatient.setDateOfBirth(user.getDateOfBirth());

                // build patient
                PatientBuilder patientBuilder = new PatientBuilder(null, fhirPatient);
                patient = patientBuilder.build();

                // create patient in FHIR, update FhirLink with newly created resource ID
                try {
                    patientEntity = fhirResource.createEntity(patient, ResourceType.Patient.name(), "patient");

                    if (patientEntity == null) {
                        return new ServerResponse("error storing patient");
                    }

                    // update fhirlink and save
                    fhirLink.setResourceId(patientEntity.getLogicalId());
                    fhirLink.setResourceType(ResourceType.Patient.name());
                    fhirLink.setVersionId(patientEntity.getVersionId());
                    fhirLink.setUpdated(patientEntity.getUpdated());
                    fhirLink.setActive(true);
                    fhirLinkRepository.save(fhirLink);
                } catch (FhirResourceException fre) {
                    return new ServerResponse("error creating patient");
                }
            }
        }

        // now have Patient object and reference to logical id in fhirlink, get practitioner
        if (CollectionUtils.isEmpty(patient.getCareProvider())) {
            UUID practitionerUuid;

            // no care providers, add practitioner and store against patient
            try {
                // build practitioner
                PractitionerBuilder practitionerBuilder = new PractitionerBuilder(null, fhirPractitioner);
                Practitioner practitioner = practitionerBuilder.build();

                // save in FHIR
                FhirDatabaseEntity createdPractitioner
                        = fhirResource.createEntity(practitioner, ResourceType.Practitioner.name(), "practitioner");
                practitionerUuid = createdPractitioner.getLogicalId();
            } catch (FhirResourceException fre) {
                return new ServerResponse("error creating practitioner");
            }

            // update FHIR patient with reference to new care provider
            ResourceReference careProvider = patient.addCareProvider();
            careProvider.setReferenceSimple("uuid");
            careProvider.setDisplaySimple(practitionerUuid.toString());

            try {
                fhirResource.updateEntity(patient, ResourceType.Patient.name(), "patient", fhirLink.getResourceId());
            } catch (FhirResourceException fre) {
                return new ServerResponse("error updating patient with practitioner");
            }
        } else {
            // patient has care providers, check which is GP (could have other practitioners)
            UUID practitionerUuid = null;
            Practitioner practitioner = null;

            for (ResourceReference ref : patient.getCareProvider()) {
                try {
                    practitioner = (Practitioner) fhirResource.get(
                            UUID.fromString(ref.getDisplaySimple()), ResourceType.Practitioner);

                    if (practitioner != null) {
                        // GP practitioners do not have a role but may in future, check against roles
                        if (CollectionUtils.isEmpty(practitioner.getRole())) {
                            practitionerUuid = UUID.fromString(ref.getDisplaySimple());
                        } else {
                            // may have role set as PractitionerRoles.GP
                            String role = practitioner.getRole().get(0).getTextSimple();
                            if (StringUtils.isNotEmpty(role) && PractitionerRoles.valueOf(role) != null
                                    && PractitionerRoles.valueOf(role).equals(PractitionerRoles.GP)) {
                                practitionerUuid = UUID.fromString(ref.getDisplaySimple());
                            }
                        }
                    }
                } catch (FhirResourceException fre) {
                    return new ServerResponse("error retrieving patient practitioner");
                }
            }

            // if practitioner is missing from FHIR, but linked to from patient (unlikely), need to create
            boolean createPractitioner = practitioner == null;

            // now should have the GP practitioner and uuid, can update
            PractitionerBuilder practitionerBuilder = new PractitionerBuilder(practitioner, fhirPractitioner);
            practitioner = practitionerBuilder.build();

            if (createPractitioner) {
                try {
                    // create practitioner
                    FhirDatabaseEntity createdPractitioner
                            = fhirResource.createEntity(practitioner, ResourceType.Practitioner.name(), "practitioner");

                    // add to patient care providers
                    ResourceReference careProvider = patient.addCareProvider();
                    careProvider.setReferenceSimple("uuid");
                    careProvider.setDisplaySimple(createdPractitioner.getLogicalId().toString());

                    // update patient
                    try {
                        fhirResource.updateEntity(patient, ResourceType.Patient.name(), "patient",
                                fhirLink.getResourceId());
                    } catch (FhirResourceException fre) {
                        return new ServerResponse("error updating patient with missing practitioner");
                    }
                } catch (FhirResourceException fre) {
                    return new ServerResponse("error creating missing practitioner");
                }
            } else {
                try {
                    fhirResource.updateEntity(
                            practitioner, ResourceType.Practitioner.name(), "practitioner", practitionerUuid);
                } catch (FhirResourceException fre) {
                    return new ServerResponse("error updating practitioner");
                }
            }
        }

        // generate invite letter, if it meets the criteria
        try {
            GpLetter gpLetter = new GpLetter();

            // gp details
            gpLetter.setGpName(fhirPractitioner.getName());
            gpLetter.setGpAddress1(fhirPractitioner.getAddress1());
            gpLetter.setGpAddress2(fhirPractitioner.getAddress2());
            gpLetter.setGpAddress3(fhirPractitioner.getAddress3());
            gpLetter.setGpAddress4(fhirPractitioner.getAddress4());
            gpLetter.setGpPostcode(fhirPractitioner.getPostcode());

            // patient details
            FhirPatient fhirPatient = new FhirPatient(patient);
            gpLetter.setPatientForename(fhirPatient.getForename());
            gpLetter.setPatientSurname(fhirPatient.getSurname());
            gpLetter.setPatientDateOfBirth(fhirPatient.getDateOfBirth());
            gpLetter.setPatientIdentifier(fhirPractitioner.getIdentifier());

            gpLetterService.createGpLetter(fhirLink, gpLetter);
        } catch (Exception e) {
            LOG.info("Could not create GP letter, continuing: " + e.getMessage());
        }

        return new ServerResponse(null, "done", true);
    }
}
