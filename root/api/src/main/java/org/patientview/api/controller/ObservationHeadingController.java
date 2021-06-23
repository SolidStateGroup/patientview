package org.patientview.api.controller;

import com.wordnik.swagger.annotations.ApiOperation;
import org.patientview.api.config.ExcludeFromApiDoc;
import org.patientview.api.model.BaseObservationHeading;
import org.patientview.api.model.ObservationHeadingGroup;
import org.patientview.api.service.ObservationHeadingService;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceInvalidException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.GetParameters;
import org.patientview.persistence.model.ObservationHeading;
import org.patientview.persistence.model.ResultCluster;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import java.util.List;

/**
 * Restful interface for managing ObservationHeading (result headings). Results are mapped to these ObservationHeading
 * by Code.
 *
 * Created by jamesr@solidstategroup.com
 * Created on 11/09/2014
 */
@RestController
public class ObservationHeadingController extends BaseController<ObservationHeadingController> {

    @Inject
    private ObservationHeadingService observationHeadingService;

    /**
     * Create a new ObservationHeading.
     * @param observationHeading ObservationHeading to create
     * @return ObservationHeading, newly created (note: consider only returning ID)
     */
    @ExcludeFromApiDoc
    @RequestMapping(value = "/observationheading", method = RequestMethod.POST
            , consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<ObservationHeading> add(@RequestBody ObservationHeading observationHeading) throws ResourceInvalidException {
        return new ResponseEntity<>(observationHeadingService.add(observationHeading), HttpStatus.OK);
    }

    /**
     * Add properties to assign an ObservationHeading to a Group, used when organising results summary and what order
     * results appear to a User by default for each specialty. Properties are panel and panel order on results page.
     * @param observationHeadingId ID of ObservationHeading (result type)
     * @param groupId ID of Group to configure properties for
     * @param panel Panel on which to show this result type
     * @param panelOrder Order on panel to show this result type
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @ExcludeFromApiDoc
    @RequestMapping(
            value = "/observationheading/{observationHeadingId}/group/{groupId}/panel/{panel}/panelorder/{panelOrder}",
            method = RequestMethod.POST)
    @ResponseBody
    public void addObservationHeadingGroup(@PathVariable("observationHeadingId") Long observationHeadingId,
            @PathVariable("groupId") Long groupId, @PathVariable("panel") Long panel,
            @PathVariable("panelOrder") Long panelOrder)
            throws ResourceNotFoundException, ResourceForbiddenException, ResourceInvalidException {
        observationHeadingService.addObservationHeadingGroup(observationHeadingId, groupId, panel, panelOrder);
    }

    /**
     * Get a Page of ObservationHeading given GetParameters.
     * @param getParameters GetParameters object for pagination/filter properties defined in UI, including page number,
     * size etc
     * @return Page of ObservationHeading objects
     */
    @ExcludeFromApiDoc
    @RequestMapping(value = "/observationheading", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Page<ObservationHeading>> findAllPaged(GetParameters getParameters) {
        return new ResponseEntity<>(observationHeadingService.findAll(getParameters), HttpStatus.OK);
    }

    /**
     * Get all ObservationHeading
     * @return List of all ObservationHeading
     */
    @ExcludeFromApiDoc
    @RequestMapping(value = "/observationheadings", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<List<BaseObservationHeading>> findAll() {
        return new ResponseEntity<>(observationHeadingService.findAll(), HttpStatus.OK);
    }

    @ExcludeFromApiDoc
    @RequestMapping(value = "/observationheading/code/{code}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<ObservationHeading> findByCode(@PathVariable("code") String code)
            throws ResourceNotFoundException {
        List<ObservationHeading> observationHeadings = observationHeadingService.findByCode(code);
        if (!CollectionUtils.isEmpty(observationHeadings)) {
            return new ResponseEntity<>(observationHeadings.get(0), HttpStatus.OK);
        } else {
            throw new ResourceNotFoundException();
        }
    }

    /**
     * Get an ObservationHeading.
     * @param observationHeadingId ID of ObservationHeading to get
     * @return ObservationHeading object
     * @throws ResourceNotFoundException
     */
    @ExcludeFromApiDoc
    @RequestMapping(value = "/observationheading/{observationHeadingId}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<ObservationHeading> get(@PathVariable("observationHeadingId") Long observationHeadingId)
            throws ResourceNotFoundException {
        return new ResponseEntity<>(observationHeadingService.get(observationHeadingId), HttpStatus.OK);
    }

    /**
     * Get available ObservationHeadings (result types) for a User that can be used when setting up alerts for new
     * results, typically all ObservationHeadings are returned as User can set up alerts for all result types.
     * @param userId ID of User to retrieve available ObservationHeadings for setting up alerts
     * @return List of ObservationHeading objects
     * @throws ResourceNotFoundException
     */
    @ExcludeFromApiDoc
    @RequestMapping(value = "/user/{userId}/availablealertobservationheadings", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<List<ObservationHeading>> getAvailableAlertObservationHeadings(
            @PathVariable("userId") Long userId)
            throws ResourceNotFoundException {
        return new ResponseEntity<>(
                observationHeadingService.getAvailableAlertObservationHeadings(userId), HttpStatus.OK);
    }

    /**
     * Get available ObservationHeading (result types) for a User, where results are currently available.
     * @param userId ID of User to retrieve ObservationHeadings where results are currently available
     * @return List of ObservationHeading objects
     * @throws ResourceNotFoundException
     * @throws FhirResourceException
     */
    @ApiOperation(value = "Get Available Observations Types For a User", notes = "Given a User ID "
            + "retrieve a list of available observation types for that user (where they have observation data).")
    @RequestMapping(value = "/user/{userId}/availableobservationheadings", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<List<ObservationHeading>> getAvailableObservationHeadings(@PathVariable("userId") Long userId)
            throws ResourceNotFoundException, FhirResourceException {
        return new ResponseEntity<>(observationHeadingService.getAvailableObservationHeadings(userId), HttpStatus.OK);
    }

    /**
     * Get available ObservationHeading (result types) for a User, where patient entered results
     * are currently available.
     * @param userId ID of User to retrieve ObservationHeadings for
     * @return List of ObservationHeading objects
     * @throws ResourceNotFoundException
     * @throws FhirResourceException
     */
    @ApiOperation(value = "Get Available Patient Entered Observations Types For a User", notes = "Given a User ID "
            + "retrieve a list of available observation types for that user (where they have "
            + "patient entered observation data).")
    @RequestMapping(value = "/user/{userId}/patiententeredobservationheadings", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<List<ObservationHeading>> getPatientEnteredObservationHeadings(
            @PathVariable("userId") Long userId) throws ResourceNotFoundException, FhirResourceException {
        return new ResponseEntity<>(
                observationHeadingService.getPatientEnteredObservationHeadings(userId), HttpStatus.OK);
    }

    /**
     * Get the available ResultCluster (groups of result types available to Users when entering their own results).
     * @return List of ResultCluster objects
     */
    @ExcludeFromApiDoc
    @RequestMapping(value = "/resultclusters", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<List<ResultCluster>> getResultClusters() {
        return new ResponseEntity<>(observationHeadingService.getResultClusters(), HttpStatus.OK);
    }

    /**
     * Get a List of the User selected ObservationHeadings, used in results table view when Users choose which results
     * to show in the table.
     * @param userId ID of User to retrieve saved ObservationHeadings as specified in results table view
     * @return List of ObservationHeading objects
     * @throws ResourceNotFoundException
     * @throws FhirResourceException
     */
    @ExcludeFromApiDoc
    @RequestMapping(value = "/user/{userId}/savedobservationheadings", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<List<ObservationHeading>> getSavedObservationHeadings(@PathVariable("userId") Long userId)
            throws ResourceNotFoundException, FhirResourceException {
        return new ResponseEntity<>(observationHeadingService.getSavedObservationHeadings(userId), HttpStatus.OK);
    }

    /**
     * Remove the relationship between an ObservationHeading and a specific Group (specialty), used when setting up the
     * results panel and panel order for the default results view.
     * @param observationHeadingGroupId ID of the ObservationHeadingGroup to remove
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @ExcludeFromApiDoc
    @RequestMapping(value = "/observationheadinggroup/{observationHeadingGroupId}", method = RequestMethod.DELETE)
    @ResponseBody
    public void removeObservationHeadingGroup(@PathVariable("observationHeadingGroupId") Long observationHeadingGroupId)
            throws ResourceNotFoundException, ResourceForbiddenException {
        observationHeadingService.removeObservationHeadingGroup(observationHeadingGroupId);
    }

    /**
     * Update an ObservationHeading.
     * @param observationHeading ObservationHeading to update
     * @throws ResourceNotFoundException
     */
    @ExcludeFromApiDoc
    @RequestMapping(value = "/observationheading", method = RequestMethod.PUT)
    @ResponseBody
    public void save(@RequestBody ObservationHeading observationHeading)
            throws ResourceNotFoundException, ResourceInvalidException {
        observationHeadingService.save(observationHeading);
    }

    /**
     * Store a User's selection of ObservationHeadings to show in the results table view.
     * @param userId ID of User for which a selection of ObservationHeadings are to be saved
     * @param codes List of Codes representing the ObservationHeadings to be associated with a User's results table view
     * @throws ResourceNotFoundException
     */
    @ExcludeFromApiDoc
    @RequestMapping(value = "/user/{userId}/saveobservationheadingselection", method = RequestMethod.POST)
    @ResponseBody
    public void saveObservationHeadingSelection(@PathVariable("userId") Long userId, @RequestBody String[] codes)
            throws ResourceNotFoundException {
        observationHeadingService.saveObservationHeadingSelection(userId, codes);
    }

    /**
     * Update an ObservationHeadingGroup representing Group specific panel, panel order properties for
     * ObservationHeadings shown on the default results view.
     * @param observationHeadingGroup ObservationHeadingGroup to update
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @ExcludeFromApiDoc
    @RequestMapping(value = "/observationheadinggroup", method = RequestMethod.PUT)
    @ResponseBody
    public void updateObservationHeadingGroup(@RequestBody ObservationHeadingGroup observationHeadingGroup)
            throws ResourceNotFoundException, ResourceForbiddenException, ResourceInvalidException {
        observationHeadingService.updateObservationHeadingGroup(observationHeadingGroup);
    }
}
