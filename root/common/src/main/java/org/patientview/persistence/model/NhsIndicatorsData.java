package org.patientview.persistence.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Transient class used when for NhsIndicators data
 * Created by jamesr@solidstategroup.com
 * Created on 16/09/16
 */
public class NhsIndicatorsData {

    Map<String, List<String>> indicatorCodeMap = new HashMap<>();
    Map<String, Long> indicatorCount = new HashMap<>();

    public NhsIndicatorsData() { }

    @JsonCreator
    public static NhsIndicatorsData createFromString(String jsonString) throws IOException {
        return new ObjectMapper().readValue(jsonString, NhsIndicatorsData.class);
    }

    public NhsIndicatorsData(Map<String, Long> indicatorCount, Map<String, List<String>> indicatorCodeMap) {
        this.indicatorCount = indicatorCount;
        this.indicatorCodeMap = indicatorCodeMap;
    }

    public Map<String, Long> getIndicatorCount() {
        return indicatorCount;
    }

    public void setIndicatorCount(Map<String, Long> indicatorCount) {
        this.indicatorCount = indicatorCount;
    }

    public Map<String, List<String>> getIndicatorCodeMap() {
        return indicatorCodeMap;
    }

    public void setIndicatorCodeMap(Map<String, List<String>> indicatorCodeMap) {
        this.indicatorCodeMap = indicatorCodeMap;
    }
}
