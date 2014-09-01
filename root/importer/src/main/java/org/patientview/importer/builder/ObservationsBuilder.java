package org.patientview.importer.builder;

import generated.Patientview;
import org.hl7.fhir.instance.model.Enumeration;
import org.hl7.fhir.instance.model.Observation;
import org.hl7.fhir.instance.model.ResourceReference;
import org.hl7.fhir.instance.model.String_;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by james@solidstategroup.com
 * Created on 01/09/2014
 */
public class ObservationsBuilder {

    ResourceReference resourceReference;
    Patientview results;
    List<Observation> observations;

    public ObservationsBuilder(Patientview results, ResourceReference resourceReference) {
        this.results = results;
        this.resourceReference = resourceReference;
        observations = new ArrayList<>();
    }

    public List<Observation> build() {


        for (Patientview.Patient.Testdetails.Test test : results.getPatient().getTestdetails().getTest()) {
            for (Patientview.Patient.Testdetails.Test.Result result : test.getResult()) {
                observations.add(createObservation(test, result));
            }
        }

        return observations;

    }


    private Observation createObservation(Patientview.Patient.Testdetails.Test test, Patientview.Patient.Testdetails.Test.Result result) {


        Observation observation = new Observation();
        observation.setReliability(new Enumeration<>(Observation.ObservationReliability.ok));
        observation.setStatusSimple(Observation.ObservationStatus.registered);
        observation.setValue(createValue(result));
        observation.setSubject(resourceReference);
        return observation;
    }

    private String_ createValue(Patientview.Patient.Testdetails.Test.Result result) {
        String_ value = new String_();
        value.setValue(result.getValue());
        return value;
    }





}
