package org.patientview.importer.manager;

import generated.Patientview;
import org.patientview.importer.exception.ImportResourceException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by james@solidstategroup.com
 * Created on 01/09/2014
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface ImportManager {

    public void validate(Patientview patientview) throws ImportResourceException;

    public void process(Patientview patientview) throws ImportResourceException;
}
