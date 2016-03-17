package org.patientview.api.model;

/**
 * ObservationHeading, representing an Observation (result) type, including meta information.
 * Created by jamesr@solidstategroup.com
 * Created on 12/09/2014
 */
public class ObservationHeading extends BaseObservationHeading {
    private Long panel;
    private Long panelOrder;
    private Long decimalPlaces;

    // used when retrieving results
    private FhirObservation latestObservation;
    private Double valueChange;

    public ObservationHeading() {
    }

    public ObservationHeading(org.patientview.persistence.model.ObservationHeading observationHeading) {
        this.panel = observationHeading.getDefaultPanel();
        this.panelOrder = observationHeading.getDefaultPanelOrder();
        this.decimalPlaces = observationHeading.getDecimalPlaces();

        // BaseObservationHeading properties
        setId(observationHeading.getId());
        setCode(observationHeading.getCode());
        setHeading(observationHeading.getHeading());
        setName(observationHeading.getName());
        setNormalRange(observationHeading.getNormalRange());
        setUnits(observationHeading.getUnits());
        setMinGraph(observationHeading.getMinGraph());
        setMaxGraph(observationHeading.getMaxGraph());
        setInfoLink(observationHeading.getInfoLink());
    }

    public Long getPanel() {
        return panel;
    }

    public void setPanel(Long panel) {
        this.panel = panel;
    }

    public Long getPanelOrder() {
        return panelOrder;
    }

    public void setPanelOrder(Long panelOrder) {
        this.panelOrder = panelOrder;
    }

    public FhirObservation getLatestObservation() {
        return latestObservation;
    }

    public void setLatestObservation(FhirObservation latestObservation) {
        this.latestObservation = latestObservation;
    }

    public Double getValueChange() {
        return valueChange;
    }

    public void setValueChange(Double valueChange) {
        this.valueChange = valueChange;
    }

    public Long getDecimalPlaces() {
        return decimalPlaces;
    }

    public void setDecimalPlaces(Long decimalPlaces) {
        this.decimalPlaces = decimalPlaces;
    }
}
