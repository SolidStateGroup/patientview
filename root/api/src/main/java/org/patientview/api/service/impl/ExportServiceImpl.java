package org.patientview.api.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.patientview.api.builder.CSVDocumentBuilder;
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
import org.patientview.persistence.model.Identifier;
import org.patientview.persistence.model.ObservationHeading;
import org.patientview.persistence.model.User;
import org.patientview.persistence.repository.UserRepository;
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
import java.util.TreeMap;

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

    @Inject
    private UserRepository userRepository;


    @Override
    public HttpEntity<byte[]> downloadResults(Long userId,
                                              String fromDate,
                                              String toDate,
                                              List<String> resultCodes)
            throws ResourceNotFoundException, FhirResourceException {
        CSVDocumentBuilder document = new CSVDocumentBuilder();
        //Setup the headers and the document structure we want to return
        List<String> headings = new ArrayList<>();
        document.addHeader("Date");
        document.addHeader("Unit");


        //If there are no codes sent with the request, get all codes applicable for this user
        for (ObservationHeading heading : observationHeadingService.getAvailableObservationHeadings(userId)) {
            if (resultCodes.contains(heading.getHeading()) || resultCodes.size() == 0) {
                headings.add(heading.getCode().toUpperCase());
                document.addHeader(heading.getName());
            }
        }
        //Get all results for a specified period of time
        Map<Long, Map<String, List<FhirObservation>>> resultsMap =
                observationService.getObservationsByMultipleCodeAndDate(userId, headings, "DESC", fromDate, toDate);

        for (Map.Entry<Long, Map<String, List<FhirObservation>>> entry : resultsMap.entrySet()) {
            ArrayList<String> unitNames = new ArrayList<>();
            document.createNewRow();
            document.resetCurrentPosition();
            document.addValueToNextCell(new SimpleDateFormat("dd-MMM-yyyy HH:mm").format(entry.getKey()));
            //Skip the units column as we don't want to fill this yet
            document.nextCell();

            for (String heading : headings) {
                Map<String, List<FhirObservation>> results = entry.getValue();
                if (results.containsKey(heading.toUpperCase())) {
                    //If there is only one result for that timestamp and code, add to the cell
                    if (results.get(heading.toUpperCase()).size() == 1) {

                        if (results.get(heading.toUpperCase()).get(0).getValue() == null) {
                            results.get(heading.toUpperCase()).get(0).setValue("");
                        }
                        if (results.get(heading.toUpperCase()).get(0).getComments() == null) {
                            results.get(heading.toUpperCase()).get(0).setComments("");
                        }

                        document.addValueToNextCell(String.format("%s %s",
                                results.get(heading.toUpperCase()).get(0).getComments(),
                                results.get(heading.toUpperCase()).get(0).getValue().trim()));
                        //Add the unit
                        if (!unitNames.contains(
                                results.get(heading.toUpperCase()).get(0).getGroup().getShortName())) {
                            unitNames.add(results.get(heading.toUpperCase()).get(0).getGroup().getShortName());
                        }

                    } else {
                        //When multiple codes exist, add to the same cell with a new line
                        for (int i = results.get(heading.toUpperCase()).size() - 1; i >= 0; i--) {
                            FhirObservation observation = results.get(heading.toUpperCase()).get(i);

                            if (observation.getValue() == null) {
                                observation.setValue("");
                            }
                            if (observation.getComments() == null) {
                                observation.setComments("");
                            }

                            String result = String.format("%s %s",
                                    observation.getComments(),
                                    observation.getValue().trim());

                            if (i == results.get(heading.toUpperCase()).size() - 1) {
                                document.addValueToNextCell(result);
                            } else {
                                document.addValueToPreviousCell(result);
                            }
                            //Add the unit
                            if (!unitNames.contains(observation.getGroup().getShortName())) {
                                unitNames.add(observation.getGroup().getShortName());
                            }
                        }
                    }
                } else {
                    document.nextCell();
                }
            }
            document.addValueToCellCascade(1, StringUtils.join(unitNames, ","));
        }
        return getDownloadContent("Results",
                makeCSVString(document.getDocument()).getBytes(Charset.forName("UTF-8")), userId, fromDate, toDate);
    }

    @Override
    public HttpEntity<byte[]> downloadMedicines(Long userId, String fromDate, String toDate)
            throws ResourceNotFoundException, FhirResourceException {
        CSVDocumentBuilder document = new CSVDocumentBuilder();
        document.addHeader("Date");
        document.addHeader("Medicine Name");
        document.addHeader("Dose");
        document.addHeader("Source");

        List<FhirMedicationStatement> medicationStatements = medicationService.getByUserId(userId, fromDate, toDate);
        TreeMap<String, FhirMedicationStatement> orderedMedicationStatement = new TreeMap<>(Collections.reverseOrder());
        for (FhirMedicationStatement fhirMedicationStatement : medicationStatements) {
            //Add current size in case multiple for that day
            orderedMedicationStatement.put(fhirMedicationStatement.getStartDate().getTime() + ""
                    + orderedMedicationStatement.size(), fhirMedicationStatement);
        }

        for (FhirMedicationStatement medicationStatement : orderedMedicationStatement.values()) {
            document.createNewRow();
            document.resetCurrentPosition();
            document.addValueToNextCell(new SimpleDateFormat("dd-MMM-yyyy").format(medicationStatement.getStartDate()));
            document.addValueToNextCell(medicationStatement.getName());
            document.addValueToNextCell(medicationStatement.getDose());
            document.addValueToNextCell(medicationStatement.getGroup().getName());
        }
        return getDownloadContent("Medicines",
                makeCSVString(document.getDocument()).getBytes(Charset.forName("UTF-8")), userId, fromDate, toDate);
    }

    @Override
    public HttpEntity<byte[]> downloadLetters(Long userId, String fromDate, String toDate)
            throws ResourceNotFoundException, FhirResourceException {
        CSVDocumentBuilder document = new CSVDocumentBuilder();
        document.addHeader("Date");
        document.addHeader("Source");
        document.addHeader("Type");
        document.addHeader("Content (expand row to view)");

        //Order letters based on date
        List<FhirDocumentReference> fhirDocuments = letterService.getByUserId(userId, fromDate, toDate);
        TreeMap<String, FhirDocumentReference> orderedfhirDocuments = new TreeMap<>(Collections.reverseOrder());
        for (FhirDocumentReference fhirDocumentReference : orderedfhirDocuments.values()) {
            //Add current size to stop any issues with multiple letters on same date
            orderedfhirDocuments.put(fhirDocumentReference.getDate().getTime() + ""
                    + orderedfhirDocuments.size(), fhirDocumentReference);
        }

        for (FhirDocumentReference fhirDoc : fhirDocuments) {
            document.createNewRow();
            document.resetCurrentPosition();

            document.addValueToNextCell(new SimpleDateFormat("dd-MMM-yyyy").format(fhirDoc.getDate()));
            document.addValueToNextCell(fhirDoc.getGroup().getName());
            document.addValueToNextCell(fhirDoc.getType());
            //Limitation of CSV cannot display some characters, so replace
            String content = fhirDoc.getContent().replaceAll("&#\\d*;", "");
            document.addValueToNextCell(content);
        }
        return getDownloadContent("Letters",
                makeCSVString(document.getDocument()).getBytes(Charset.forName("UTF-8")), userId, fromDate, toDate);

    }

    /**
     * Transforms a byle array into a download file
     *
     * @param fileName The name of the file to be downloaded
     * @param content  The file content (i.e. the CSV file)
     * @return The downloadable document
     */
    private HttpEntity<byte[]> getDownloadContent(String fileName,
                                                  byte[] content,
                                                  Long userId,
                                                  String fromDate,
                                                  String toDate) {
        FileData fileData = new FileData();

        fileData.setType("text-csv");
        fileData.setContent(content);

        if (fileData != null) {
            User user = userRepository.findOne(userId);
            Identifier identifier = user.getIdentifiers().iterator().next();
            if (identifier.getIdentifier() != null) {
                fileData.setName(String.format("%s-%s-%s-%s.csv",
                        fileName,
                        identifier.getIdentifier(),
                        fromDate.replace("-", ""),
                        toDate.replace("-", "")));
            } else {
                fileData.setName(String.format("%s-%s-%s.csv",
                        fileName, fromDate, toDate));
            }

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
     *
     * @param document collection of rows and cells 1st Array is the row, 2nd is the individual cells
     * @return A comma separated, quoted string that can be transformed into a document
     */
    private String makeCSVString(ArrayList<ArrayList<String>> document) {
        String documentString = "";

        for (ArrayList<String> row : document) {
            for (String cell : row) {
                cell = cell.replace("\"", "\"\"");
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
