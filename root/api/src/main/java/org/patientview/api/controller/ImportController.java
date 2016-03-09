package org.patientview.api.controller;

import org.patientview.api.config.ExcludeFromApiDoc;
import org.patientview.api.service.ApiDiagnosticService;
import org.patientview.api.service.ApiMedicationService;
import org.patientview.api.service.ApiObservationService;
import org.patientview.api.service.ApiPatientService;
import org.patientview.api.service.ApiPractitionerService;
import org.patientview.api.service.LetterService;
import org.patientview.persistence.model.FhirDiagnosticReportRange;
import org.patientview.persistence.model.FhirDocumentReference;
import org.patientview.persistence.model.FhirMedicationStatementRange;
import org.patientview.persistence.model.FhirObservationRange;
import org.patientview.persistence.model.FhirPatient;
import org.patientview.persistence.model.FhirPractitioner;
import org.patientview.persistence.model.ServerResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;

@RestController
@ExcludeFromApiDoc
public class ImportController extends BaseController<ImportController> {

    @Inject
    private ApiDiagnosticService apiDiagnosticService;

    @Inject
    private ApiMedicationService apiMedicationService;

    @Inject
    private ApiObservationService apiObservationService;

    @Inject
    private ApiPatientService apiPatientService;

    @Inject
    private ApiPractitionerService apiPractitionerService;

    @Inject
    private LetterService letterService;

    @RequestMapping(value = "/import/diagnostics", method = RequestMethod.POST)
    public ResponseEntity<ServerResponse> importDiagnostics(
            @RequestBody FhirDiagnosticReportRange fhirDiagnosticReportRange) {
        return new ResponseEntity<>(apiDiagnosticService.importDiagnostics(fhirDiagnosticReportRange), HttpStatus.OK);
    }

    @RequestMapping(value = "/import/letter", method = RequestMethod.POST)
    public ResponseEntity<ServerResponse> importLetter(@RequestBody FhirDocumentReference fhirDocumentReference) {
        return new ResponseEntity<>(letterService.importLetter(fhirDocumentReference), HttpStatus.OK);
    }

    @RequestMapping(value = "/import/medication", method = RequestMethod.POST)
    public ResponseEntity<ServerResponse> importMedication(
            @RequestBody FhirMedicationStatementRange fhirMedicationStatementRange) {
        return new ResponseEntity<>(apiMedicationService.importMedication(fhirMedicationStatementRange), HttpStatus.OK);
    }

    @RequestMapping(value = "/import/observations", method = RequestMethod.POST)
    public ResponseEntity<ServerResponse> importObservations(@RequestBody FhirObservationRange fhirObservationRange) {
        return new ResponseEntity<>(apiObservationService.importObservations(fhirObservationRange), HttpStatus.OK);
    }

    @RequestMapping(value = "/import/patient", method = RequestMethod.POST)
    public ResponseEntity<ServerResponse> importPatient(@RequestBody FhirPatient fhirPatient) {
        return new ResponseEntity<>(apiPatientService.importPatient(fhirPatient), HttpStatus.OK);
    }

    @RequestMapping(value = "/import/practitioner", method = RequestMethod.POST)
    public ResponseEntity<ServerResponse> importPractitioner(@RequestBody FhirPractitioner fhirPractitioner) {
        return new ResponseEntity<>(apiPractitionerService.importPractitioner(fhirPractitioner), HttpStatus.OK);
    }
}
