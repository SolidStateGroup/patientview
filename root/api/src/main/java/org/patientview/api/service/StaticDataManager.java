package org.patientview.api.service;

import org.patientview.persistence.model.Feature;
import org.patientview.persistence.model.Lookup;
import org.patientview.persistence.model.enums.LookupTypes;

import java.util.List;

/**
 * Static data manager for retrieving Lookups and Features.
 *
 * Created by james@solidstategroup.com
 * Created on 05/06/2014
 */
public interface StaticDataManager {

    /**
     * Get all Lookups.
     * @return List of Lookups
     */
    List<Lookup> getAllLookups();

    /**
     * Get all Features, optionally by type.
     * @return List of Feature objects
     */
    List<Feature> getAllFeatures();

    /**
     * Get all Features by type.
     * @param featureType Type of Feature to retrieve
     * @return List of Feature objects
     */
    List<Feature> getFeaturesByType(String featureType);

    /**
     * Get Lookups by type of Lookup.
     * @param lookupType String for type of Lookup to retrieve
     * @return Lookup object containing typically static data
     */
    List<Lookup> getLookupsByType(LookupTypes lookupType);

    /**
     * Get a single Lookup by type and value.
     * @param lookupType String for type of Lookup to retrieve
     * @param lookupValue String for value of Lookup to retrieve
     * @return Lookup object containing typically static data
     */
    Lookup getLookupByTypeAndValue(LookupTypes lookupType, String lookupValue);
}
