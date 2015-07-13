package org.patientview.migration.service.impl;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.patientview.config.utils.CommonUtils;
import org.patientview.migration.service.AdminDataMigrationService;
import org.patientview.migration.service.UserDataMigrationService;
import org.patientview.migration.util.JsonUtil;
import org.patientview.migration.util.exception.JsonMigrationException;
import org.patientview.migration.util.exception.JsonMigrationExistsException;
import org.patientview.model.Patient;
import org.patientview.patientview.model.Aboutme;
import org.patientview.patientview.model.Diagnosis;
import org.patientview.patientview.model.EmailVerification;
import org.patientview.patientview.model.EyeCheckup;
import org.patientview.patientview.model.FootCheckup;
import org.patientview.patientview.model.Letter;
import org.patientview.patientview.model.Medicine;
import org.patientview.patientview.model.SpecialtyUserRole;
import org.patientview.patientview.model.UktStatus;
import org.patientview.patientview.model.UserMapping;
import org.patientview.patientview.model.enums.DiagnosticType;
import org.patientview.persistence.model.Feature;
import org.patientview.persistence.model.FhirAllergy;
import org.patientview.persistence.model.FhirCondition;
import org.patientview.persistence.model.FhirContact;
import org.patientview.persistence.model.FhirDiagnosticReport;
import org.patientview.persistence.model.FhirDocumentReference;
import org.patientview.persistence.model.FhirEncounter;
import org.patientview.persistence.model.FhirIdentifier;
import org.patientview.persistence.model.FhirMedicationStatement;
import org.patientview.persistence.model.FhirObservation;
import org.patientview.persistence.model.FhirPatient;
import org.patientview.persistence.model.FhirPractitioner;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.Identifier;
import org.patientview.persistence.model.Lookup;
import org.patientview.persistence.model.MigrationUser;
import org.patientview.persistence.model.Question;
import org.patientview.persistence.model.QuestionAnswer;
import org.patientview.persistence.model.QuestionOption;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.Survey;
import org.patientview.persistence.model.SurveyResponse;
import org.patientview.persistence.model.SurveyResponseScore;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.UserFeature;
import org.patientview.persistence.model.UserInformation;
import org.patientview.persistence.model.enums.BodySites;
import org.patientview.persistence.model.enums.DiagnosisTypes;
import org.patientview.persistence.model.enums.DiagnosticReportObservationTypes;
import org.patientview.persistence.model.enums.DiagnosticReportTypes;
import org.patientview.persistence.model.enums.EncounterTypes;
import org.patientview.persistence.model.enums.FeatureType;
import org.patientview.persistence.model.enums.IbdDiseaseExtent;
import org.patientview.persistence.model.enums.IdentifierTypes;
import org.patientview.persistence.model.enums.LetterTypes;
import org.patientview.persistence.model.enums.MigrationStatus;
import org.patientview.persistence.model.enums.NonTestObservationTypes;
import org.patientview.persistence.model.enums.PractitionerRoles;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.persistence.model.enums.RoleType;
import org.patientview.persistence.model.enums.ScoreSeverity;
import org.patientview.persistence.model.enums.SurveyResponseScoreTypes;
import org.patientview.persistence.model.enums.UserInformationTypes;
import org.patientview.repository.AboutmeDao;
import org.patientview.repository.EmailVerificationDao;
import org.patientview.repository.EyeCheckupDao;
import org.patientview.repository.FootCheckupDao;
import org.patientview.repository.PatientDao;
import org.patientview.repository.SpecialtyUserRoleDao;
import org.patientview.repository.UktStatusDao;
import org.patientview.repository.UserDao;
import org.patientview.repository.UserMappingDao;
import org.patientview.service.DiagnosisManager;
import org.patientview.service.EyeCheckupManager;
import org.patientview.service.FootCheckupManager;
import org.patientview.service.LetterManager;
import org.patientview.service.MedicineManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by james@solidstategroup.com
 * Created on 06/06/2014
 */
@Service
public class UserDataMigrationServiceImpl implements UserDataMigrationService {

    private static final Logger LOG = LoggerFactory.getLogger(UserDataMigrationServiceImpl.class);
    private static final String COMMENT_RESULT_HEADING = "resultcomment";

    @Inject
    private UserDao userDao;

    @Inject
    private UserMappingDao userMappingDao;

    @Inject
    private AboutmeDao aboutMeDao;

    @Inject
    private EmailVerificationDao emailVerificationDao;

    @Inject
    private DiagnosisManager diagnosisManager;

    @Inject
    private PatientDao patientDao;

    @Inject
    private AdminDataMigrationService adminDataMigrationService;

    @Inject
    private EyeCheckupManager eyeCheckupManager;

    @Inject
    private EyeCheckupDao eyeCheckupDao;

    @Inject
    private FootCheckupDao footCheckupDao;

    @Inject
    private FootCheckupManager footCheckupManager;

    @Inject
    private LetterManager letterManager;

    @Inject
    private MedicineManager medicineManager;

    @Inject
    private SpecialtyUserRoleDao specialtyUserRoleDao;

    @Inject
    private UktStatusDao ukTransplantDao;

    @Inject
    private ExecutorService userTaskExecutor;

    private List<Group> groups;
    private List<Role> roles;
    private List<Lookup> lookups;
    private List<Feature> features;

    private Set<String> eyeCheckupNhsNos;
    private Set<String> footCheckupNhsNos;

    private @Value("${migration.username}") String migrationUsername;
    private @Value("${migration.password}") String migrationPassword;
    private @Value("${patientview.api.url}") String patientviewApiUrl;
    private @Value("${jdbc.url}") String jdbcUrl;
    private @Value("${jdbc.username}") String jdbcUsername;
    private @Value("${jdbc.password}") String jdbcPassword;

    private static final boolean IBD = true;

    @Inject
    private DataSource dataSource;

    private MigrationUser addAllergyTableData(MigrationUser migrationUser, String nhsNo, Group unit) {
        Connection connection = null;
        String sql = "SELECT confidenceLevel, infoSource, reaction, " +
                "recordedDate, substance, typeCode " +
                "FROM pv_allergy WHERE nhsno = "  + nhsNo + " AND unitcode = '" + unit.getCode() + "'";

        try {
            DataSource dataSource = new DriverManagerDataSource(jdbcUrl, jdbcUsername, jdbcPassword);
            connection = dataSource.getConnection();
            Statement statement = connection.createStatement();
            ResultSet results = statement.executeQuery(sql);

            while ((results.next())) {

                FhirAllergy fhirAllergy = new FhirAllergy();
                fhirAllergy.setIdentifier(nhsNo);
                fhirAllergy.setStatus("Active");
                fhirAllergy.setGroup(getGroupByCode(unit.getCode()));

                // todo: fhir mapping
                if (StringUtils.isNotEmpty(results.getString(1))) {
                    fhirAllergy.setConfidenceLevel(results.getString(1));
                }

                // todo: fhir mapping
                if (StringUtils.isNotEmpty(results.getString(2))) {
                    fhirAllergy.setInfoSource(results.getString(2));
                }

                if (StringUtils.isNotEmpty(results.getString(3))) {
                    fhirAllergy.setReaction(results.getString(3));
                }

                if (results.getTimestamp(4) != null) {
                    fhirAllergy.setRecordedDate(results.getTimestamp(4));
                }

                if (StringUtils.isNotEmpty(results.getString(5))) {
                    fhirAllergy.setSubstance(results.getString(5));
                }

                if (StringUtils.isNotEmpty(results.getString(6))) {
                    fhirAllergy.setType(results.getString(6));
                }

                // add to transport object
                migrationUser.getAllergies().add(fhirAllergy);
            }
        } catch (SQLException se) {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException se2) {
                    LOG.error(se2.getMessage());
                }
            }
        }

        return migrationUser;
    }

    private MigrationUser addCheckupTablesData(MigrationUser migrationUser, Group unit) {

        List<EyeCheckup> eyeCheckups = eyeCheckupManager.get(migrationUser.getUser().getUsername());

        if (CollectionUtils.isNotEmpty(eyeCheckups)) {
            for (EyeCheckup eyeCheckup : eyeCheckups) {
                if (eyeCheckup.getUnitcode().equals(unit.getCode())) {

                    // leftMGrade
                    if (StringUtils.isNotEmpty(eyeCheckup.getLeftMGrade())) {
                        FhirObservation eyeCheckupLeftMGrade = new FhirObservation();
                        eyeCheckupLeftMGrade.setBodySite(BodySites.LEFT_EYE.toString());
                        eyeCheckupLeftMGrade.setIdentifier(eyeCheckup.getNhsno());
                        eyeCheckupLeftMGrade.setName(NonTestObservationTypes.MGRADE.toString());
                        eyeCheckupLeftMGrade.setValue(eyeCheckup.getLeftMGrade());
                        eyeCheckupLeftMGrade.setGroup(unit);
                        eyeCheckupLeftMGrade.setApplies(eyeCheckup.getLastRetinalDate().getTime());
                        eyeCheckupLeftMGrade.setLocation(eyeCheckup.getLastRetinalPlace());
                        migrationUser.getObservations().add(eyeCheckupLeftMGrade);
                    }

                    // leftRGrade
                    if (StringUtils.isNotEmpty(eyeCheckup.getLeftRGrade())) {
                        FhirObservation eyeCheckupLeftRGrade = new FhirObservation();
                        eyeCheckupLeftRGrade.setBodySite(BodySites.LEFT_EYE.toString());
                        eyeCheckupLeftRGrade.setIdentifier(eyeCheckup.getNhsno());
                        eyeCheckupLeftRGrade.setName(NonTestObservationTypes.RGRADE.toString());
                        eyeCheckupLeftRGrade.setValue(eyeCheckup.getLeftRGrade());
                        eyeCheckupLeftRGrade.setGroup(unit);
                        eyeCheckupLeftRGrade.setApplies(eyeCheckup.getLastRetinalDate().getTime());
                        eyeCheckupLeftRGrade.setLocation(eyeCheckup.getLastRetinalPlace());
                        migrationUser.getObservations().add(eyeCheckupLeftRGrade);
                    }

                    // leftVA
                    if (StringUtils.isNotEmpty(eyeCheckup.getLeftVA())) {
                        FhirObservation eyeCheckupLeftVA = new FhirObservation();
                        eyeCheckupLeftVA.setBodySite(BodySites.LEFT_EYE.toString());
                        eyeCheckupLeftVA.setIdentifier(eyeCheckup.getNhsno());
                        eyeCheckupLeftVA.setName(NonTestObservationTypes.VA.toString());
                        eyeCheckupLeftVA.setValue(eyeCheckup.getLeftVA());
                        eyeCheckupLeftVA.setGroup(unit);
                        eyeCheckupLeftVA.setApplies(eyeCheckup.getLastRetinalDate().getTime());
                        eyeCheckupLeftVA.setLocation(eyeCheckup.getLastRetinalPlace());
                        migrationUser.getObservations().add(eyeCheckupLeftVA);
                    }

                    // rightMGrade
                    if (StringUtils.isNotEmpty(eyeCheckup.getRightMGrade())) {
                        FhirObservation eyeCheckupRightMGrade = new FhirObservation();
                        eyeCheckupRightMGrade.setBodySite(BodySites.RIGHT_EYE.toString());
                        eyeCheckupRightMGrade.setIdentifier(eyeCheckup.getNhsno());
                        eyeCheckupRightMGrade.setName(NonTestObservationTypes.MGRADE.toString());
                        eyeCheckupRightMGrade.setValue(eyeCheckup.getRightMGrade());
                        eyeCheckupRightMGrade.setGroup(unit);
                        eyeCheckupRightMGrade.setApplies(eyeCheckup.getLastRetinalDate().getTime());
                        eyeCheckupRightMGrade.setLocation(eyeCheckup.getLastRetinalPlace());
                        migrationUser.getObservations().add(eyeCheckupRightMGrade);
                    }

                    // rightRGrade
                    if (StringUtils.isNotEmpty(eyeCheckup.getRightRGrade())) {
                        FhirObservation eyeCheckupRightRGrade = new FhirObservation();
                        eyeCheckupRightRGrade.setBodySite(BodySites.RIGHT_EYE.toString());
                        eyeCheckupRightRGrade.setIdentifier(eyeCheckup.getNhsno());
                        eyeCheckupRightRGrade.setName(NonTestObservationTypes.RGRADE.toString());
                        eyeCheckupRightRGrade.setValue(eyeCheckup.getRightRGrade());
                        eyeCheckupRightRGrade.setGroup(unit);
                        eyeCheckupRightRGrade.setApplies(eyeCheckup.getLastRetinalDate().getTime());
                        eyeCheckupRightRGrade.setLocation(eyeCheckup.getLastRetinalPlace());
                        migrationUser.getObservations().add(eyeCheckupRightRGrade);
                    }

                    // rightVA
                    if (StringUtils.isNotEmpty(eyeCheckup.getRightVA())) {
                        FhirObservation eyeCheckupRightVA = new FhirObservation();
                        eyeCheckupRightVA.setBodySite(BodySites.RIGHT_EYE.toString());
                        eyeCheckupRightVA.setIdentifier(eyeCheckup.getNhsno());
                        eyeCheckupRightVA.setName(NonTestObservationTypes.VA.toString());
                        eyeCheckupRightVA.setValue(eyeCheckup.getRightVA());
                        eyeCheckupRightVA.setGroup(unit);
                        eyeCheckupRightVA.setApplies(eyeCheckup.getLastRetinalDate().getTime());
                        eyeCheckupRightVA.setLocation(eyeCheckup.getLastRetinalPlace());
                        migrationUser.getObservations().add(eyeCheckupRightVA);
                    }
                }
            }
        }

        List<FootCheckup> footCheckups = footCheckupManager.get(migrationUser.getUser().getUsername());

        if (CollectionUtils.isNotEmpty(footCheckups)) {
            for (FootCheckup footCheckup : footCheckups) {
                if (footCheckup.getUnitcode().equals(unit.getCode())) {

                    // leftDpPulse
                    if (StringUtils.isNotEmpty(footCheckup.getLeftDpPulse())) {
                        FhirObservation footCheckupLeftDpPulse = new FhirObservation();
                        footCheckupLeftDpPulse.setBodySite(BodySites.LEFT_FOOT.toString());
                        footCheckupLeftDpPulse.setIdentifier(footCheckup.getNhsno());
                        footCheckupLeftDpPulse.setName(NonTestObservationTypes.DPPULSE.toString());
                        footCheckupLeftDpPulse.setValue(footCheckup.getLeftDpPulse());
                        footCheckupLeftDpPulse.setGroup(unit);
                        footCheckupLeftDpPulse.setApplies(footCheckup.getFootCheckDate().getTime());
                        footCheckupLeftDpPulse.setLocation(footCheckup.getFootCheckPlace());
                        migrationUser.getObservations().add(footCheckupLeftDpPulse);
                    }

                    // leftPtPulse
                    if (StringUtils.isNotEmpty(footCheckup.getLeftPtPulse())) {
                        FhirObservation footCheckupLeftPtPulse = new FhirObservation();
                        footCheckupLeftPtPulse.setBodySite(BodySites.LEFT_FOOT.toString());
                        footCheckupLeftPtPulse.setIdentifier(footCheckup.getNhsno());
                        footCheckupLeftPtPulse.setName(NonTestObservationTypes.PTPULSE.toString());
                        footCheckupLeftPtPulse.setValue(footCheckup.getLeftPtPulse());
                        footCheckupLeftPtPulse.setGroup(unit);
                        footCheckupLeftPtPulse.setApplies(footCheckup.getFootCheckDate().getTime());
                        footCheckupLeftPtPulse.setLocation(footCheckup.getFootCheckPlace());
                        migrationUser.getObservations().add(footCheckupLeftPtPulse);
                    }

                    // rightDpPulse
                    if (StringUtils.isNotEmpty(footCheckup.getRightDpPulse())) {
                        FhirObservation footCheckupRightDpPulse = new FhirObservation();
                        footCheckupRightDpPulse.setBodySite(BodySites.RIGHT_FOOT.toString());
                        footCheckupRightDpPulse.setIdentifier(footCheckup.getNhsno());
                        footCheckupRightDpPulse.setName(NonTestObservationTypes.DPPULSE.toString());
                        footCheckupRightDpPulse.setValue(footCheckup.getRightDpPulse());
                        footCheckupRightDpPulse.setGroup(unit);
                        footCheckupRightDpPulse.setApplies(footCheckup.getFootCheckDate().getTime());
                        footCheckupRightDpPulse.setLocation(footCheckup.getFootCheckPlace());
                        migrationUser.getObservations().add(footCheckupRightDpPulse);
                    }

                    // rightPtPulse
                    if (StringUtils.isNotEmpty(footCheckup.getRightPtPulse())) {
                        FhirObservation footCheckupRightPtPulse = new FhirObservation();
                        footCheckupRightPtPulse.setBodySite(BodySites.RIGHT_FOOT.toString());
                        footCheckupRightPtPulse.setIdentifier(footCheckup.getNhsno());
                        footCheckupRightPtPulse.setName(NonTestObservationTypes.PTPULSE.toString());
                        footCheckupRightPtPulse.setValue(footCheckup.getRightPtPulse());
                        footCheckupRightPtPulse.setGroup(unit);
                        footCheckupRightPtPulse.setApplies(footCheckup.getFootCheckDate().getTime());
                        footCheckupRightPtPulse.setLocation(footCheckup.getFootCheckPlace());
                        migrationUser.getObservations().add(footCheckupRightPtPulse);
                    }
                }
            }
        }

        return migrationUser;
    }

    private MigrationUser addColitisSymptoms(MigrationUser migrationUser, String nhsNo) {
        Connection connection = null;
        String sql = "SELECT feeling_id, score, symptomDate, " +
                "complication_id, number_of_stools_daytime_id, number_of_stools_nighttime_id, " +
                "present_blood_id, toilet_timing_id FROM ibd_colitis_symptoms WHERE nhsno = "  + nhsNo;

        // note these survey responses are hardcoded (see V6__Questions.sql), between ibd id and question options
        Map<Long, Long> feelingMap = new HashMap<Long, Long>();
        feelingMap.put(0L, 5L);
        feelingMap.put(1L, 6L);
        feelingMap.put(2L, 7L);
        feelingMap.put(3L, 8L);
        feelingMap.put(4L, 9L);

        Map<Long, Long> complicationMap = new HashMap<Long, Long>();
        complicationMap.put(0L, 10L);
        complicationMap.put(1L, 11L);
        complicationMap.put(2L, 12L);
        complicationMap.put(3L, 13L);
        complicationMap.put(4L, 14L);
        complicationMap.put(5L, 15L);
        complicationMap.put(6L, 16L);
        complicationMap.put(7L, 17L);

        Map<Long, Long> stoolDaytimeMap = new HashMap<Long, Long>();
        stoolDaytimeMap.put(0L, 22L);
        stoolDaytimeMap.put(1L, 23L);
        stoolDaytimeMap.put(2L, 24L);
        stoolDaytimeMap.put(3L, 25L);

        Map<Long, Long> stoolNighttimeMap = new HashMap<Long, Long>();
        stoolNighttimeMap.put(-1L, null);
        stoolNighttimeMap.put(1L, 26L);
        stoolNighttimeMap.put(2L, 27L);
        stoolNighttimeMap.put(3L, 28L);

        Map<Long, Long> presentBloodMap = new HashMap<Long, Long>();
        presentBloodMap.put(0L, 33L);
        presentBloodMap.put(1L, 34L);
        presentBloodMap.put(2L, 35L);
        presentBloodMap.put(3L, 36L);

        Map<Long, Long> toiletTimingMap = new HashMap<Long, Long>();
        toiletTimingMap.put(0L, 29L);
        toiletTimingMap.put(1L, 30L);
        toiletTimingMap.put(2L, 31L);
        toiletTimingMap.put(3L, 32L);

        try {
            DataSource dataSource = new DriverManagerDataSource(jdbcUrl, jdbcUsername, jdbcPassword);
            connection = dataSource.getConnection();
            Statement statement = connection.createStatement();
            ResultSet results = statement.executeQuery(sql);

            while ((results.next())) {

                Survey survey = new Survey();
                survey.setId(2L);

                SurveyResponse surveyResponse = new SurveyResponse();
                surveyResponse.setSurvey(survey);

                // feeling_id
                QuestionOption questionOption1 = new QuestionOption();
                questionOption1.setId(feelingMap.get(results.getLong(1)));
                QuestionAnswer questionAnswer1 = new QuestionAnswer();
                questionAnswer1.setQuestionOption(questionOption1);
                Question question1 = new Question();
                question1.setId(10L);
                questionAnswer1.setQuestion(question1);
                surveyResponse.getQuestionAnswers().add(questionAnswer1);

                // complication_id
                QuestionOption questionOption2 = new QuestionOption();
                questionOption2.setId(complicationMap.get(results.getLong(4)));
                QuestionAnswer questionAnswer2 = new QuestionAnswer();
                questionAnswer2.setQuestionOption(questionOption2);
                Question question2 = new Question();
                question2.setId(11L);
                questionAnswer2.setQuestion(question2);
                surveyResponse.getQuestionAnswers().add(questionAnswer2);

                // number_of_stools_daytime_id
                QuestionOption questionOption3 = new QuestionOption();
                questionOption3.setId(stoolDaytimeMap.get(results.getLong(5)));
                QuestionAnswer questionAnswer3 = new QuestionAnswer();
                questionAnswer3.setQuestionOption(questionOption3);
                Question question3 = new Question();
                question3.setId(6L);
                questionAnswer3.setQuestion(question3);
                surveyResponse.getQuestionAnswers().add(questionAnswer3);

                // number_of_stools_nighttime_id
                if (results.getLong(6) != -1L) {
                    QuestionOption questionOption4 = new QuestionOption();
                    questionOption4.setId(stoolNighttimeMap.get(results.getLong(6)));
                    QuestionAnswer questionAnswer4 = new QuestionAnswer();
                    questionAnswer4.setQuestionOption(questionOption4);
                    Question question4 = new Question();
                    question4.setId(7L);
                    questionAnswer4.setQuestion(question4);
                    surveyResponse.getQuestionAnswers().add(questionAnswer4);
                }

                // present_blood_id
                QuestionOption questionOption5 = new QuestionOption();
                questionOption5.setId(presentBloodMap.get(results.getLong(7)));
                QuestionAnswer questionAnswer5 = new QuestionAnswer();
                questionAnswer5.setQuestionOption(questionOption5);
                Question question5 = new Question();
                question5.setId(9L);
                questionAnswer5.setQuestion(question5);
                surveyResponse.getQuestionAnswers().add(questionAnswer5);

                // toilet_timing_id
                QuestionOption questionOption6 = new QuestionOption();
                questionOption6.setId(toiletTimingMap.get(results.getLong(8)));
                QuestionAnswer questionAnswer6 = new QuestionAnswer();
                questionAnswer6.setQuestionOption(questionOption6);
                Question question6 = new Question();
                question6.setId(8L);
                questionAnswer6.setQuestion(question6);
                surveyResponse.getQuestionAnswers().add(questionAnswer6);

                // symptomDate
                surveyResponse.setDate(results.getTimestamp(3));

                // score
                Integer score = results.getInt(2);
                ScoreSeverity severity = ScoreSeverity.UNKNOWN;

                if (score >= 10) {
                    severity = ScoreSeverity.HIGH;
                } else if (score >= 4) {
                    severity = ScoreSeverity.MEDIUM;
                } else if (score < 4) {
                    severity = ScoreSeverity.LOW;
                }

                surveyResponse.getSurveyResponseScores().add(
                        new SurveyResponseScore(null, SurveyResponseScoreTypes.SYMPTOM_SCORE, score, severity));

                // add to transport object
                migrationUser.getSurveyResponses().add(surveyResponse);
            }
        } catch (SQLException se) {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException se2) {
                    LOG.error(se2.getMessage());
                }
            }
        }

        return migrationUser;
    }

    private MigrationUser addCrohnsSymptoms(MigrationUser migrationUser, String nhsNo) {
        Connection connection = null;
        String sql = "SELECT feeling_id, score, symptomDate, " +
                "abdominal_pain_id, complication_id, mass_in_tummy_id, " +
                "openBowels FROM ibd_crohns_symptoms WHERE nhsno = "  + nhsNo;


        // note these survey responses are hardcoded (see V6__Questions.sql), between ibd id and question options
        Map<Long, Long> abdominalMap = new HashMap<Long, Long>();
        abdominalMap.put(0L, 1L);
        abdominalMap.put(1L, 2L);
        abdominalMap.put(2L, 3L);
        abdominalMap.put(3L, 4L);

        Map<Long, Long> feelingMap = new HashMap<Long, Long>();
        feelingMap.put(0L, 5L);
        feelingMap.put(1L, 6L);
        feelingMap.put(2L, 7L);
        feelingMap.put(3L, 8L);
        feelingMap.put(4L, 9L);

        Map<Long, Long> complicationMap = new HashMap<Long, Long>();
        complicationMap.put(0L, 10L);
        complicationMap.put(1L, 11L);
        complicationMap.put(2L, 12L);
        complicationMap.put(3L, 13L);
        complicationMap.put(4L, 14L);
        complicationMap.put(5L, 15L);
        complicationMap.put(6L, 16L);
        complicationMap.put(7L, 17L);

        Map<Long, Long> massTummyMap = new HashMap<Long, Long>();
        massTummyMap.put(0L, 18L);
        massTummyMap.put(1L, 19L);
        massTummyMap.put(2L, 20L);
        massTummyMap.put(3L, 21L);

        try {
            DataSource dataSource = new DriverManagerDataSource(jdbcUrl, jdbcUsername, jdbcPassword);
            connection = dataSource.getConnection();
            Statement statement = connection.createStatement();
            ResultSet results = statement.executeQuery(sql);

            while ((results.next())) {

                Survey survey = new Survey();
                survey.setId(1L);

                SurveyResponse surveyResponse = new SurveyResponse();
                surveyResponse.setSurvey(survey);

                // feeling_id
                QuestionOption questionOption1 = new QuestionOption();
                questionOption1.setId(feelingMap.get(results.getLong(1)));
                QuestionAnswer questionAnswer1 = new QuestionAnswer();
                questionAnswer1.setQuestionOption(questionOption1);
                Question question1 = new Question();
                question1.setId(3L);
                questionAnswer1.setQuestion(question1);
                surveyResponse.getQuestionAnswers().add(questionAnswer1);

                // abdominal_pain_id
                QuestionOption questionOption2 = new QuestionOption();
                questionOption2.setId(abdominalMap.get(results.getLong(4)));
                QuestionAnswer questionAnswer2 = new QuestionAnswer();
                questionAnswer2.setQuestionOption(questionOption2);
                Question question2 = new Question();
                question2.setId(1L);
                questionAnswer2.setQuestion(question2);
                surveyResponse.getQuestionAnswers().add(questionAnswer2);

                // complication_id
                QuestionOption questionOption3 = new QuestionOption();
                questionOption3.setId(complicationMap.get(results.getLong(5)));
                QuestionAnswer questionAnswer3 = new QuestionAnswer();
                questionAnswer3.setQuestionOption(questionOption3);
                Question question3 = new Question();
                question3.setId(4L);
                questionAnswer3.setQuestion(question3);
                surveyResponse.getQuestionAnswers().add(questionAnswer3);

                // mass_in_tummy_id
                QuestionOption questionOption4 = new QuestionOption();
                questionOption4.setId(massTummyMap.get(results.getLong(6)));
                QuestionAnswer questionAnswer4 = new QuestionAnswer();
                questionAnswer4.setQuestionOption(questionOption4);
                Question question4 = new Question();
                question4.setId(5L);
                questionAnswer4.setQuestion(question4);
                surveyResponse.getQuestionAnswers().add(questionAnswer4);

                // openBowels
                QuestionAnswer questionAnswer5 = new QuestionAnswer();
                questionAnswer5.setValue(results.getString(7));
                Question question5 = new Question();
                question5.setId(2L);
                questionAnswer5.setQuestion(question5);
                surveyResponse.getQuestionAnswers().add(questionAnswer5);

                // symptomDate
                surveyResponse.setDate(results.getTimestamp(3));

                // score
                Integer score = results.getInt(2);
                ScoreSeverity severity = ScoreSeverity.UNKNOWN;

                if (score >= 16) {
                    severity = ScoreSeverity.HIGH;
                } else if (score >= 4) {
                    severity = ScoreSeverity.MEDIUM;
                } else if (score < 4) {
                    severity = ScoreSeverity.LOW;
                }

                surveyResponse.getSurveyResponseScores().add(
                        new SurveyResponseScore(null, SurveyResponseScoreTypes.SYMPTOM_SCORE, score, severity));

                // add to transport object
                migrationUser.getSurveyResponses().add(surveyResponse);
            }
        } catch (SQLException se) {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException se2) {
                    LOG.error(se2.getMessage());
                }
            }
        }

        return migrationUser;
    }

    private MigrationUser addDiagnosisTableData(MigrationUser migrationUser, String nhsNo, Group unit) {
        List<Diagnosis> diagnoses = diagnosisManager.getOtherDiagnoses(nhsNo, unit.getCode());

        if (CollectionUtils.isNotEmpty(diagnoses)) {
            for (Diagnosis diagnosis : diagnoses) {
                FhirCondition condition = new FhirCondition();
                condition.setCategory(DiagnosisTypes.DIAGNOSIS.toString());
                if (StringUtils.isNotEmpty(diagnosis.getDiagnosis())) {
                    condition.setCode(diagnosis.getDiagnosis());
                    condition.setNotes(diagnosis.getDiagnosis());
                }
                condition.setGroup(unit);
                condition.setIdentifier(nhsNo);
                migrationUser.getConditions().add(condition);
            }
        }

        return migrationUser;
    }

    private MigrationUser addDiagnosticTableData(MigrationUser migrationUser, String nhsNo, Group unit) {
        Connection connection = null;
        String sql = "SELECT datestamp, description, diagnostic_type_id FROM diagnostic WHERE nhsno = "
                + nhsNo;

        Map<Long, DiagnosticType> typeMap = new HashMap<Long, DiagnosticType>();
        typeMap.put(1L, DiagnosticType.IMAGING);
        typeMap.put(2L, DiagnosticType.ENDOSCOPY);

        try {
            DataSource dataSource = new DriverManagerDataSource(jdbcUrl, jdbcUsername, jdbcPassword);
            connection = dataSource.getConnection();
            Statement statement = connection.createStatement();
            ResultSet results = statement.executeQuery(sql);

            while ((results.next())) {
                FhirObservation observation = new FhirObservation();
                observation.setValue(results.getString(2));
                observation.setName(DiagnosticReportObservationTypes.DIAGNOSTIC_RESULT.toString());

                FhirDiagnosticReport diagnosticReport = new FhirDiagnosticReport();
                diagnosticReport.setGroup(unit);
                diagnosticReport.setDate(results.getTimestamp(1));
                diagnosticReport.setType(typeMap.get(results.getLong(3)).toString());
                diagnosticReport.setName(" ");
                diagnosticReport.setResult(observation);
                diagnosticReport.setIdentifier(nhsNo);

                migrationUser.getDiagnosticReports().add(diagnosticReport);
            }
        } catch (SQLException se) {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException se2) {
                    LOG.error(se2.getMessage());
                }
            }
        }

        return migrationUser;
    }

    private MigrationUser addIbdData(MigrationUser migrationUser, String nhsNo, Group unit) {

        // allow setting of known diagnosis and disease extent
        Map<Long, String> diagnoses = new HashMap<Long, String>();
        diagnoses.put(1L, "Ulcerative Colitis");
        diagnoses.put(2L, "IBD - Unclassified (IBDU)");
        diagnoses.put(3L, "Crohn's Disease");

        Map<Long, String> diseaseExtents = new HashMap<Long, String>();
        diseaseExtents.put(1L, IbdDiseaseExtent.PROCTITIS.getName());
        diseaseExtents.put(2L, IbdDiseaseExtent.LEFT_SIDED_COLITIS.getName());
        diseaseExtents.put(3L, IbdDiseaseExtent.EXTENSIVE_COLITIS.getName());
        diseaseExtents.put(4L, IbdDiseaseExtent.ILEAL_CROHNS.getName());
        diseaseExtents.put(5L, IbdDiseaseExtent.ILEO_COLONIC_DISEASE.getName());
        diseaseExtents.put(6L, IbdDiseaseExtent.CROHNS_COLITIS.getName());
        diseaseExtents.put(7L, IbdDiseaseExtent.ISOLATED_UPPER_GI_DISEASE.getName());

        Connection connection = null;
        String sql = "SELECT bodyPartAffected, diagnosis_id, disease_extent_id, " +
                "yearForSurveillanceColonoscopy, yearOfDiagnosis, familyHistory, smoking, surgery, vaccinationRecord," +
                "eiManifestations, complications FROM ibd_myibd WHERE nhsno = " + nhsNo;

        try {
            DataSource dataSource = new DriverManagerDataSource(jdbcUrl, jdbcUsername, jdbcPassword);
            connection = dataSource.getConnection();
            Statement statement = connection.createStatement();
            ResultSet results = statement.executeQuery(sql);

            if (results.next()) {
                if (StringUtils.isNotEmpty(results.getString(1))) {
                    migrationUser.getObservations().add(createFhirObservation(nhsNo,
                            NonTestObservationTypes.BODY_PARTS_AFFECTED.toString(), results.getString(1), null, unit));
                }
                if (results.getLong(2) != -1L) {
                    // add condition with year of diagnosis, used instead of diagnosis table
                    FhirCondition condition = new FhirCondition();
                    condition.setCategory(DiagnosisTypes.DIAGNOSIS.toString());
                    condition.setCode(diagnoses.get(results.getLong(2)));
                    condition.setNotes(diagnoses.get(results.getLong(2)));
                    condition.setGroup(unit);
                    condition.setIdentifier(nhsNo);
                    if (results.getTimestamp(5) != null) {
                        condition.setDate(results.getTimestamp(5));
                    }
                    migrationUser.getConditions().add(condition);
                }
                if (results.getLong(3) != -1) {
                    migrationUser.getObservations().add(createFhirObservation(nhsNo,
                            NonTestObservationTypes.IBD_DISEASE_EXTENT.toString(), diseaseExtents.get(results.getLong(3)),
                            null, unit));
                }
                if (results.getTimestamp(4) != null) {
                    migrationUser.getObservations().add(createFhirObservation(nhsNo,
                            NonTestObservationTypes.COLONOSCOPY_SURVEILLANCE.toString(), null,
                            results.getTimestamp(4), unit));
                }
                if (StringUtils.isNotEmpty(results.getString(6))) {
                    migrationUser.getObservations().add(createFhirObservation(nhsNo,
                            NonTestObservationTypes.FAMILY_HISTORY.toString(), results.getString(6), null, unit));
                }
                if (StringUtils.isNotEmpty(results.getString(7))) {
                    migrationUser.getObservations().add(createFhirObservation(nhsNo,
                            NonTestObservationTypes.SMOKING_HISTORY.toString(), results.getString(7), null, unit));
                }
                if (StringUtils.isNotEmpty(results.getString(8))) {
                    migrationUser.getObservations().add(createFhirObservation(nhsNo,
                            NonTestObservationTypes.SURGICAL_HISTORY.toString(), results.getString(8), null, unit));
                }
                if (StringUtils.isNotEmpty(results.getString(9))) {
                    migrationUser.getObservations().add(createFhirObservation(nhsNo,
                            NonTestObservationTypes.VACCINATION_RECORD.toString(), results.getString(9), null, unit));
                }
                if (StringUtils.isNotEmpty(results.getString(10))) {
                    migrationUser.getObservations().add(createFhirObservation(nhsNo,
                            NonTestObservationTypes.IBD_EI_MANIFESTATIONS.toString(), results.getString(10), null, unit));
                }
                if (StringUtils.isNotEmpty(results.getString(11))) {
                    migrationUser.getObservations().add(createFhirObservation(nhsNo,
                            NonTestObservationTypes.IBD_DISEASE_COMPLICATIONS.toString(), results.getString(11), null,
                            unit));
                }
            }
        } catch (SQLException se) {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException se2) {
                    LOG.error(se2.getMessage());
                }
            }
        }

        return migrationUser;
    }

    private FhirPatient addIbdPractitioners(FhirPatient patient, String nhsNo) {
        Connection connection = null;
        String sql = "SELECT namedConsultant, nurses FROM ibd_myibd WHERE nhsno = " + nhsNo;

        try {
            DataSource dataSource = new DriverManagerDataSource(jdbcUrl, jdbcUsername, jdbcPassword);
            connection = dataSource.getConnection();
            Statement statement = connection.createStatement();
            ResultSet results = statement.executeQuery(sql);

            if (results.next()) {
                if (StringUtils.isNotEmpty(results.getString(1))) {
                    FhirPractitioner practitioner = new FhirPractitioner();
                    practitioner.setName(CommonUtils.cleanSql(results.getString(1)));
                    practitioner.setRole(PractitionerRoles.NAMED_CONSULTANT.toString());
                    patient.getPractitioners().add(practitioner);
                }
                if (StringUtils.isNotEmpty(results.getString(2))) {
                    FhirPractitioner practitioner = new FhirPractitioner();
                    practitioner.setName(CommonUtils.cleanSql(results.getString(2)));
                    practitioner.setRole(PractitionerRoles.IBD_NURSE.toString());
                    patient.getPractitioners().add(practitioner);
                }
            }
        } catch (SQLException se) {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException se2) {
                    LOG.error(se2.getMessage());
                }
            }
        }

        return patient;
    }

    private MigrationUser addLetterTableData(MigrationUser migrationUser, String nhsNo, Group unit) {

        List<Letter> letters = letterManager.getByNhsnoAndUnitcode(nhsNo, unit.getCode());

        if (CollectionUtils.isNotEmpty(letters)) {

            // remove duplicates and empty letters
            Map<String, Letter> letterMap = new HashMap<String, Letter>();
            for (Letter letter : letters) {
                if (StringUtils.isNotEmpty(letter.getContent())) {
                    letterMap.put(letter.getDate().toString() + letter.getContent(), letter);
                }
            }

            for (Letter letter : letterMap.values()) {
                FhirDocumentReference documentReference = new FhirDocumentReference();
                documentReference.setGroup(unit);
                if (letter.getDate() != null) {
                    documentReference.setDate(letter.getDate().getTime());
                }
                if (StringUtils.isNotEmpty(letter.getType())) {
                    documentReference.setType(letter.getType());
                }
                if (StringUtils.isNotEmpty(letter.getContent())) {
                    String utf8Content = letter.getContent().replaceAll("[^\\u0000-\\uFFFF]", "");
                    documentReference.setContent(utf8Content);
                }
                documentReference.setIdentifier(nhsNo);
                migrationUser.getDocumentReferences().add(documentReference);
            }
        }

        return migrationUser;
    }

    private MigrationUser addMedicineTableData(MigrationUser migrationUser, String nhsNo, Group unit) {

        List<Medicine> medicines = medicineManager.getByNhsnoAndUnitcode(nhsNo, unit.getCode());

        if (CollectionUtils.isNotEmpty(medicines)) {
            for (Medicine medicine : medicines) {
                FhirMedicationStatement medicationStatement = new FhirMedicationStatement();
                if (StringUtils.isNotEmpty(medicine.getDose())) {
                    medicationStatement.setDose(medicine.getDose());
                }
                if (StringUtils.isNotEmpty(medicine.getName())) {
                    medicationStatement.setName(medicine.getName());
                }
                if (medicine.getStartdate() != null) {
                    medicationStatement.setStartDate(medicine.getStartdate().getTime());
                }
                medicationStatement.setGroup(unit);
                medicationStatement.setIdentifier(nhsNo);
                migrationUser.getMedicationStatements().add(medicationStatement);
            }
        }

        return migrationUser;
    }

    private MigrationUser addPatientTableData(MigrationUser migrationUser, Patient pv1PatientRecord,
                                              Group unit, UktStatus uktStatus) {
        // date of birth in user object
        if (pv1PatientRecord.getDateofbirth() != null) {
            migrationUser.getUser().setDateOfBirth(pv1PatientRecord.getDateofbirth());
        }

        // - basic patient data
        FhirPatient patient = new FhirPatient();
        if (StringUtils.isNotEmpty(pv1PatientRecord.getForename())) {
            patient.setForename(pv1PatientRecord.getForename());
        }
        if (StringUtils.isNotEmpty(pv1PatientRecord.getSurname())) {
            if (pv1PatientRecord.getSurname().equals(".")) {
                patient.setSurname("");
            } else {
                patient.setSurname(pv1PatientRecord.getSurname());
            }
        }
        if (StringUtils.isNotEmpty(pv1PatientRecord.getSex())) {
            patient.setGender(pv1PatientRecord.getSex());
        }
        if (StringUtils.isNotEmpty(pv1PatientRecord.getAddress1())) {
            patient.setAddress1(pv1PatientRecord.getAddress1());
        }
        if (StringUtils.isNotEmpty(pv1PatientRecord.getAddress2())) {
            patient.setAddress2(pv1PatientRecord.getAddress2());
        }
        if (StringUtils.isNotEmpty(pv1PatientRecord.getAddress3())) {
            patient.setAddress3(pv1PatientRecord.getAddress3());
        }
        if (StringUtils.isNotEmpty(pv1PatientRecord.getAddress4())) {
            patient.setAddress4(pv1PatientRecord.getAddress4());
        }
        if (StringUtils.isNotEmpty(pv1PatientRecord.getPostcode())) {
            patient.setPostcode(pv1PatientRecord.getPostcode());
        }
        if (pv1PatientRecord.getDateofbirth() != null) {
            patient.setDateOfBirth(pv1PatientRecord.getDateofbirth());
        }
        if (StringUtils.isNotEmpty(pv1PatientRecord.getNhsno())) {
            patient.setIdentifier(pv1PatientRecord.getNhsno());
        }
        patient.setGroup(unit);

        // - patient identifiers (FHIR identifiers to be stored in FHIR patient record,
        // not patientview identifiers)
        if (StringUtils.isNotEmpty(pv1PatientRecord.getNhsno())) {
            FhirIdentifier nhsNumber = new FhirIdentifier();
            nhsNumber.setValue(pv1PatientRecord.getNhsno());

            // set label based on contents (NHS/CHI/H&SC
            nhsNumber.setLabel(getIdentifierType(pv1PatientRecord.getNhsno()).getValue());
            patient.getIdentifiers().add(nhsNumber);
        }

        // hospital number (not shown on ui)
        if (StringUtils.isNotEmpty(pv1PatientRecord.getHospitalnumber())) {
            FhirIdentifier hospitalNumber = new FhirIdentifier();
            hospitalNumber.setValue(pv1PatientRecord.getHospitalnumber());
            hospitalNumber.setLabel(IdentifierTypes.HOSPITAL_NUMBER.toString());
            patient.getIdentifiers().add(hospitalNumber);
        }

        // - patient contact data
        patient.setContacts(new ArrayList<FhirContact>());

        if (StringUtils.isNotEmpty(pv1PatientRecord.getTelephone1())) {
            FhirContact fhirContact = new FhirContact();
            fhirContact.setUse("home");
            fhirContact.setSystem("phone");
            fhirContact.setValue(pv1PatientRecord.getTelephone1());
            patient.getContacts().add(fhirContact);
        }

        if (StringUtils.isNotEmpty(pv1PatientRecord.getTelephone2())) {
            FhirContact fhirContact = new FhirContact();
            fhirContact.setUse("home");
            fhirContact.setSystem("phone");
            fhirContact.setValue(pv1PatientRecord.getTelephone2());
            patient.getContacts().add(fhirContact);
        }

        if (StringUtils.isNotEmpty(pv1PatientRecord.getMobile())) {
            FhirContact fhirContact = new FhirContact();
            fhirContact.setUse("mobile");
            fhirContact.setSystem("phone");
            fhirContact.setValue(pv1PatientRecord.getMobile());
            patient.getContacts().add(fhirContact);
        }

        // add Condition / EDTA diagnosis (pv1 patient table) - note genericDiagnosis is RADAR
        if (StringUtils.isNotEmpty(pv1PatientRecord.getDiagnosis())) {
            FhirCondition conditionEdta = new FhirCondition();
            conditionEdta.setCategory(DiagnosisTypes.DIAGNOSIS_EDTA.toString());
            conditionEdta.setCode(pv1PatientRecord.getDiagnosis());
            conditionEdta.setNotes(pv1PatientRecord.getDiagnosis());
            conditionEdta.setGroup(unit);
            conditionEdta.setIdentifier(pv1PatientRecord.getNhsno());
            conditionEdta.setDate(pv1PatientRecord.getDiagnosisDate());
            migrationUser.getConditions().add(conditionEdta);
        }

        // add Encounter / treatment  (pv1 patient table)
        if (StringUtils.isNotEmpty(pv1PatientRecord.getTreatment())) {
            FhirEncounter treatment = new FhirEncounter();
            treatment.setEncounterType(EncounterTypes.TREATMENT.toString());
            treatment.setGroup(unit);
            treatment.setStatus(pv1PatientRecord.getTreatment());
            treatment.setIdentifier(pv1PatientRecord.getNhsno());
            migrationUser.getEncounters().add(treatment);
        }

        // add Encounter / transplant status (pv1 patient table)
        if (StringUtils.isNotEmpty(pv1PatientRecord.getTransplantstatus())) {
            FhirEncounter transplant = new FhirEncounter();
            transplant.setEncounterType(EncounterTypes.TRANSPLANT_STATUS.toString());
            transplant.setGroup(unit);
            transplant.setStatus(pv1PatientRecord.getTransplantstatus());
            transplant.setIdentifier(pv1PatientRecord.getNhsno());
            migrationUser.getEncounters().add(transplant);
        }

        // ukt status
        if (uktStatus != null) {
            if (StringUtils.isNotEmpty(uktStatus.getKidney())) {
                FhirEncounter transplant = new FhirEncounter();
                transplant.setEncounterType(EncounterTypes.TRANSPLANT_STATUS_KIDNEY.toString());
                transplant.setGroup(unit);
                transplant.setStatus(uktStatus.getKidney());
                transplant.setIdentifier(pv1PatientRecord.getNhsno());
                migrationUser.getEncounters().add(transplant);
            }
            /*if (StringUtils.isNotEmpty(uktStatus.getPancreas())) {
                FhirEncounter transplant = new FhirEncounter();
                transplant.setEncounterType(EncounterTypes.TRANSPLANT_STATUS_PANCREAS.toString());
                transplant.setGroup(unit);
                transplant.setStatus(uktStatus.getPancreas());
                transplant.setIdentifier(pv1PatientRecord.getNhsno());
                migrationUser.getEncounters().add(transplant);
            }*/
        }

        // - practitioner (gp)
        if (StringUtils.isNotEmpty(pv1PatientRecord.getGpname())
                || StringUtils.isNotEmpty(pv1PatientRecord.getGpaddress1())) {
            FhirPractitioner practitioner = new FhirPractitioner();

            if (StringUtils.isNotEmpty(pv1PatientRecord.getGpname())) {
                practitioner.setName(CommonUtils.cleanSql(pv1PatientRecord.getGpname()));
            }
            if (StringUtils.isNotEmpty(pv1PatientRecord.getGpaddress1())) {
                practitioner.setAddress1(CommonUtils.cleanSql(pv1PatientRecord.getGpaddress1()));
            }
            if (StringUtils.isNotEmpty(pv1PatientRecord.getGpaddress2())) {
                practitioner.setAddress2(pv1PatientRecord.getGpaddress2());
            }
            if (StringUtils.isNotEmpty(pv1PatientRecord.getGpaddress3())) {
                practitioner.setAddress3(pv1PatientRecord.getGpaddress3());
            }
            if (StringUtils.isNotEmpty(pv1PatientRecord.getGppostcode())) {
                practitioner.setPostcode(pv1PatientRecord.getGppostcode());
            }
            practitioner.setContacts(new ArrayList<FhirContact>());

            if (StringUtils.isNotEmpty(pv1PatientRecord.getGptelephone())) {
                // - practitioner contact data
                FhirContact practitionerContact = new FhirContact();
                practitionerContact.setUse("work");
                practitionerContact.setSystem("phone");
                practitionerContact.setValue(pv1PatientRecord.getGptelephone());
                practitioner.getContacts().add(practitionerContact);
            }

            patient.getPractitioners().add(practitioner);
        }

        // blood group
        if (StringUtils.isNotEmpty(pv1PatientRecord.getBloodgroup())) {
            FhirObservation bloodGroup = new FhirObservation();
            bloodGroup.setIdentifier(pv1PatientRecord.getNhsno());
            bloodGroup.setName(NonTestObservationTypes.BLOOD_GROUP.toString());
            bloodGroup.setValue(pv1PatientRecord.getBloodgroup());
            bloodGroup.setGroup(unit);

            // if date of birth is set, set to that, otherwise set to new date
            if (pv1PatientRecord.getDateofbirth() != null) {
                bloodGroup.setApplies(pv1PatientRecord.getDateofbirth());
            } else {
                bloodGroup.setApplies(new Date());
            }
            migrationUser.getObservations().add(bloodGroup);
        }

        // IBD practitioners
        if (IBD) {
            patient = addIbdPractitioners(patient, pv1PatientRecord.getNhsno());
        }

        migrationUser.getPatients().add(patient);
        return migrationUser;
    }

    public void bulkUserCreate(String unitCode1, String unitCode2, Long count, RoleName roleName) {
        LOG.info("Starting creation of " + count
                + " generated users, must have -Durl=\"http://localhost:8080/api\" or equivalent");

        ExecutorService concurrentTaskExecutor = Executors.newFixedThreadPool(10);
        Group userUnit1 = adminDataMigrationService.getGroupByCode(unitCode1);
        Group userUnit2 = adminDataMigrationService.getGroupByCode(unitCode2);
        Role userRole = adminDataMigrationService.getRoleByName(roleName);

        List<MigrationUser> migrationUsers = new ArrayList<MigrationUser>();

        if (userUnit1 != null && userUnit2 != null && userRole != null) {
            Date now = new Date();
            LOG.info("Sending " + count + " users to REST service");

            // create users based on Date.now + count increment
            for (Long time = now.getTime(); time<now.getTime() + count; time++) {

                // testing only
                //Long time = 1234L;

                // create user
                User newUser = new User();
                newUser.setForename("sfore" + time.toString());
                newUser.setSurname("ssur");
                newUser.setChangePassword(true);
                newUser.setPassword("pppppp");
                newUser.setLocked(false);
                newUser.setDummy(true);
                newUser.setFailedLogonAttempts(0);
                newUser.setEmail("test" + time.toString() + "@solidstategroup.com");
                newUser.setEmailVerified(false);
                // todo: needs consideration during migration
                newUser.setVerificationCode("emailverify" + time);
                newUser.setUsername(time.toString());
                newUser.setIdentifiers(new HashSet<Identifier>());
                newUser.setLastLogin(now);
                newUser.setDateOfBirth(now);

                // todo: do we need to migrate user.accounthidden?

                // add group role (specialty is added automatically when creating user within a UNIT group)
                Role role = new Role();
                role.setId(userRole.getId());

                Group group1 = new Group();
                group1.setId(userUnit1.getId());
                GroupRole groupRole1 = new GroupRole();
                groupRole1.setGroup(group1);
                groupRole1.setRole(role);

                Group group2 = new Group();
                group2.setId(userUnit2.getId());
                GroupRole groupRole2 = new GroupRole();
                groupRole2.setGroup(group2);
                groupRole2.setRole(role);

                newUser.setGroupRoles(new HashSet<GroupRole>());
                newUser.getGroupRoles().add(groupRole1);
                newUser.getGroupRoles().add(groupRole2);

                // add user feature (usually for staff)
                newUser.setUserFeatures(new HashSet<UserFeature>());
                UserFeature userFeature = new UserFeature();
                Feature feature = adminDataMigrationService.getFeatureByName(FeatureType.MESSAGING.toString());
                userFeature.setFeature(feature);
                newUser.getUserFeatures().add(userFeature);

                if (userRole.getRoleType().getValue().equals(RoleType.PATIENT)) {
                    // add user information (assuming just one SHOULD_KNOW and TALK_ABOUT per user)
                    newUser.setUserInformation(new HashSet<UserInformation>());
                    UserInformation shouldKnow = new UserInformation();
                    shouldKnow.setType(UserInformationTypes.SHOULD_KNOW);
                    shouldKnow.setValue("Should know about me...");
                    newUser.getUserInformation().add(shouldKnow);
                    UserInformation talkAbout = new UserInformation();
                    talkAbout.setType(UserInformationTypes.TALK_ABOUT);
                    talkAbout.setValue("Would like to talk about...");
                    newUser.getUserInformation().add(talkAbout);
                }

                MigrationUser migrationUser = new MigrationUser(newUser);

                // set patientview1 id
                migrationUser.setPatientview1Id(time);

                if (userRole.getRoleType().getValue().equals(RoleType.PATIENT)) {
                    // add comment Observation (comment table in pv1), attach to group (not present in pv1)
                    FhirObservation comment = new FhirObservation();
                    comment.setValue("a patient entered comment about my results");
                    comment.setApplies(new Date(time));
                    comment.setGroup(group1);
                    comment.setComments("a patient entered comment about my results");
                    comment.setName(COMMENT_RESULT_HEADING);
                    comment.setIdentifier(time.toString());
                    migrationUser.getObservations().add(comment);

                    // add Condition / generic diagnosis (pv1 diagnosis table)
                    FhirCondition condition = new FhirCondition();
                    condition.setCategory(DiagnosisTypes.DIAGNOSIS.toString());
                    condition.setCode("Something else 1");
                    condition.setNotes("Something else 1");
                    condition.setGroup(userUnit1);
                    condition.setIdentifier(time.toString());
                    migrationUser.getConditions().add(condition);

                    FhirCondition condition2 = new FhirCondition();
                    condition2.setCategory(DiagnosisTypes.DIAGNOSIS.toString());
                    condition2.setCode("Something else 2");
                    condition2.setNotes("Something else 2");
                    condition2.setGroup(userUnit2);
                    condition2.setIdentifier(time.toString());
                    migrationUser.getConditions().add(condition);

                    // add Condition / EDTA diagnosis (pv1 patient table)
                    FhirCondition conditionEdta = new FhirCondition();
                    conditionEdta.setCategory(DiagnosisTypes.DIAGNOSIS_EDTA.toString());
                    conditionEdta.setCode("00");
                    conditionEdta.setNotes("00");
                    conditionEdta.setGroup(userUnit1);
                    conditionEdta.setIdentifier(time.toString());
                    migrationUser.getConditions().add(conditionEdta);

                    FhirCondition conditionEdta2 = new FhirCondition();
                    conditionEdta2.setCategory(DiagnosisTypes.DIAGNOSIS_EDTA.toString());
                    conditionEdta2.setCode("00");
                    conditionEdta2.setNotes("00");
                    conditionEdta2.setGroup(userUnit2);
                    conditionEdta2.setIdentifier(time.toString());
                    migrationUser.getConditions().add(conditionEdta2);

                    // add Encounter / transplant status (pv1 patient table)
                    FhirEncounter transplant = new FhirEncounter();
                    transplant.setEncounterType(EncounterTypes.TRANSPLANT_STATUS.toString());
                    transplant.setStatus("Live donor transplant");
                    transplant.setIdentifier(time.toString());
                    migrationUser.getEncounters().add(transplant);

                    // add Encounter / treatment  (pv1 patient table)
                    FhirEncounter treatment = new FhirEncounter();
                    treatment.setEncounterType(EncounterTypes.TREATMENT.toString());
                    treatment.setStatus("TP");
                    treatment.setIdentifier(time.toString());
                    migrationUser.getEncounters().add(treatment);

                    // add MedicationStatement
                    FhirMedicationStatement medicationStatement = new FhirMedicationStatement();
                    medicationStatement.setDose("500g");
                    medicationStatement.setName("Paracetemol");
                    medicationStatement.setStartDate(now);
                    medicationStatement.setGroup(userUnit1);
                    medicationStatement.setIdentifier(time.toString());
                    migrationUser.getMedicationStatements().add(medicationStatement);

                    FhirMedicationStatement medicationStatement2 = new FhirMedicationStatement();
                    medicationStatement2.setDose("500g 2");
                    medicationStatement2.setName("Paracetemol 2");
                    medicationStatement2.setStartDate(now);
                    medicationStatement2.setGroup(userUnit2);
                    medicationStatement2.setIdentifier(time.toString());
                    migrationUser.getMedicationStatements().add(medicationStatement2);

                    // add DiagnosticReport and associated Observation (diagnostics, originally IBD now generic)
                    FhirObservation observation = new FhirObservation();
                    observation.setValue("1234567890");
                    observation.setName(DiagnosticReportObservationTypes.DIAGNOSTIC_RESULT.toString());

                    FhirDiagnosticReport diagnosticReport = new FhirDiagnosticReport();
                    diagnosticReport.setGroup(userUnit1);
                    diagnosticReport.setDate(now);
                    diagnosticReport.setType(DiagnosticReportTypes.IMAGING.toString());
                    diagnosticReport.setName("Photo of patient");
                    diagnosticReport.setResult(observation);
                    diagnosticReport.setIdentifier(time.toString());
                    migrationUser.getDiagnosticReports().add(diagnosticReport);

                    FhirObservation observation2 = new FhirObservation();
                    observation2.setValue("1234567890 2");
                    observation2.setName(DiagnosticReportObservationTypes.DIAGNOSTIC_RESULT.toString());

                    FhirDiagnosticReport diagnosticReport2 = new FhirDiagnosticReport();
                    diagnosticReport2.setGroup(userUnit2);
                    diagnosticReport2.setDate(now);
                    diagnosticReport2.setType(DiagnosticReportTypes.IMAGING.toString());
                    diagnosticReport2.setName("Photo of patient 2");
                    diagnosticReport2.setResult(observation2);
                    diagnosticReport2.setIdentifier(time.toString());
                    migrationUser.getDiagnosticReports().add(diagnosticReport2);

                    // add DocumentReference / letter
                    FhirDocumentReference documentReference = new FhirDocumentReference();
                    documentReference.setGroup(userUnit1);
                    documentReference.setDate(now);
                    documentReference.setType(LetterTypes.GENERAL_LETTER.getName());
                    documentReference.setContent("Letter content: text text text " + time + " etc.");
                    documentReference.setIdentifier(time.toString());
                    migrationUser.getDocumentReferences().add(documentReference);

                    FhirDocumentReference documentReference2 = new FhirDocumentReference();
                    documentReference2.setGroup(userUnit2);
                    documentReference2.setDate(now);
                    documentReference2.setType(LetterTypes.GENERAL_LETTER.getName());
                    documentReference2.setContent("Letter content: text text text " + time + " etc. 2");
                    documentReference2.setIdentifier(time.toString());
                    migrationUser.getDocumentReferences().add(documentReference2);

                    // PatientView identifiers
                    // - nhs number (pv1 table)
                    Identifier identifier = new Identifier();
                    identifier.setIdentifier(time.toString());
                    identifier.setIdentifierType(
                            adminDataMigrationService.getLookupByName(IdentifierTypes.NHS_NUMBER.toString()));
                    newUser.getIdentifiers().add(identifier);

                    // - Radar No (pv1 patient table)
                    Identifier radarNo = new Identifier();
                    radarNo.setIdentifier("radar" + time.toString());
                    radarNo.setIdentifierType(
                            adminDataMigrationService.getLookupByName(IdentifierTypes.RADAR_NUMBER.toString()));
                    newUser.getIdentifiers().add(radarNo);

                    // - Hospital No (pv1 patient table)
                /*Identifier hospitalNo = new Identifier();
                hospitalNo.setIdentifier("hospital" + time.toString());
                hospitalNo.setIdentifierType(
                        adminDataMigrationService.getLookupByName(IdentifierTypes.HOSPITAL_NUMBER.toString()));
                newUser.getIdentifiers().add(hospitalNo);*/

                    // add Patient / pv1 patient table data (iterate through pv1 patient)
                    // - basic patient data
                    FhirPatient patient = new FhirPatient();
                    patient.setForename("forename");
                    patient.setSurname("surname");
                    patient.setGender("Male");
                    patient.setAddress1("address1");
                    patient.setAddress2("address2");
                    patient.setAddress3("address3");
                    patient.setAddress4("address4");
                    patient.setPostcode("postcode");
                    patient.setDateOfBirth(now);
                    patient.setIdentifier(time.toString());
                    patient.setGroup(userUnit1);

                    // - patient contact data
                    patient.setContacts(new ArrayList<FhirContact>());
                    FhirContact fhirContact = new FhirContact();
                    fhirContact.setUse("home");
                    fhirContact.setSystem("phone");
                    fhirContact.setValue("01234 56789012");
                    patient.getContacts().add(fhirContact);

                    // - patient identifiers (FHIR identifiers to be stored in FHIR patient record, not patientview identifiers)
                    FhirIdentifier nhsNumber = new FhirIdentifier();
                    nhsNumber.setValue(time.toString());
                    nhsNumber.setLabel(IdentifierTypes.NHS_NUMBER.toString());
                    patient.getIdentifiers().add(nhsNumber);

                    FhirIdentifier hospitalNumber = new FhirIdentifier();
                    hospitalNumber.setValue("hospital" + time.toString());
                    hospitalNumber.setLabel(IdentifierTypes.HOSPITAL_NUMBER.toString());
                    patient.getIdentifiers().add(hospitalNumber);

                /*FhirIdentifier radarNumber = new FhirIdentifier();
                radarNumber.setValue("radar" + time.toString());
                radarNumber.setLabel(IdentifierTypes.NON_UK_UNIQUE.toString());
                patient.getIdentifiers().add(radarNumber);*/

                    // - practitioner (gp)
                    FhirPractitioner practitioner = new FhirPractitioner();
                    practitioner.setName("gpname");
                    practitioner.setAddress1("gpaddress1");
                    practitioner.setAddress2("gpaddress2");
                    practitioner.setAddress3("gpaddress3");
                    practitioner.setPostcode("gppostcode");
                    practitioner.setContacts(new ArrayList<FhirContact>());

                    // - practitioner contact data
                    FhirContact practitionerContact = new FhirContact();
                    practitionerContact.setUse("work");
                    practitionerContact.setSystem("phone");
                    practitionerContact.setValue("09876 54321098");
                    practitioner.getContacts().add(practitionerContact);
                    patient.getPractitioners().add(practitioner);

                    migrationUser.getPatients().add(patient);

                    // second group patient data (less data)
                    // - basic patient data
                    FhirPatient patient2 = new FhirPatient();
                    patient2.setForename("forename 2");
                    patient2.setSurname("surname 2");
                    patient2.setGender("Male 2");
                    patient2.setAddress1("address1 2");
                    patient2.setAddress2("address2 2");
                    patient2.setAddress3("address3 2");
                    patient2.setAddress4("address4 2");
                    patient2.setPostcode("postcode 2");
                    patient2.setDateOfBirth(now);
                    patient2.setIdentifier(time.toString());
                    patient2.setGroup(userUnit2);

                    // - patient contact data
                    patient2.setContacts(new ArrayList<FhirContact>());
                    FhirContact fhirContact2 = new FhirContact();
                    fhirContact2.setUse("home");
                    fhirContact2.setSystem("phone");
                    fhirContact2.setValue("01234 56789012 2");
                    patient2.getContacts().add(fhirContact2);

                    // - patient identifiers (FHIR identifiers to be stored in FHIR patient record, not patientview identifiers)
                    FhirIdentifier nhsNumber2 = new FhirIdentifier();
                    nhsNumber2.setValue(time.toString());
                    nhsNumber2.setLabel(IdentifierTypes.NHS_NUMBER.toString());
                    patient.getIdentifiers().add(nhsNumber);

                    migrationUser.getPatients().add(patient2);

                    // Non test observation types
                    // foot checkup
                    FhirObservation footCheckup1 = new FhirObservation();
                    footCheckup1.setBodySite(BodySites.LEFT_FOOT.toString());
                    footCheckup1.setIdentifier(time.toString());
                    footCheckup1.setName(NonTestObservationTypes.DPPULSE.toString());
                    footCheckup1.setValue("O/E - L.dorsalis pedis absent");
                    footCheckup1.setGroup(group1);
                    footCheckup1.setApplies(new Date(time));
                    migrationUser.getObservations().add(footCheckup1);

                    // eye checkup
                    FhirObservation eyeCheckup1 = new FhirObservation();
                    eyeCheckup1.setBodySite(BodySites.LEFT_EYE.toString());
                    eyeCheckup1.setIdentifier(time.toString());
                    eyeCheckup1.setName(NonTestObservationTypes.MGRADE.toString());
                    eyeCheckup1.setValue("Mx");
                    eyeCheckup1.setGroup(group1);
                    eyeCheckup1.setApplies(new Date(time));
                    migrationUser.getObservations().add(eyeCheckup1);

                    // blood group
                    FhirObservation bloodGroup = new FhirObservation();
                    bloodGroup.setIdentifier(time.toString());
                    bloodGroup.setName(NonTestObservationTypes.BLOOD_GROUP.toString());
                    bloodGroup.setValue("A");
                    bloodGroup.setGroup(group1);
                    bloodGroup.setApplies(new Date(time));
                    migrationUser.getObservations().add(bloodGroup);

                    // set to a patient user
                    migrationUser.setPatient(true);
                } else {
                    // set to a staff user
                    migrationUser.setPatient(false);
                }

                migrationUsers.add(migrationUser);

                // add task and run
                concurrentTaskExecutor.submit(new AsyncMigrateUserTask(migrationUser));
            }

            try {
                // wait forever until all threads are finished
                concurrentTaskExecutor.shutdown();
                concurrentTaskExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
            } catch (Exception e) {
                LOG.error(e.getMessage());
            }

        } else {
            LOG.error("unitcode1: " + unitCode1 + " or unitcode2: " + unitCode1 + ", or role: " + roleName + " do not exist");
        }
    }

    private FhirObservation createFhirObservation(String nhsno, String name, String value, Date applies, Group unit) {
        FhirObservation observation = new FhirObservation();
        observation.setIdentifier(nhsno);
        observation.setName(name);
        observation.setValue(value);
        observation.setGroup(unit);
        observation.setApplies(applies);
        return observation;
    }

    private MigrationUser createMigrationUser(org.patientview.patientview.model.User oldUser, Role patientRole) {

        //LOG.info("--- Migrating " + oldUser.getUsername() + ": starting ---");
        Set<String> identifiers = new HashSet<String>();

        // basic user information
        User newUser = createUser(oldUser);
        boolean isPatient = false;
        boolean error = false;

        List<UserMapping> userMappings = null;

        try {
            userMappings = userMappingDao.getAllNative(oldUser.getUsername());
        } catch (Exception e) {
            LOG.error("Usermapping exception for user " + oldUser.getUsername() + " : " , e);
        }

        if (userMappings != null) {

            for (UserMapping userMapping : userMappings) {
                if (!userMapping.getUnitcode().equalsIgnoreCase("PATIENT") && newUser != null) {

                    // assume usermapping with nhsnumber is a patient
                    if (StringUtils.isNotEmpty(userMapping.getNhsno())) {

                        // is a patient
                        isPatient = true;
                        identifiers.add(userMapping.getNhsno());

                        // add group (specialty is added automatically when creating user within a UNIT group)
                        Group group = getGroupByCode(userMapping.getUnitcode());

                        if (group != null && patientRole != null) {
                            GroupRole groupRole = new GroupRole();
                            groupRole.setGroup(group);
                            groupRole.setRole(patientRole);
                            newUser.getGroupRoles().add(groupRole);
                        }
                    } else {
                        if (IBD) {
                            // get role from tenancyuserrole
                            Group group = getGroupByCode(userMapping.getUnitcode());
                            Role role = getTenancyUserRole(oldUser.getId());

                            if (group != null && role != null) {
                                GroupRole groupRole = new GroupRole();
                                groupRole.setGroup(group);
                                groupRole.setRole(role);
                                newUser.getGroupRoles().add(groupRole);
                            }
                        } else {
                            // is a staff member
                            Role role = null;
                            List<SpecialtyUserRole> specialtyUserRoles
                                    = specialtyUserRoleDao.getRolesNative(oldUser.getId());

                            // TODO: try and fix this - get the first role and apply it to all group (no group role mapping)
                            // TODO: required hack from original PatientView
                            if (CollectionUtils.isNotEmpty(specialtyUserRoles)) {
                                String roleName = specialtyUserRoles.get(0).getRole();

                                if (roleName.equals("unitadmin")) {
                                    role = getRoleByName(RoleName.UNIT_ADMIN);
                                } else if (roleName.equals("unitstaff")) {
                                    role = getRoleByName(RoleName.STAFF_ADMIN);
                                }

                                // add group (specialty is added automatically when creating user within a UNIT group)
                                Group group = getGroupByCode(userMapping.getUnitcode());

                                if (group != null && role != null) {
                                    GroupRole groupRole = new GroupRole();
                                    groupRole.setGroup(group);
                                    groupRole.setRole(role);
                                    newUser.getGroupRoles().add(groupRole);
                                }
                            }
                        }
                    }
                }
            }

            // identifiers and about me (will only be for patient)
            if (isPatient) {
                for (String identifierText : identifiers) {
                    if (StringUtils.isNotEmpty(identifierText)) {
                        Identifier identifier = new Identifier();
                        identifier.setIdentifier(identifierText);

                        // set type based on numeric value (if possible)
                        identifier.setIdentifierType(getIdentifierType(identifierText));
                        newUser.getIdentifiers().add(identifier);

                        Aboutme aboutMe = aboutMeDao.getNative(identifierText);
                        if (aboutMe != null) {
                            newUser.setUserInformation(new HashSet<UserInformation>());

                            UserInformation shouldKnow = new UserInformation();
                            shouldKnow.setType(UserInformationTypes.SHOULD_KNOW);
                            shouldKnow.setValue(aboutMe.getAboutme());
                            newUser.getUserInformation().add(shouldKnow);

                            UserInformation talkAbout = new UserInformation();
                            talkAbout.setType(UserInformationTypes.TALK_ABOUT);
                            talkAbout.setValue(aboutMe.getTalkabout());
                            newUser.getUserInformation().add(talkAbout);
                        }
                    }
                }
            }

            // messaging recipient
            if (oldUser.isIsrecipient()) {
                Feature feature = getFeatureByName(FeatureType.MESSAGING.toString());
                if (feature != null) {
                    newUser.getUserFeatures().add(new UserFeature(feature));
                }
            }

            // feedback recipient
            if (oldUser.isFeedbackRecipient()) {
                Feature feature = getFeatureByName(FeatureType.FEEDBACK.toString());
                if (feature != null) {
                    newUser.getUserFeatures().add(new UserFeature(feature));
                }
            }

            // ECS / GP Medication
            if (oldUser.isEcrOptInStatus()) {
                Feature feature = getFeatureByName(FeatureType.GP_MEDICATION.toString());
                if (feature != null) {
                    UserFeature userFeature = new UserFeature(feature);
                    userFeature.setOptInStatus(true);
                    if (oldUser.getEcrOptInDate() != null) {
                        userFeature.setOptInDate(oldUser.getEcrOptInDate());
                    }
                    newUser.getUserFeatures().add(userFeature);

                    // add to ECS group
                    Group ecsGroup = getGroupByCode("ECS");
                    GroupRole groupRole = new GroupRole();
                    groupRole.setGroup(ecsGroup);
                    groupRole.setRole(patientRole);
                    newUser.getGroupRoles().add(groupRole);
                }
            }

            if (oldUser.getCreated() != null) {
                newUser.setCreated(oldUser.getCreated());
            }

            if (oldUser.getUpdated() != null) {
                newUser.setLastUpdate(oldUser.getUpdated());
            }

            // convert to transport object
            MigrationUser migrationUser = new MigrationUser(newUser);
            migrationUser.setPatient(isPatient);
            migrationUser.setPatientview1Id(oldUser.getId());
            /*migrationUser.setPatients(new ArrayList<FhirPatient>());
            migrationUser.setConditions(new ArrayList<FhirCondition>());
            migrationUser.setEncounters(new ArrayList<FhirEncounter>());
            migrationUser.setObservations(new ArrayList<FhirObservation>());
            migrationUser.setDiagnosticReports(new ArrayList<FhirDiagnosticReport>());
            migrationUser.setDocumentReferences(new ArrayList<FhirDocumentReference>());
            migrationUser.setMedicationStatements(new ArrayList<FhirMedicationStatement>());*/

            //LOG.info("--- Migrating " + oldUser.getUsername() + ": set basic user information ---");

            // FHIR related patient data (not test result observations)
            if (isPatient) {

                String nhsNo = newUser.getIdentifiers().iterator().next().getIdentifier();
                List<Patient> pv1PatientRecords;

                if (IBD) {
                    pv1PatientRecords = getPatientNative(nhsNo);
                } else {
                    pv1PatientRecords = patientDao.getByNhsNo(nhsNo);
                }

                UktStatus uktStatus = ukTransplantDao.getNative(nhsNo);

                if (pv1PatientRecords != null) {
                    for (Patient pv1PatientRecord : pv1PatientRecords) {
                        Group unit = getGroupByCode(pv1PatientRecord.getUnitcode());

                        if (unit != null) {
                            migrationUser = addPatientTableData(migrationUser, pv1PatientRecord, unit, uktStatus);
                            migrationUser = addLetterTableData(migrationUser, pv1PatientRecord.getNhsno(), unit);
                            migrationUser = addMedicineTableData(migrationUser, pv1PatientRecord.getNhsno(), unit);
                            migrationUser = addAllergyTableData(migrationUser, pv1PatientRecord.getNhsno(), unit);
                            migrationUser = addDiagnosticTableData(migrationUser, pv1PatientRecord.getNhsno(), unit);

                            if (!IBD) {
                                // diagnosis for IBD comes from ibd_myibd table
                                migrationUser = addDiagnosisTableData(migrationUser, pv1PatientRecord.getNhsno(), unit);

                                // eye and foot checkups
                                if (eyeCheckupNhsNos.contains(nhsNo) || footCheckupNhsNos.contains(nhsNo)) {
                                    migrationUser = addCheckupTablesData(migrationUser, unit);
                                }
                            }

                            if (IBD) {
                                migrationUser = addIbdData(migrationUser, pv1PatientRecord.getNhsno(), unit);
                                migrationUser = addCrohnsSymptoms(migrationUser, pv1PatientRecord.getNhsno());
                                migrationUser = addColitisSymptoms(migrationUser, pv1PatientRecord.getNhsno());
                            }

                        } else {
                            LOG.error("Patient group not found from unitcode: " + pv1PatientRecord.getUnitcode()
                                    + " for pv1 id: " + pv1PatientRecord.getId());
                            error = true;
                        }
                    }
                }
                //LOG.info("--- Migrating " + oldUser.getUsername() + ": set patient information ---");
            }

            if (!error) {
                return migrationUser;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    // to set type of identifier based on numeric range
    private Lookup getIdentifierType(String identifier) {
        return getLookupByName(CommonUtils.getIdentifierType(identifier).toString());
    }

    private User createUser(org.patientview.patientview.model.User user) {
        User newUser = new User();
        newUser.setForename(user.getFirstName());
        newUser.setSurname(user.getLastName());
        newUser.setChangePassword(false);
        newUser.setPassword(user.getPassword());
        newUser.setLocked(user.isAccountlocked());
        newUser.setDummy(user.isDummypatient());
        newUser.setFailedLogonAttempts(user.getFailedlogons());

        if (StringUtils.isEmpty(user.getEmail())) {
            newUser.setEmail("");
        } else{
            newUser.setEmail(CommonUtils.cleanSql(user.getEmail()));
        }
        newUser.setUsername(user.getUsername());
        newUser.setEmailVerified(user.isEmailverified());

        try {
            List<EmailVerification> emailVerifications = emailVerificationDao.getByEmailNative(user.getEmail());
            if (CollectionUtils.isNotEmpty(emailVerifications)) {
                newUser.setVerificationCode(emailVerifications.get(0).getVerificationcode());
            }
        } catch (Exception e) {
            LOG.error("Email verification exception: ", e);
        }

        newUser.setLastLogin(user.getLastlogon());
        newUser.setIdentifiers(new HashSet<Identifier>());
        newUser.setGroupRoles(new HashSet<GroupRole>());
        newUser.setUserFeatures(new HashSet<UserFeature>());

        return newUser;
    }

    private Feature getFeatureByName(String value) {
        for (Feature feature : features) {
            if (feature.getName().equalsIgnoreCase(value)) {
                return feature;
            }
        }
        return null;
    }

    private Group getGroupByCode(String code) {
        for (Group group : groups) {
            if (group.getCode().equalsIgnoreCase(code)) {
                return group;
            }
        }
        return null;
    }

    private Lookup getLookupByName(String value) {
        for (Lookup lookup : lookups) {
            if (lookup.getValue().equalsIgnoreCase(value)) {
                return  lookup;
            }
        }
        return null;
    }

    private List<Patient> getPatientNative(String nhsNo) {
        List<Patient> patients = new ArrayList<Patient>();
        Connection connection = null;
        String sql = "SELECT id, nhsno, surname, forename, dateofbirth, sex, address1, address2, address3, postcode," +
                "telephone1, telephone2, mobile, centreCode, diagnosis, treatment, transplantstatus, hospitalnumber," +
                "gpname, gpaddress1, gpaddress2, gpaddress3, gppostcode, gptelephone, otherConditions, address4," +
                "bloodgroup, bmdexam, gpemail, diagnosisDate FROM patient " +
                "WHERE nhsno = '" + nhsNo + "'";

        try {
            DataSource dataSource = new DriverManagerDataSource(jdbcUrl, jdbcUsername, jdbcPassword);
            connection = dataSource.getConnection();
            Statement statement = connection.createStatement();
            ResultSet results = statement.executeQuery(sql);

            while ((results.next())) {
                Patient patient = new Patient();
                patient.setId(results.getLong(1));
                patient.setNhsno(nhsNo);
                if (StringUtils.isNotEmpty(results.getString(3))) {
                    patient.setSurname(results.getString(3));
                }
                if (StringUtils.isNotEmpty(results.getString(4))) {
                    patient.setForename(results.getString(4));
                }
                if (StringUtils.isNotEmpty(results.getString(5))) {
                    patient.setDateofbirth(results.getTimestamp(5));
                }
                if (StringUtils.isNotEmpty(results.getString(6))) {
                    patient.setSex(results.getString(6));
                }
                if (StringUtils.isNotEmpty(results.getString(7))) {
                    patient.setAddress1(results.getString(7));
                }
                if (StringUtils.isNotEmpty(results.getString(8))) {
                    patient.setAddress2(results.getString(8));
                }
                if (StringUtils.isNotEmpty(results.getString(9))) {
                    patient.setAddress3(results.getString(9));
                }
                if (StringUtils.isNotEmpty(results.getString(10))) {
                    patient.setPostcode(results.getString(10));
                }
                if (StringUtils.isNotEmpty(results.getString(11))) {
                    patient.setTelephone1(results.getString(11));
                }
                if (StringUtils.isNotEmpty(results.getString(12))) {
                    patient.setTelephone2(results.getString(12));
                }
                if (StringUtils.isNotEmpty(results.getString(13))) {
                    patient.setMobile(results.getString(13));
                }
                if (StringUtils.isNotEmpty(results.getString(14))) {
                    patient.setUnitcode(results.getString(14));
                }
                if (StringUtils.isNotEmpty(results.getString(15))) {
                    patient.setDiagnosis(results.getString(15));
                }
                if (StringUtils.isNotEmpty(results.getString(16))) {
                    // handle GEN (general nephrology), incorrect data in IBD db
                    if (!results.getString(16).equals("GEN")) {
                        patient.setTreatment(results.getString(16));
                    }
                }
                if (StringUtils.isNotEmpty(results.getString(17))) {
                    patient.setTransplantstatus(results.getString(17));
                }
                if (StringUtils.isNotEmpty(results.getString(18))) {
                    patient.setHospitalnumber(results.getString(18));
                }
                if (StringUtils.isNotEmpty(results.getString(19))) {
                    patient.setGpname(results.getString(19));
                }
                if (StringUtils.isNotEmpty(results.getString(20))) {
                    patient.setGpaddress1(results.getString(20));
                }
                if (StringUtils.isNotEmpty(results.getString(21))) {
                    patient.setGpaddress2(results.getString(21));
                }
                if (StringUtils.isNotEmpty(results.getString(22))) {
                    patient.setGpaddress3(results.getString(22));
                }
                if (StringUtils.isNotEmpty(results.getString(23))) {
                    patient.setGppostcode(results.getString(23));
                }
                if (StringUtils.isNotEmpty(results.getString(24))) {
                    patient.setGptelephone(results.getString(24));
                }
                if (StringUtils.isNotEmpty(results.getString(25))) {
                    patient.setOtherConditions(results.getString(25));
                }
                if (StringUtils.isNotEmpty(results.getString(26))) {
                    patient.setAddress4(results.getString(26));
                }
                if (StringUtils.isNotEmpty(results.getString(27))) {
                    patient.setBloodgroup(results.getString(27));
                }
                if (results.getTimestamp(28) != null) {
                    patient.setBmdexam(results.getTimestamp(28));
                }
                if (StringUtils.isNotEmpty(results.getString(29))) {
                    patient.setGpemail(results.getString(29));
                }
                if (results.getTimestamp(30) != null) {
                    patient.setDiagnosisDate(results.getTimestamp(30));
                }

                patients.add(patient);
            }

            connection.close();
        } catch (SQLException se) {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException se2) {
                    LOG.error(se2.getMessage());
                }
            }
        }


        return patients;
    }

    private Role getRoleByName(RoleName name) {
        for (Role role : roles) {
            if (role.getName().equals(name)) {
                return role;
            }
        }
        return null;
    }

    private Role getTenancyUserRole(Long id) {
        Connection connection = null;
        String sql = "SELECT role FROM tenancyuserrole WHERE user_id = " + id;
        Role role = getRoleByName(RoleName.STAFF_ADMIN);

        try {
            DataSource dataSource = new DriverManagerDataSource(jdbcUrl, jdbcUsername, jdbcPassword);
            connection = dataSource.getConnection();
            Statement statement = connection.createStatement();
            ResultSet results = statement.executeQuery(sql);
            if (results.next()) {
                if (results.getString(1).equals("unitadmin") || results.getString(1).equals("superadmin") ) {
                    role = getRoleByName(RoleName.UNIT_ADMIN);
                } else if (results.getString(1).equals("unitstaff")) {
                    role = getRoleByName(RoleName.STAFF_ADMIN);
                }
            }
        } catch (SQLException se) {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException se2) {
                    LOG.error(se2.getMessage());
                }
            }
        }

        return role;
    }

    private org.patientview.patientview.model.User getUserNative(Long oldUserId) {
        Connection connection = null;
        org.patientview.patientview.model.User user = null;
        String sql = "SELECT username, password, name, email, emailverified, firstlogon, dummypatient, " +
                "lastlogon, failedlogons, accountlocked, id FROM user WHERE id = " + oldUserId;

        try {
            DataSource dataSource = new DriverManagerDataSource(jdbcUrl, jdbcUsername, jdbcPassword);
            connection = dataSource.getConnection();
            Statement statement = connection.createStatement();
            ResultSet results = statement.executeQuery(sql);

            if (results.next()) {
                user = new org.patientview.patientview.model.User();
                user.setUsername(results.getString(1));
                user.setPassword(results.getString(2));
                if (results.getString(3).contains(" ")) {
                    String firstName = results.getString(3).substring(0, results.getString(3).lastIndexOf(" "));
                    String lastName = results.getString(3).substring(results.getString(3).lastIndexOf(" ") + 1, results.getString(3).length());
                    if (StringUtils.isNotEmpty(firstName)) {
                        user.setFirstName(firstName);
                    }
                    if (StringUtils.isNotEmpty(lastName)) {
                        user.setLastName(lastName);
                    }
                } else {
                    user.setFirstName(results.getString(3));
                }
                if (StringUtils.isNotEmpty(results.getString(4))) {
                    user.setEmail(results.getString(4));
                }
                user.setEmailverified(results.getBoolean(5));
                user.setFirstlogon(results.getBoolean(6));
                user.setDummypatient(results.getBoolean(7));
                if (results.getTimestamp(8) != null) {
                    user.setLastlogon(results.getTimestamp(8));
                }
                user.setFailedlogons(results.getInt(9));
                user.setAccountlocked(results.getBoolean(10));
                user.setId(results.getLong(11));
            }
        } catch (SQLException se) {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException se2) {
                    LOG.error(se2.getMessage());
                }
            }
        }

        return user;
    }

    private void init() throws JsonMigrationException {
        try {
            JsonUtil.setPatientviewApiUrl(patientviewApiUrl);
            JsonUtil.token = JsonUtil.authenticate(migrationUsername, migrationPassword);
            lookups = JsonUtil.getStaticDataLookups(JsonUtil.pvUrl + "/lookup");
            features = JsonUtil.getStaticDataFeatures(JsonUtil.pvUrl + "/feature");
            roles = JsonUtil.getRoles(JsonUtil.pvUrl + "/role");
            groups = JsonUtil.getGroups(JsonUtil.pvUrl + "/group");
        } catch (JsonMigrationException e) {
            LOG.error("Could not authenticate {} ", e.getCause());
            throw new JsonMigrationException(e.getMessage());
        } catch (JsonMigrationExistsException e) {
            LOG.error("Could not authenticate {} ", e.getCause());
        }
    }

    // migrate all user data, not including observations
    public void migrate(String groupCode) throws JsonMigrationException {

        int maxThreads = 10;

        ExecutorService executorService =
                new ThreadPoolExecutor(
                        maxThreads, // core thread pool size
                        maxThreads, // maximum thread pool size
                        1, // time to wait before resizing pool
                        TimeUnit.MINUTES,
                        new ArrayBlockingQueue<Runnable>(maxThreads, true),
                        new ThreadPoolExecutor.CallerRunsPolicy());

        init();
        Role patientRole = getRoleByName(RoleName.PATIENT);

        List<Long> migratedPv1IdsThisRun = new ArrayList<Long>();
        List<Long> previouslyMigratedPv1Ids
                = JsonUtil.getMigratedPatientview1IdsByStatus(MigrationStatus.PATIENT_MIGRATED);
        previouslyMigratedPv1Ids.addAll(JsonUtil.getMigratedPatientview1IdsByStatus(MigrationStatus.USER_MIGRATED));
        previouslyMigratedPv1Ids.addAll(JsonUtil.getMigratedPatientview1IdsByStatus(MigrationStatus.OBSERVATIONS_MIGRATED));

        if (!IBD) {
            // create set of nhs numbers with eye checkup
            eyeCheckupNhsNos = new HashSet<String>();
            List<EyeCheckup> allEyeCheckup = eyeCheckupDao.getAll();
            for (EyeCheckup eyeCheckup : allEyeCheckup) {
                eyeCheckupNhsNos.add(eyeCheckup.getNhsno());
            }

            // create set of nhs numbers with foot checkup
            footCheckupNhsNos = new HashSet<String>();
            List<FootCheckup> allFootCheckup = footCheckupDao.getAll();
            for (FootCheckup footCheckup : allFootCheckup) {
                footCheckupNhsNos.add(footCheckup.getNhsno());
            }
        }

        LOG.info("--- Starting migration ---");

        // all groups
        List<Group> groupsToAdd = groups;

        // testing
        //List<Group> groupsToAdd = new ArrayList<Group>();
        //groupsToAdd.add(getGroupByCode("R1H00"));
        //groupsToAdd.add(getGroupByCode("RSC02"));

        // handle user entered group
        if (StringUtils.isNotEmpty(groupCode)) {
            groupsToAdd = new ArrayList<Group>();
            Group group = getGroupByCode(groupCode);
            if (group != null) {
                groupsToAdd.add(group);
            }
        }

        LOG.info(groupsToAdd.size() + " Groups");

        // do not migrate these pv1 ids (avoid data clashes etc with ibd migration)
        List<Long> ignoredIds = new ArrayList<Long>();

        // test patients in ibd
        ignoredIds.add(41L);

        // need partial migration (using single user migration)
        ignoredIds.add(1004L);
        ignoredIds.add(1010L);
        ignoredIds.add(1046L);
        ignoredIds.add(1058L);

        boolean singleUser = true;
        boolean replaceExisting = false;

        if (!singleUser) {
            for (Group group : groupsToAdd) {
                LOG.info("(Migration) From Group: " + group.getCode());
                try {
                    List<Long> groupUserIds = userDao.getIdsByUnitcodeNoGpNative(group.getCode());

                    LOG.info("(Migration) From Group: " + group.getCode() + ", " + groupUserIds.size() + " users");

                    if (CollectionUtils.isNotEmpty(groupUserIds)) {
                        for (Long oldUserId : groupUserIds) {
                            if (!migratedPv1IdsThisRun.contains(oldUserId) && !ignoredIds.contains(oldUserId)) {
                                if ((!replaceExisting && !previouslyMigratedPv1Ids.contains(oldUserId))
                                        || replaceExisting) {
                                    try {
                                        org.patientview.patientview.model.User oldUser;
                                        if (IBD) {
                                            oldUser = getUserNative(oldUserId);
                                        } else {
                                            oldUser = userDao.get(oldUserId);
                                        }

                                        if (oldUser != null) {
                                            if (!oldUser.getUsername().endsWith("-GP")) {
                                                MigrationUser migrationUser = createMigrationUser(oldUser, patientRole);

                                                if (migrationUser != null) {
                                                    try {
                                                        LOG.info("(Migration) User: " + oldUser.getUsername() + " from Group "
                                                                + group.getCode() + " submitting to REST");
                                                        executorService.submit(new AsyncMigrateUserTask(migrationUser));

                                                        migratedPv1IdsThisRun.add(oldUser.getId());
                                                    } catch (Exception e) {
                                                        LOG.error("REST submit exception: ", e);
                                                    }
                                                }
                                            }
                                        }
                                    } catch (Exception e) {
                                        LOG.error("Exception: ", e);
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    LOG.error("Migration exception: ", e);
                }
            }
        } else {
            LOG.info("--- Single user migration ---");

            List<Long> userIds = new ArrayList<Long>();

            // single user testing, user is not already present in pv2, has test results to migrate as well
            userIds.add(1004L);
            userIds.add(1010L);
            userIds.add(1046L);
            userIds.add(1058L);

            for (Long userId : userIds) {
                try {
                    org.patientview.patientview.model.User oldUser;
                    if (IBD) {
                        oldUser = getUserNative(userId);
                    } else {
                        oldUser = userDao.get(userId);
                    }
                    String username = oldUser.getUsername();

                    if (!username.endsWith("-GP")) {
                        MigrationUser migrationUser = createMigrationUser(oldUser, patientRole);

                        // for partial migration
                        migrationUser.setPartialMigration(true);

                        if (migrationUser != null) {
                            try {
                                LOG.info("(Migration) User: " + oldUser.getUsername() + " submitting to REST");
                                executorService.submit(new AsyncMigrateUserTask(migrationUser));

                                migratedPv1IdsThisRun.add(oldUser.getId());
                            } catch (Exception e) {
                                LOG.error("REST submit exception: ", e);
                            }
                        }
                    }
                } catch (Exception e) {
                    LOG.error("Exception: ", e);
                }
            }
        }

        try {
            // wait forever until all threads are finished
            executorService.shutdown();
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
        } catch (Exception e) {
            LOG.error(e.getMessage());
        }
    }
}
