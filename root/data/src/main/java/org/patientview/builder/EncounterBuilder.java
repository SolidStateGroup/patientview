package org.patientview.builder;

import org.apache.commons.lang.StringUtils;
import org.hl7.fhir.instance.model.CodeableConcept;
import org.hl7.fhir.instance.model.DateAndTime;
import org.hl7.fhir.instance.model.Encounter;
import org.hl7.fhir.instance.model.Identifier;
import org.hl7.fhir.instance.model.Period;
import org.hl7.fhir.instance.model.ResourceReference;
import org.patientview.config.utils.CommonUtils;
import org.patientview.persistence.model.FhirEncounter;

/**
 * Build Encounter object, suitable for insertion/update into FHIR. Handles update and create, with assumption that
 * empty strings means clear existing data, null strings means leave alone and do not update. For Date, clear if null.
 *
 * Created by jamesr@solidstategroup.com
 * Created on 18/03/2016
 */
public class EncounterBuilder {

    private Encounter encounter;
    private FhirEncounter fhirEncounter;
    private ResourceReference organizationReference;
    private ResourceReference patientReference;

    public EncounterBuilder(Encounter encounter, FhirEncounter fhirEncounter, ResourceReference patientReference) {
        this.encounter = encounter;
        this.fhirEncounter = fhirEncounter;
        this.patientReference = patientReference;
    }

    public EncounterBuilder(Encounter encounter, FhirEncounter fhirEncounter, ResourceReference patientReference,
                            ResourceReference organizationReference) {
        this.encounter = encounter;
        this.fhirEncounter = fhirEncounter;
        this.organizationReference = organizationReference;
        this.patientReference = patientReference;
    }

    public Encounter build() {
        if (encounter == null) {
            encounter = new Encounter();
        }

        encounter.setStatusSimple(Encounter.EncounterState.finished);
        encounter.setSubject(patientReference);

        if (organizationReference != null) {
            encounter.setServiceProvider(organizationReference);
        }

        // set identifier -> value (e.g. TREATMENT, SURGERY, TRANSPLANT_STATUS_KIDNEY)
        if (fhirEncounter.getEncounterType() != null) {
            encounter.getIdentifier().clear();
            if (StringUtils.isNotEmpty(fhirEncounter.getEncounterType())) {
                Identifier identifier = encounter.addIdentifier();
                identifier.setValueSimple(CommonUtils.cleanSql(fhirEncounter.getEncounterType()));
            }
        }

        // set type -> text (e.g. GEN, T)
        if (fhirEncounter.getStatus() != null) {
            encounter.getType().clear();
            if (StringUtils.isNotEmpty(fhirEncounter.getStatus())) {
                CodeableConcept type = encounter.addType();
                type.setTextSimple(CommonUtils.cleanSql(fhirEncounter.getStatus()));
            }
        }

        // date, set period -> start and period -> end
        if (fhirEncounter.getDate() != null) {
            DateAndTime dateAndTime = new DateAndTime(fhirEncounter.getDate());
            Period period = new Period();
            period.setStartSimple(dateAndTime);
            period.setEndSimple(dateAndTime);
            encounter.setPeriod(period);
        }

        return encounter;
    }
}
