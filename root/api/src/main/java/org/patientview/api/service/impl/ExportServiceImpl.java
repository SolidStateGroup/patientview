package org.patientview.api.service.impl;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.patientview.api.builder.CSVDocumentBuilder;
import org.patientview.api.model.FhirDocumentReference;
import org.patientview.api.model.FhirMedicationStatement;
import org.patientview.api.model.FhirObservation;
import org.patientview.api.model.enums.FileTypes;
import org.patientview.api.service.ApiMedicationService;
import org.patientview.api.service.ApiObservationService;
import org.patientview.api.service.DocumentService;
import org.patientview.api.service.ExportService;
import org.patientview.api.service.ObservationHeadingService;
import org.patientview.api.service.UserService;
import org.patientview.api.util.ApiUtil;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.FhirLink;
import org.patientview.persistence.model.FileData;
import org.patientview.persistence.model.GetParameters;
import org.patientview.persistence.model.GpMaster;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.Identifier;
import org.patientview.persistence.model.ObservationHeading;
import org.patientview.persistence.model.Question;
import org.patientview.persistence.model.QuestionAnswer;
import org.patientview.persistence.model.Survey;
import org.patientview.persistence.model.SurveyResponse;
import org.patientview.persistence.model.SurveyResponseScore;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.AuditActions;
import org.patientview.persistence.model.enums.GroupTypes;
import org.patientview.persistence.model.enums.HiddenGroupCodes;
import org.patientview.persistence.model.enums.QuestionElementTypes;
import org.patientview.persistence.model.enums.QuestionTypes;
import org.patientview.persistence.model.enums.SurveyResponseScoreTypes;
import org.patientview.persistence.model.enums.SurveyTypes;
import org.patientview.persistence.repository.FhirLinkRepository;
import org.patientview.persistence.repository.GpMasterRepository;
import org.patientview.persistence.repository.QuestionRepository;
import org.patientview.persistence.repository.SurveyResponseRepository;
import org.patientview.persistence.repository.UserRepository;
import org.patientview.persistence.resource.FhirResource;
import org.patientview.service.AuditService;
import org.patientview.util.Util;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeMap;

import static org.patientview.api.util.ApiUtil.getCurrentUser;

/**
 * Class to control the crud operations of the Observation Headings.
 * <p/>
 * Created by jamesr@solidstategroup.com
 * Created on 11/09/2014
 */
@Service
public class ExportServiceImpl extends AbstractServiceImpl<ExportServiceImpl> implements ExportService {

    @Inject
    private AuditService auditService;

    @Inject
    private ApiObservationService apiObservationService;

    @Inject
    private ApiMedicationService apiMedicationService;

    @Inject
    private DocumentService documentService;

    @Inject
    private FhirLinkRepository fhirLinkRepository;

    @Inject
    private FhirResource fhirResource;

    @Inject
    private GpMasterRepository gpMasterRepository;

    @Inject
    private ObservationHeadingService observationHeadingService;

    @Inject
    private QuestionRepository questionRepository;

    @Inject
    private SurveyResponseRepository surveyResponseRepository;

    @Inject
    private UserService userService;

    @Inject
    private UserRepository userRepository;

    private static final int YEARS_RANGE = 999;

    @Override
    public HttpEntity<byte[]> downloadGpMaster() {
        CSVDocumentBuilder document = new CSVDocumentBuilder();
        document.addHeader("Practice Code");
        document.addHeader("Practice Name");
        document.addHeader("Address 1");
        document.addHeader("Address 2");
        document.addHeader("Address 3");
        document.addHeader("Address 4");
        document.addHeader("Postcode");
        document.addHeader("Country");
        document.addHeader("Telephone");
        document.addHeader("Status Code");
        document.addHeader("Created Date");
        document.addHeader("Update Date");

        List<GpMaster> gpMasters
                = gpMasterRepository.findAll(new Sort(new Sort.Order(Sort.Direction.ASC, "practiceCode")));

        for (GpMaster gp : gpMasters) {
            document.createNewRow();
            document.resetCurrentPosition();

            document.addValueToNextCell(gp.getPracticeCode());
            document.addValueToNextCell(gp.getPracticeName());
            document.addValueToNextCell(gp.getAddress1());
            document.addValueToNextCell(gp.getAddress2());
            document.addValueToNextCell(gp.getAddress3());
            document.addValueToNextCell(gp.getAddress4());
            document.addValueToNextCell(gp.getPostcode());
            document.addValueToNextCell(gp.getCountry().toString());
            document.addValueToNextCell(gp.getTelephone());
            document.addValueToNextCell(gp.getStatusCode());
            document.addValueToNextCell(new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss").format(gp.getCreated()));
            if (gp.getLastUpdate() != null) {
                document.addValueToNextCell(new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss").format(gp.getLastUpdate()));
            } else {
                document.addValueToNextCell("");
            }
        }

        return getDownloadContent("GP Master List",
                makeCSVString(
                        document.getDocument()).getBytes(Charset.forName("UTF-8")), null, null, null, FileTypes.CSV);
    }

    @Override
    public HttpEntity<byte[]> downloadPatientList(GetParameters getParameters)
            throws ResourceNotFoundException, ResourceForbiddenException {

        //Log the download action
        auditService.createAudit(AuditActions.EXPORT_PATIENT_LIST, null, getCurrentUser(), null, null, null);

        getParameters.setSize("5000");
        Page<org.patientview.api.model.User> users = userService.getApiUsersByGroupsAndRoles(getParameters);

        CSVDocumentBuilder document = new CSVDocumentBuilder();
        document.addHeader("Surname");
        document.addHeader("Forename");
        document.addHeader("Identifier");
        document.addHeader("DOB");
        document.addHeader("PV Username");
        document.addHeader("Group");
        document.addHeader("Email Verified");
        document.addHeader("Account Locked");
        document.addHeader("Last Login Date");

        for (org.patientview.api.model.User user : users) {
            for (org.patientview.api.model.GroupRole groupRole : user.getGroupRoles()) {

                // Don't include specialty groups
                if (!groupRole.getGroup().getGroupType().getValue().equals(GroupTypes.SPECIALTY.toString())) {
                    // Loop over each identifier
                    for (Identifier identifier : (Set<Identifier>) user.getIdentifiers()) {
                        document.createNewRow();
                        document.resetCurrentPosition();
                        document.addValueToNextCell(user.getSurname());
                        document.addValueToNextCell(user.getForename());
                        document.addValueToNextCell(identifier.getIdentifier());
                        if (user.getDateOfBirth() != null) {
                            document.addValueToNextCell(
                                    new SimpleDateFormat("dd-MMM-yyyy").format(user.getDateOfBirth()));
                        } else {
                            document.addValueToNextCell("");
                        }
                        document.addValueToNextCell(user.getUsername());
                        document.addValueToNextCell(groupRole.getGroup().getName());
                        document.addValueToNextCell(user.getEmailVerified().toString());
                        document.addValueToNextCell(user.getLocked().toString());

                        if (user.getCurrentLogin() != null) {
                            document.addValueToNextCell(
                                    new SimpleDateFormat("dd-MMM-yyyy").format(user.getCurrentLogin()));
                        } else {
                            document.addValueToNextCell("No Last Login Date");
                        }
                    }
                }
            }
        }

        return getDownloadContent("PatientViewExport_" + new SimpleDateFormat("dd-MMM-yyyy_HHmmss").format(new Date()),
                makeCSVString(
                        document.getDocument()).getBytes(Charset.forName("UTF-8")), null, null, null, FileTypes.CSV);
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
        List<FhirDocumentReference> fhirDocuments = documentService.getByUserIdAndClass(userId, null, fromDate, toDate);
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

        return getDownloadContent("Letters", makeCSVString(
                document.getDocument()).getBytes(Charset.forName("UTF-8")), userId, fromDate, toDate, FileTypes.CSV);
    }

    @Override
    public HttpEntity<byte[]> downloadMedicines(Long userId, String fromDate, String toDate)
            throws ResourceNotFoundException, FhirResourceException {
        CSVDocumentBuilder document = new CSVDocumentBuilder();
        document.addHeader("Date");
        document.addHeader("Medicine Name");
        document.addHeader("Dose");
        document.addHeader("Source");

        List<FhirMedicationStatement> medicationStatements = apiMedicationService.getByUserId(userId, fromDate, toDate);
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

        return getDownloadContent("Medicines", makeCSVString(
                document.getDocument()).getBytes(Charset.forName("UTF-8")), userId, fromDate, toDate, FileTypes.CSV);
    }

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
        document.addHeader("Comments");

        //If there are no codes sent with the request, get all codes applicable for this user
        for (ObservationHeading heading : observationHeadingService.getAvailableObservationHeadings(userId)) {
            if (resultCodes.contains(heading.getHeading()) || resultCodes.size() == 0) {
                headings.add(heading.getCode().toUpperCase());
                document.addHeader(heading.getName());
            }
        }
        //Get all results for a specified period of time
        Map<Long, Map<String, List<FhirObservation>>> resultsMap =
                apiObservationService.getObservationsByMultipleCodeAndDate(userId, headings, "DESC", fromDate, toDate);

        for (Map.Entry<Long, Map<String, List<FhirObservation>>> entry : resultsMap.entrySet()) {
            ArrayList<String> unitNames = new ArrayList<>();
            document.createNewRow();
            document.resetCurrentPosition();
            document.addValueToNextCell(new SimpleDateFormat("dd-MMM-yyyy HH:mm").format(entry.getKey()));
            //Skip the units and comments column as we don't want to fill this yet
            document.nextCell();
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
                        document.appendUnqiueValueToCurrentRowCell(2,
                                results.get(heading.toUpperCase()).get(0).getComments());

                        document.addValueToNextCell(results.get(heading.toUpperCase()).get(0).getValue().trim());
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

                            if (i == results.get(heading.toUpperCase()).size() - 1) {
                                document.addValueToNextCell(observation.getValue().trim());
                                document.appendUnqiueValueToCurrentRowCell(2, observation.getComments());

                            } else {
                                document.addValueToPreviousCell(observation.getValue().trim());
                                document.appendUniqueValueToLastRowCell(2, observation.getComments());
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
        return getDownloadContent("Results", makeCSVString(
                document.getDocument()).getBytes(Charset.forName("UTF-8")), userId, fromDate, toDate, FileTypes.CSV);
    }

    @Override
    public HttpEntity<byte[]> downloadSurveyResponsePdf(Long userId, Long surveyResponseId)
            throws DocumentException, ResourceNotFoundException {
        SurveyResponse surveyResponse = surveyResponseRepository.findById(surveyResponseId)
                .orElseThrow(() -> new ResourceNotFoundException("Not found"));

        User user = userRepository.getOne(userId);

        // create new itext pdf document
        Document document = new Document();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, baos);
        document.open();

        // add header
        Font large = new Font(Font.FontFamily.HELVETICA, 24, Font.NORMAL, BaseColor.BLACK);
        Font bold = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.BLACK);

        // patientview image
        try {
            Image image = Image.getInstance(this.getClass().getClassLoader().getResource("images/pv-logo-large.png"));
            float w = image.getPlainWidth();
            w *= 0.1;
            float h = image.getPlainHeight();
            h *= 0.1;
            image.scaleAbsolute(w, h);
            document.add(image);
        } catch (IOException e) {
            document.add(new Chunk("PatientView", large));
        }

        document.add(new Paragraph(surveyResponse.getSurvey().getDescription()));
        document.add(new Chunk(new LineSeparator()));

        // name
        Paragraph name = new Paragraph();
        name.add(new Chunk("Name: ", bold));
        name.add(new Chunk(user.getName()));
        document.add(name);

        // identifiers
        Paragraph identifiers = new Paragraph();
        identifiers.add(new Chunk("Identifier(s): ", bold));
        for (Identifier identifier : user.getIdentifiers()) {
            identifiers.add(new Chunk(
                    identifier.getIdentifier() + " (" + identifier.getIdentifierType().getDescription() + ")"));
        }
        document.add(identifiers);

        // staff user (and group roles) if present
        if (surveyResponse.getStaffUser() != null) {
            Paragraph staffUser = new Paragraph();
            staffUser.add(new Chunk("Form last updated by: ", bold));
            staffUser.add(new Chunk(surveyResponse.getStaffUser().getName()));

            StringBuilder sb = new StringBuilder();
            for (GroupRole groupRole : surveyResponse.getStaffUser().getGroupRoles()) {
                if (groupRole.getGroup().getGroupType().getValue().equals(GroupTypes.UNIT.toString())) {
                    sb.append(", ");
                    sb.append(groupRole.getGroup().getName());
                    sb.append(" (");
                    sb.append(groupRole.getRole().getDescription());
                    sb.append(") ");
                }
            }

            staffUser.add(new Chunk(sb.toString()));
            document.add(staffUser);
        }

        // date
        Paragraph date = new Paragraph();
        date.add(new Chunk("Date: ", bold));
        date.add(new Chunk(new SimpleDateFormat("dd-MMM-yyyy").format(surveyResponse.getDate())));
        document.add(date);

        // add content
        document.add(new Paragraph(" "));

        List<QuestionAnswer> questionAnswers = new ArrayList<>(surveyResponse.getQuestionAnswers());
        Collections.sort(questionAnswers, new Comparator<QuestionAnswer>() {
            @Override
            public int compare(QuestionAnswer qa1, QuestionAnswer qa2) {
                return qa1.getQuestion().getDisplayOrder().compareTo(qa2.getQuestion().getDisplayOrder());
            }
        });

        for (QuestionAnswer answer : questionAnswers) {
            document.add(new Paragraph(new Chunk(answer.getQuestion().getText(), bold)));
            document.add(new Paragraph(new Chunk(answer.getValue())));
            document.add(new Paragraph(" "));
        }

        // close document and return in correct format
        document.close();

        return getDownloadContent(
                surveyResponse.getSurvey().getDescription(), baos.toByteArray(), userId, null, null, FileTypes.PDF);
    }

    @Override
    public HttpEntity<byte[]> downloadSurveyResponses(Long userId, String type) throws ResourceNotFoundException {
        if (!ApiUtil.isInEnum(type, SurveyTypes.class)) {
            throw new ResourceNotFoundException("Survey type not found");
        }

        List<SurveyResponse> surveyResponses
                = surveyResponseRepository.findByUserAndSurveyType(getCurrentUser(), type);

        if (CollectionUtils.isEmpty(surveyResponses)) {
            throw new ResourceNotFoundException("No survey responses found");
        }

        // survey specific output with dates from and to based on survey response dates
        Date fromDate = new DateTime().plusYears(YEARS_RANGE).toDate();
        Date toDate = new DateTime().minusYears(YEARS_RANGE).toDate();
        Survey survey = surveyResponses.get(0).getSurvey();
        CSVDocumentBuilder document = new CSVDocumentBuilder();

        List<String> questionTypes = new ArrayList<>();
        boolean includeScore = false;
        boolean includeSeverity = false;

        // question types
        switch (survey.getType()) {
            case "IBD_CONTROL":
                includeScore = true;
                break;
            case "CROHNS_SYMPTOM_SCORE":
                questionTypes.add(QuestionTypes.ABDOMINAL_PAIN.toString());
                questionTypes.add(QuestionTypes.OPEN_BOWELS.toString());
                questionTypes.add(QuestionTypes.FEELING.toString());
                questionTypes.add(QuestionTypes.COMPLICATION.toString());
                questionTypes.add(QuestionTypes.MASS_IN_TUMMY.toString());
                includeScore = true;
                includeSeverity = true;
                break;
            case "COLITIS_SYMPTOM_SCORE":
                questionTypes.add(QuestionTypes.NUMBER_OF_STOOLS_DAYTIME.toString());
                questionTypes.add(QuestionTypes.NUMBER_OF_STOOLS_NIGHTTIME.toString());
                questionTypes.add(QuestionTypes.TOILET_TIMING.toString());
                questionTypes.add(QuestionTypes.PRESENT_BLOOD.toString());
                questionTypes.add(QuestionTypes.FEELING.toString());
                questionTypes.add(QuestionTypes.COMPLICATION.toString());
                includeScore = true;
                includeSeverity = true;
                break;
            case "IBD_FATIGUE":
                // section 1
                for (QuestionTypes questionType : QuestionTypes.values()) {
                    if (questionType.toString().contains("IBD_FATIGUE_I")) {
                        questionTypes.add(questionType.toString());
                    }
                }
                // section 2
                for (QuestionTypes questionType : QuestionTypes.values()) {
                    if (questionType.toString().contains("IBD_DAS")) {
                        questionTypes.add(questionType.toString());
                    }
                }
                // section 3
                for (QuestionTypes questionType : QuestionTypes.values()) {
                    if (questionType.toString().contains("IBD_FATIGUE_EXTRA")) {
                        questionTypes.add(questionType.toString());
                    }
                }
                includeScore = true;
                includeSeverity = true;
                break;
            default:
                // if not a known survey type, only export score and severity
                includeScore = true;
                includeSeverity = true;
                break;
        }

        document.addHeader("Date Taken");

        // set CSV headers
        for (String questionType : questionTypes) {
            try {
                Question question = questionRepository.findByType(questionType).iterator().next();
                if (StringUtils.isNotEmpty(question.getText())) {
                    document.addHeader(question.getText());
                } else {
                    document.addHeader(question.getType());
                }
            } catch (NoSuchElementException | NullPointerException nse) {
                throw new ResourceNotFoundException("Error retrieving questions");
            }
        }

        // set score header if required
        if (includeScore) {
            for (SurveyResponseScore score : surveyResponses.get(0).getSurveyResponseScores()) {
                String header = "Score";
                if (Util.isInEnum(score.getType(), SurveyResponseScoreTypes.class)) {
                    header = SurveyResponseScoreTypes.valueOf(score.getType()).getName() + " " + header;
                }
                if (includeSeverity) {
                    header += " (severity)";
                }
                document.addHeader(header);
            }
        }

        // order by date in survey desc
        Collections.sort(surveyResponses, new Comparator<SurveyResponse>() {
            @Override
            public int compare(SurveyResponse s1, SurveyResponse s2) {
                return s2.getDate().compareTo(s1.getDate());
            }
        });

        for (SurveyResponse surveyResponse : surveyResponses) {
            // create map of specific answers
            List<QuestionAnswer> answers = surveyResponse.getQuestionAnswers();
            Map<String, String> answerMap = new HashMap<>();

            for (QuestionAnswer questionAnswer : answers) {
                // only care about certain questions
                if (Util.isInEnum(questionAnswer.getQuestion().getType(), QuestionTypes.class)
                        && questionTypes.contains(questionAnswer.getQuestion().getType())) {
                    // if is a select, then get the text of the question option
                    if (questionAnswer.getQuestion().getElementType().equals(QuestionElementTypes.SINGLE_SELECT)) {
                        answerMap.put(questionAnswer.getQuestion().getType(),
                                questionAnswer.getQuestionOption().getText());
                    }
                    // if is a ranged value then get value
                    if (questionAnswer.getQuestion().getElementType().equals(QuestionElementTypes.SINGLE_SELECT_RANGE)
                        || questionAnswer.getQuestion().getElementType().equals(QuestionElementTypes.TEXT)
                        || questionAnswer.getQuestion().getElementType().equals(QuestionElementTypes.TEXT_NUMERIC)) {
                        answerMap.put(questionAnswer.getQuestion().getType(), questionAnswer.getValue());
                    }
                }
            }

            // set from and to dates
            if (surveyResponse.getDate().before(fromDate)) {
                fromDate = surveyResponse.getDate();
            }
            if (surveyResponse.getDate().after(toDate)) {
                toDate = surveyResponse.getDate();
            }

            // create CSV row
            document.createNewRow();
            document.resetCurrentPosition();

            // set date column
            document.addValueToNextCell(new SimpleDateFormat("dd-MMM-yyyy").format(surveyResponse.getDate()));

            // set answer columns
            for (String questionType : questionTypes) {
                if (answerMap.containsKey(questionType)) {
                    document.addValueToNextCell(answerMap.get(questionType));
                } else {
                    document.addValueToNextCell("");
                }
            }

            if (includeScore) {
                for (SurveyResponseScore score : surveyResponse.getSurveyResponseScores()) {
                    String scoreString = score.getScore().toString();
                    if (includeSeverity) {
                        scoreString += " (" + score.getSeverity().getName() + ")";
                    }
                    document.addValueToNextCell(scoreString);
                }
            }
        }

        return getDownloadContent(survey.getType(),
                makeCSVString(document.getDocument()).getBytes(Charset.forName("UTF-8")), userId,
                new SimpleDateFormat("dd-MMM-yyyy").format(fromDate),
                new SimpleDateFormat("dd-MMM-yyyy").format(toDate), FileTypes.CSV);
    }

    @Override
    public HttpEntity<byte[]> downloadTreatmentData() throws FhirResourceException {
        CSVDocumentBuilder document = new CSVDocumentBuilder();
        document.addHeader("Identifier");
        document.addHeader("Group Code");
        document.addHeader("Group Name");
        document.addHeader("Treatment");

        // map of subject id to treatment code
        Map<String, List<String>> treatments = fhirResource.getAllEncounterTreatments();

        // don't include if no treatment code or ECS group
        for (FhirLink fhirLink : fhirLinkRepository.findAll()) {
            String subjectId = fhirLink.getResourceId().toString();
            if (!CollectionUtils.isEmpty(treatments.get(subjectId))
                    && !fhirLink.getGroup().getCode().equals(HiddenGroupCodes.ECS.toString())) {
                for (String code : treatments.get(subjectId)) {
                    document.createNewRow();
                    document.resetCurrentPosition();
                    document.addValueToNextCell(fhirLink.getIdentifier().getIdentifier());
                    document.addValueToNextCell(fhirLink.getGroup().getCode());
                    document.addValueToNextCell(fhirLink.getGroup().getName());
                    document.addValueToNextCell(code);
                }
            }
        }

        return getDownloadContent("Treatment Export",
            makeCSVString(document.getDocument()).getBytes(Charset.forName("UTF-8")), null, null, null, FileTypes.CSV);
    }

    /**
     * Transforms a byte array into a download file
     *
     * @param fileName The name of the file to be downloaded
     * @param content  The file content (i.e. the CSV file)
     * @return The downloadable document
     */
    private HttpEntity<byte[]> getDownloadContent(String fileName,
                                                  byte[] content,
                                                  Long userId,
                                                  String fromDate,
                                                  String toDate, FileTypes fileType) {
        FileData fileData = new FileData();
        if (fileType.equals(FileTypes.CSV)) {
            fileData.setType("text-csv");
        } else if (fileType.equals(FileTypes.PDF)) {
            fileData.setType("application/pdf");
        }
        fileData.setContent(content);

        StringBuilder sb = new StringBuilder(fileName);

        if (userId != null) {
            User user = userRepository.findById(userId).get();
            Identifier identifier = user.getIdentifiers().iterator().next();

            if (identifier.getIdentifier() != null) {
                sb.append("-");
                sb.append(identifier.getIdentifier());
            }
        }

        if (fromDate != null && toDate != null) {
            sb.append("-");
            sb.append(fromDate.replace("-", ""));
            sb.append("-");
            sb.append(toDate.replace("-", ""));
        }

        sb.append(".");
        sb.append(fileType.getName());
        fileData.setName(sb.toString());

        HttpHeaders header = new HttpHeaders();
        String[] contentTypeArr = fileData.getType().split("/");
        if (contentTypeArr.length == 2) {
            header.setContentType(new MediaType(contentTypeArr[0], contentTypeArr[1]));
        }
        header.set("Content-Disposition", "attachment; filename=" + fileData.getName().replace(" ", "_"));
        header.setContentLength(fileData.getContent().length);
        return new HttpEntity<>(fileData.getContent(), header);
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
                if (cell == null) {
                    documentString += "\"\",";
                } else {
                    cell = cell.replace("\"", "\"\"");
                    documentString += "\"" + cell + "\",";
                }
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
