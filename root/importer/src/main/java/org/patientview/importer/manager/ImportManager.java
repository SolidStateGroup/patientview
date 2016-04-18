package org.patientview.importer.manager;

import generated.Patientview;
import generated.Survey;
import org.patientview.config.exception.ImportResourceException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by james@solidstategroup.com
 * Created on 01/09/2014
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface ImportManager {

    void process(Patientview patientview, String xml, Long importerUserId) throws ImportResourceException;

    void process(Survey survey, String xml, Long importerUserId) throws ImportResourceException;

    void validate(Patientview patientview) throws ImportResourceException;

    void validate(Survey survey) throws ImportResourceException;
}
