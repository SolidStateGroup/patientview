package org.patientview.api.service.impl;

import org.patientview.api.service.LookupService;
import org.patientview.persistence.model.Lookup;
import org.patientview.persistence.model.LookupType;
import org.patientview.persistence.model.enums.LookupTypes;
import org.patientview.persistence.model.enums.LookupTypesPatientManagement;
import org.patientview.persistence.repository.LookupRepository;
import org.patientview.persistence.repository.LookupTypeRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by james@solidstategroup.com
 * Created on 08/09/2014
 */
@Service
public class LookupServiceImpl implements LookupService {

    @Inject
    private LookupRepository lookupRepository;

    @Inject
    private LookupTypeRepository lookupTypeRepository;

    @Override
    public List<org.patientview.api.model.LookupType> getPatientManagementLookupTypes() {
        List<org.patientview.api.model.LookupType> types = new ArrayList<>();

        for (LookupTypesPatientManagement type : LookupTypesPatientManagement.values()) {
            LookupType lookupType = lookupTypeRepository.findByType(LookupTypes.valueOf(type.toString()));
            if (lookupType != null) {
                types.add(new org.patientview.api.model.LookupType(lookupType));
            }
        }

        return types;
    }

    @Override
    @Cacheable(value = "findLookupByTypeAndValue")
    public Lookup findByTypeAndValue(final LookupTypes lookupType, final String lookupValue) {
        return lookupRepository.findByTypeAndValue(lookupType, lookupValue);
    }
}
