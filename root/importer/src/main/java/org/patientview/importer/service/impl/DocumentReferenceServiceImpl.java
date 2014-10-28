package org.patientview.importer.service.impl;

import generated.Patientview;
import org.hl7.fhir.instance.model.DocumentReference;
import org.hl7.fhir.instance.model.ResourceReference;
import org.hl7.fhir.instance.model.ResourceType;
import org.patientview.importer.builder.DocumentReferenceBuilder;
import org.patientview.importer.resource.FhirResource;
import org.patientview.importer.service.DocumentReferenceService;
import org.patientview.importer.Utility.Util;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.persistence.model.FhirLink;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 07/10/2014
 */
@Service
public class DocumentReferenceServiceImpl extends AbstractServiceImpl<DocumentReferenceService> implements DocumentReferenceService {

    @Inject
    private FhirResource fhirResource;

    /**
     * Creates all of the FHIR DocumentReference records from the Patientview object.
     * Links them to the Patient by subject.
     *
     * @param data patientview data from xml
     * @param fhirLink FhirLink for user
     */
    @Override
    public void add(final Patientview data, final FhirLink fhirLink) throws FhirResourceException, SQLException {

        LOG.info("Starting DocumentReference (letter) Process");
        ResourceReference patientReference = Util.createResourceReference(fhirLink.getResourceId());
        int success = 0;

        DocumentReferenceBuilder documentReferenceBuilder = new DocumentReferenceBuilder(data, patientReference);
        List<DocumentReference> documentReferences = documentReferenceBuilder.build();
        LOG.info("Built {} of {} DocumentReference", documentReferenceBuilder.getSuccess(),
                documentReferenceBuilder.getCount());

        // get currently existing DocumentReference by subject Id
        Map<UUID, DocumentReference> existingMap = getExistingBySubjectId(fhirLink);

        for (DocumentReference newDocumentReference : documentReferences) {

            // delete any existing DocumentReference for this Subject that have same date
            List<UUID> existingUuids = getExistingByDate(newDocumentReference, existingMap);
            if (!existingUuids.isEmpty()) {
                for (UUID existingUuid : existingUuids)
                fhirResource.delete(existingUuid, ResourceType.DocumentReference);
            }

            // create new DocumentReference
            try {
                fhirResource.create(newDocumentReference);
                success++;
            } catch (FhirResourceException e) {
                LOG.error("Unable to create DocumentReference");
            }
        }

        LOG.info("Processed {} of {} letters", success, documentReferenceBuilder.getCount());
    }

    public void deleteBySubjectId(UUID subjectId) throws FhirResourceException, SQLException {
        for (UUID uuid : fhirResource.getLogicalIdsBySubjectId("documentreference", subjectId)) {
            fhirResource.delete(uuid, ResourceType.DocumentReference);
        }
    }

    private Map<UUID, DocumentReference> getExistingBySubjectId(FhirLink fhirLink)
            throws FhirResourceException, SQLException {
        Map<UUID, DocumentReference> existingMap = new HashMap<>();

        for (UUID uuid : fhirResource.getLogicalIdsBySubjectId("documentreference", fhirLink.getResourceId())) {
            DocumentReference existing = (DocumentReference) fhirResource.get(uuid, ResourceType.DocumentReference);
            existingMap.put(uuid, existing);
        }

        return existingMap;
    }

    private List<UUID> getExistingByDate(DocumentReference documentReference,
                                         Map<UUID, DocumentReference> existingMap) {
        List<UUID> existingByDate = new ArrayList<>();

        Iterator iter = existingMap.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry keyValue = (Map.Entry)iter.next();
            DocumentReference existing = (DocumentReference) keyValue.getValue();
            //LOG.debug(documentReference.getCreated().getValue().toString() + " " + existing.getCreated().getValue().toString());
            if (documentReference.getCreated().getValue().toString().equals(existing.getCreated().getValue().toString())) {
                existingByDate.add((UUID) keyValue.getKey());
            }
            //iter.remove(); // avoids a ConcurrentModificationException
        }

        return existingByDate;
    }
}


