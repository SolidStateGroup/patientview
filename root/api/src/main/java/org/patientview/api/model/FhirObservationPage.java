package org.patientview.api.model;

import java.util.List;
import java.util.Map;

/**
 * FhirObservationPage, representing a Page of Observations, used when returning Observations searched for by code.
 * Created by jamesr@solidstategroup.com
 * Created on 28/10/2014
 */
public class FhirObservationPage {

    private Long totalElements;
    private Long totalPages;
    private Map<Long, Map<String, List<FhirObservation>>> data;

    public FhirObservationPage() {
    }


    public FhirObservationPage(Map<Long, Map<String, List<FhirObservation>>> data, Long totalElements,
                               Long totalPages) {
        this.data = data;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
    }

    public Map<Long, Map<String, List<FhirObservation>>> getData() {
        return data;
    }

    public void setData(Map<Long, Map<String, List<FhirObservation>>> data) {
        this.data = data;
    }

    public Long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(Long totalElements) {
        this.totalElements = totalElements;
    }

    public Long getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(Long totalPages) {
        this.totalPages = totalPages;
    }
}
