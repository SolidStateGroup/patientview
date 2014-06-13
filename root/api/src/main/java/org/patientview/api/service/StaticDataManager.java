package org.patientview.api.service;

import org.patientview.persistence.model.Feature;
import org.patientview.persistence.model.Lookup;

import java.util.List;

/**
 * Created by james@solidstategroup.com
 * Created on 05/06/2014
 */
public interface StaticDataManager {

    List<Lookup> getAllLookups() ;

    List<Feature> getAllFeatures();

}
