package org.patientview.service.impl;

import generated.Patientview;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.lang.StringUtils;
import org.hl7.fhir.instance.model.DateAndTime;
import org.hl7.fhir.instance.model.DateTime;
import org.hl7.fhir.instance.model.Observation;
import org.hl7.fhir.instance.model.ResourceReference;
import org.patientview.config.utils.CommonUtils;
import org.patientview.builder.ObservationsBuilder;
import org.patientview.persistence.model.BasicObservation;
import org.patientview.persistence.model.DateRange;
import org.patientview.persistence.model.Alert;
import org.patientview.persistence.model.FhirDatabaseObservation;
import org.patientview.persistence.model.enums.AlertTypes;
import org.patientview.persistence.model.enums.DiagnosticReportObservationTypes;
import org.patientview.persistence.repository.AlertRepository;
import org.patientview.persistence.repository.IdentifierRepository;
import org.patientview.persistence.resource.FhirResource;
import org.patientview.service.ObservationService;
import org.patientview.util.Util;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.persistence.model.FhirLink;
import org.patientview.persistence.model.enums.NonTestObservationTypes;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by james@solidstategroup.com
 * Created on 01/09/2014
 */
@Service
public class ObservationServiceImpl extends AbstractServiceImpl<ObservationService> implements ObservationService {

    @Inject
    private FhirResource fhirResource;

    @Inject
    @Named("fhir")
    private BasicDataSource dataSource;

    @Inject
    private IdentifierRepository identifierRepository;

    @Inject
    private AlertRepository alertRepository;

    private String nhsno;

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
                        Alert entityAlert = alertRepository.findOne(alert.getId());
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

    private Date convertDateTime(DateTime dateTime) {
        DateAndTime dateAndTime = dateTime.getValue();
        Calendar calendar = Calendar.getInstance();
        calendar.set(dateAndTime.getYear(), dateAndTime.getMonth() - 1, dateAndTime.getDay(),
                dateAndTime.getHour(), dateAndTime.getMinute());
        return calendar.getTime();
    }

    private List<BasicObservation> getBasicObservationBySubjectId(final UUID subjectId)
            throws FhirResourceException {
        
        try {
            // build query
            StringBuilder query = new StringBuilder();
            query.append("SELECT logical_id, content->'appliesDateTime', content->'name'->'text' ");
            query.append("FROM observation ");
            query.append("WHERE content -> 'subject' ->> 'display' = '");
            query.append(subjectId);
            query.append("' ");
    
            // execute and return map of logical ids and applies
            Connection connection = dataSource.getConnection();
            java.sql.Statement statement = connection.createStatement();
            ResultSet results = statement.executeQuery(query.toString());
            List<BasicObservation> observations = new ArrayList<>();

            while ((results.next())) {

                // remove timezone and parse date
                try {
                    String codeString = results.getString(3).replace("\"", "");

                    // ignore DIAGNOSTIC_RESULT
                    if(!codeString.equals(DiagnosticReportObservationTypes.DIAGNOSTIC_RESULT.toString())) {

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

            connection.close();
            return observations;
        } catch (Exception e) {
            LOG.error("Error getting existing observations", e);
            throw new FhirResourceException(e);
        }
    }
}


