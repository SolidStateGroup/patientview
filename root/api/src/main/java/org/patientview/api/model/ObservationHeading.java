package org.patientview.api.model;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 12/09/2014
 */
public class ObservationHeading {

    private Long id;
    private String code;
    private String heading;
    private String name;
    private String normalRange;
    private String units;
    private Double minGraph;
    private Double maxGraph;
    private String infoLink;
    private Long panel;
    private Long panelOrder;
    private Long decimalPlaces;

    // used when retrieving results
    private FhirObservation latestObservation;
    private Double valueChange;

    public ObservationHeading(org.patientview.persistence.model.ObservationHeading observationHeading) {
        this.id = observationHeading.getId();
        this.code = observationHeading.getCode();
        this.heading = observationHeading.getHeading();
        this.name = observationHeading.getName();
        this.normalRange = observationHeading.getNormalRange();
        this.units = observationHeading.getUnits();
        this.minGraph = observationHeading.getMinGraph();
        this.maxGraph = observationHeading.getMaxGraph();
        this.infoLink = observationHeading.getInfoLink();
        this.panel = observationHeading.getDefaultPanel();
        this.panelOrder = observationHeading.getDefaultPanelOrder();
        this.decimalPlaces = observationHeading.getDecimalPlaces();
    }

    public ObservationHeading() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getHeading() {
        return heading;
    }

    public void setHeading(String heading) {
        this.heading = heading;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNormalRange() {
        return normalRange;
    }

    public void setNormalRange(String normalRange) {
        this.normalRange = normalRange;
    }

    public String getUnits() {
        return units;
    }

    public void setUnits(String units) {
        this.units = units;
    }

    public Double getMinGraph() {
        return minGraph;
    }

    public void setMinGraph(Double minGraph) {
        this.minGraph = minGraph;
    }

    public Double getMaxGraph() {
        return maxGraph;
    }

    public void setMaxGraph(Double maxGraph) {
        this.maxGraph = maxGraph;
    }

    public String getInfoLink() {
        return infoLink;
    }

    public void setInfoLink(String infoLink) {
        this.infoLink = infoLink;
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
