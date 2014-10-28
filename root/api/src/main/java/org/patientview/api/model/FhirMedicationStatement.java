package org.patientview.api.model;

import java.util.Date;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 28/10/2014
 *
 * Reduced Group information FhirMedicationStatement, for transport use
 */
public class FhirMedicationStatement {

    // set from FHIR
    private Date startDate;
    private String name;
    private String dose;

    // set from FhirLink
    private BaseGroup group;

    public FhirMedicationStatement() {
    }

    public FhirMedicationStatement(org.patientview.persistence.model.FhirMedicationStatement fhirMedicationStatement) {
        this.startDate = fhirMedicationStatement.getStartDate();
        this.name = fhirMedicationStatement.getName();
        this.dose = fhirMedicationStatement.getDose();
        if (fhirMedicationStatement.getGroup() != null) {
            this.group = new BaseGroup(fhirMedicationStatement.getGroup());
        }
    }

    public Date getStartDate() {
        return startDate;
    }

    public String getName() {
        return name;
    }

    public String getDose() {
        return dose;
    }

    public BaseGroup getGroup() {
        return group;
    }
}
