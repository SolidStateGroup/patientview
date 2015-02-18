package org.patientview.api.service;

import org.patientview.persistence.model.Lookup;
import org.patientview.persistence.model.enums.LookupTypes;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Lookup service, used to get Lookups, referenced by other objects for static data. Note that newer code uses
 * Enums more than Lookups.
 *
 * Created by jamesr@solidstategroup.com
 * Created on 08/09/2014
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface LookupService {

    /**
     * Get a Lookup given the LookupTypes type and value.
     * @param lookupType LookupTypes type of Lookup, e.g. LookupTypes.CODE_TYPE
     * @param lookupValue String value of Lookup
     * @return Lookup object
     */
    Lookup findByTypeAndValue(LookupTypes lookupType, String lookupValue);
}
