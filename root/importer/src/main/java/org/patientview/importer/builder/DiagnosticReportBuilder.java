package org.patientview.importer.builder;

import generated.Patientview.Patient.Diagnostics.Diagnostic;
import org.hl7.fhir.instance.model.CodeableConcept;
import org.hl7.fhir.instance.model.DateAndTime;
import org.hl7.fhir.instance.model.DateTime;
import org.hl7.fhir.instance.model.DiagnosticReport;

/**
 * This maps between parameters from old PatientView and the new PatientView fhir record
 *
 * Created by jamesr@solidstategroup.com
 * Created on 06/10/2014
 */
public class DiagnosticReportBuilder {

    private Diagnostic data;

    public DiagnosticReportBuilder(Diagnostic data) {
        this.data = data;
    }

    public DiagnosticReport build() {
        DiagnosticReport diagnosticReport = new DiagnosticReport();

        DateAndTime dateAndTime = new DateAndTime(data.getDiagnosticdate().toGregorianCalendar().getTime());
        DateTime diagnosticDate = new DateTime();
        diagnosticDate.setValue(dateAndTime);
        diagnosticReport.setDiagnostic(diagnosticDate);

        CodeableConcept name = new CodeableConcept();
        name.setTextSimple(data.getDiagnosticname());
        diagnosticReport.setName(name);

        CodeableConcept type = new CodeableConcept();
        name.setTextSimple(data.getDiagnostictype());
        diagnosticReport.setServiceCategory(type);

        return diagnosticReport;
    }
}
