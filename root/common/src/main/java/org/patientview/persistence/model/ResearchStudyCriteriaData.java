package org.patientview.persistence.model;

import lombok.Getter;
import lombok.Setter;

/**
 * Models a research study that is available to a user
 */

@Getter
@Setter
public class ResearchStudyCriteriaData {

    private Long groupId;
    private Long treatmentId;
    private String gender;
    private Integer fromAge;
    private Integer toAge;
}
