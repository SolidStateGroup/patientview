package org.patientview.persistence.model;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 11/09/2014
 */
@Entity
@Table(name = "pv_observation_heading")
public class ObservationHeading extends AuditModel {

    @Column(name = "code")
    private String code;

    @Column(name = "heading")
    private String heading;

    @Column(name = "name")
    private String name;

    @Column(name = "normal_range")
    private String normalRange;

    @Column(name = "units")
    private String units;

    @Column(name = "min_graph", columnDefinition="numeric", precision=19, scale=2)
    private Double minGraph;

    @Column(name = "max_graph", columnDefinition="numeric", precision=19, scale=2)
    private Double maxGraph;

    @Column(name = "info_link")
    private String infoLink;

    @Column(name = "default_panel")
    private Long defaultPanel;

    @Column(name = "default_panel_order")
    private Long defaultPanelOrder;

    @OneToMany(mappedBy = "observationHeading", cascade = {CascadeType.ALL})
    private Set<ObservationHeadingGroup> observationHeadingGroups = new HashSet<>();

    public ObservationHeading() {

    }

    public ObservationHeading(Long id, String code, String heading, String name, String normalRange, String units,
                              Double minGraph, Double maxGraph) {
        setId(id);
        setCode(code);
        setHeading(heading);
        setName(name);
        setNormalRange(normalRange);
        setUnits(units);
        setMinGraph(minGraph);
        setMaxGraph(maxGraph);
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

    public Long getDefaultPanel() {
        return defaultPanel;
    }

    public void setDefaultPanel(Long defaultPanel) {
        this.defaultPanel = defaultPanel;
    }

    public Long getDefaultPanelOrder() {
        return defaultPanelOrder;
    }

    public void setDefaultPanelOrder(Long defaultPanelOrder) {
        this.defaultPanelOrder = defaultPanelOrder;
    }

    public Set<ObservationHeadingGroup> getObservationHeadingGroups() {
        return observationHeadingGroups;
    }

    public void setObservationHeadingGroups(Set<ObservationHeadingGroup> observationHeadingGroups) {
        this.observationHeadingGroups = observationHeadingGroups;
    }
}
