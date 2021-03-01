package org.patientview.api.service.impl;

import org.apache.commons.lang.StringUtils;
import org.hl7.fhir.instance.model.CodeableConcept;
import org.hl7.fhir.instance.model.DateAndTime;
import org.hl7.fhir.instance.model.DateTime;
import org.hl7.fhir.instance.model.DocumentReference;
import org.hl7.fhir.instance.model.Media;
import org.hl7.fhir.instance.model.Patient;
import org.hl7.fhir.instance.model.ResourceType;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.patientview.api.builder.PatientBuilder;
import org.patientview.api.service.ApiPatientService;
import org.patientview.api.service.LetterService;
import org.patientview.api.service.UserService;
import org.patientview.api.util.ApiUtil;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.Audit;
import org.patientview.persistence.model.FhirDatabaseEntity;
import org.patientview.persistence.model.FhirLink;
import org.patientview.persistence.model.FhirPatient;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.Identifier;
import org.patientview.persistence.model.ServerResponse;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.AuditActions;
import org.patientview.persistence.model.enums.AuditObjectTypes;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.persistence.repository.FhirLinkRepository;
import org.patientview.persistence.repository.FileDataRepository;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.IdentifierRepository;
import org.patientview.persistence.repository.UserRepository;
import org.patientview.persistence.resource.FhirResource;
import org.patientview.service.AuditService;
import org.patientview.service.DocumentReferenceService;
import org.patientview.util.Util;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.patientview.api.util.ApiUtil.getCurrentUser;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 07/10/2014
 */
@Service
@Transactional
public class LetterServiceImpl extends AbstractServiceImpl<LetterServiceImpl> implements LetterService {

    @Inject
    private ApiPatientService apiPatientService;

    @Inject
    private AuditService auditService;

    @Inject
    private DocumentReferenceService documentReferenceService;

    @Inject
    private FhirLinkRepository fhirLinkRepository;

    @Inject
    private FhirResource fhirResource;

    @Inject
    private FileDataRepository fileDataRepository;

    @Inject
    private GroupRepository groupRepository;

    @Inject
    private IdentifierRepository identifierRepository;

    @Inject
    private UserRepository userRepository;

    @Inject
    private UserService userService;

    // used by migration
    @Override
    public void addLetter(
            org.patientview.persistence.model.FhirDocumentReference fhirDocumentReference, FhirLink fhirLink)
            throws FhirResourceException {

        DocumentReference documentReference = new DocumentReference();
        documentReference.setStatusSimple(DocumentReference.DocumentReferenceStatus.current);
        documentReference.setSubject(Util.createResourceReference(fhirLink.getResourceId()));

        if (StringUtils.isNotEmpty(fhirDocumentReference.getType())) {
            CodeableConcept type = new CodeableConcept();
            type.setTextSimple(fhirDocumentReference.getType());
            documentReference.setType(type);
        }

        if (StringUtils.isNotEmpty(fhirDocumentReference.getContent())) {
            documentReference.setDescriptionSimple(fhirDocumentReference.getContent());
        }

        if (fhirDocumentReference.getDate() != null) {
            try {
                DateAndTime dateAndTime = new DateAndTime(fhirDocumentReference.getDate());
                DateTime date = new DateTime();
                date.setValue(dateAndTime);
                documentReference.setCreated(date);
            } catch (NullPointerException npe) {
                throw new FhirResourceException("Letter timestamp is incorrectly formatted");
            }
        }

        fhirResource.createEntity(documentReference, ResourceType.DocumentReference.name(), "documentreference");
    }

    @Override
    public void delete(Long userId, Long groupId, Long date) throws ResourceNotFoundException, FhirResourceException {
        User entityUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Group entityGroup = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found"));

        List<UUID> documentReferenceUuids = new ArrayList<>();
        Map<UUID, DocumentReference> documentReferenceMap = new HashMap<>();
        Map<UUID, FhirLink> fhirLinkMap = new HashMap<>();

        // get all letters
        for (FhirLink fhirLink : fhirLinkRepository.findByUserAndGroup(entityUser, entityGroup)) {
            List<UUID> logicalIds
                    = fhirResource.getLogicalIdsBySubjectId("documentreference", fhirLink.getResourceId());

            for (UUID logicalId : logicalIds) {
                fhirLinkMap.put(logicalId, fhirLink);
            }

            documentReferenceUuids.addAll(logicalIds);
        }

        List<UUID> documentReferenceUuidsToDelete = new ArrayList<>();
        List<UUID> mediaUuidsToDelete = new ArrayList<>();

        // get documentreference to be deleted by date
        for (UUID uuid : documentReferenceUuids) {
            DocumentReference documentReference
                    = (DocumentReference) fhirResource.get(uuid, ResourceType.DocumentReference);

            DateAndTime dateAndTime = documentReference.getCreated().getValue();
            String dateString = dateAndTime.toString();
            DateTimeFormatter parser2 = ISODateTimeFormat.dateTimeNoMillis();
            org.joda.time.DateTime dateTime = parser2.parseDateTime(dateString);

            // if documentreference date matches and doesn't have a class (letters do not have class)
            if (dateTime.getMillis() == date && documentReference.getClass_() == null) {
                documentReferenceUuidsToDelete.add(uuid);
                documentReferenceMap.put(uuid, documentReference);

                // add media uuid to delete
                if (StringUtils.isNotEmpty(documentReference.getLocationSimple())) {
                    mediaUuidsToDelete.add(UUID.fromString(documentReference.getLocationSimple()));
                }
            }
        }

        for (UUID uuid : documentReferenceUuidsToDelete) {
            // delete documentreference
            fhirResource.deleteEntity(uuid, "documentreference");

            FhirLink fhirLink = fhirLinkMap.get(uuid);
            Group group = null;

            if (fhirLink != null && fhirLink.getGroup() != null) {
                group = fhirLink.getGroup();
            }

            Audit audit = new Audit();
            audit.setAuditActions(AuditActions.PATIENT_LETTER_DELETE);
            audit.setUsername(entityUser.getUsername());
            audit.setActorId(getCurrentUser().getId());
            audit.setGroup(group);
            audit.setSourceObjectId(entityUser.getId());
            audit.setSourceObjectType(AuditObjectTypes.User);

            DocumentReference doc = documentReferenceMap.get(uuid);

            String type = null;
            if (doc.getType() != null) {
                type = doc.getType().getTextSimple();
            }

            String dateString;
            if (doc.getCreated() != null) {
                dateString = doc.getCreated().getValue().toHumanDisplay();
            } else {
                dateString = "unknown date";
            }

            audit.setInformation(type + " (" + dateString + ")");
            auditService.save(audit);
        }

        // delete associated media and file data
        for (UUID uuid : mediaUuidsToDelete) {
            // get media
            Media media = (Media) fhirResource.get(uuid, ResourceType.Media);
            if (media != null) {
                // check if filedata information available
                if (media.getContent() != null && media.getContent().getUrl() != null) {
                    try {
                        // delete file data
                        fileDataRepository.deleteById(Long.valueOf(media.getContent().getUrlSimple()));
                    } catch (NumberFormatException nfe) {
                        LOG.info("Error deleting binary data, Media reference to binary data is not Long, ignoring..");
                    }
                }

                // delete media
                fhirResource.deleteEntity(uuid, "media");
            }
        }
    }

    @Override
    public ServerResponse importLetter(org.patientview.persistence.model.FhirDocumentReference fhirDocumentReference) {
        if (StringUtils.isEmpty(fhirDocumentReference.getGroupCode())) {
            return new ServerResponse("group code not set");
        }
        if (StringUtils.isEmpty(fhirDocumentReference.getIdentifier())) {
            return new ServerResponse("identifier not set");
        }
        if (StringUtils.isEmpty(fhirDocumentReference.getContent())) {
            return new ServerResponse("content not set");
        }
        if (StringUtils.isEmpty(fhirDocumentReference.getType())) {
            return new ServerResponse("type not set");
        }
        if (fhirDocumentReference.getDate() == null) {
            return new ServerResponse("date not set");
        }

        Group group = groupRepository.findByCode(fhirDocumentReference.getGroupCode());

        if (group == null) {
            return new ServerResponse("group not found");
        }

        // check current logged in user has rights to this group
        if (!(ApiUtil.currentUserHasRole(RoleName.GLOBAL_ADMIN)
                || ApiUtil.doesContainGroupAndRole(group.getId(), RoleName.IMPORTER))) {
            return new ServerResponse("failed group and role validation");
        }

        List<Identifier> identifiers = identifierRepository.findByValue(fhirDocumentReference.getIdentifier());

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
        FhirLink fhirLink = Util.getFhirLink(group, fhirDocumentReference.getIdentifier(), user.getFhirLinks());

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

        // now have Patient object and reference to logical id in fhirlink
        try {
            documentReferenceService.add(fhirDocumentReference, fhirLink);
        } catch (FhirResourceException e) {
            return new ServerResponse("error adding letter: " + e.getMessage());
        }

        return new ServerResponse(null, "done", true);
    }
}
