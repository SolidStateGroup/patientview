package org.patientview.persistence.model;

import java.util.List;

/**
 * FhirClinicalData, representing a collection of objects for treatment (Encounter)
 * and diagnosis (Condition). Used for API import.
 * Created by jamesr@solidstategroup.com
 * Created on 09/03/2016
 */
public class FhirClinicalData extends BaseImport {
    private List<FhirCondition> diagnoses;
    private List<FhirEncounter> treatments;

    public FhirClinicalData() { }

    public List<FhirCondition> getDiagnoses() {
        return diagnoses;
    }

    public void setDiagnoses(List<FhirCondition> diagnoses) {
        this.diagnoses = diagnoses;
    }

    public List<FhirEncounter> getTreatments() {
        return treatments;
    }

    public void setTreatments(List<FhirEncounter> treatments) {
        this.treatments = treatments;
    }
}
