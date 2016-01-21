package org.patientview.api.model;

import java.util.Date;

/**
 * Import Alert, used to show staff if imports failed in last week
 * Created by jamesr@solidstategroup.com
 * Created on 21/01/2016
 */
public class ImportAlert {

    private BaseGroup group;
    private Long failedImports;
    private Date since;

    public ImportAlert(org.patientview.persistence.model.Group group, Long failedImports, Date since) {
        this.group = new BaseGroup(group);
        this.failedImports = failedImports;
        this.since = since;
    }

    public ImportAlert(Group group, Long failedImports, Date since) {
        this.group = new BaseGroup(group);
        this.failedImports = failedImports;
        this.since = since;
    }

    public BaseGroup getGroup() {
        return group;
    }

    public void setGroup(BaseGroup group) {
        this.group = group;
    }

    public Long getFailedImports() {
        return failedImports;
    }

    public void setFailedImports(Long failedImports) {
        this.failedImports = failedImports;
    }

    public Date getSince() {
        return since;
    }

    public void setSince(Date since) {
        this.since = since;
    }
}
