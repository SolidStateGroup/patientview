package org.patientview.api.model;

import org.patientview.persistence.model.Code;
import org.patientview.persistence.model.NhsIndicatorsData;

import java.util.HashMap;
import java.util.Map;

/**
 * NhsIndicators, representing NHS Indicators for a Group.
 * Created by jamesr@solidstategroup.com
 * Created on 06/10/2016
 */
public class NhsIndicators {

    private Long groupId;

    private NhsIndicatorsData data = new NhsIndicatorsData();

    private Map<String, Code> codeMap = new HashMap<>();

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

    public NhsIndicatorsData getData() {
        return data;
    }

    public void setData(NhsIndicatorsData data) {
        this.data = data;
    }

    public Map<String, Code> getCodeMap() {
        return codeMap;
    }

    public void setCodeMap(Map<String, Code> codeMap) {
        this.codeMap = codeMap;
    }
}
