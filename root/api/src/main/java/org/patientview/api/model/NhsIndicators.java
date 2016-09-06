package org.patientview.api.model;

/**
 * NhsIndicators, representing NHS Indicators for a Group.
 * Created by jamesr@solidstategroup.com
 * Created on 06/10/2016
 */
public class NhsIndicators {

    private Long groupId;

    public NhsIndicators() {
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }
}
