package org.patientview.api.service.impl;

import org.hl7.fhir.instance.model.CodeableConcept;
import org.hl7.fhir.instance.model.DateAndTime;
import org.hl7.fhir.instance.model.DateTime;
import org.hl7.fhir.instance.model.DocumentReference;
import org.hl7.fhir.instance.model.ResourceType;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.patientview.api.controller.BaseController;
import org.patientview.api.model.FhirDocumentReference;
import org.patientview.api.service.LetterService;
import org.patientview.api.util.Util;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.FhirLink;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.User;
import org.patientview.persistence.repository.FhirLinkRepository;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.UserRepository;
import org.patientview.persistence.resource.FhirResource;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.inject.Named;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 07/10/2014
 */
@Service
public class LetterServiceImpl extends BaseController<LetterServiceImpl> implements LetterService {

    @Inject
    private FhirResource fhirResource;

    @Inject
    private FhirLinkRepository fhirLinkRepository;

    @Inject
    private UserRepository userRepository;

    @Inject
    private GroupRepository groupRepository;

    @Inject
    @Named("fhir")
    private DataSource dataSource;

    @Override
    public List<FhirDocumentReference> getByUserId(final Long userId)
            throws ResourceNotFoundException, FhirResourceException {

        User user = userRepository.findOne(userId);
        if (user == null) {
            throw new ResourceNotFoundException("Could not find user");
        }

        List<FhirDocumentReference> fhirDocumentReferences = new ArrayList<>();

        for (FhirLink fhirLink : user.getFhirLinks()) {
            if (fhirLink.getActive()) {
                StringBuilder query = new StringBuilder();
                query.append("SELECT  content::varchar ");
                query.append("FROM    documentreference ");
                query.append("WHERE   content->> 'subject' = '{\"display\": \"");
                query.append(fhirLink.getResourceId().toString());
                query.append("\", \"reference\": \"uuid\"}'");

                // get list of DocumentReference
                List<DocumentReference> documentReferences
                    = fhirResource.findResourceByQuery(query.toString(), DocumentReference.class);

                // for each, create new transport object
                for (DocumentReference documentReference : documentReferences) {
                    org.patientview.persistence.model.FhirDocumentReference fhirDocumentReference
                            = new org.patientview.persistence.model.FhirDocumentReference(
                            documentReference, fhirLink.getGroup());
                    fhirDocumentReferences.add(new FhirDocumentReference(fhirDocumentReference));
                }
            }
        }

        return fhirDocumentReferences;
    }

    @Override
    public void addLetter(
            org.patientview.persistence.model.FhirDocumentReference fhirDocumentReference, FhirLink fhirLink)
            throws FhirResourceException {

        DocumentReference documentReference = new DocumentReference();
        documentReference.setStatusSimple(DocumentReference.DocumentReferenceStatus.current);
        documentReference.setSubject(Util.createFhirResourceReference(fhirLink.getResourceId()));

        CodeableConcept type = new CodeableConcept();
        type.setTextSimple(fhirDocumentReference.getType());
        documentReference.setType(type);

        documentReference.setDescriptionSimple(fhirDocumentReference.getContent());

        try {
            DateAndTime dateAndTime = new DateAndTime(fhirDocumentReference.getDate());
            DateTime date = new DateTime();
            date.setValue(dateAndTime);
            documentReference.setCreated(date);
        } catch (NullPointerException npe) {
            throw new FhirResourceException("Letter timestamp is incorrectly formatted");
        }

        fhirResource.create(documentReference);
    }

    @Override
    public void delete(Long userId, Long groupId, Long date) throws ResourceNotFoundException, FhirResourceException {
        User entityUser = userRepository.findOne(userId);
        if (entityUser == null) {
            throw new ResourceNotFoundException("User not found");
        }
        Group entityGroup = groupRepository.findOne(groupId);
        if (entityGroup == null) {
            throw new ResourceNotFoundException("Group not found");
        }

        List<UUID> documentReferenceUuids = new ArrayList<>();

        // get all letters
        for (FhirLink fhirLink : fhirLinkRepository.findByUserAndGroup(entityUser, entityGroup)) {
            documentReferenceUuids.addAll(
                    fhirResource.getLogicalIdsBySubjectId("documentreference", fhirLink.getResourceId()));
        }

        List<UUID> documentReferenceUuidsToDelete = new ArrayList<>();

        // get to be deleted by date
        for (UUID uuid : documentReferenceUuids) {
            DocumentReference documentReference
                    = (DocumentReference) fhirResource.get(uuid, ResourceType.DocumentReference);

            DateAndTime dateAndTime = documentReference.getCreated().getValue();
            String dateString = dateAndTime.toString();
            DateTimeFormatter parser2 = ISODateTimeFormat.dateTimeNoMillis();
            org.joda.time.DateTime dateTime = parser2.parseDateTime(dateString);

            if (dateTime.getMillis() == date) {
                documentReferenceUuidsToDelete.add(uuid);
            }
        }

        for (UUID uuid : documentReferenceUuidsToDelete) {
            fhirResource.delete(uuid, ResourceType.DocumentReference);
        }
    } 
}
