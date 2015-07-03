package org.patientview.api.service.impl;

import org.joda.time.DateTime;
import org.patientview.api.model.FhirDocumentReference;
import org.patientview.api.model.FhirMedicationStatement;
import org.patientview.api.model.FhirObservation;
import org.patientview.api.service.ExportService;
import org.patientview.api.service.LetterService;
import org.patientview.api.service.MedicationService;
import org.patientview.api.service.ObservationHeadingService;
import org.patientview.api.service.ObservationService;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.FileData;
import org.patientview.persistence.model.ObservationHeading;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Class to control the crud operations of the Observation Headings.
 * <p/>
 * Created by jamesr@solidstategroup.com
 * Created on 11/09/2014
 */
@Service
public class ExportServiceImpl extends AbstractServiceImpl<ExportServiceImpl> implements ExportService {


    @Inject
    private ObservationService observationService;

    @Inject
    private ObservationHeadingService observationHeadingService;

    @Inject
    private LetterService letterService;

    @Inject
    private MedicationService medicationService;


    @Override
    public HttpEntity<byte[]> downloadResults(Long userId, String fromDate, String toDate, List<String> resultCodes) throws ResourceNotFoundException, FhirResourceException {
        //Setup the headers and the document structure we want to return
        ArrayList<ArrayList<String>> document = new ArrayList<ArrayList<String>>();
        List<String> headings = new ArrayList<>();
        ArrayList<String> row = new ArrayList<>();
        row.add("Date");

        //If there are no codes sent with the request, get all codes applicable for this user
        if (resultCodes.size() == 0) {
            for (ObservationHeading heading : observationHeadingService.getAvailableObservationHeadings(userId)) {
                headings.add(heading.getHeading());
                row.add(heading.getHeading());
            }
        }else {
            //Sort the result codes into alphabetical order and add as a heading
            Collections.sort(resultCodes);
            for (String heading : resultCodes) {
                headings.add(heading);
                row.add(heading);
            }
        }
        document.add(row);
        //Get all results for a specified period of time
        Map<Long, Map<String, List<FhirObservation>>> resultsMap = observationService.getObservationsByMultipleCodeAndDate(userId, resultCodes, "DESC", fromDate, toDate);
        for (Map.Entry<Long, Map<String, List<FhirObservation>>> entry : resultsMap.entrySet()) {
            row = new ArrayList<>();
            row.add(new SimpleDateFormat("dd-MMM-yyyy HH:mm").format(entry.getKey()));

            for (String heading : headings) {
                Map<String, List<FhirObservation>> results = entry.getValue();
                if (results.containsKey(heading.toUpperCase())) {
                    //If there is only one result for that timestamp and code, add to the cell
                    if (results.get(heading.toUpperCase()).size() == 1) {
                        row.add(((results.get(heading.toUpperCase()).get(0).getComments() == null) ? "" : results.get(heading.toUpperCase()).get(0).getComments()) + " " + results.get(heading.toUpperCase()).get(0).getValue());
                    }
                    //When multiple codes exist, add to the same cell with a new line
                    else {
                        String multipleResults = "";
                        for (int i = results.get(heading.toUpperCase()).size() - 1; i >= 0; i--) {
                            FhirObservation observation = results.get(heading.toUpperCase()).get(i);
                            if (i == 0) {
                                multipleResults += ((observation.getComments() == null) ? "" : observation.getComments()) + " " + observation.getValue() + "";
                            } else {
                                multipleResults += ((observation.getComments() == null) ? "" : observation.getComments()) + " " + observation.getValue() + "\n";
                            }
                        }
                        row.add(multipleResults);
                    }
                } else {
                    //Add a blank value for no results
                    row.add("");
                }
            }
            document.add(row);
        }
        return getDownloadContent("Results", makeCSVString(document).getBytes(Charset.forName("UTF-8")));
    }


    @Override
    public HttpEntity<byte[]> downloadMedicines(Long userId, String fromDate, String toDate)
            throws ResourceNotFoundException, FhirResourceException {
        ArrayList<ArrayList<String>> document = new ArrayList<ArrayList<String>>();
        ArrayList<String> row = new ArrayList<>();
        row.add("Date");
        row.add("Medicine Name");
        row.add("Dose");
        row.add("Source");
        document.add(row);

        List<FhirMedicationStatement> medicationStatements = medicationService.getByUserId(userId, fromDate, toDate);
        for (FhirMedicationStatement medicationStatement : medicationStatements) {
            row = new ArrayList<>();
            row.add(new SimpleDateFormat("dd-MMM-yyyy").format(medicationStatement.getStartDate()));
            row.add(medicationStatement.getName());
            row.add(medicationStatement.getDose());
            row.add(medicationStatement.getGroup().getName());
            document.add(row);
        }
        return getDownloadContent("Medicines", makeCSVString(document).getBytes(Charset.forName("UTF-8")));
    }

    @Override
    public HttpEntity<byte[]> downloadLetters(Long userId, String fromDate, String toDate)
            throws ResourceNotFoundException, FhirResourceException {
        List<FhirDocumentReference> fhirDocuments = letterService.getByUserId(userId, fromDate, toDate);
        ArrayList<ArrayList<String>> document = new ArrayList<ArrayList<String>>();
        ArrayList<String> row = new ArrayList<>();
        row.add("Start Date");
        row.add("Source");
        row.add("Type");
        document.add(row);

        for (FhirDocumentReference fhirDoc : fhirDocuments) {
            row = new ArrayList<>();
            row.add(new SimpleDateFormat("dd-MMM-yyyy").format(fhirDoc.getDate()));
            row.add(fhirDoc.getGroup().getName());
            row.add(fhirDoc.getType());
            document.add(row);
        }
        return getDownloadContent("Letters", makeCSVString(document).getBytes(Charset.forName("UTF-8")));

    }

    /**
     * Transforms a byle array into a download file
     * @param fileName The name of the file to be downloaded
     * @param content The file content (i.e. the CSV file)
     * @return The downloadable document
     */
    private HttpEntity<byte[]> getDownloadContent(String fileName, byte[] content) {
        FileData fileData = new FileData();

        fileData.setType("text-csv");
        fileData.setContent(content);
        fileData.setName(fileName + "-" + DateTime.now().toString("dd-MM-yy") + ".csv");

        if (fileData != null) {
            HttpHeaders header = new HttpHeaders();
            String[] contentTypeArr = fileData.getType().split("/");
            if (contentTypeArr.length == 2) {
                header.setContentType(new MediaType(contentTypeArr[0], contentTypeArr[1]));
            }
            header.set("Content-Disposition", "attachment; filename=" + fileData.getName().replace(" ", "_"));
            header.setContentLength(fileData.getContent().length);
            return new HttpEntity<>(fileData.getContent(), header);
        }

        return null;
    }

    /**
     * Transform an ArrayList in to a csv string
     * @param document  collection of rows and cells 1st Array is the row, 2nd is the individual cells
     * @return A comma separated, quoted string that can be transformed into a document
     */
    private String makeCSVString(ArrayList<ArrayList<String>> document) {
        String documentString = "";

        for (ArrayList<String> row : document) {
            for (String cell : row) {
                documentString += "\"" + cell + "\",";
            }
            documentString += "\n";
            //When there is no data
            if (document.size() < 2) {
                documentString += "\"No data to display\"";
            }
        }
        return documentString;
    }


}
