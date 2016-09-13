package org.patientview.api.model;

import org.patientview.persistence.model.Code;

import java.util.HashMap;
import java.util.Map;

/**
 * NhsIndicators, representing NHS Indicators for a Group.
 * Created by jamesr@solidstategroup.com
 * Created on 06/10/2016
 */
public class NhsIndicators {

    private Long groupId;

    private Map<String, Long> codeCount = new HashMap<>();

    private Map<String, Code> codeMap = new HashMap<>();

    public NhsIndicators() {
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public Map<String, Long> getCodeCount() {
        return codeCount;
    }

    public void setCodeCount(Map<String, Long> codeCount) {
        this.codeCount = codeCount;
    }

    public Map<String, Code> getCodeMap() {
        return codeMap;
    }

    public void setCodeMap(Map<String, Code> codeMap) {
        this.codeMap = codeMap;
    }
}
