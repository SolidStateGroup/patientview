package org.patientview.api.service;

import org.patientview.persistence.model.Feature;
import org.patientview.persistence.model.Lookup;
import org.patientview.persistence.model.enums.LookupTypes;

import java.util.List;

/**
 * Created by james@solidstategroup.com
 * Created on 05/06/2014
 */
public interface StaticDataManager {

    List<Lookup> getAllLookups();

    List<Lookup> getLookupsByType(LookupTypes lookupType);

    Lookup getLookupByTypeAndValue(LookupTypes lookupType, String lookupValue);

    List<Feature> getAllFeatures();

    List<Feature> getFeaturesByType(String featureType);

}
