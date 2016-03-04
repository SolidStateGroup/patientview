package org.patientview.api.model;

/**
 * ObservationHeading, representing an Observation (result) type, including meta information.
 * Created by jamesr@solidstategroup.com
 * Created on 12/09/2014
 */
public class ObservationHeading extends BaseObservationHeading{
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
