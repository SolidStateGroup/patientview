package org.patientview.api.controller;

import com.wordnik.swagger.annotations.ApiOperation;
import org.patientview.api.config.ExcludeFromApiDoc;
import org.patientview.api.service.StaticDataManager;
import org.patientview.persistence.model.Feature;
import org.patientview.persistence.model.Lookup;
import org.patientview.persistence.model.enums.LookupTypes;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * RESTful interface for retrieving static data such as Lookups or Features. Note: consider refactoring to remove this,
 * it is not heavily used by the front end as now storing most static data in user information retrieved after login.
 *
 * Created by james@solidstategroup.com
 * Created on 05/06/2014
 */
@ExcludeFromApiDoc
@RestController
public class StaticDataController extends BaseController<StaticDataController> {

    @Inject
    private StaticDataManager staticDataManager;

    /**
     * Get all Features, optionally by type.
     * @param featureType Optional type of Feature to retrieve
     * @param request HttpServletRequest used to determine if type is passed as request parameter
     * @return List of Feature objects
     */
    @ApiOperation(value = "Get All Features", notes = "Get all Features, optionally by type.")
    @RequestMapping(value = "/feature", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<List<Feature>> getAllFeatures(
            @RequestParam(value = "type", required = false) String featureType, HttpServletRequest request) {
        if (!request.getParameterMap().containsKey("type")) {
            return new ResponseEntity<>(staticDataManager.getAllFeatures(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(staticDataManager.getFeaturesByType(featureType), HttpStatus.OK);
        }
    }

    /**
     * Get all Lookups.
     * @return List of Lookups
     */
    @RequestMapping(value = "/lookup", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<List<Lookup>> getAllLookups() {
        return new ResponseEntity<>(staticDataManager.getAllLookups(), HttpStatus.OK);
    }

    /**
     * Get Lookups by type of Lookup.
     * @param lookupType String for type of Lookup to retrieve
     * @return Lookup object containing typically static data
     */
    @RequestMapping(value = "/lookupType/{lookupType}/lookups", method = RequestMethod.GET
            , produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<List<Lookup>> getLookupsByType(@PathVariable("lookupType") LookupTypes lookupType) {
        return new ResponseEntity<>(staticDataManager.getLookupsByType(lookupType), HttpStatus.OK);
    }

    /**
     * Get a single Lookup by type and value.
     * @param lookupType String for type of Lookup to retrieve
     * @param lookupValue String for value of Lookup to retrieve
     * @return Lookup object containing typically static data
     */
    @RequestMapping(value = "/lookupType/{lookupType}/lookups/{lookupValue}", method = RequestMethod.GET
            , produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Lookup> getLookupByTypeAndValue(@PathVariable("lookupType") LookupTypes lookupType,
                                                          @PathVariable("lookupValue") String lookupValue) {
        return new ResponseEntity<>(staticDataManager.getLookupByTypeAndValue(lookupType, lookupValue), HttpStatus.OK);
    }
}
