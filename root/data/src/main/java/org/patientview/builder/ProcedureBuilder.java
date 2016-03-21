package org.patientview.builder;

import org.apache.commons.lang.StringUtils;
import org.hl7.fhir.instance.model.CodeableConcept;
import org.hl7.fhir.instance.model.Procedure;
import org.hl7.fhir.instance.model.ResourceReference;
import org.patientview.config.utils.CommonUtils;
import org.patientview.persistence.model.FhirProcedure;

/**
 * Build Procedure object, suitable for insertion/update into FHIR. Handles update and create, with assumption that
 * empty strings means clear existing data, null strings means leave alone and do not update. For Date, clear if null.
 *
 * Created by jamesr@solidstategroup.com
 * Created on 21/03/2016
 */
public class ProcedureBuilder {

    private Procedure procedure;
    private FhirProcedure fhirProcedure;
    private ResourceReference patientReference;
    private ResourceReference encounterReference;

    public ProcedureBuilder(Procedure procedure, FhirProcedure fhirProcedure,
                            ResourceReference patientReference, ResourceReference encounterReference) {
        this.procedure = procedure;
        this.fhirProcedure = fhirProcedure;
        this.patientReference = patientReference;
        this.encounterReference = encounterReference;
    }

    public Procedure build() {
        if (procedure == null) {
            procedure = new Procedure();
        }

        procedure.setSubject(patientReference);
        procedure.setEncounter(encounterReference);

        // body site
        if (fhirProcedure.getBodySite() != null) {
            procedure.getBodySite().clear();

            if (StringUtils.isNotEmpty(fhirProcedure.getBodySite())) {
                CodeableConcept bodySite = new CodeableConcept();
                bodySite.setTextSimple(CommonUtils.cleanSql(fhirProcedure.getBodySite()));
                procedure.getBodySite().add(bodySite);
            }
        }

        // type
        if (fhirProcedure.getType() != null) {
            if (StringUtils.isNotEmpty(fhirProcedure.getType())) {
                CodeableConcept type = new CodeableConcept();
                type.setTextSimple(CommonUtils.cleanSql(fhirProcedure.getType()));
                procedure.setType(type);
            } else {
                procedure.setType(null);
            }
        }

        return procedure;
    }
}
