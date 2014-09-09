package org.patientview.importer.service;

import generated.Patientview;
import org.hl7.fhir.instance.model.ResourceReference;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 05/09/2014
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface ConditionService {

    public void add(Patientview data, ResourceReference patientReference);

}
