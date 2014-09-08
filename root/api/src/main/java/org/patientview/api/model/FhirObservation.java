package org.patientview.api.model;

import org.hl7.fhir.instance.model.DateAndTime;
import org.hl7.fhir.instance.model.DateTime;
import org.hl7.fhir.instance.model.Decimal;
import org.hl7.fhir.instance.model.Observation;
import org.patientview.persistence.model.BaseModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 08/09/2014
 */
public class FhirObservation extends BaseModel{

    private Date applies;
    private String name;
    private Double value;

    private static final Logger LOG = LoggerFactory.getLogger(FhirObservation.class);

    public FhirObservation() {
    }

    public FhirObservation(Observation observation) {

        setName(observation.getName().getTextSimple());

        try {
            Decimal dec = (Decimal) observation.getValue().getChildByName("value").getValues().get(0);
            setValue(Double.valueOf(dec.getStringValue()));
        } catch (NumberFormatException nfe) {
           LOG.debug("Error attempting to convert FHIR observation");
        }

        DateTime applies = (DateTime) observation.getApplies();
        DateAndTime date = applies.getValue();
        setApplies(new Date(new GregorianCalendar(date.getYear(), date.getMonth()-1,
                date.getDay(), date.getHour(), date.getMinute(), date.getSecond()).getTimeInMillis()));
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public Date getApplies() {
        return applies;
    }

    public void setApplies(Date applies) {
        this.applies = applies;
    }
}
