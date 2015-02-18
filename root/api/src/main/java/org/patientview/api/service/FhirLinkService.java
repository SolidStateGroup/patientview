package org.patientview.api.service;

import org.patientview.persistence.model.FhirLink;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * FhirLink service, handling standard CRUD operations.
 *
 * Created by jamesr@solidstategroup.com
 * Created on 12/09/2014
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface FhirLinkService extends CrudService<FhirLink> {

}
