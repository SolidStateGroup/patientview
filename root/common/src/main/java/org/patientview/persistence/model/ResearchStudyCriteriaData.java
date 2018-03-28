package org.patientview.persistence.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Transient;
import java.util.List;

/**
 * Models a research study that is available to a user
 */

@Getter
@Setter
public class ResearchStudyCriteriaData {

    private Long[] groupIds;
    private Long[] treatmentIds;
    private Long[] diagnosisIds;
    private String gender;
    private Integer fromAge;
    private Integer toAge;

    @Transient
    private transient List groups;
    @Transient
    private transient List treatments;
    @Transient
    private transient List diagnosis;
}
