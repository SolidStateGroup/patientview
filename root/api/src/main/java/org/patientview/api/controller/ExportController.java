package org.patientview.api.controller;

import com.itextpdf.text.DocumentException;
import org.patientview.api.config.ExcludeFromApiDoc;
import org.patientview.api.service.ExportService;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.GetParameters;
import org.springframework.http.HttpEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import java.util.List;

/**
 * RESTful interface for exporting data as binary files
 * <p/>
 * Created by jamesr@solidstategroup.com
 * Created on 07/10/2014
 */
@ExcludeFromApiDoc
@RestController
public class ExportController extends BaseController<ExportController> {

    @Inject
    private ExportService exportService;

    @RequestMapping(value = "/gp/mastertable/download", method = RequestMethod.GET)
    @ResponseBody
    public HttpEntity<byte[]> downloadGpMaster() {
        return exportService.downloadGpMaster();
    }

    /**
     * Download list of patients given get parameters (from currently shown in UI).
     * @param getParameters GetParameters with filters for patient selection
     * @return HttpEnttiy of byte[] containing CSV file of patients
     * @throws ResourceNotFoundException thrown when getting users
     * @throws ResourceForbiddenException thrown when getting users
     */
    @RequestMapping(value = "/export/patients/download", method = RequestMethod.GET)
    @ResponseBody
    public HttpEntity<byte[]> downloadPatientList(GetParameters getParameters)
            throws ResourceNotFoundException, ResourceForbiddenException {
        return exportService.downloadPatientList(getParameters);
    }
    /**
     * Download a letter, given User ID and FileData ID.
     *
     * @param userId ID of User to download letter for
     * @return HttpEntity to allow client to download in browser
     * @throws ResourceNotFoundException
     * @throws FhirResourceException
     */
    @RequestMapping(value = "/user/{userId}/export/letters/{fromDate}/{toDate}/download", method = RequestMethod.GET)
    @ResponseBody
    public HttpEntity<byte[]> downloadLetters(@PathVariable("userId") Long userId,
                                              @PathVariable("fromDate") String fromDate,
                                              @PathVariable("toDate") String toDate)
            throws ResourceNotFoundException, FhirResourceException {
        return exportService.downloadLetters(userId, fromDate, toDate);
    }

    /**
     * Download an export of all medicines, given User ID and FileData ID.
     *
     * @param userId ID of User to download letter for
     * @return HttpEntity to allow client to download in browser
     * @throws ResourceNotFoundException
     * @throws FhirResourceException
     */
    @RequestMapping(value = "/user/{userId}/export/medicines/{fromDate}/{toDate}/download", method = RequestMethod.GET)
    @ResponseBody
    public HttpEntity<byte[]> downloadMedicines(@PathVariable("userId") Long userId,
                                                @PathVariable("fromDate") String fromDate,
                                                @PathVariable("toDate") String toDate)
            throws ResourceNotFoundException, FhirResourceException {
        return exportService.downloadMedicines(userId, fromDate, toDate);
    }

    /**
     * Download a letter, given User ID and FileData ID.
     *
     * @param userId ID of User to download letter for
     * @return HttpEntity to allow client to download in browser
     * @throws ResourceNotFoundException
     * @throws FhirResourceException
     */
    @RequestMapping(value = "/user/{userId}/export/results/{fromDate}/{toDate}/download", method = RequestMethod.GET)
    @ResponseBody
    public HttpEntity<byte[]> downloadResults(@PathVariable("userId") Long userId,
                                              @PathVariable("fromDate") String fromDate,
                                              @PathVariable("toDate") String toDate,
                                              @RequestParam List<String> resultCodes)
            throws ResourceNotFoundException, FhirResourceException {
        return exportService.downloadResults(userId, fromDate, toDate, resultCodes);
    }

    /**
     * Export a survey response as PDF
     * @param userId ID of user to export survey response for
     * @param surveyResponseId ID of survey response to export
     * @return byte array PDF file
     * @throws DocumentException
     * @throws ResourceNotFoundException
     */
    @RequestMapping(value = "/user/{userId}/export/surveyresponse/{surveyResponseId}/pdf", method = RequestMethod.GET)
    @ResponseBody
    public HttpEntity<byte[]> downloadSurveyResponsePdf(@PathVariable("userId") Long userId,
                                                      @PathVariable("surveyResponseId") Long surveyResponseId)
            throws DocumentException, ResourceNotFoundException {
        return exportService.downloadSurveyResponsePdf(userId, surveyResponseId);
    }

    /**
     * Produce byte array (CSV file) of survey responses based on user ID and survey type
     * @param userId ID of user to download survey responses for
     * @param type String of type SurveyTypes
     * @return byte array CSV file containing responses
     * @throws ResourceNotFoundException
     */
    @RequestMapping(value = "/user/{userId}/export/survey/{type}/download", method = RequestMethod.GET)
    @ResponseBody
    public HttpEntity<byte[]> downloadSurveyResponses(@PathVariable("userId") Long userId,
            @PathVariable("type") String type) throws ResourceNotFoundException {
        return exportService.downloadSurveyResponses(userId, type);
    }

    /**
     * Download all patient's treatment data (Encounter type TREATMENT, from rrtstatus in import XML).
     * @return byte array CSV file containing identifier, group code, group name, treatment code
     * @throws FhirResourceException thrown getting data from FHIR
     */
    @RequestMapping(value = "/export/treatment/download", method = RequestMethod.GET)
    @ResponseBody
    public HttpEntity<byte[]> downloadTreatmentData() throws FhirResourceException{
        return exportService.downloadTreatmentData();
    }
}
