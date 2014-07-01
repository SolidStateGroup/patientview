package org.patientview.api.service.impl;

import org.patientview.api.service.StaticDataManager;
import org.patientview.api.util.Util;
import org.patientview.persistence.model.Feature;
import org.patientview.persistence.model.Lookup;
import org.patientview.persistence.model.LookupType;
import org.patientview.persistence.repository.FeatureRepository;
import org.patientview.persistence.repository.LookupRepository;
import org.patientview.persistence.repository.LookupTypeRepository;
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
public class StaticDataManagerImpl implements StaticDataManager {

    @Inject
    private LookupRepository lookupRepository;
    @Inject
    private LookupTypeRepository lookupTypeRepository;
    @Inject
    private FeatureRepository featureRepository;

    public List<Lookup> getAllLookups() {
        return Util.iterableToList(lookupRepository.findAll());
    }

    public List<Feature> getAllFeatures() {
        return Util.iterableToList(featureRepository.findAll());
    }

    public List<Lookup> getLookupsByType(String type) {
        LookupType lookupType = lookupTypeRepository.getByType(type);
        if (lookupType != null) {
            return Util.iterableToList(lookupRepository.getByLookupType(lookupType));
        }
        return Collections.<Lookup>emptyList();
    }

    public List<Feature> getFeaturesByType(String featureType) {
        List<Lookup> lookups = Util.iterableToList(lookupRepository.getByLookupTypeAndValue("FEATURE_TYPE", featureType));
        if (!lookups.isEmpty()) {
            return Util.iterableToList(featureRepository.findByType(lookups.get(0)));
        }
        return Collections.<Feature>emptyList();
    }
}
