package org.patientview.persistence.model.enums;

/**
 * Taken from original IBD portal
 * Created by jamesr@solidstategroup.com
 * Created on 10/06/2015
 */
public enum QuestionOptionTypes {
    // Crohn's
    NO,
    MILD,
    MODERATE,
    SEVERE,
    WELL,
    BELOW_PAR,
    POOR,
    VERY_POOR,
    TERRIBLE,
    NONE,
    IBD_RELATED_JOINT_COMPLICATIONS,
    IBD_RELATED_SKIN_COMPLICATIONS,
    IBD_RELATED_EYE_COMPLICATIONS,
    MOUTH_ULCERS,
    ANAL_FISSURE,
    FISTULA,
    ABSCESS,
    POSSIBLE,
    DEFINITE,
    DEFINITE_AND_TENDER,

    // Colitis
    ZERO_TO_THREE,
    FOUR_TO_SIX,
    SEVEN_TO_NINE,
    MORE_THAN_NINE,
    ONE_TO_THREE,
    DONT_NEED_TO_RUSH,
    NEED_TO_HURRY,
    NEED_TO_GO_IMMEDIATELY,
    HAVING_ACCIDENTS,
    A_TRACE,
    OCCASIONAL,
    ALWAYS_PRESENT,
    JOINT_PROBLEMS,
    SKIN_PROBLEMS,
    EYE_PROBLEMS,

    // IBD Control Questionnaire
    YES,
    NOT_SURE,
    BETTER,
    NO_CHANGE,
    WORSE,

    // Heart Symptom Scores
    EVERY_MORNING,
    THREE_OR_MORE_PER_WEEK,
    ONE_OR_TWO_PER_WEEK,
    LT_ONE_PER_WEEK,
    NOT_IN_TWO_WEEKS,
    ALL_THE_TIME,
    SEVERAL_TIMES_DAILY,
    AT_LEAST_ONCE_DAILY,
    EVERY_NIGHT
}
