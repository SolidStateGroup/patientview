package org.patientview.service.impl;

import generated.Patientview;
import org.apache.commons.lang.StringUtils;
import org.hl7.fhir.instance.model.AllergyIntolerance;
import org.hl7.fhir.instance.model.DiagnosticReport;
import org.hl7.fhir.instance.model.DocumentReference;
import org.hl7.fhir.instance.model.Enumeration;
import org.hl7.fhir.instance.model.HumanName;
import org.hl7.fhir.instance.model.Media;
import org.hl7.fhir.instance.model.MedicationStatement;
import org.hl7.fhir.instance.model.Patient;
import org.hl7.fhir.instance.model.ResourceReference;
import org.hl7.fhir.instance.model.ResourceType;
import org.patientview.builder.PatientBuilder;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.FhirDatabaseEntity;
import org.patientview.persistence.model.FhirLink;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.Identifier;
import org.patientview.persistence.model.User;
import org.patientview.persistence.repository.FhirLinkRepository;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.IdentifierRepository;
import org.patientview.persistence.repository.UserRepository;
import org.patientview.persistence.resource.FhirResource;
import org.patientview.service.FileDataService;
import org.patientview.service.PatientService;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.inject.Inject;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Created by james@solidstategroup.com
 * Created on 21/08/2014
 */
@Service
public class PatientServiceImpl extends AbstractServiceImpl<PatientServiceImpl> implements PatientService {

    @Inject
    private FileDataService fileDataService;

    @Inject
    private FhirLinkRepository fhirLinkRepository;

    @Inject
    private FhirResource fhirResource;

    @Inject
    private GroupRepository groupRepository;

    @Inject
    private IdentifierRepository identifierRepository;

    @Inject
    private UserRepository userRepository;

    private String nhsno;

    /**
     * We link fhir records against NHS Number, Unit and User.
     *
     * @param patient
     * @throws ResourceNotFoundException
     * @throws FhirResourceException
     */
    @Override
    public FhirLink add(final Patientview patient, final ResourceReference practitionerReference)
            throws ResourceNotFoundException, FhirResourceException {

        this.nhsno = patient.getPatient().getPersonaldetails().getNhsno();
        LOG.trace(nhsno + ": Starting Patient Process");

        // Find the identifier which the patient is linked to.
        Identifier identifier = matchPatientByIdentifierValue(patient);

        // Find the group that is importing the data
        Group group = groupRepository.findByCode(patient.getCentredetails().getCentrecode());

        // Find the link between the existing User, Unit and the FHIR Record
        FhirLink fhirLink = retrieveLink(group, identifier);

        // create Fhir patient from XML
        PatientBuilder patientBuilder = new PatientBuilder(patient, practitionerReference);
        Patient newFhirPatient = patientBuilder.build();

        if (fhirLink != null) {
            // link to FHIR exists, native update patient
            FhirDatabaseEntity patientEntity
                    = fhirResource.updateEntity(newFhirPatient,
                    ResourceType.Patient.name(), "patient", fhirLink.getResourceId());

            // update FHIR link
            fhirLink.setVersionId(patientEntity.getVersionId());
            fhirLink.setUpdated(patientEntity.getUpdated());
            fhirLinkRepository.save(fhirLink);
        } else {
            // Create a new FHIR record and add the link to the User and Unit
            FhirDatabaseEntity patientEntity
                    = fhirResource.createEntity(newFhirPatient, ResourceType.Patient.name(), "patient");
            fhirLink = addLink(identifier, group, patientEntity);

            // set isNew for use when creating GP letter entries
            fhirLink.setIsNew(true);
        }

        // update User date of birth, forename, surname (if present in imported data)
        if (patient.getPatient().getPersonaldetails().getDateofbirth() != null
                || StringUtils.isNotEmpty(patient.getPatient().getPersonaldetails().getForename())
                || StringUtils.isNotEmpty(patient.getPatient().getPersonaldetails().getSurname())) {

            User entityUser = userRepository.findById(fhirLink.getUser().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            if (patient.getPatient().getPersonaldetails().getDateofbirth() != null) {
                entityUser.setDateOfBirth(
                        patient.getPatient().getPersonaldetails().getDateofbirth().toGregorianCalendar().getTime());
            }

            if (StringUtils.isNotEmpty(patient.getPatient().getPersonaldetails().getForename())) {
                entityUser.setForename(patient.getPatient().getPersonaldetails().getForename());
            }

            if (StringUtils.isNotEmpty(patient.getPatient().getPersonaldetails().getSurname())) {
                entityUser.setSurname(patient.getPatient().getPersonaldetails().getSurname());
            }

            userRepository.save(entityUser);
        }

        LOG.info(nhsno + ": Processed Patient");
        return fhirLink;
    }

    private Patient addIdentifier(Patient patient, Identifier identifier) {
        org.hl7.fhir.instance.model.Identifier fhirIdentifier = patient.addIdentifier();
        fhirIdentifier.setLabelSimple(identifier.getIdentifierType().getValue());
        fhirIdentifier.setValueSimple(identifier.getIdentifier());
        return patient;
    }

    private FhirLink addLink(Identifier identifier, Group group, FhirDatabaseEntity entity) {
        if (CollectionUtils.isEmpty(identifier.getUser().getFhirLinks())) {
            identifier.getUser().setFhirLinks(new HashSet<FhirLink>());
        }

        Date now = new Date();

        FhirLink fhirLink = new FhirLink();
        fhirLink.setUser(identifier.getUser());
        fhirLink.setIdentifier(identifier);
        fhirLink.setGroup(group);
        fhirLink.setResourceId(entity.getLogicalId());
        fhirLink.setVersionId(entity.getVersionId());
        fhirLink.setResourceType(ResourceType.Patient.name());
        fhirLink.setActive(true);
        fhirLink.setCreated(now);
        fhirLink.setUpdated(now);

        identifier.getUser().getFhirLinks().add(fhirLink);
        userRepository.save(identifier.getUser());

        return fhirLink;
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
        if (StringUtils.isNotEmpty(user.getSurname())) {
            humanName.addFamilySimple(user.getSurname());
        }
        if (StringUtils.isNotEmpty(user.getForename())) {
            humanName.addGivenSimple(user.getForename());
        }
        Enumeration<HumanName.NameUse> nameUse = new Enumeration<>(HumanName.NameUse.usual);
        humanName.setUse(nameUse);
        return patient;
    }

    @Override
    public void deleteExistingPatientData(Set<FhirLink> fhirLinks) throws FhirResourceException {

        if (fhirLinks != null) {
            for (FhirLink fhirLink : fhirLinks) {

                UUID subjectId = fhirLink.getResourceId();

                // Patient
                fhirResource.deleteEntity(subjectId, "patient");

                // Conditions
                for (UUID uuid : fhirResource.getLogicalIdsBySubjectId("condition", subjectId)) {
                    fhirResource.deleteEntity(uuid, "condition");
                }

                // Encounters
                for (UUID uuid : fhirResource.getLogicalIdsBySubjectId("encounter", subjectId)) {
                    fhirResource.deleteEntity(uuid, "encounter");
                }

                // MedicationStatements (and associated Medicine)
                for (UUID uuid : fhirResource.getLogicalIdsByPatientId("medicationstatement", subjectId)) {

                    // delete medication associated with medication statement
                    MedicationStatement medicationStatement
                            = (MedicationStatement) fhirResource.get(uuid, ResourceType.MedicationStatement);
                    if (medicationStatement != null) {
                        fhirResource.deleteEntity(
                                UUID.fromString(medicationStatement.getMedication().getDisplaySimple()), "medication");

                        // delete medication statement
                        fhirResource.deleteEntity(uuid, "medicationstatement");
                    }
                }

                // DiagnosticReports (and associated Observation)
                for (UUID uuid : fhirResource.getLogicalIdsBySubjectId("diagnosticreport", subjectId)) {

                    // delete observation (result) associated with diagnostic report
                    DiagnosticReport diagnosticReport
                            = (DiagnosticReport) fhirResource.get(uuid, ResourceType.DiagnosticReport);
                    if (diagnosticReport != null) {
                        fhirResource.deleteEntity(
                                UUID.fromString(diagnosticReport.getResult().get(0).getDisplaySimple()), "observation");

                        // delete diagnostic report
                        fhirResource.deleteEntity(uuid, "diagnosticreport");
                    }
                }

                // DocumentReferences (letters) including associated Media and FileData
                for (UUID uuid : fhirResource.getLogicalIdsBySubjectId("documentreference", subjectId)) {
                    DocumentReference documentReference
                            = (DocumentReference) fhirResource.get(uuid, ResourceType.DocumentReference);
                    if (documentReference != null) {
                        if (documentReference.getLocation() != null) {
                            // check if media exists, if so try deleting binary data associated with media url
                            Media media = (Media) fhirResource.get(
                                    UUID.fromString(documentReference.getLocationSimple()), ResourceType.Media);

                            if (media != null) {
                                // delete media
                                fhirResource.deleteEntity(
                                        UUID.fromString(documentReference.getLocationSimple()), "media");
                                if (media.getContent() != null && media.getContent().getUrl() != null) {
                                    try {
                                        // delete binary data
                                        fileDataService.delete(Long.valueOf(media.getContent().getUrlSimple()));
                                    } catch (NumberFormatException nfe) {
                                        LOG.error("Error deleting FileData, NumberFormatException for Media url");
                                    } catch (EmptyResultDataAccessException e) {
                                        LOG.error("Error deleting FileData, no entity with id exists");
                                    }
                                }
                            }
                        }
                        // delete DocumentReference
                        fhirResource.deleteEntity(uuid, "documentreference");
                    }
                }

                // AllergyIntolerance and Substance (allergy)
                for (UUID uuid : fhirResource.getLogicalIdsBySubjectId("allergyintolerance", subjectId)) {

                    // delete Substance associated with AllergyIntolerance
                    AllergyIntolerance allergyIntolerance
                            = (AllergyIntolerance) fhirResource.get(uuid, ResourceType.AllergyIntolerance);
                    if (allergyIntolerance != null) {
                        fhirResource.deleteEntity(UUID.fromString(allergyIntolerance.getSubstance().getDisplaySimple()),
                                "substance");

                        // delete AllergyIntolerance
                        fhirResource.deleteEntity(uuid, "allergyintolerance");
                    }
                }

                // AdverseReaction (allergy)
                for (UUID uuid : fhirResource.getLogicalIdsBySubjectId("adversereaction", subjectId)) {
                    fhirResource.deleteEntity(uuid, "adversereaction");
                }
            }
        }
    }

    public Identifier matchPatientByIdentifierValue(Patientview patientview) throws ResourceNotFoundException {

        // should only ever be one
        List<Identifier> identifiers = identifierRepository.findByValue(
                patientview.getPatient().getPersonaldetails().getNhsno());

        if (CollectionUtils.isEmpty(identifiers)) {
            throw new ResourceNotFoundException(nhsno + ": The Identifier value is not linked with PatientView");
        }

        return identifiers.get(0);
    }

    // return most recent as ordered by created DESC
    private FhirLink retrieveLink(Group group, Identifier identifier) {
        List<FhirLink> fhirLinks = fhirLinkRepository.findByUserAndGroupAndIdentifier(
                identifier.getUser(), group, identifier);

        if (!fhirLinks.isEmpty()) {
            return fhirLinks.get(0);
        } else {
            return null;
        }
    }
}
