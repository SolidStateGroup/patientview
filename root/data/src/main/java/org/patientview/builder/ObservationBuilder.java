package org.patientview.builder;

import org.apache.commons.lang.StringUtils;
import org.hl7.fhir.instance.model.CodeableConcept;
import org.hl7.fhir.instance.model.DateAndTime;
import org.hl7.fhir.instance.model.DateTime;
import org.hl7.fhir.instance.model.Decimal;
import org.hl7.fhir.instance.model.Enumeration;
import org.hl7.fhir.instance.model.Observation;
import org.hl7.fhir.instance.model.Quantity;
import org.hl7.fhir.instance.model.ResourceReference;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.utils.CommonUtils;
import org.patientview.persistence.model.FhirObservation;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;

/**
 * Build Observation object, suitable for insertion/update into FHIR. Handles update and create, with assumption that
 * empty strings means clear existing data, null strings means leave alone and do not update. For Date, clear if null.
 *
 * Created by jamesr@solidstategroup.com
 * Created on 21/03/2016
 */
public class ObservationBuilder {

    private Observation observation;
    private FhirObservation fhirObservation;
    private ResourceReference patientReference;
    private ResourceReference encounterReference;

    public ObservationBuilder(Observation observation, FhirObservation fhirObservation,
                              ResourceReference patientReference) {
        this.observation = observation;
        this.fhirObservation = fhirObservation;
        this.patientReference = patientReference;
    }

    public ObservationBuilder(Observation observation, FhirObservation fhirObservation,
                              ResourceReference patientReference, ResourceReference encounterReference) {
        this.observation = observation;
        this.fhirObservation = fhirObservation;
        this.patientReference = patientReference;
        this.encounterReference = encounterReference;
    }

    public Observation build() {
        if (observation == null) {
            observation = new Observation();
        }

        observation.setReliability(new Enumeration<>(Observation.ObservationReliability.ok));
        observation.setStatusSimple(Observation.ObservationStatus.registered);
        observation.setSubject(patientReference);

        // if encounter, store in Performer (latest FHIR has direct reference but we do not)
        if (encounterReference != null) {
            observation.getPerformer().clear();
            observation.getPerformer().add(encounterReference);
        }

        // value (string or numeric)
        if (fhirObservation.getValue() != null) {
            if (StringUtils.isNotEmpty(fhirObservation.getValue())) {
                try {
                    observation.setValue(createQuantity(fhirObservation));
                } catch (FhirResourceException e) {
                    // text based value, note only quantity has units
                    CodeableConcept valueConcept = new CodeableConcept();
                    valueConcept.setTextSimple(CommonUtils.cleanSql(fhirObservation.getValue()));
                    observation.setValue(valueConcept);
                }
            } else {
                observation.setValue(null);
            }
        }

        // name (result code or NonTestObservationTypes)
        if (fhirObservation.getName() != null) {
            if (StringUtils.isNotEmpty(fhirObservation.getName())) {
                CodeableConcept nameConcept = new CodeableConcept();
                nameConcept.setTextSimple(CommonUtils.cleanSql(fhirObservation.getName()));
                observation.setName(nameConcept);
            } else {
                observation.setName(null);
            }
        }

        // date
        if (fhirObservation.getApplies() != null) {
            DateTime dateTime = new DateTime();
            DateAndTime dateAndTime = new DateAndTime(fhirObservation.getApplies());
            dateTime.setValue(dateAndTime);
            observation.setApplies(dateTime);
        }

        // comments
        if (fhirObservation.getComments() != null) {
            if (StringUtils.isNotEmpty(fhirObservation.getComments())) {
                observation.setCommentsSimple(CommonUtils.cleanSql(fhirObservation.getComments()));
            } else {
                observation.setComments(null);
            }
        }

        // note: no body data
        return observation;
    }

    private Decimal createDecimal(String text) throws FhirResourceException {
        if (StringUtils.isEmpty(text)) {
            throw new FhirResourceException("Empty value for observation");
        }
        Decimal decimal = new Decimal();

        String resultString = text.replaceAll(">", "").replaceAll(">=", "")
                .replaceAll("<", "").replaceAll("<=", "");

        // attempt to parse remaining
        NumberFormat decimalFormat = DecimalFormat.getInstance();

        if (StringUtils.isNotEmpty(resultString)) {
            try {
                decimal.setValue(BigDecimal.valueOf((decimalFormat.parse(resultString)).doubleValue()));
            } catch (ParseException nfe) {
                throw new FhirResourceException("Invalid value for observation (will try text based): " + text);
            }
        } else {
            throw new FhirResourceException("Empty value for observation");
        }
        return decimal;
    }

    private Quantity createQuantity(FhirObservation fhirObservation) throws FhirResourceException {
        Quantity quantity = new Quantity();
        quantity.setValue(createDecimal(fhirObservation.getValue()));

        Quantity.QuantityComparator comparator = getComparator(fhirObservation);
        if (comparator != null) {
            quantity.setComparatorSimple(comparator);
        }

        if (StringUtils.isNotEmpty(fhirObservation.getUnits())) {
            quantity.setUnitsSimple(fhirObservation.getUnits());
        }
        return quantity;
    }

    private Quantity.QuantityComparator getComparator(FhirObservation fhirObservation) {
        if (StringUtils.isNotEmpty(fhirObservation.getComparator())) {
            if (fhirObservation.getComparator().contains(">=")) {
                return Quantity.QuantityComparator.greaterOrEqual;
            }

            if (fhirObservation.getComparator().contains("<=")) {
                return Quantity.QuantityComparator.lessOrEqual;
            }

            if (fhirObservation.getComparator().contains(">")) {
                return Quantity.QuantityComparator.greaterThan;
            }

            if (fhirObservation.getComparator().contains("<")) {
                return Quantity.QuantityComparator.lessThan;
            }
        }

        return null;
    }
}
