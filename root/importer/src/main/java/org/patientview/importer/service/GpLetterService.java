package org.patientview.importer.service;

import generated.Patientview;
import org.patientview.persistence.model.GpLetter;
import org.patientview.persistence.model.Group;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 02/02/2016
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface GpLetterService {

    void add(Patientview patientview, Group sourceGroup);

    boolean hasValidPracticeDetails(Patientview patientview);

    boolean hasValidPracticeDetailsSingleMaster(Patientview patientview);

    List<GpLetter> matchByGpDetails(Patientview patientview);
}
