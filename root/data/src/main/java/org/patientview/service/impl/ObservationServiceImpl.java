package org.patientview.service.impl;

import com.zaxxer.hikari.HikariDataSource;
import generated.Patientview;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.hl7.fhir.instance.model.CodeableConcept;
import org.hl7.fhir.instance.model.DateAndTime;
import org.hl7.fhir.instance.model.DateTime;
import org.hl7.fhir.instance.model.Decimal;
import org.hl7.fhir.instance.model.Enumeration;
import org.hl7.fhir.instance.model.Observation;
import org.hl7.fhir.instance.model.Quantity;
import org.hl7.fhir.instance.model.ResourceReference;
import org.hl7.fhir.instance.model.ResourceType;
import org.patientview.builder.ObservationBuilder;
import org.patientview.builder.ObservationsBuilder;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.config.utils.CommonUtils;
import org.patientview.persistence.model.Alert;
import org.patientview.persistence.model.BasicObservation;
import org.patientview.persistence.model.DateRange;
import org.patientview.persistence.model.FhirDatabaseObservation;
import org.patientview.persistence.model.FhirLink;
import org.patientview.persistence.model.FhirObservation;
import org.patientview.persistence.model.ObservationHeading;
import org.patientview.persistence.model.enums.AlertTypes;
import org.patientview.persistence.model.enums.DiagnosticReportObservationTypes;
import org.patientview.persistence.model.enums.NonTestObservationTypes;
import org.patientview.persistence.repository.AlertRepository;
import org.patientview.persistence.repository.IdentifierRepository;
import org.patientview.persistence.resource.FhirResource;
import org.patientview.service.ObservationService;
import org.patientview.util.Util;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Created by james@solidstategroup.com
 * Created on 01/09/2014
 */
@Service
public class ObservationServiceImpl extends AbstractServiceImpl<ObservationService> implements ObservationService {

    @Inject
    private AlertRepository alertRepository;

    @Inject
    @Named("fhir")
    private HikariDataSource dataSource;

    @Inject
    private FhirResource fhirResource;

    @Inject
    private IdentifierRepository identifierRepository;

    private String nhsno;

    private static final String OBSERVATION_HEADER_COMMENT_CODE = "resultcomment";

    /**
     * Creates all of the FHIR observation records from the Patientview object. Links then to the PatientReference
     */
    @Override
    public void add(final Patientview data, final FhirLink fhirLink) throws FhirResourceException, SQLException {

        this.nhsno = data.getPatient().getPersonaldetails().getNhsno();
        LOG.trace(nhsno + ": Starting Observation Process");

        // create map to hold user alerts (if present)
        Map<String, Alert> currentAlertMap = new HashMap<>();
        List<org.patientview.persistence.model.Identifier> identifiers
                = identifierRepository.findByValue(data.getPatient().getPersonaldetails().getNhsno());

        if (!CollectionUtils.isEmpty(identifiers)) {
            List<Alert> alerts
                    = alertRepository.findByUserAndAlertType(identifiers.get(0).getUser(), AlertTypes.RESULT);
            if (!CollectionUtils.isEmpty(alerts)) {
                for (Alert currentAlert : alerts) {
                    currentAlertMap.put(currentAlert.getObservationHeading().getCode().toUpperCase(), currentAlert);
                }
            }
        }

        ResourceReference patientReference = Util.createResourceReference(fhirLink.getResourceId());
        ObservationsBuilder observationsBuilder = new ObservationsBuilder(data, patientReference);
        observationsBuilder.setAlertMap(currentAlertMap);
        observationsBuilder.build();

        LOG.trace(nhsno + ": Getting Existing Observations");
        List<BasicObservation> observations = getBasicObservationBySubjectId(fhirLink.getResourceId());

        // get uuids of existing observations to delete
        try {
            LOG.trace(nhsno + ": Deleting Existing Observations");
            List<UUID> observationsUuidsToDelete = new ArrayList<>();

            for (BasicObservation observation : observations) {
                String code = observation.getCode();
                UUID uuid = observation.getLogicalId();
                Date applies = observation.getApplies();

                // only delete test result observations between date range (not BLOOD_GROUP, DIAGNOSTIC_RESULT etc)
                if (!Util.isInEnum(code, NonTestObservationTypes.class)
                        && !Util.isInEnum(code, DiagnosticReportObservationTypes.class)) {

                    Patientview.Patient.Testdetails.Test.Daterange daterange
                            = observationsBuilder.getDateRanges().get(code.toUpperCase());

                    // between dates in <test><daterange>
                    if (daterange != null) {
                        DateRange convertedDateRange = new DateRange(daterange);

                        Long start = convertedDateRange.getStart().getTime();
                        Long end = convertedDateRange.getEnd().getTime();

                        if (applies.getTime() >= start && applies.getTime() <= end) {
                            observationsUuidsToDelete.add(uuid);
                        }
                    }
                } else if (Util.isInEnum(code, NonTestObservationTypes.class)) {
                    // if observation is NonTestObservationType.BLOOD_GROUP, PTPULSE, DPPULSE etc then delete
                    observationsUuidsToDelete.add(uuid);
                }
            }

            // natively delete observations
            if (!CollectionUtils.isEmpty(observationsUuidsToDelete)) {
                StringBuilder sb = new StringBuilder();
                sb.append("DELETE FROM observation WHERE logical_id IN (");

                for (int i = 0; i < observationsUuidsToDelete.size(); i++) {
                    UUID uuid = observationsUuidsToDelete.get(i);

                    sb.append("'").append(uuid).append("'");

                    if (i != (observationsUuidsToDelete.size() - 1)) {
                        sb.append(",");
                    }
                }

                sb.append(")");
                fhirResource.executeSQL(sb.toString());
            }
        } catch (Exception e) {
            LOG.error("Error deleting existing observations", e);
            throw new FhirResourceException(e);
        }

        int count = 0;

        try {
            LOG.trace(nhsno + ": Creating New Observations");
            List<FhirDatabaseObservation> fhirDatabaseObservations = new ArrayList<>();

            for (Observation observation : observationsBuilder.getObservations()) {
                LOG.trace("Creating... observation " + count);
                try {
                    // only add observations within daterange or those without a daterange (non test observation type)
                    Patientview.Patient.Testdetails.Test.Daterange daterange
                            = observationsBuilder.getDateRanges().get(observation.getIdentifier()
                            .getValueSimple().toUpperCase());

                    if (daterange != null) {
                        DateRange convertedDateRange = new DateRange(daterange);
                        Date applies = convertDateTime((DateTime) observation.getApplies());

                        Long start = convertedDateRange.getStart().getTime();
                        Long end = convertedDateRange.getEnd().getTime();

                        if (applies.getTime() >= start && applies.getTime() <= end) {
                            fhirDatabaseObservations.add(
                                    new FhirDatabaseObservation(fhirResource.marshallFhirRecord(observation)));
                        }
                    } else {
                        fhirDatabaseObservations.add(
                                new FhirDatabaseObservation(fhirResource.marshallFhirRecord(observation)));
                    }
                } catch (FhirResourceException e) {
                    LOG.error(nhsno + ": Unable to build observation {} " + e.getCause());
                }
                LOG.trace(nhsno + ": Finished creating observation " + count++);
            }

            // now have collection, manually insert using native SQL
            if (!CollectionUtils.isEmpty(fhirDatabaseObservations)) {
                StringBuilder sb = new StringBuilder();
                sb.append("INSERT INTO observation ");
                sb.append("(logical_id, version_id, resource_type, published, updated, content) VALUES ");

                for (int i = 0; i < fhirDatabaseObservations.size(); i++) {
                    FhirDatabaseObservation obs = fhirDatabaseObservations.get(i);
                    sb.append("(");
                    sb.append("'").append(obs.getLogicalId().toString()).append("','");
                    sb.append(obs.getVersionId().toString()).append("','");
                    sb.append(obs.getResourceType()).append("','");
                    sb.append(obs.getPublished().toString()).append("','");
                    sb.append(obs.getUpdated().toString()).append("','");
                    sb.append(CommonUtils.cleanSql(obs.getContent()));
                    sb.append("')");
                    if (i != (fhirDatabaseObservations.size() - 1)) {
                        sb.append(",");
                    }
                }
                fhirResource.executeSQL(sb.toString());

                // handle updating alerts if present, emails are sent by scheduled task, not here
                Map<String, Alert> alertMap = observationsBuilder.getAlertMap();

                for (String code : alertMap.keySet()) {
                    Alert alert = alertMap.get(code);
                    if (alert.isUpdated()) {
                        Alert entityAlert = alertRepository.findById(alert.getId())
                                .orElseThrow(() -> new ResourceNotFoundException("Alert not found"));

                        entityAlert.setLatestValue(alert.getLatestValue());
                        entityAlert.setLatestDate(alert.getLatestDate());
                        entityAlert.setWebAlertViewed(alert.isWebAlertViewed());
                        entityAlert.setEmailAlertSent(alert.isEmailAlertSent());
                        entityAlert.setLastUpdate(new Date());
                        alertRepository.save(entityAlert);
                    }
                }
            }

            LOG.info(nhsno + ": Processed {} of {} observations",
                    observationsBuilder.getSuccess(), observationsBuilder.getCount());
        } catch (Exception e) {
            LOG.error("Error creating observations", e);
            throw new FhirResourceException(e);
        }
    }

    @Override
    public void add(FhirObservation fhirObservation, FhirLink fhirLink) throws FhirResourceException {
        ObservationBuilder observationBuilder
                = new ObservationBuilder(null, fhirObservation, Util.createResourceReference(fhirLink.getResourceId()));

        fhirResource.createEntity(observationBuilder.build(), ResourceType.Observation.name(), "observation");
    }

    @Override
    public Observation buildNonTestObservation(FhirObservation fhirObservation) throws FhirResourceException {

        Observation observation = new Observation();
        if (fhirObservation.getApplies() != null) {
            DateTime dateTime = new DateTime();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(fhirObservation.getApplies());
            DateAndTime dateAndTime = new DateAndTime(calendar);
            dateTime.setValue(dateAndTime);
            observation.setApplies(dateTime);
        }
        observation.setReliability(new Enumeration<>(Observation.ObservationReliability.ok));
        observation.setStatusSimple(Observation.ObservationStatus.registered);

        if (StringUtils.isNotEmpty(fhirObservation.getValue())) {
            try {
                Quantity quantity = new Quantity();
                quantity.setValue(createDecimal(fhirObservation.getValue()));
                quantity.setComparatorSimple(getComparator(fhirObservation.getComparator()));
                observation.setValue(quantity);
            } catch (ParseException pe) {
                // parse exception, likely to be a string, e.g. comments store as text
                CodeableConcept comment = new CodeableConcept();
                comment.setTextSimple(fhirObservation.getValue());
                observation.setValue(comment);
            }
        }

        if (StringUtils.isNotEmpty(fhirObservation.getName())) {
            CodeableConcept name = new CodeableConcept();
            name.setTextSimple(fhirObservation.getName().toUpperCase());
            name.addCoding().setDisplaySimple(fhirObservation.getName().toUpperCase());
            observation.setName(name);
            observation.setIdentifier(createIdentifier(fhirObservation.getName().toUpperCase()));
        }

        if (StringUtils.isNotEmpty(fhirObservation.getComments())) {
            observation.setCommentsSimple(fhirObservation.getComments());
        }

        if (StringUtils.isNotEmpty(fhirObservation.getBodySite())) {
            CodeableConcept bodySite = new CodeableConcept();
            bodySite.setTextSimple(fhirObservation.getBodySite());
            observation.setBodySite(bodySite);
        }

        if (StringUtils.isNotEmpty(fhirObservation.getLocation())) {
            observation.setCommentsSimple(fhirObservation.getLocation());
        }

        return observation;
    }

    @Override
    public Observation buildObservation(DateTime applies, String value, String comparator, String comments,
                                        ObservationHeading observationHeading, boolean editable)
            throws FhirResourceException {
        Observation observation = new Observation();
        if (applies != null) {
            observation.setApplies(applies);
        }
        observation.setReliability(new Enumeration<>(Observation.ObservationReliability.ok));
        // set flag to control if observation is editable
        observation.setStatusSimple(editable ? Observation.ObservationStatus.registered :
                Observation.ObservationStatus.final_);

        if (StringUtils.isNotEmpty(value)) {
            try {
                // need a quick fix, as allowed to save comment as Quantity if numeric
                if (OBSERVATION_HEADER_COMMENT_CODE.equals(observationHeading.getCode())) {
                    CodeableConcept comment = new CodeableConcept();
                    comment.setTextSimple(value);
                    comment.addCoding().setDisplaySimple(observationHeading.getHeading());
                    observation.setValue(comment);

                } else {
                    Quantity quantity = new Quantity();
                    quantity.setValue(createDecimal(value));
                    quantity.setComparatorSimple(getComparator(comparator));
                    quantity.setUnitsSimple(observationHeading.getUnits());
                    observation.setValue(quantity);
                }
            } catch (ParseException pe) {
                // parse exception, likely to be a string, e.g. comments store as text
                CodeableConcept comment = new CodeableConcept();
                comment.setTextSimple(value);
                comment.addCoding().setDisplaySimple(observationHeading.getHeading());
                observation.setValue(comment);
            }
        }

        CodeableConcept name = new CodeableConcept();
        name.setTextSimple(observationHeading.getCode());
        name.addCoding().setDisplaySimple(observationHeading.getHeading());
        observation.setName(name);

        observation.setIdentifier(createIdentifier(observationHeading.getCode()));

        if (StringUtils.isNotEmpty(comments)) {
            observation.setCommentsSimple(comments);
        }

        return observation;
    }

    @Override
    public Observation copyObservation(Observation observation, Date applies, String value)
            throws FhirResourceException {

        Observation newObservation = observation.copy();

        if (applies != null) {
            DateTime dateTime = new DateTime();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(applies);
            DateAndTime dateAndTime = new DateAndTime(calendar);
            dateTime.setValue(dateAndTime);
            newObservation.setApplies(dateTime);
        }

        if (StringUtils.isNotEmpty(value)) {
            try {

                if (observation.getValue().getClass().equals(Quantity.class)) {
                    // quantity value
                    Quantity quantity = (Quantity) newObservation.getValue();
                    quantity.setValue(createDecimal(value));
                    newObservation.setValue(quantity);
                } else if (observation.getValue().getClass().equals(CodeableConcept.class)) {
                    // comment text
                    CodeableConcept comment = (CodeableConcept) newObservation.getValue();
                    comment.setTextSimple(value);
                    newObservation.setValue(comment);
                    newObservation.setCommentsSimple(value);
                } else {
                    throw new FhirResourceException("Cannot convert FHIR observation, unknown Value type");
                }
            } catch (ParseException pe) {
                throw new FhirResourceException("Cannot convert FHIR observation, invalid quantity");
            }
        }
        return newObservation;
    }

    private Date convertDateTime(DateTime dateTime) {
        DateAndTime dateAndTime = dateTime.getValue();
        Calendar calendar = Calendar.getInstance();
        calendar.set(dateAndTime.getYear(), dateAndTime.getMonth() - 1, dateAndTime.getDay(),
                dateAndTime.getHour(), dateAndTime.getMinute());
        return calendar.getTime();
    }

    private Decimal createDecimal(String result) throws ParseException {
        Decimal decimal = new Decimal();

        // remove all but numeric and . -
        String resultString = result.replaceAll("/[^\\d.-]+/", "");

        // attempt to parse remaining
        NumberFormat decimalFormat = DecimalFormat.getInstance();

        try {
            if (StringUtils.isNotEmpty(resultString)) {
                decimal.setValue(BigDecimal.valueOf((decimalFormat.parse(resultString)).doubleValue()));
            }
        } catch (ParseException nfe) {
            throw new ParseException("Invalid value for observation", nfe.getErrorOffset());
        }

        return decimal;
    }

    private org.hl7.fhir.instance.model.Identifier createIdentifier(String code) {
        org.hl7.fhir.instance.model.Identifier identifier = new org.hl7.fhir.instance.model.Identifier();
        identifier.setLabelSimple("resultcode");
        identifier.setValueSimple(code);
        return identifier;
    }

    @Override
    public void deleteAllExistingObservationData(Set<FhirLink> fhirLinks) throws FhirResourceException {

        if (fhirLinks != null) {
            for (FhirLink fhirLink : fhirLinks) {
                UUID subjectId = fhirLink.getResourceId();
                deleteObservations(fhirResource.getLogicalIdsBySubjectId("observation", subjectId));
            }
        }
    }

    @Override
    public void deleteObservations(List<UUID> observationsUuidsToDelete) throws FhirResourceException {
        if (!CollectionUtils.isEmpty(observationsUuidsToDelete)) {
            StringBuilder sb = new StringBuilder();
            sb.append("DELETE FROM observation WHERE logical_id IN (");

            for (int i = 0; i < observationsUuidsToDelete.size(); i++) {
                UUID uuid = observationsUuidsToDelete.get(i);

                sb.append("'").append(uuid).append("'");

                if (i != (observationsUuidsToDelete.size() - 1)) {
                    sb.append(",");
                }
            }

            sb.append(")");
            fhirResource.executeSQL(sb.toString());
        }
    }

    private List<BasicObservation> getBasicObservationBySubjectId(final UUID subjectId)
            throws FhirResourceException {

        Connection connection = null;
        java.sql.Statement statement = null;
        ResultSet results = null;
        try {
            // build query
            StringBuilder query = new StringBuilder();
            query.append("SELECT logical_id, content->'appliesDateTime', content->'name'->'text' ");
            query.append("FROM observation ");
            query.append("WHERE content -> 'subject' ->> 'display' = '");
            query.append(subjectId);
            query.append("' ");

            // execute and return map of logical ids and applies
            connection = dataSource.getConnection();
            statement = connection.createStatement();
            results = statement.executeQuery(query.toString());
            List<BasicObservation> observations = new ArrayList<>();

            while ((results.next())) {

                // remove timezone and parse date
                try {
                    String codeString = results.getString(3).replace("\"", "");

                    // ignore DIAGNOSTIC_RESULT
                    if (!codeString.equals(DiagnosticReportObservationTypes.DIAGNOSTIC_RESULT.toString())) {

                        Date applies = null;

                        if (StringUtils.isNotEmpty(results.getString(2))) {
                            String dateString = results.getString(2).replace("\"", "");
                            XMLGregorianCalendar xmlDate
                                    = DatatypeFactory.newInstance().newXMLGregorianCalendar(dateString);
                            applies = xmlDate.toGregorianCalendar().getTime();
                        }

                        observations.add(new BasicObservation(
                                UUID.fromString(results.getString(1)), applies, codeString));
                    }

                } catch (DatatypeConfigurationException e) {
                    LOG.error(e.getMessage());
                }
            }

            return observations;
        } catch (Exception e) {
            LOG.error("Error getting existing observations", e);
            throw new FhirResourceException(e);
        } finally {
            DbUtils.closeQuietly(connection, statement, results);
        }
    }

    private Quantity.QuantityComparator getComparator(String comparator) {

        if (StringUtils.isNotEmpty(comparator)) {
            if (comparator.contains(">=")) {
                return Quantity.QuantityComparator.greaterOrEqual;
            }

            if (comparator.contains("<=")) {
                return Quantity.QuantityComparator.lessOrEqual;
            }

            if (comparator.contains(">")) {
                return Quantity.QuantityComparator.greaterThan;
            }

            if (comparator.contains("<")) {
                return Quantity.QuantityComparator.lessThan;
            }
        }

        return null;
    }

    @Override
    public void insertFhirDatabaseObservations(List<FhirDatabaseObservation> fhirDatabaseObservations)
            throws FhirResourceException {
        // now have collection, manually insert using native SQL
        if (!CollectionUtils.isEmpty(fhirDatabaseObservations)) {
            StringBuilder sb = new StringBuilder();
            sb.append("INSERT INTO observation ");
            sb.append("(logical_id, version_id, resource_type, published, updated, content) VALUES ");

            for (int i = 0; i < fhirDatabaseObservations.size(); i++) {
                FhirDatabaseObservation obs = fhirDatabaseObservations.get(i);
                sb.append("(");
                sb.append("'").append(obs.getLogicalId().toString()).append("','");
                sb.append(obs.getVersionId().toString()).append("','");
                sb.append(obs.getResourceType()).append("','");
                sb.append(obs.getPublished().toString()).append("','");
                sb.append(obs.getUpdated().toString()).append("','");
                sb.append(StringEscapeUtils.escapeSql(obs.getContent()));
                sb.append("')");
                if (i != (fhirDatabaseObservations.size() - 1)) {
                    sb.append(",");
                }
            }
            fhirResource.executeSQL(sb.toString());
        }
    }
}


