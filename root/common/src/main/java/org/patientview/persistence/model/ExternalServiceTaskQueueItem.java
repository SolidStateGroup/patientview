package org.patientview.persistence.model;

import org.patientview.persistence.model.enums.ExternalServiceTaskQueueStatus;
import org.patientview.persistence.model.enums.ExternalServices;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import java.util.Date;

/**
 * External service task queue item, used when sending data to external services periodically using cron job.
 *
 * Created by james@solidstategroup.com
 * Created on 30/04/2015
 */
@Entity
@Table(name = "pv_external_service_task_queue_item")
public class ExternalServiceTaskQueueItem extends AuditModel {

    @Column(name = "url")
    private String url;

    @Column(name = "method")
    private String method;

    @Column(name = "content")
    private String content;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private ExternalServiceTaskQueueStatus status;

    @Column(name = "service_type")
    @Enumerated(EnumType.STRING)
    private ExternalServices serviceType;

    @Column(name = "response_code")
    private Integer responseCode;

    @Column(name = "response_reason")
    private String responseReason;

    public ExternalServiceTaskQueueItem() {}

    public ExternalServiceTaskQueueItem(String url,
                                        String method,
                                        String content,
                                        ExternalServiceTaskQueueStatus status,
                                        ExternalServices serviceType,
                                        User creator,
                                        Date creationDate) {
        this.url = url;
        this.method = method;
        this.content = content;
        this.status = status;
        this.serviceType = serviceType;
        this.setCreator(creator);
        this.setCreated(creationDate);
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public ExternalServices getServiceType() {
        return serviceType;
    }

    public void setServiceType(ExternalServices serviceType) {
        this.serviceType = serviceType;
    }

    public ExternalServiceTaskQueueStatus getStatus() {
        return status;
    }

    public void setStatus(ExternalServiceTaskQueueStatus status) {
        this.status = status;
    }

    public Integer getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(Integer responseCode) {
        this.responseCode = responseCode;
    }

    public String getResponseReason() {
        return responseReason;
    }

    public void setResponseReason(String responseReason) {
        this.responseReason = responseReason;
    }
}
