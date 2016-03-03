package org.patientview.api.controller;

import org.patientview.api.config.ExcludeFromApiDoc;
import org.patientview.api.service.ApiObservationService;
import org.patientview.api.service.ApiPatientService;
import org.patientview.persistence.model.FhirObservationRange;
import org.patientview.persistence.model.FhirPatient;
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
    ApiObservationService apiObservationService;

    @Inject
    ApiPatientService apiPatientService;

    @RequestMapping(value = "/import/observations", method = RequestMethod.POST)
    public ResponseEntity<ServerResponse> importObservations(@RequestBody FhirObservationRange fhirObservationRange) {
        return new ResponseEntity<>(apiObservationService.importObservations(fhirObservationRange), HttpStatus.OK);
    }

    @RequestMapping(value = "/import/patient", method = RequestMethod.POST)
    public ResponseEntity<ServerResponse> importPatient(@RequestBody FhirPatient fhirPatient) {
        return new ResponseEntity<>(apiPatientService.importPatient(fhirPatient), HttpStatus.OK);
    }
}
