package org.patientview.importer.builder;

import generated.Patientview;
import org.hl7.fhir.instance.model.CodeableConcept;
import org.hl7.fhir.instance.model.Encounter;
import org.hl7.fhir.instance.model.Identifier;
import org.hl7.fhir.instance.model.ResourceReference;
import org.patientview.persistence.exception.FhirResourceException;
import org.patientview.persistence.model.enums.EncounterTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 08/09/2014
 */
public class EncountersBuilder {

    private final Logger LOG = LoggerFactory.getLogger(EncountersBuilder.class);
    private ResourceReference patientReference;
    private ResourceReference groupReference;
    private Patientview data;
    private List<Encounter> encounters;
    private int success = 0;
    private int count = 0;

    public EncountersBuilder(Patientview data, ResourceReference patientReference, ResourceReference groupReference) {
        this.data = data;
        this.patientReference = patientReference;
        this.groupReference = groupReference;
        encounters = new ArrayList<>();
    }

    // Normally any invalid data would fail the whole XML
    public List<Encounter> build() {

        // Treatment
        try {
            encounters.add(createEncounter(data.getPatient().getClinicaldetails().getRrtstatus().value(),
                    EncounterTypes.TREATMENT.toString()));
            success++;
        } catch (FhirResourceException e) {
            LOG.error("Invalid data in XML: ", e.getMessage());
        }

        count++;

        // Transplant Status
        try {
            encounters.add(createEncounter(data.getPatient().getClinicaldetails().getTpstatus(),
                    EncounterTypes.TRANSPLANT_STATUS.toString()));
            success++;
        } catch (FhirResourceException e) {
            LOG.error("Invalid data in XML: ", e.getMessage());
        }

        count++;

        return encounters;
    }

    private Encounter createEncounter(String status, String encounterType) throws FhirResourceException {
        Encounter encounter = new Encounter();
        encounter.setStatusSimple(Encounter.EncounterState.finished);
        Identifier identifier = encounter.addIdentifier();
        identifier.setValueSimple(encounterType);

        CodeableConcept code = encounter.addType();
        code.setTextSimple(status);

        encounter.setSubject(this.patientReference);
        encounter.setServiceProvider(this.groupReference);

        return encounter;
    }

    public int getSuccess() {
        return success;
    }

    public int getCount() {
        return count;
    }
}
