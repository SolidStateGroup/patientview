package org.patientview.api.service;

import org.patientview.api.annotation.UserOnly;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.SymptomScore;

import java.util.List;

/**
 * SymptomScore service, used by IBD
 *
 * Created by jamesr@solidstategroup.com
 * Created on 05/06/2015
 */
public interface SymptomScoreService {

    @UserOnly
    List<SymptomScore> getByUserId(Long userId) throws ResourceNotFoundException;
}
