package org.patientview.api.controller;

import org.patientview.persistence.model.FhirMedicationStatement;
import org.patientview.api.service.MedicationService;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.config.exception.FhirResourceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import java.util.List;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 29/09/2014
 */
@RestController
public class MedicationController extends BaseController<MedicationController> {

    @Inject
    private MedicationService medicationService;

    @RequestMapping(value = "/user/{userId}/medication", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<List<FhirMedicationStatement>> getAllMedication(@PathVariable("userId") Long userId)
            throws FhirResourceException, ResourceNotFoundException {
        return new ResponseEntity<>(medicationService.getByUserId(userId), HttpStatus.OK);
    }
}
