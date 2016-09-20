package org.patientview.api.service;

import com.itextpdf.text.DocumentException;
import org.patientview.api.annotation.GroupMemberOnly;
import org.patientview.api.annotation.RoleOnly;
import org.patientview.api.annotation.UserOnly;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.GetParameters;
import org.patientview.persistence.model.enums.RoleName;
import org.springframework.http.HttpEntity;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Export Service, serves binary files to user for downloading results, medicines, letters, survey responses etc
 * <p/>
 * Created by jamesr@solidstategroup.com
 * Created on 11/09/2014
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface ExportService {

    /**
     * Download the entire GP master list (global admin only)
     * @return CSV file
     */
    @RoleOnly
    HttpEntity<byte[]> downloadGpMaster();

    /**
     * Download the list of patients currently shown
     * @return CSV file
     */
    @GroupMemberOnly(roles = { RoleName.SPECIALTY_ADMIN, RoleName.UNIT_ADMIN, RoleName.STAFF_ADMIN })
    HttpEntity<byte[]> downloadPatientList(GetParameters getParameters) throws ResourceNotFoundException, ResourceForbiddenException;


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
    HttpEntity<byte[]> downloadLetters(Long userId, String fromDate, String toDate)
    throws ResourceNotFoundException, FhirResourceException;

    /**
     * Download all medicines for a specified period
     *
     * @param userId   The user requesting the download
     * @param fromDate The initial date to search on
     * @param toDate   The last date to search on
     * @return byte array CSV file containing medicines
     * @throws ResourceNotFoundException
     * @throws FhirResourceException
     */
    @UserOnly
    HttpEntity<byte[]> downloadMedicines(Long userId, String fromDate, String toDate)
            throws ResourceNotFoundException, FhirResourceException;

    /**
     * Download the results for a specified user and specified result codes.
     * If no codes are specified, all results are returned
     *
     * @param userId   The user requesting the download
     * @param fromDate The inital date to search on
     * @param toDate   The last date to search on
     * @return byte array CSV file containing results
     * @throws ResourceNotFoundException
     * @throws FhirResourceException
     */
    @UserOnly
    HttpEntity<byte[]> downloadResults(Long userId, String fromDate, String toDate, List<String> resultCodes)
    throws ResourceNotFoundException, FhirResourceException;

    /**
     * Produce byte array (PDF) of survey response given user ID and survey response ID
     * @param userId ID of user
     * @param surveyResponseId ID of survey response
     * @return byte array PDF file of survey response
     * @throws DocumentException
     * @throws ResourceNotFoundException
     */
    @UserOnly
    HttpEntity<byte[]> downloadSurveyResponsePdf(Long userId, Long surveyResponseId)
            throws DocumentException, ResourceNotFoundException;

    /**
     * Produce byte array (CSV file) of survey responses based on user ID and survey type
     * @param userId ID of user to download survey responses for
     * @param type String of type SurveyTypes
     * @return byte array CSV file containing responses
     * @throws ResourceNotFoundException
     */
    @UserOnly
    HttpEntity<byte[]> downloadSurveyResponses(Long userId, String type) throws ResourceNotFoundException;
}
