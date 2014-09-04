package org.patientview.importer.builder;

import generated.Patientview;
import org.apache.commons.lang.StringUtils;
import org.hl7.fhir.instance.model.CodeableConcept;
import org.hl7.fhir.instance.model.DateAndTime;
import org.hl7.fhir.instance.model.DateTime;
import org.hl7.fhir.instance.model.Decimal;
import org.hl7.fhir.instance.model.Enumeration;
import org.hl7.fhir.instance.model.Observation;
import org.hl7.fhir.instance.model.Quantity;
import org.hl7.fhir.instance.model.ResourceReference;
import org.patientview.persistence.exception.FhirResourceException;
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
    private int success = 0;
    private int count = 0;

    public ObservationsBuilder(Patientview results, ResourceReference resourceReference) {
        this.results = results;
        this.resourceReference = resourceReference;
        observations = new ArrayList<>();
    }

    // Normally and invalid data would fail the whole XML
    public List<Observation> build() {

        for (Patientview.Patient.Testdetails.Test test : results.getPatient().getTestdetails().getTest()) {
            for (Patientview.Patient.Testdetails.Test.Result result : test.getResult()) {
                try {
                    observations.add(createObservation(test, result));
                    success++;
                } catch (FhirResourceException e) {
                    LOG.error("Invalid data in XML: ", e.getMessage());
                }
                count++;
            }
        }
        return observations;
    }

    private Observation createObservation(Patientview.Patient.Testdetails.Test test, Patientview.Patient.Testdetails.Test.Result result)
        throws FhirResourceException{
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
                                    Patientview.Patient.Testdetails.Test test) throws FhirResourceException {
        Quantity quantity = new Quantity();
        quantity.setValue(createDecimal(result));
        quantity.setUnitsSimple(test.getUnits());

        return quantity;
    }

    private Decimal createDecimal(Patientview.Patient.Testdetails.Test.Result result) throws FhirResourceException {
        Decimal decimal = new Decimal();
        String resultString = result.getValue().replaceAll("[^.\\d]", "");
        NumberFormat decimalFormat = DecimalFormat.getInstance();
        if (StringUtils.isNotEmpty(resultString)) {
            try {
                decimal.setValue(BigDecimal.valueOf((decimalFormat.parse(resultString)).longValue()));
            } catch (ParseException nfe) {
                LOG.info("Check down for parsing extra characters needs adding");
            }
        } else {
            throw new FhirResourceException("Invalid value for observation");
        }
        return decimal;

    }


    private org.hl7.fhir.instance.model.Identifier createIdentifier(Patientview.Patient.Testdetails.Test test) {
        org.hl7.fhir.instance.model.Identifier identifier = new org.hl7.fhir.instance.model.Identifier();
        identifier.setLabelSimple("resultcode");
        identifier.setValueSimple(test.getTestcode().name());
        return identifier;
    }

    private CodeableConcept createConcept(Patientview.Patient.Testdetails.Test test) {
        CodeableConcept codeableConcept = new CodeableConcept();
        codeableConcept.setTextSimple(test.getTestcode().name());
        codeableConcept.addCoding().setDisplaySimple(test.getTestname());
        return codeableConcept;
    }

    private DateTime createDateTime(Patientview.Patient.Testdetails.Test.Result result) throws FhirResourceException {
        DateTime dateTime = new DateTime();
        try {
            DateAndTime dateAndTime = DateAndTime.now();
            dateAndTime.setYear(result.getDatestamp().getYear());
            dateAndTime.setMonth(result.getDatestamp().getMonth());
            dateAndTime.setDay(result.getDatestamp().getDay());
            dateAndTime.setHour(result.getDatestamp().getHour());
            dateAndTime.setMinute(result.getDatestamp().getMinute());
            dateAndTime.setSecond(result.getDatestamp().getSecond());
            dateTime.setValue(dateAndTime);
        } catch (NullPointerException npe) {
            throw new FhirResourceException("Result timestamp has not been supplied");
        }

        return dateTime;
    }

    public int getSuccess() {
        return success;
    }

    public int getCount() {
        return count;
    }

}
