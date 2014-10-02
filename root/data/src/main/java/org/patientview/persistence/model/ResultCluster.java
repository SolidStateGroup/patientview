package org.patientview.persistence.model;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 05/06/2014
 */
@Entity
@Table(name = "pv_result_cluster")
public class ResultCluster extends BaseModel {

    @Column(name = "name")
    private String name;

    @ManyToMany(cascade = CascadeType.MERGE)
    @JoinTable(name="pv_result_cluster_observation_heading",
            joinColumns = @JoinColumn(name="result_cluster_id", referencedColumnName="id"),
            inverseJoinColumns = @JoinColumn(name="observation_heading_id", referencedColumnName="id"))
    private Set<ObservationHeading> observationHeadings = new HashSet<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<ObservationHeading> getObservationHeadings() {
        return observationHeadings;
    }

    public void setObservationHeadings(Set<ObservationHeading> observationHeadings) {
        this.observationHeadings = observationHeadings;
    }
}
