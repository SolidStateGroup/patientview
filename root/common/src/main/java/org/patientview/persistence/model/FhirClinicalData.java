package org.patientview.persistence.model;

import java.util.ArrayList;
import java.util.List;

/**
 * FhirClinicalData, representing a collection of objects for treatment (Encounter)
 * and diagnosis/diagnosisedta (Condition). Used for API import.
 * Created by jamesr@solidstategroup.com
 * Created on 09/03/2016
 */
public class FhirClinicalData extends BaseImport {
    private FhirCondition diagnosis;
    private List<FhirCondition> otherDiagnoses = new ArrayList<>();
    private FhirEncounter treatment;

    public FhirClinicalData() { }

    public FhirCondition getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(FhirCondition diagnosis) {
        this.diagnosis = diagnosis;
    }

    public List<FhirCondition> getOtherDiagnoses() {
        return otherDiagnoses;
    }

    public void setOtherDiagnoses(List<FhirCondition> otherDiagnoses) {
        this.otherDiagnoses = otherDiagnoses;
    }

    public FhirEncounter getTreatment() {
        return treatment;
    }

    public void setTreatment(FhirEncounter treatment) {
        this.treatment = treatment;
    }
}
