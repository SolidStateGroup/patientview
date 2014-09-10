package org.patientview.importer.service.impl;

import generated.Patientview;
import org.hl7.fhir.instance.model.Practitioner;
import org.json.JSONObject;
import org.patientview.importer.builder.PractitionerBuilder;
import org.patientview.importer.resource.FhirResource;
import org.patientview.importer.service.PractitionerService;
import org.patientview.importer.util.Util;
import org.patientview.persistence.exception.FhirResourceException;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.UUID;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 05/09/2014
 */
@Service
public class PractitionerServiceImpl extends AbstractServiceImpl<PractitionerService> implements PractitionerService{

    @Inject
    private FhirResource fhirResource;

    /**
     * Creates FHIR practitioner record from the Patientview object.
     *
     * @param data
     */
    @Override
    public UUID add(final Patientview data) {
        PractitionerBuilder practitionerBuilder = new PractitionerBuilder(data);

        try {
            JSONObject jsonObject = create(practitionerBuilder.build());
            LOG.info("Processed Practitioner");
            return Util.getResourceId(jsonObject);
        } catch (FhirResourceException e) {
            LOG.error("Unable to build practitioner");
            return null;
        }
    }

    private JSONObject create(Practitioner practitioner) throws FhirResourceException {
        try {
            return fhirResource.create(practitioner);
        } catch (Exception e) {
            LOG.error("Could not build practitioner resource", e);
            throw new FhirResourceException(e.getMessage());
        }
    }
}


