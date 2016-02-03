package org.patientview.persistence.model;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 02/02/2016
 */
@Entity
@Table(name = "pv_gp_letter")
public class GpLetter extends AuditModel {

    @Column(name = "claimed_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date claimedDate;

    @Column(name = "gp_name")
    private String gpName;

    @OneToOne(cascade = CascadeType.MERGE, fetch = FetchType.EAGER)
    @JoinColumn(name = "group_id")
    private Group group;

    public GpLetter() { }

    public Date getClaimedDate() {
        return claimedDate;
    }

    public void setClaimedDate(Date claimedDate) {
        this.claimedDate = claimedDate;
    }

    public String getGpName() {
        return gpName;
    }

    public void setGpName(String gpName) {
        this.gpName = gpName;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }
}
