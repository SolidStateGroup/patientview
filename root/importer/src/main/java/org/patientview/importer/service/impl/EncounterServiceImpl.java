package org.patientview.importer.service.impl;

import generated.Patientview;
import org.hl7.fhir.instance.model.Encounter;
import org.hl7.fhir.instance.model.ResourceReference;
import org.hl7.fhir.instance.model.ResourceType;
import org.patientview.importer.builder.EncountersBuilder;
import org.patientview.persistence.model.enums.EncounterTypes;
import org.patientview.persistence.resource.FhirResource;
import org.patientview.importer.service.EncounterService;
import org.patientview.importer.Utility.Util;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.persistence.model.FhirLink;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.inject.Inject;
import java.sql.SQLException;
import java.util.UUID;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 08/09/2014
 */
@Service
public class EncounterServiceImpl extends AbstractServiceImpl<EncounterService> implements EncounterService{

    @Inject
    private FhirResource fhirResource;

    private String nhsno;

    /**
     * Creates FHIR encounter (treatment and transplant details) records from the Patientview object.
     *
     * @param data
     */
    @Override
    public void add(final Patientview data, final FhirLink fhirLink, final ResourceReference groupReference)
            throws FhirResourceException, SQLException {

        this.nhsno = data.getPatient().getPersonaldetails().getNhsno();
        ResourceReference patientReference = Util.createResourceReference(fhirLink.getResourceId());
        EncountersBuilder encountersBuilder = new EncountersBuilder(data, patientReference, groupReference);

        LOG.info(nhsno + ": Starting Encounter Process");

        // delete existing
        deleteBySubjectId(fhirLink.getResourceId());

        int count = 0;
        for (Encounter encounter : encountersBuilder.build()) {
            LOG.trace(nhsno + ": Creating... encounter " + count);
            try {
                fhirResource.create(encounter);
            } catch (FhirResourceException e) {
                LOG.error(nhsno + ": Unable to build encounter");
            }
            LOG.trace(nhsno + ": Finished creating encounter " + count++);
        }
        LOG.info(nhsno + ": Processed {} of {} encounters", encountersBuilder.getSuccess(),
                encountersBuilder.getCount());
    }

    public void deleteBySubjectId(UUID subjectId) throws FhirResourceException, SQLException {
        for (UUID uuid : fhirResource.getLogicalIdsBySubjectId("encounter", subjectId)) {

            // do not delete EncounterType TRANSPLANT_STATUS_KIDNEY or TRANSPLANT_STATUS_PANCREAS
            // as these come from uktstatus table during migration
            Encounter encounter = (Encounter) fhirResource.get(uuid, ResourceType.Encounter);

            if (!CollectionUtils.isEmpty(encounter.getIdentifier())) {
                String encounterType = encounter.getIdentifier().get(0).getValueSimple();
                if (!encounterType.equals(EncounterTypes.TRANSPLANT_STATUS_KIDNEY.toString())
                        && !encounterType.equals(EncounterTypes.TRANSPLANT_STATUS_PANCREAS.toString())) {
                    fhirResource.delete(uuid, ResourceType.Encounter);
                }
            }
        }
    }
}


