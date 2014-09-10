package org.patientview.api.service.impl;

import org.patientview.api.service.LookupService;
import org.patientview.persistence.model.Lookup;
import org.patientview.persistence.model.enums.LookupTypes;
import org.patientview.persistence.repository.LookupRepository;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

/**
 * Created by james@solidstategroup.com
 * Created on 08/09/2014
 */
@Service
public class LookupServiceImpl implements LookupService {

    @Inject
    private LookupRepository lookupRepository;

    public Lookup findByTypeAndValue(final LookupTypes lookupType, final String lookupValue) {
        return lookupRepository.findByTypeAndValue(lookupType, lookupValue);
    }
}
