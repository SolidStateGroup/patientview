package org.patientview.builder;

import generated.Patientview;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.hl7.fhir.instance.model.CodeableConcept;
import org.hl7.fhir.instance.model.DateAndTime;
import org.hl7.fhir.instance.model.DateTime;
import org.hl7.fhir.instance.model.Decimal;
import org.hl7.fhir.instance.model.Identifier;
import org.hl7.fhir.instance.model.Observation;
import org.hl7.fhir.instance.model.Quantity;
import org.hl7.fhir.instance.model.ResourceReference;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.utils.CommonUtils;
import org.patientview.persistence.model.Alert;
import org.patientview.persistence.model.enums.BodySites;
import org.patientview.persistence.model.enums.NonTestObservationTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private Map<String, Patientview.Patient.Testdetails.Test.Daterange> dateRanges;
    private Map<String, Alert> alertMap;

    public ObservationsBuilder(Patientview results, ResourceReference resourceReference) {
        this.results = results;
        this.resourceReference = resourceReference;
        observations = new ArrayList<>();
    }

    // Normally any invalid data would fail the whole XML
    public void build() {
        dateRanges = new HashMap<>();

        // build from tests e.g. ciclosporin, weight etc
        if (results.getPatient().getTestdetails() != null) {
            for (Patientview.Patient.Testdetails.Test test : results.getPatient().getTestdetails().getTest()) {

                dateRanges.put(test.getTestcode().toUpperCase(), test.getDaterange());
                String testCode = test.getTestcode().toUpperCase();

                if (alertMap == null) {
                    alertMap = new HashMap<>();
                }

                Alert alert = alertMap.get(testCode);
                if (alert != null) {
                    alert.setUpdated(false);
                }

                for (Patientview.Patient.Testdetails.Test.Result result : test.getResult()) {
                    try {
                        observations.add(createObservation(test, result));

                        // handle alerts
                        if (alertMap.containsKey(testCode) && alert != null) {
                            if (alert.getLatestDate() == null) {
                                // is the first time a result has come in for this alert
                                alert.setLatestDate(CommonUtils.getDateFromString(result.getDatestamp()));
                                alert.setLatestValue(result.getValue());
                                alert.setEmailAlertSent(false);
                                alert.setMobileAlertSent(false);
                                alert.setWebAlertViewed(false);
                                alert.setUpdated(true);
                            } else {
                                // previous result has been alerted, check if this one is newer
                                if (alert.getLatestDate().getTime()
                                        < CommonUtils.getDateFromString(result.getDatestamp()).getTime()) {
                                    alert.setLatestDate(CommonUtils.getDateFromString(result.getDatestamp()));
                                    alert.setLatestValue(result.getValue());
                                    alert.setEmailAlertSent(false);
                                    alert.setMobileAlertSent(false);
                                    alert.setWebAlertViewed(false);
                                    alert.setUpdated(true);
                                }
                            }
                        }

                        success++;
                    } catch (Exception e) {
                        LOG.error("Invalid data in XML: " + e.getMessage());
                    }
                    count++;
                }
            }
        }

        // build from foot checkup
        if (!CollectionUtils.isEmpty(results.getPatient().getFootcheckup())) {
            for (Patientview.Patient.Footcheckup footcheckup : results.getPatient().getFootcheckup()) {
                List<Observation> footObservations = createFootcheckupObservations(footcheckup);
                if (footObservations != null) {
                    observations.addAll(footObservations);
                }
            }
        }

        // build from eye checkup
        if (!CollectionUtils.isEmpty(results.getPatient().getEyecheckup())) {
            for (Patientview.Patient.Eyecheckup eyecheckup : results.getPatient().getEyecheckup()) {
                List<Observation> eyeObservations = createEyecheckupObservations(eyecheckup);
                if (eyeObservations != null) {
                    observations.addAll(eyeObservations);
                }
            }
        }

        // build from specific non-test fields
        if (results.getPatient().getClinicaldetails() != null) {
            // blood group
            if (results.getPatient().getClinicaldetails().getBloodgroup() != null) {
                try {
                    observations.add(createObservationNonTest(NonTestObservationTypes.BLOOD_GROUP.toString(),
                            results.getPatient().getClinicaldetails().getBloodgroup()));
                } catch (FhirResourceException e) {
                    LOG.error("Invalid data in XML: " + e.getMessage());
                }
            }

            // IBD disease complication
            if (StringUtils.isNotEmpty(results.getPatient().getClinicaldetails().getIbddiseasecomplications())) {
                try {
                    observations.add(
                            createObservationNonTest(NonTestObservationTypes.IBD_DISEASE_COMPLICATIONS.toString(),
                                    results.getPatient().getClinicaldetails().getIbddiseasecomplications()));
                } catch (FhirResourceException e) {
                    LOG.error("Invalid data in XML: " + e.getMessage());
                }
            }

            // IBD disease extent
            if (StringUtils.isNotEmpty(results.getPatient().getClinicaldetails().getIbddiseaseextent())) {
                try {
                    observations.add(createObservationNonTest(NonTestObservationTypes.IBD_DISEASE_EXTENT.toString(),
                            results.getPatient().getClinicaldetails().getIbddiseaseextent()));
                } catch (FhirResourceException e) {
                    LOG.error("Invalid data in XML: " + e.getMessage());
                }
            }

            // IBD EI manifestation
            if (StringUtils.isNotEmpty(results.getPatient().getClinicaldetails().getIbdeimanifestations())) {
                try {
                    observations.add(createObservationNonTest(NonTestObservationTypes.IBD_EI_MANIFESTATIONS.toString(),
                            results.getPatient().getClinicaldetails().getIbdeimanifestations()));
                } catch (FhirResourceException e) {
                    LOG.error("Invalid data in XML: " + e.getMessage());
                }
            }

            // (IBD) body parts affected
            if (StringUtils.isNotEmpty(results.getPatient().getClinicaldetails().getBodypartsaffected())) {
                try {
                    observations.add(createObservationNonTest(NonTestObservationTypes.BODY_PARTS_AFFECTED.toString(),
                            results.getPatient().getClinicaldetails().getBodypartsaffected()));
                } catch (FhirResourceException e) {
                    LOG.error("Invalid data in XML: " + e.getMessage());
                }
            }

            // (IBD) family history
            if (StringUtils.isNotEmpty(results.getPatient().getClinicaldetails().getFamilyhistory())) {
                try {
                    observations.add(createObservationNonTest(NonTestObservationTypes.FAMILY_HISTORY.toString(),
                            results.getPatient().getClinicaldetails().getFamilyhistory()));
                } catch (FhirResourceException e) {
                    LOG.error("Invalid data in XML: " + e.getMessage());
                }
            }

            // (IBD) smoking history
            if (StringUtils.isNotEmpty(results.getPatient().getClinicaldetails().getSmokinghistory())) {
                try {
                    observations.add(createObservationNonTest(NonTestObservationTypes.SMOKING_HISTORY.toString(),
                            results.getPatient().getClinicaldetails().getSmokinghistory()));
                } catch (FhirResourceException e) {
                    LOG.error("Invalid data in XML: " + e.getMessage());
                }
            }

            // (IBD) surgical history
            if (StringUtils.isNotEmpty(results.getPatient().getClinicaldetails().getSurgicalhistory())) {
                try {
                    observations.add(createObservationNonTest(NonTestObservationTypes.SURGICAL_HISTORY.toString(),
                            results.getPatient().getClinicaldetails().getSurgicalhistory()));
                } catch (FhirResourceException e) {
                    LOG.error("Invalid data in XML: " + e.getMessage());
                }
            }

            // (IBD) vaccination record
            if (StringUtils.isNotEmpty(results.getPatient().getClinicaldetails().getVaccinationrecord())) {
                try {
                    observations.add(createObservationNonTest(NonTestObservationTypes.VACCINATION_RECORD.toString(),
                            results.getPatient().getClinicaldetails().getVaccinationrecord()));
                } catch (FhirResourceException e) {
                    LOG.error("Invalid data in XML: " + e.getMessage());
                }
            }

            // (IBD) colonoscopy surveillance (date)
            if (results.getPatient().getClinicaldetails().getColonoscopysurveillance() != null) {
                Observation observation = new Observation();

                DateTime applies = new DateTime();
                DateAndTime dateAndTime = new DateAndTime(
                        results.getPatient().getClinicaldetails().getColonoscopysurveillance().toGregorianCalendar());
                applies.setValue(dateAndTime);
                observation.setApplies(applies);

                Identifier identifier = new Identifier();
                identifier.setValueSimple(NonTestObservationTypes.COLONOSCOPY_SURVEILLANCE.toString());
                observation.setIdentifier(identifier);

                CodeableConcept name = new CodeableConcept();
                name.setTextSimple(NonTestObservationTypes.COLONOSCOPY_SURVEILLANCE.toString());
                observation.setName(name);

                observation.setSubject(resourceReference);
                observations.add(observation);
            }

            // (IBD) bmdexam (date)
            if (results.getPatient().getClinicaldetails().getBmdexam() != null) {
                Observation observation = new Observation();

                DateTime applies = new DateTime();
                DateAndTime dateAndTime = new DateAndTime(
                        results.getPatient().getClinicaldetails().getBmdexam().toGregorianCalendar());
                applies.setValue(dateAndTime);
                observation.setApplies(applies);

                Identifier identifier = new Identifier();
                identifier.setValueSimple(NonTestObservationTypes.BMD_EXAM.toString());
                observation.setIdentifier(identifier);

                CodeableConcept name = new CodeableConcept();
                name.setTextSimple(NonTestObservationTypes.BMD_EXAM.toString());
                observation.setName(name);

                observation.setSubject(resourceReference);
                observations.add(observation);
            }
        }
    }

    private class BodyData {
        private String location;
        private String type;
        private String value;

        public BodyData() {

        }

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    // one observation per side dppulse or ptpulse so expect 4 per footcheckup
    private List<Observation> createFootcheckupObservations(Patientview.Patient.Footcheckup footcheckup) {

        // only proceed if correct timestamp
        if (footcheckup.getDatestamp() != null) {

            List<Observation> observations = new ArrayList<>();
            List<BodyData> bodyDatas = new ArrayList<>();

            for (Patientview.Patient.Footcheckup.Foot foot : footcheckup.getFoot()) {

                BodyData bodyData = new BodyData();

                if (foot.getSide().equals("left")) {
                    bodyData.setLocation(BodySites.LEFT_FOOT.toString());
                } else if (foot.getSide().equals("right")) {
                    bodyData.setLocation(BodySites.RIGHT_FOOT.toString());
                }

                if (foot.getDppulse() != null) {
                    bodyData.setValue(foot.getDppulse());
                    bodyData.setType(NonTestObservationTypes.DPPULSE.toString());
                }

                if (foot.getPtpulse() != null) {
                    bodyData.setValue(foot.getPtpulse());
                    bodyData.setType(NonTestObservationTypes.PTPULSE.toString());
                }

                bodyDatas.add(bodyData);
            }

            for (BodyData bodyData : bodyDatas) {
                Observation observation = new Observation();

                DateTime applies = new DateTime();
                DateAndTime dateAndTime = new DateAndTime(footcheckup.getDatestamp().toGregorianCalendar().getTime());
                applies.setValue(dateAndTime);
                observation.setApplies(applies);

                CodeableConcept bodySite = new CodeableConcept();
                bodySite.setTextSimple(bodyData.getLocation());
                observation.setBodySite(bodySite);

                CodeableConcept value = new CodeableConcept();
                value.setTextSimple(bodyData.getValue());
                observation.setValue(value);

                Identifier identifier = new Identifier();
                identifier.setValueSimple(bodyData.getType());
                observation.setIdentifier(identifier);

                CodeableConcept name = new CodeableConcept();
                name.setTextSimple(bodyData.getType());
                observation.setName(name);

                observation.setSubject(resourceReference);

                observations.add(observation);
            }
            return observations;
        } else {
            return null;
        }
    }

    // one observation per side rgrade, mgrade or va so expect 6 per eyecheckup
    private List<Observation> createEyecheckupObservations(Patientview.Patient.Eyecheckup eyecheckup) {

        // only proceed if correct timestamp
        if (eyecheckup.getDatestamp() != null) {

            List<Observation> observations = new ArrayList<>();
            List<BodyData> bodyDatas = new ArrayList<>();

            for (Patientview.Patient.Eyecheckup.Eye eye : eyecheckup.getEye()) {

                BodySites bodysite = null;

                if (eye.getSide().equals("left")) {
                    bodysite = BodySites.LEFT_EYE;
                } else if (eye.getSide().equals("right")) {
                    bodysite = BodySites.RIGHT_EYE;
                } else {
                    LOG.error("Eye side is not set, continuing without this observation");
                }

                if (bodysite != null) {
                    if (eye.getMgrade() != null) {
                        BodyData bodyData = new BodyData();
                        bodyData.setValue(eye.getMgrade());
                        bodyData.setType(NonTestObservationTypes.MGRADE.toString());
                        bodyData.setLocation(bodysite.toString());
                        bodyDatas.add(bodyData);
                    }

                    if (eye.getRgrade() != null) {
                        BodyData bodyData = new BodyData();
                        bodyData.setValue(eye.getRgrade());
                        bodyData.setType(NonTestObservationTypes.RGRADE.toString());
                        bodyData.setLocation(bodysite.toString());
                        bodyDatas.add(bodyData);
                    }

                    if (eye.getVa() != null) {
                        BodyData bodyData = new BodyData();
                        bodyData.setValue(eye.getVa());
                        bodyData.setType(NonTestObservationTypes.VA.toString());
                        bodyData.setLocation(bodysite.toString());
                        bodyDatas.add(bodyData);
                    }
                }
            }

            for (BodyData bodyData : bodyDatas) {
                Observation observation = new Observation();

                DateTime applies = new DateTime();
                DateAndTime dateAndTime = new DateAndTime(eyecheckup.getDatestamp().toGregorianCalendar().getTime());
                applies.setValue(dateAndTime);
                observation.setApplies(applies);

                CodeableConcept bodySite = new CodeableConcept();
                bodySite.setTextSimple(bodyData.getLocation());
                observation.setBodySite(bodySite);

                CodeableConcept value = new CodeableConcept();
                value.setTextSimple(bodyData.getValue());
                observation.setValue(value);

                Identifier identifier = new Identifier();
                identifier.setValueSimple(bodyData.getType());
                observation.setIdentifier(identifier);

                CodeableConcept name = new CodeableConcept();
                name.setTextSimple(bodyData.getType());
                observation.setName(name);

                if (StringUtils.isNotEmpty(eyecheckup.getLocation())) {
                    observation.setCommentsSimple(eyecheckup.getLocation());
                }
                observation.setSubject(resourceReference);

                observations.add(observation);
            }
            return observations;
        } else {
            return null;
        }
    }

    private Observation createObservation(Patientview.Patient.Testdetails.Test test,
                                          Patientview.Patient.Testdetails.Test.Result result)
            throws FhirResourceException {
        Observation observation = new Observation();

        try {
            observation.setValue(createQuantity(result, test));
        } catch (FhirResourceException e) {
            // text based value
            CodeableConcept valueConcept = new CodeableConcept();
            valueConcept.setTextSimple(CommonUtils.cleanSql(result.getValue()));
            valueConcept.addCoding().setDisplaySimple(CommonUtils.cleanSql(result.getValue()));
            observation.setValue(valueConcept);
        }

        observation.setSubject(resourceReference);
        observation.setName(createConcept(test));
        observation.setApplies(createDateTime(result));
        observation.setIdentifier(createIdentifier(test));
        if (result.getPrepost() != null) {
            observation.setCommentsSimple(result.getPrepost().toString());
        }

        return observation;
    }

    // store type in name and identifier, store value in comments
    private Observation createObservationNonTest(String type, String value) throws FhirResourceException {
        Observation observation = new Observation();

        // text based value
        CodeableConcept valueConcept = new CodeableConcept();
        valueConcept.setTextSimple(value);
        valueConcept.addCoding().setDisplaySimple(value);
        observation.setValue(valueConcept);

        observation.setSubject(resourceReference);

        CodeableConcept name = new CodeableConcept();
        name.setTextSimple(type);
        name.addCoding().setDisplaySimple(type);
        observation.setName(name);

        Identifier identifier = new Identifier();
        identifier.setValueSimple(type);
        observation.setIdentifier(identifier);

        return observation;
    }

    private Quantity createQuantity(Patientview.Patient.Testdetails.Test.Result result,
                                    Patientview.Patient.Testdetails.Test test) throws FhirResourceException {
        Quantity quantity = new Quantity();
        quantity.setValue(createDecimal(result));

        Quantity.QuantityComparator comparator = getComparator(result);
        if (comparator != null) {
            quantity.setComparatorSimple(comparator);
        }

        if (StringUtils.isNotEmpty(test.getUnits())) {
            quantity.setUnitsSimple(test.getUnits());
        }
        return quantity;
    }

    private Quantity.QuantityComparator getComparator(Patientview.Patient.Testdetails.Test.Result result) {

        String resultString = result.getValue();

        if (resultString.contains(">=")) {
            return Quantity.QuantityComparator.greaterOrEqual;
        }

        if (resultString.contains("<=")) {
            return Quantity.QuantityComparator.lessOrEqual;
        }

        if (resultString.contains(">")) {
            return Quantity.QuantityComparator.greaterThan;
        }

        if (resultString.contains("<")) {
            return Quantity.QuantityComparator.lessThan;
        }

        return null;
    }

    private Decimal createDecimal(Patientview.Patient.Testdetails.Test.Result result) throws FhirResourceException {
        Decimal decimal = new Decimal();

        // remove all but numeric and . -
        //String resultString = result.getValue().replaceAll("/[^\\d.-]+/g", "");
        String resultString = result.getValue().replaceAll(">", "").replaceAll(">=", "")
                .replaceAll("<", "").replaceAll("<=", "");

        // attempt to parse remaining
        NumberFormat decimalFormat = DecimalFormat.getInstance();

        if (StringUtils.isNotEmpty(resultString)) {
            try {
                decimal.setValue(BigDecimal.valueOf((decimalFormat.parse(resultString)).doubleValue()));
            } catch (ParseException nfe) {
                throw new FhirResourceException("Invalid value for observation (will try text based): "
                        + result.getValue());
            }
        } else {
            throw new FhirResourceException("Empty value for observation");
        }
        return decimal;
    }

    private org.hl7.fhir.instance.model.Identifier createIdentifier(Patientview.Patient.Testdetails.Test test) {
        org.hl7.fhir.instance.model.Identifier identifier = new org.hl7.fhir.instance.model.Identifier();
        identifier.setLabelSimple("resultcode");
        // note: name is generated from xsd so hco3 becomes HCO_3 therefore use value
        identifier.setValueSimple(test.getTestcode());
        return identifier;
    }

    private CodeableConcept createConcept(Patientview.Patient.Testdetails.Test test) {
        CodeableConcept codeableConcept = new CodeableConcept();
        codeableConcept.setTextSimple(test.getTestcode());
        if (StringUtils.isNotEmpty(test.getTestname())) {
            codeableConcept.addCoding().setDisplaySimple(StringEscapeUtils.escapeSql(test.getTestname()));
        }
        return codeableConcept;
    }

    private DateTime createDateTime(Patientview.Patient.Testdetails.Test.Result result) throws FhirResourceException {
        try {
            DateTime dateTime = new DateTime();
            DateAndTime dateAndTime = new DateAndTime(CommonUtils.getDateFromString(result.getDatestamp()));
            dateTime.setValue(dateAndTime);
            return dateTime;
        } catch (NullPointerException | IllegalArgumentException e) {
            throw new FhirResourceException("Result timestamp is incorrectly formatted");
        }
    }

    public int getSuccess() {
        return success;
    }

    public int getCount() {
        return count;
    }

    public List<Observation> getObservations() {
        return observations;
    }

    public Map<String, Alert> getAlertMap() {
        return alertMap;
    }

    public void setAlertMap(Map<String, Alert> alertMap) {
        this.alertMap = alertMap;
    }

    public Map<String, Patientview.Patient.Testdetails.Test.Daterange> getDateRanges() {
        return dateRanges;
    }
}
