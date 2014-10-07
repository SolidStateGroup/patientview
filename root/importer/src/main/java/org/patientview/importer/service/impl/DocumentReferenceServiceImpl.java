package org.patientview.importer.service.impl;

import generated.Patientview;
import org.hl7.fhir.instance.model.DocumentReference;
import org.hl7.fhir.instance.model.ResourceReference;
import org.hl7.fhir.instance.model.ResourceType;
import org.patientview.importer.builder.DocumentReferenceBuilder;
import org.patientview.importer.resource.FhirResource;
import org.patientview.importer.service.DocumentReferenceService;
import org.patientview.persistence.exception.FhirResourceException;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.sql.SQLException;
import java.util.List;
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
     * Creates all of the FHIR documentreference records from the Patientview object.
     * Links them to the PatientReference.
     *
     * @param data patientview data from xml
     * @param patientReference reference to fhir patient (UUID)
     */
    @Override
    public void add(final Patientview data, final ResourceReference patientReference) {

        LOG.info("Starting DocumentReference (letter) Process");
        int success = 0;

        DocumentReferenceBuilder documentReferenceBuilder = new DocumentReferenceBuilder(data, patientReference);
        List<DocumentReference> documentReferences = documentReferenceBuilder.build();
        LOG.info("Built {} of {} DocumentReference", documentReferenceBuilder.getSuccess(),
                documentReferenceBuilder.getCount());

        for (DocumentReference documentReference : documentReferences) {
            try {
                fhirResource.create(documentReference);
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
}


