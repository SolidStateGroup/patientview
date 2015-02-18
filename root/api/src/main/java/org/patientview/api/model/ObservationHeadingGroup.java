package org.patientview.api.model;

/**
 * ObservationHeadingGroup, representing the relationship between an ObservationHeading and a Group (specialty), used
 * when determining panel and panel order of results in the default results view.
 * Created by jamesr@solidstategroup.com
 * Created on 17/09/2014
 */
public class ObservationHeadingGroup {

    private Long id;
    private Long observationHeadingId;
    private Long groupId;
    private Long panel;
    private Long panelOrder;

    public ObservationHeadingGroup() {
    }

    public ObservationHeadingGroup(org.patientview.persistence.model.ObservationHeadingGroup observationHeadingGroup) {
        setId(observationHeadingGroup.getId());
        setObservationHeadingId(observationHeadingGroup.getObservationHeading().getId());
        setGroupId(observationHeadingGroup.getGroup().getId());
        setPanel(observationHeadingGroup.getPanel());
        setPanelOrder(observationHeadingGroup.getPanelOrder());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getObservationHeadingId() {
        return observationHeadingId;
    }

    public void setObservationHeadingId(Long observationHeadingId) {
        this.observationHeadingId = observationHeadingId;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
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
}
