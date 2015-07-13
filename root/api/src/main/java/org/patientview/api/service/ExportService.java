package org.patientview.api.service;

import org.patientview.api.annotation.UserOnly;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.springframework.http.HttpEntity;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * ObservationHeading service, for managing result types and visibility of results for patients when viewing or entering
 * their own results.
 * <p/>
 * Created by jamesr@solidstategroup.com
 * Created on 11/09/2014
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface ExportService {
    /**
     * Download the results for a specified user and specified result codes.
     * If no codes are specified, all results are returned
     *
     * @param userId   The user requesting the download
     * @param fromDate The inital date to search on
     * @param toDate   The last date to search on
     * @return
     * @throws ResourceNotFoundException
     * @throws FhirResourceException
     */
    @UserOnly
    HttpEntity<byte[]> downloadResults(Long userId,
                                       String fromDate,
                                       String toDate,
                                       List<String> resultCodes)
            throws ResourceNotFoundException, FhirResourceException;

    /**
     * Download all medicines for a specified period
     *
     * @param userId   The user requesting the download
     * @param fromDate The initial date to search on
     * @param toDate   The last date to search on
     * @return
     * @throws ResourceNotFoundException
     * @throws FhirResourceException
     */
    @UserOnly
    HttpEntity<byte[]> downloadMedicines(Long userId, String fromDate, String toDate)
            throws ResourceNotFoundException, FhirResourceException;

    /**
     * Gets all letters within a specified period
     *
     * @param userId   The user requesting the download
     * @param fromDate The initial date to search on
     * @param toDate   The last date to search on
     * @return A CSV file
     * @throws ResourceNotFoundException
     * @throws FhirResourceException
     */
    @UserOnly
    HttpEntity<byte[]> downloadLetters(Long userId,
                                       String fromDate,
                                       String toDate)
            throws ResourceNotFoundException, FhirResourceException;
}
