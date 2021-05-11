package org.patientview.api.service;

import org.patientview.api.annotation.RoleOnly;
import org.patientview.api.annotation.UserOnly;
import org.patientview.api.model.BaseObservationHeading;
import org.patientview.api.model.ObservationHeadingGroup;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceInvalidException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.GetParameters;
import org.patientview.persistence.model.ObservationHeading;
import org.patientview.persistence.model.ResultCluster;
import org.patientview.persistence.model.enums.RoleName;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * ObservationHeading service, for managing result types and visibility of results for patients when viewing or entering
 * their own results.
 *
 * Created by jamesr@solidstategroup.com
 * Created on 11/09/2014
 */
public interface ObservationHeadingService extends CrudService<ObservationHeading> {

    /**
     * Create a new ObservationHeading.
     * @param observationHeading ObservationHeading to create
     * @return ObservationHeading, newly created (note: consider only returning ID)
     */
    @RoleOnly
    ObservationHeading add(ObservationHeading observationHeading) throws ResourceInvalidException;

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
    @RoleOnly(roles = { RoleName.SPECIALTY_ADMIN })
    void addObservationHeadingGroup(Long observationHeadingId, Long groupId, Long panel, Long panelOrder)
    throws ResourceNotFoundException, ResourceForbiddenException;

    /**
     * Get all ObservationHeading
     * @return List of all ObservationHeading
     */
    List<BaseObservationHeading> findAll();

    /**
     * Get a Page of ObservationHeading given GetParameters.
     * @param getParameters GetParameters object for pagination/filter properties defined in UI, including page number,
     * size etc
     * @return Page of ObservationHeading objects
     */
    Page<ObservationHeading> findAll(GetParameters getParameters);

    /**
     * Get a List of ObservationHeading given a String code, should only return one as code should be unique.
     * @param code String code of Observation Headings to retrieve
     * @return List of ObservationHeadings
     */
    List<ObservationHeading> findByCode(String code);

    /**
     * Get an ObservationHeading.
     * @param observationHeadingId ID of ObservationHeading to get
     * @return ObservationHeading object
     * @throws ResourceNotFoundException
     */
    ObservationHeading get(Long observationHeadingId) throws ResourceNotFoundException;

    /**
     * Get available ObservationHeadings (result types) for a User that can be used when setting up alerts for new
     * results, typically all ObservationHeadings are returned as User can set up alerts for all result types.
     * @param userId ID of User to retrieve available ObservationHeadings for setting up alerts
     * @return List of ObservationHeading objects
     * @throws ResourceNotFoundException
     */
    @UserOnly
    List<ObservationHeading> getAvailableAlertObservationHeadings(Long userId) throws ResourceNotFoundException;

    /**
     * Get available ObservationHeading (result types) for a User, where results are currently available.
     * @param userId ID of User to retrieve ObservationHeadings where results are currently available
     * @return List of ObservationHeading objects
     * @throws ResourceNotFoundException
     * @throws FhirResourceException
     */
    @UserOnly
    List<ObservationHeading> getAvailableObservationHeadings(Long userId)
            throws ResourceNotFoundException, FhirResourceException;

    /**
     * Get available ObservationHeading (result types) for a User, where patient entered results
     * are currently available.
     * @param userId ID of User to retrieve ObservationHeadings for
     * @return List of ObservationHeading objects
     * @throws ResourceNotFoundException
     * @throws FhirResourceException
     */
    @UserOnly
    List<ObservationHeading> getPatientEnteredObservationHeadings(Long userId)
            throws ResourceNotFoundException, FhirResourceException;

    /**
     * Get the available ResultCluster (groups of result types available to Users when entering their own results).
     * @return List of ResultCluster objects
     */
    @RoleOnly(roles = { RoleName.PATIENT })
    List<ResultCluster> getResultClusters();

    /**
     * Get a List of the User selected ObservationHeadings, used in results table view when Users choose which results
     * to show in the table.
     * @param userId ID of User to retrieve saved ObservationHeadings as specified in results table view
     * @return List of ObservationHeading objects
     * @throws ResourceNotFoundException
     * @throws FhirResourceException
     */
    @UserOnly
    List<ObservationHeading> getSavedObservationHeadings(Long userId)
            throws ResourceNotFoundException, FhirResourceException;

    /**
     * Remove the relationship between an ObservationHeading and a specific Group (specialty), used when setting up the
     * results panel and panel order for the default results view.
     * @param observationHeadingGroupId ID of the ObservationHeadingGroup to remove
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @RoleOnly(roles = { RoleName.SPECIALTY_ADMIN })
    void removeObservationHeadingGroup(Long observationHeadingGroupId)
            throws ResourceNotFoundException, ResourceForbiddenException;

    /**
     * Update an ObservationHeading.
     * @param observationHeading ObservationHeading to update
     * @throws ResourceNotFoundException
     */
    @RoleOnly
    ObservationHeading save(ObservationHeading observationHeading) throws ResourceNotFoundException;

    /**
     * Store a User's selection of ObservationHeadings to show in the results table view.
     * @param userId ID of User for which a selection of ObservationHeadings are to be saved
     * @param codes List of Codes representing the ObservationHeadings to be associated with a User's results table view
     * @throws ResourceNotFoundException
     */
    @UserOnly
    void saveObservationHeadingSelection(Long userId, String[] codes) throws ResourceNotFoundException;

    /**
     * Update an ObservationHeadingGroup representing Group specific panel, panel order properties for
     * ObservationHeadings shown on the default results view.
     * @param observationHeadingGroup ObservationHeadingGroup to update
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @RoleOnly(roles = { RoleName.SPECIALTY_ADMIN })
    void updateObservationHeadingGroup(ObservationHeadingGroup observationHeadingGroup)
            throws ResourceNotFoundException, ResourceForbiddenException;
}
