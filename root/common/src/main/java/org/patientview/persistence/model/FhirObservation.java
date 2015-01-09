package org.patientview.persistence.model;

import org.apache.commons.lang.StringUtils;
import org.hl7.fhir.instance.model.CodeableConcept;
import org.hl7.fhir.instance.model.DateAndTime;
import org.hl7.fhir.instance.model.DateTime;
import org.hl7.fhir.instance.model.Observation;
import org.hl7.fhir.instance.model.Quantity;
import org.patientview.config.exception.FhirResourceException;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.UUID;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 08/09/2014
 */
public class FhirObservation extends BaseModel {

    private Date applies;

    // observation heading code
    private String name;

    private String value;
    private String comments;
    private String comparator;
    private Group group;
    private String temporaryUuid;
    private String bodySite;

    // used for foot/eye checkups
    private String location;

    // only used by migration
    private String identifier;

    public FhirObservation() {
    }

    // if converting from actual Observation
    public FhirObservation(Observation observation) throws FhirResourceException {

        if (observation.getName() == null) {
            throw new FhirResourceException("Cannot convert FHIR observation, missing Name");
        }

        if (observation.getValue() == null) {
            throw new FhirResourceException("Cannot convert FHIR observation, missing Value");
        }

        setTemporaryUuid(UUID.randomUUID().toString());
        setName(observation.getName().getTextSimple().toUpperCase());
        setComments(observation.getCommentsSimple());

        if (observation.getValue().getClass().equals(Quantity.class)) {

            // value
            Quantity value = (Quantity) observation.getValue();
            if (value.getValueSimple() != null) {
                setValue(value.getValueSimple().toString());
            }

            // comparator
            Quantity.QuantityComparator quantityComparator = value.getComparatorSimple();
            if (quantityComparator != null && !quantityComparator.equals(Quantity.QuantityComparator.Null)) {
                setComparator(quantityComparator.toCode());
            }

        } else if (observation.getValue().getClass().equals(CodeableConcept.class)) {
            CodeableConcept value = (CodeableConcept) observation.getValue();
            setValue(value.getTextSimple());
        } else {
            throw new FhirResourceException("Cannot convert FHIR observation, unknown Value type");
        }

        if (observation.getApplies() != null) {
            DateTime applies = (DateTime) observation.getApplies();
            DateAndTime date = applies.getValue();
            setApplies(new Date(new GregorianCalendar(date.getYear(), date.getMonth() - 1,
                    date.getDay(), date.getHour(), date.getMinute(), date.getSecond()).getTimeInMillis()));
        }

        if (observation.getBodySite() != null) {
            setBodySite(observation.getBodySite().getTextSimple());
        }

        // used for eye/foot checkup
        if (StringUtils.isNotEmpty(observation.getCommentsSimple())) {
            setLocation(observation.getCommentsSimple());
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Date getApplies() {
        return applies;
    }

    public void setApplies(Date applies) {
        this.applies = applies;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getComparator() {
        return comparator;
    }

    public void setComparator(String comparator) {
        this.comparator = comparator;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public String getTemporaryUuid() {
        return temporaryUuid;
    }

    public void setTemporaryUuid(String temporaryUuid) {
        this.temporaryUuid = temporaryUuid;
    }

    public String getBodySite() {
        return bodySite;
    }

    public void setBodySite(String bodySite) {
        this.bodySite = bodySite;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }
}
