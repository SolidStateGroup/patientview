package org.patientview.persistence.model;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 05/06/2014
 */
@Entity
@Table(name = "pv_result_cluster")
public class ResultCluster extends BaseModel {

    @Column(name = "name")
    private String name;

    @OneToMany(mappedBy = "resultCluster", cascade = {CascadeType.ALL})
    @OrderBy("order asc")
    private List<ResultClusterObservationHeading> resultClusterObservationHeadings = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ResultClusterObservationHeading> getResultClusterObservationHeadings() {
        return resultClusterObservationHeadings;
    }

    public void setResultClusterObservationHeadings(List<ResultClusterObservationHeading> resultClusterObservationHeadings) {
        this.resultClusterObservationHeadings = resultClusterObservationHeadings;
    }
}
