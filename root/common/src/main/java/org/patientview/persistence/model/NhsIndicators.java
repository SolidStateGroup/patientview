package org.patientview.persistence.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 16/09/16
 */
@Entity
@Table(name = "pv_nhs_indicators")
public class NhsIndicators extends BaseModel {

    @Column(name = "group_id", nullable = false)
    private Long groupId;

    @Column(name = "creation_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date created = new Date();

    // JSON string of data
    @Column(name = "data", nullable = false)
    private String data;

    public NhsIndicators() { }

    public NhsIndicators(Long groupId, String data) {
        setGroupId(groupId);
        setData(data);
    }

    public NhsIndicators(Long groupId, String data, Date created) {
        setGroupId(groupId);
        setData(data);
        setCreated(created);
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
