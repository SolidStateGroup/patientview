package org.patientview.api.service.impl;

import org.patientview.api.service.StaticDataManager;
import org.patientview.persistence.model.Feature;
import org.patientview.persistence.model.Lookup;
import org.patientview.persistence.model.LookupType;
import org.patientview.persistence.model.enums.LookupTypes;
import org.patientview.persistence.repository.FeatureRepository;
import org.patientview.persistence.repository.LookupRepository;
import org.patientview.persistence.repository.LookupTypeRepository;
import org.patientview.util.Util;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;

/**
 * Created by james@solidstategroup.com
 * Created on 05/06/2014
 *
 * TODO name change and refactor
 */
@Service
@Transactional(propagation = Propagation.REQUIRES_NEW)
public class StaticDataManagerImpl extends AbstractServiceImpl<StaticDataManagerImpl>
        implements StaticDataManager {
    @Inject
    private LookupRepository lookupRepository;

    @Inject
    private LookupTypeRepository lookupTypeRepository;

    @Inject
    private FeatureRepository featureRepository;

    @Override
    @Cacheable(value = "getAllLookups")
    public List<Lookup> getAllLookups() {
        return Util.convertIterable(lookupRepository.findAll());
    }

    @Override
    @Cacheable(value = "getAllFeatures")
    public List<Feature> getAllFeatures() {
        return Util.convertIterable(featureRepository.findAll());
    }

    @Override
    @Cacheable(value = "getLookupsByType")
    public List<Lookup> getLookupsByType(LookupTypes type) {
        LookupType lookupType = lookupTypeRepository.findByType(type);
        if (lookupType != null) {
            return Util.convertIterable(lookupRepository.findByType(lookupType));
        }
        return Collections.emptyList();
    }

    @Override
    @Cacheable(value = "getLookupByTypeAndValue")
    public Lookup getLookupByTypeAndValue(LookupTypes type, String value) {
        return lookupRepository.findByTypeAndValue(type, value);
    }

    @Override
    @Cacheable(value = "getFeaturesByType")
    public List<Feature> getFeaturesByType(String featureType) {
        Lookup lookup = lookupRepository.findByTypeAndValue(LookupTypes.FEATURE_TYPE, featureType);
        if (lookup != null) {
            return Util.convertIterable(featureRepository.findByType(lookup));
        }
        return Collections.emptyList();
    }
}
