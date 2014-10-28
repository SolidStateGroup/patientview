package org.patientview.importer.service.impl;

import generated.Patientview;
import org.hl7.fhir.instance.model.DateAndTime;
import org.hl7.fhir.instance.model.DateTime;
import org.hl7.fhir.instance.model.Observation;
import org.hl7.fhir.instance.model.ResourceReference;
import org.hl7.fhir.instance.model.ResourceType;
import org.patientview.importer.builder.ObservationsBuilder;
import org.patientview.importer.model.BasicObservation;
import org.patientview.importer.model.DateRange;
import org.patientview.importer.resource.FhirResource;
import org.patientview.importer.service.ObservationService;
import org.patientview.importer.Utility.Util;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.persistence.model.FhirLink;
import org.patientview.persistence.model.enums.NonTestObservationTypes;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Created by james@solidstategroup.com
 * Created on 01/09/2014
 */
@Service
public class ObservationServiceImpl extends AbstractServiceImpl<ObservationService> implements ObservationService {

    @Inject
    private FhirResource fhirResource;

    /**
     * Creates all of the FHIR observation records from the Patientview object. Links then to the PatientReference
     */
    @Override
    public void add(final Patientview data, final FhirLink fhirLink) throws FhirResourceException, SQLException {

        LOG.info("Starting Observation Process");
        ResourceReference patientReference = Util.createResourceReference(fhirLink.getResourceId());
        ObservationsBuilder observationsBuilder = new ObservationsBuilder(data, patientReference);
        observationsBuilder.build();

        LOG.info("Getting Existing Observations");
        List<BasicObservation> observations = fhirResource.getBasicObservationBySubjectId(fhirLink.getResourceId());

        // delete existing observations between dates in <test><daterange>
        LOG.info("Deleting Existing Observations in date ranges");
        for (BasicObservation observation : observations) {
            String code = observation.getCode();
            UUID uuid = observation.getLogicalId();
            Date applies = observation.getApplies();

            // only delete test result observations (not BLOOD_GROUP, DIAGNOSTIC_RESULT etc)
            if (!Util.isInEnum(code, NonTestObservationTypes.class)) {

                Patientview.Patient.Testdetails.Test.Daterange daterange
                        = observationsBuilder.getDateRanges().get(code);

                if (daterange != null) {
                    DateRange convertedDateRange = new DateRange(daterange);

                    if (applies.after(convertedDateRange.getStart()) && applies.before(convertedDateRange.getEnd())) {
                        fhirResource.delete(uuid, ResourceType.Observation);
                    }
                }
            }

            // if observation is NonTestObservationType.BLOOD_GROUP then delete
            if (code.equals(NonTestObservationTypes.BLOOD_GROUP.toString())) {
                fhirResource.delete(uuid, ResourceType.Observation);
            }
        }

        int count = 0;

        LOG.info("Creating New Observations");
        for (Observation observation : observationsBuilder.getObservations()) {
            LOG.trace("Creating... observation " + count);
            try {
                // only add observations within daterange
                Patientview.Patient.Testdetails.Test.Daterange daterange
                        = observationsBuilder.getDateRanges().get(observation.getIdentifier()
                            .getValueSimple().toUpperCase());

                if (daterange != null) {
                    DateRange convertedDateRange = new DateRange(daterange);
                    Date applies = convertDateTime((DateTime) observation.getApplies());

                    if (applies.after(convertedDateRange.getStart()) && applies.before(convertedDateRange.getEnd())) {
                        fhirResource.create(observation);
                    }
                }
            } catch (FhirResourceException e) {
                LOG.error("Unable to build observation {} " + e.getCause());
            }
            LOG.trace("Finished creating observation " + count++);
        }
        LOG.info("Processed {} of {} observations", observationsBuilder.getSuccess(), observationsBuilder.getCount());
    }

    public void deleteBySubjectId(UUID subjectId) throws FhirResourceException, SQLException {
        for (UUID uuid : fhirResource.getLogicalIdsBySubjectId("observation", subjectId)) {
            fhirResource.delete(uuid, ResourceType.Observation);
        }
    }

    private Date convertDateTime(DateTime dateTime) {
        DateAndTime dateAndTime = dateTime.getValue();
        Calendar calendar = Calendar.getInstance();
        calendar.set(dateAndTime.getYear(), dateAndTime.getMonth() - 1, dateAndTime.getDay(),
                dateAndTime.getHour(), dateAndTime.getMinute());
        return calendar.getTime();
    }
}


