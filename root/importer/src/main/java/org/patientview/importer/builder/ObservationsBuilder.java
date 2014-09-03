package org.patientview.importer.builder;

import generated.Patientview;
import org.hl7.fhir.instance.model.CodeableConcept;
import org.hl7.fhir.instance.model.DateAndTime;
import org.hl7.fhir.instance.model.DateTime;
import org.hl7.fhir.instance.model.Decimal;
import org.hl7.fhir.instance.model.Enumeration;
import org.hl7.fhir.instance.model.Observation;
import org.hl7.fhir.instance.model.Quantity;
import org.hl7.fhir.instance.model.ResourceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by james@solidstategroup.com
 * Created on 01/09/2014
 */
public class ObservationsBuilder {

    private final Logger LOG = LoggerFactory.getLogger(ObservationsBuilder.class);

    private ResourceReference resourceReference;
    private Patientview results;
    private List<Observation> observations;

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
        observation.setValue(createQuantity(result, test));
        observation.setSubject(resourceReference);
        observation.setName(createConcept(test));
        observation.setApplies(createDateTime(result));
        observation.setIdentifier(createIdentifier(test));
        return observation;
    }

    private Quantity createQuantity(Patientview.Patient.Testdetails.Test.Result result,
                                    Patientview.Patient.Testdetails.Test test) {
        Quantity quantity = new Quantity();
        quantity.setValue(createDecimal(result));
        quantity.setUnitsSimple(test.getUnits());
        return quantity;
    }

    private Decimal createDecimal(Patientview.Patient.Testdetails.Test.Result result) {
        Decimal decimal = new Decimal();
        String resultString = result.getValue().replaceAll("[^.\\d]", "");
        NumberFormat decimalFormat = DecimalFormat.getInstance();
        try {
            decimal.setValue((BigDecimal) decimalFormat.parse(resultString));
        } catch (ParseException nfe) {
            LOG.info("Check down for parsing extra characters needs adding");
        }
        return decimal;

    }


    private org.hl7.fhir.instance.model.Identifier createIdentifier(Patientview.Patient.Testdetails.Test test) {
        org.hl7.fhir.instance.model.Identifier identifier = new org.hl7.fhir.instance.model.Identifier();
        identifier.setLabelSimple("ResultCode");
        identifier.setValueSimple(test.getTestcode().name());
        return identifier;
    }

    private CodeableConcept createConcept(Patientview.Patient.Testdetails.Test test) {
        CodeableConcept codeableConcept = new CodeableConcept();
        codeableConcept.setTextSimple(test.getTestcode().name());
        codeableConcept.addCoding().setDisplaySimple(test.getTestname());
        return codeableConcept;
    }

    private DateTime createDateTime(Patientview.Patient.Testdetails.Test.Result result) {
        DateTime dateTime = new DateTime();
        try {
            dateTime.setValue(DateAndTime.parseV3(result.getDatestamp().toXMLFormat()));
        } catch (ParseException e) {
            LOG.error("Unable to parse date");
        }

        return dateTime;
    }


}
