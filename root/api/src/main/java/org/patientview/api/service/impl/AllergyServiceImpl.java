package org.patientview.api.service.impl;

import org.hl7.fhir.instance.model.AdverseReaction;
import org.hl7.fhir.instance.model.AllergyIntolerance;
import org.hl7.fhir.instance.model.ResourceType;
import org.hl7.fhir.instance.model.Substance;
import org.patientview.api.service.AllergyService;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.persistence.model.FhirAllergy;
import org.patientview.persistence.resource.FhirResource;
import org.patientview.persistence.util.DataUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 03/06/2015
 */
@Service
public class AllergyServiceImpl extends AbstractServiceImpl<AllergyServiceImpl> implements AllergyService {

    @Inject
    private FhirResource fhirResource;

    @Override
    public List<FhirAllergy> getBySubject(UUID subjectUuid) throws FhirResourceException {
        List<FhirAllergy> fhirAllergies = new ArrayList<>();

        StringBuilder query = new StringBuilder();
        query.append("SELECT  content::varchar ");
        query.append("FROM    allergyintolerance ");
        query.append("WHERE   content -> 'subject' ->> 'display' = '");
        query.append(subjectUuid.toString());
        query.append("' ");

        // get list of AllergyIntolerance
        List<AllergyIntolerance> allergyIntolerances
                = fhirResource.findResourceByQuery(query.toString(), AllergyIntolerance.class);

        if (!CollectionUtils.isEmpty(allergyIntolerances)) {
            for (AllergyIntolerance allergyIntolerance : allergyIntolerances) {
                Substance substance = null;
                AdverseReaction adverseReaction = null;

                // substance
                if (allergyIntolerance.getSubstance() != null) {
                    try {
                        substance = (Substance) DataUtils.getResource(fhirResource.getResource(
                            UUID.fromString(allergyIntolerance.getSubstance().getDisplaySimple()),
                                ResourceType.Substance));
                    } catch (Exception e) {
                        LOG.error("Could not get substance for AllergyIntolerance, continuing: " + e.getMessage());
                    }
                }

                // first adverse reaction
                if (!CollectionUtils.isEmpty(allergyIntolerance.getReaction())) {
                    try {
                        adverseReaction = (AdverseReaction) DataUtils.getResource(fhirResource.getResource(
                            UUID.fromString(allergyIntolerance.getReaction().get(0).getDisplaySimple()),
                                ResourceType.AdverseReaction));
                    } catch (Exception e) {
                        LOG.error("Could not get adverse reaction for AllergyIntolerance, continuing: "
                            + e.getMessage());
                    }
                }

                fhirAllergies.add(new FhirAllergy(allergyIntolerance, substance, adverseReaction));
            }
        }

        return fhirAllergies;
    }
}
