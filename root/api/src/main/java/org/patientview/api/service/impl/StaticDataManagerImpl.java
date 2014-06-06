package org.patientview.api.service.impl;

import org.patientview.api.service.StaticDataManager;
import org.patientview.api.util.Util;
import org.patientview.persistence.model.Feature;
import org.patientview.persistence.model.GroupFeature;
import org.patientview.persistence.model.Lookup;
import org.patientview.persistence.repository.FeatureRepository;
import org.patientview.persistence.repository.GroupFeatureRepository;
import org.patientview.persistence.repository.LookupRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.List;

/**
 * Created by james@solidstategroup.com
 * Created on 05/06/2014
 */
@Service
@Transactional(propagation = Propagation.REQUIRES_NEW)
public class StaticDataManagerImpl implements StaticDataManager {

    @Inject
    private LookupRepository lookupRepository;

    @Inject
    private FeatureRepository featureRepository;

    @Inject
    private GroupFeatureRepository groupFeatureRepository;

    public List<Lookup> getAllLookups() {
        return Util.iterableToList(lookupRepository.findAll());
    }

    public List<Feature> getAllFeatures() {
        return Util.iterableToList(featureRepository.findAll());
    }

    public GroupFeature createGroupFeature(GroupFeature groupFeature) {
        return groupFeatureRepository.save(groupFeature);
    }
}
