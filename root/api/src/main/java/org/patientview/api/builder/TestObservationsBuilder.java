package org.patientview.api.builder;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.hl7.fhir.instance.model.CodeableConcept;
import org.hl7.fhir.instance.model.DateAndTime;
import org.hl7.fhir.instance.model.DateTime;
import org.hl7.fhir.instance.model.Decimal;
import org.hl7.fhir.instance.model.Enumeration;
import org.hl7.fhir.instance.model.Identifier;
import org.hl7.fhir.instance.model.Observation;
import org.hl7.fhir.instance.model.Quantity;
import org.hl7.fhir.instance.model.ResourceReference;
import org.patientview.api.model.FhirObservation;
import org.patientview.api.model.FhirObservationRange;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.config.utils.CommonUtils;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Build Test Observation objects, suitable for insertion into FHIR.
 *
 * Created by jamesr@solidstategroup.com
 * Created on 02/03/2015
 */
public class TestObservationsBuilder {
    private ResourceReference resourceReference;
    private FhirObservationRange fhirObservationRange;
    private List<Observation> observations;

    public TestObservationsBuilder(FhirObservationRange fhirObservationRange, ResourceReference resourceReference) {
        this.fhirObservationRange = fhirObservationRange;
        this.resourceReference = resourceReference;
        observations = new ArrayList<>();
    }

    // Normally any invalid data would fail the whole XML
    public void build() throws ResourceNotFoundException, ResourceForbiddenException, FhirResourceException {
        List<FhirObservation> fhirObservations = fhirObservationRange.getObservations();
        if (!CollectionUtils.isEmpty(fhirObservations)) {
            for (FhirObservation fhirObservation : fhirObservations) {
                observations.add(createObservation(fhirObservation));
            }
        }
    }

    private Observation createObservation(FhirObservation fhirObservation)
        throws FhirResourceException {
        Observation observation = new Observation();
        observation.setReliability(new Enumeration<>(Observation.ObservationReliability.ok));
        observation.setStatusSimple(Observation.ObservationStatus.registered);

        try {
            observation.setValue(createQuantity(fhirObservation));
        } catch (FhirResourceException e) {
            // text based value
            CodeableConcept valueConcept = new CodeableConcept();
            valueConcept.setTextSimple(CommonUtils.cleanSql(fhirObservation.getValue()));
            valueConcept.addCoding().setDisplaySimple(CommonUtils.cleanSql(fhirObservation.getValue()));
            observation.setValue(valueConcept);
        }

        observation.setSubject(resourceReference);
        observation.setName(createConcept(fhirObservationRange.getCode()));
        observation.setApplies(createDateTime(fhirObservation));
        observation.setIdentifier(createIdentifier(fhirObservationRange.getCode()));
        if (StringUtils.isNotEmpty(fhirObservation.getComments())) {
            observation.setCommentsSimple(fhirObservation.getComments());
        }

        return observation;
    }

    // note: not adding units
    private Quantity createQuantity(FhirObservation fhirObservation) throws FhirResourceException {
        Quantity quantity = new Quantity();
        quantity.setValue(createDecimal(fhirObservation.getValue()));

        Quantity.QuantityComparator comparator = getComparator(fhirObservation);
        if (comparator != null) {
            quantity.setComparatorSimple(comparator);
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

    private Decimal createDecimal(String text) throws FhirResourceException {
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

    private Identifier createIdentifier(String text) {
        Identifier identifier = new Identifier();
        identifier.setLabelSimple("resultcode");
        // note: name is generated from xsd so hco3 becomes HCO_3 therefore use value
        identifier.setValueSimple(text);
        return identifier;
    }

    private CodeableConcept createConcept(String text) {
        CodeableConcept codeableConcept = new CodeableConcept();
        codeableConcept.setTextSimple(text);
        codeableConcept.addCoding().setDisplaySimple(StringEscapeUtils.escapeSql(text));
        return codeableConcept;
    }

    private DateTime createDateTime(FhirObservation fhirObservation) throws FhirResourceException {
        try {
            DateTime dateTime = new DateTime();
            DateAndTime dateAndTime = new DateAndTime(fhirObservation.getApplies());
            dateTime.setValue(dateAndTime);
            return dateTime;
        } catch (NullPointerException npe) {
            throw new FhirResourceException("Result timestamp is incorrectly formatted");
        }
    }

    public List<Observation> getObservations() {
        return observations;
    }
}
