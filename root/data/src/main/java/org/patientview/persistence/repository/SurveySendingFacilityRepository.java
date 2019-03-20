package org.patientview.persistence.repository;

import org.patientview.persistence.model.SurveySendingFacility;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SurveySendingFacilityRepository extends CrudRepository<SurveySendingFacility, Long> {

    SurveySendingFacility findBySurveyGroup_Id(@Param("surveyGroupId") Long surveyGroupId);
}
