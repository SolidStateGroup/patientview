package org.patientview.persistence.model;

import org.hl7.fhir.instance.model.Encounter;

import java.util.Set;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 08/09/2014
 *
 * Used for treatment, transplant status, surgery
 */
public class FhirEncounter extends BaseModel {

    // maps to FHIR type -> text
    // will be EncounterTypes enum
    private String encounterType;

    // maps to FHIR identifier -> value
    private String status;

    // only used during migration
    private String identifier;
    private Group group;

    // used during display in UI
    Set<Link> links;

    // used when adding surgery information, IBD patient management
    Set<FhirObservation> observations;

    // used when adding surgery procedures, IBD patient management
    Set<FhirProcedure> procedures;

    public FhirEncounter() {
    }

    public FhirEncounter(Encounter encounter) {
        if (!encounter.getIdentifier().isEmpty()) {
            setEncounterType(encounter.getIdentifier().get(0).getValue().getValue());
        }
        if (!encounter.getType().isEmpty()) {
            setStatus(encounter.getType().get(0).getTextSimple());
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
}
