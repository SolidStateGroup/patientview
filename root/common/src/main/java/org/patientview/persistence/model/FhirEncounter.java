package org.patientview.persistence.model;

import org.hl7.fhir.instance.model.DateAndTime;
import org.hl7.fhir.instance.model.DateTime;
import org.hl7.fhir.instance.model.Encounter;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Set;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 08/09/2014
 *
 * Used for treatment, transplant status, surgery
 */
public class FhirEncounter extends BaseModel {

    // will be EncounterTypes enum
    // maps to FHIR encounter identifier -> value
    private String encounterType;

    // maps to FHIR type -> text
    private String status;

    // only used during migration
    private String identifier;
    private Group group;

    // used during display in UI
    private Set<Link> links;

    // used when adding surgery information, IBD patient management
    private Set<FhirObservation> observations;
    private Set<FhirProcedure> procedures;
    private Date date;

    public FhirEncounter() {
    }

    public FhirEncounter(Encounter encounter) {
        if (!encounter.getIdentifier().isEmpty()) {
            setEncounterType(encounter.getIdentifier().get(0).getValue().getValue());
        }
        if (!encounter.getType().isEmpty()) {
            setStatus(encounter.getType().get(0).getTextSimple());
        }
        if (encounter.getPeriod() != null && encounter.getPeriod().getStart() != null) {
            DateTime start = encounter.getPeriod().getStart();
            DateAndTime date = start.getValue();
            setDate(new Date(new GregorianCalendar(date.getYear(), date.getMonth() - 1,
                    date.getDay(), date.getHour(), date.getMinute(), date.getSecond()).getTimeInMillis()));
        }
    }

    public String getEncounterType() {
        return encounterType;
    }

    public void setEncounterType(String encounterType) {
        this.encounterType = encounterType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public Set<Link> getLinks() {
        return links;
    }

    public void setLinks(Set<Link> links) {
        this.links = links;
    }

    public Set<FhirObservation> getObservations() {
        return observations;
    }

    public void setObservations(Set<FhirObservation> observations) {
        this.observations = observations;
    }

    public Set<FhirProcedure> getProcedures() {
        return procedures;
    }

    public void setProcedures(Set<FhirProcedure> procedures) {
        this.procedures = procedures;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
