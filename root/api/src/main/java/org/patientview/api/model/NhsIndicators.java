package org.patientview.api.model;

import org.patientview.persistence.model.Code;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * NhsIndicators, representing NHS Indicators for a Group.
 * Created by jamesr@solidstategroup.com
 * Created on 06/10/2016
 */
public class NhsIndicators {

    private Long groupId;

    private Map<String, Long> codeCount = new HashMap<>();

    private Map<String, List<Code>> codeMap = new HashMap<>();

    public NhsIndicators() {
    }

    public NhsIndicators(Long groupId) {
        setGroupId(groupId);
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

    public Map<String, List<Code>> getCodeMap() {
        return codeMap;
    }

    public void setCodeMap(Map<String, List<Code>> codeMap) {
        this.codeMap = codeMap;
    }
}
