package org.patientview.persistence.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import java.util.Date;

/**
 * Models a research study criteria that is available to a user
 */
@Entity
@Getter
@Setter
@Table(name = "pv_research_study")
public class ResearchStudy extends BaseModel {
    @Column(name = "name")
    private String researchName;

    @Column(name = "description")
    private String description;

    @Column(name = "created_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdDate;

    @OneToOne
    @JoinColumn(name = "created_by")
    private User creator;

    @Column(name = "last_update_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastUpdate;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_updated_by")
    private User lastUpdater;

    @Column(name = "available_from")
    @Temporal(TemporalType.TIMESTAMP)
    private Date availableFrom;

    @Column(name = "available_to")
    @Temporal(TemporalType.TIMESTAMP)
    private Date availableTo;

    @Column(name = "contact_address")
    private String contactAddress;

    @Column(name = "contact_email")
    private String contactEmail;

    @Column(name = "contact_name")
    private String contactName;

    @Column(name = "contact_phone")
    private String contactPhone;

    @Transient
    private ResearchStudyCriteria[] criteria;
}
