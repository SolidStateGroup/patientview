package org.patientview.api.model;

/**
 * BaseObservationHeading, representing an Observation (result) type, including meta information. Reduced data.
 * Created by jamesr@solidstategroup.com
 * Created on 12/09/2014
 */
public class BaseObservationHeading {

    private Long id;
    private String code;
    private String heading;
    private String name;
    private String normalRange;
    private String units;
    private Double minGraph;
    private Double maxGraph;
    private String infoLink;

    public BaseObservationHeading() {
    }

    public BaseObservationHeading(org.patientview.persistence.model.ObservationHeading observationHeading) {
        this.id = observationHeading.getId();
        this.code = observationHeading.getCode();
        this.heading = observationHeading.getHeading();
        this.name = observationHeading.getName();
        this.normalRange = observationHeading.getNormalRange();
        this.units = observationHeading.getUnits();
        this.minGraph = observationHeading.getMinGraph();
        this.maxGraph = observationHeading.getMaxGraph();
        this.infoLink = observationHeading.getInfoLink();
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
}
