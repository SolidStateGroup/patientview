package org.patientview.api.model;

import java.util.HashMap;
import java.util.List;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 12/09/2014
 */
public class ObservationSummary {
    private Group group;
    private HashMap<Long, List<ObservationHeading>> panels = new HashMap<>();

    public ObservationSummary () {

    }

    public HashMap<Long, List<ObservationHeading>> getPanels() {
        return panels;
    }

    public void setPanels(HashMap<Long, List<ObservationHeading>> panels) {
        this.panels = panels;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }
}
