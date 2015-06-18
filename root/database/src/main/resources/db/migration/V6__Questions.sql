/* Survey */
INSERT INTO "pv_survey" ("id", "type", "description")
VALUES (1, 'CROHNS_SYMPTOM_SCORE', 'Crohns Symptom Score');
INSERT INTO "pv_survey" ("id", "type", "description")
VALUES (2, 'COLITIS_SYMPTOM_SCORE', 'Colitis Symptom Score');
INSERT INTO "pv_survey" ("id", "type", "description")
VALUES (3, 'IBD_CONTROL', 'IBD Control Questionnaire');

/* Question Group */
INSERT INTO "pv_question_group" ("id", "survey_id", "text", "description", "number", "display_order")
VALUES (1, 1, 'Crohn''s Questions', 'Crohn''s Questions', null, 1);
INSERT INTO "pv_question_group" ("id", "survey_id", "text", "description", "number", "display_order")
VALUES (2, 2, 'Colitis Questions', 'Colitis Questions', null, 1);
INSERT INTO "pv_question_group" ("id", "survey_id", "text", "description", "number", "display_order")
VALUES (3, 3, 'Do you believe that', 'Do you believe that', null, 1);

/* Questions */
INSERT INTO "pv_question" ("id", "question_group_id", "element_type", "html_type", "text", "description", "number", "display_order", "range_start", "range_end", "type")
VALUES (1, 1, 'SINGLE_SELECT', 'SELECT', 'Do you have any abdominal pain at present?', null, null, 1, null, null, 'ABDOMINAL_PAIN');
INSERT INTO "pv_question" ("id", "question_group_id", "element_type", "html_type", "text", "description", "number", "display_order", "range_start", "range_end", "type")
VALUES (2, 1, 'SINGLE_SELECT_RANGE', 'SELECT', 'How many times are your bowels open a day?', null, null, 2, 0, 20, 'OPEN_BOWELS');
INSERT INTO "pv_question" ("id", "question_group_id", "element_type", "html_type", "text", "description", "number", "display_order", "range_start", "range_end", "type")
VALUES (3, 1, 'SINGLE_SELECT', 'SELECT', 'How are you feeling?', null, null, 3, null, null, 'FEELING');
INSERT INTO "pv_question" ("id", "question_group_id", "element_type", "html_type", "text", "description", "number", "display_order", "range_start", "range_end", "type")
VALUES (4, 1, 'SINGLE_SELECT', 'SELECT', 'Do you have any complications from your IBD?', null, null, 4, null, null, 'COMPLICATION');
INSERT INTO "pv_question" ("id", "question_group_id", "element_type", "html_type", "text", "description", "number", "display_order", "range_start", "range_end", "type")
VALUES (5, 1, 'SINGLE_SELECT', 'SELECT', 'Has the Doctor informed you about any mass (lump) in your abdomen?', null, null, 5, null, null, 'MASS_IN_TUMMY');

INSERT INTO "pv_question" ("id", "question_group_id", "element_type", "html_type", "text", "description", "number", "display_order", "range_start", "range_end", "type")
VALUES (6, 2, 'SINGLE_SELECT', 'SELECT', 'Number of Stools (Day)', null, null, 1, null, null, 'NUMBER_OF_STOOLS_DAYTIME');
INSERT INTO "pv_question" ("id", "question_group_id", "element_type", "html_type", "text", "description", "number", "display_order", "range_start", "range_end", "type")
VALUES (7, 2, 'SINGLE_SELECT', 'SELECT', 'Number of Stools (Night)', null, null, 2, null, null, 'NUMBER_OF_STOOLS_NIGHTTIME');
INSERT INTO "pv_question" ("id", "question_group_id", "element_type", "html_type", "text", "description", "number", "display_order", "range_start", "range_end", "type")
VALUES (8, 2, 'SINGLE_SELECT', 'SELECT', 'When I go to the toilet?', null, null, 3, null, null, 'TOILET_TIMING');
INSERT INTO "pv_question" ("id", "question_group_id", "element_type", "html_type", "text", "description", "number", "display_order", "range_start", "range_end", "type")
VALUES (9, 2, 'SINGLE_SELECT', 'SELECT', 'Is there blood present mixed in the stool?', null, null, 4, null, null, 'PRESENT_BLOOD');
INSERT INTO "pv_question" ("id", "question_group_id", "element_type", "html_type", "text", "description", "number", "display_order", "range_start", "range_end", "type")
VALUES (10, 2, 'SINGLE_SELECT', 'SELECT', 'How do I feel?', null, null, 5, null, null, 'FEELING');
INSERT INTO "pv_question" ("id", "question_group_id", "element_type", "html_type", "text", "description", "number", "display_order", "range_start", "range_end", "type")
VALUES (11, 2, 'SINGLE_SELECT', 'SELECT', 'Do I have any further complications?', null, null, 5, null, null, 'COMPLICATION');

INSERT INTO "pv_question" ("id", "question_group_id", "element_type", "html_type", "text", "description", "number", "display_order", "range_start", "range_end", "type")
VALUES (12, 3, 'SINGLE_SELECT', 'SELECT', 'a. Your IBD has been well controlled in the past two weeks?', null, null, 1, null, null, 'IBD_CONTROLLED_TWO_WEEKS');

/* Question Options */
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (1, 1, 'No', null, 1, 'NO', 0);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (2, 1, 'Mild', null, 2, 'MILD', 1);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (3, 1, 'Moderate', null, 3, 'MODERATE', 2);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (4, 1, 'Severe', null, 4, 'SEVERE', 3);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (5, 3, 'I feel well', null, 1, 'WELL', 0);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (6, 3, 'Slightly below par', null, 2, 'BELOW_PAR', 1);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (7, 3, 'Poor', null, 3, 'POOR', 2);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (8, 3, 'Very poor', null, 4, 'VERY_POOR', 3);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (9, 3, 'Terrible', null, 5, 'TERRIBLE', 4);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (10, 4, 'None', null, 1, 'NONE', 0);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (11, 4, 'IBD related joint complications (inflammatory arthritis, sacroilelitis)', null, 2, 'IBD_RELATED_JOINT_COMPLICATIONS', 1);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (12, 4, 'IBD related skin complications (erythema nodosum, pyoderma gangrenosum)', null, 3, 'IBD_RELATED_SKIN_COMPLICATIONS', 1);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (13, 4, 'IBD related eye complications (uveitis, episclreitis)', null, 4, 'IBD_RELATED_EYE_COMPLICATIONS', 1);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (14, 4, 'Mouth ulcers', null, 5, 'MOUTH_ULCERS', 1);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (15, 4, 'Anal fissure', null, 6, 'ANAL_FISSURE', 1);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (16, 4, 'Fistula', null, 7, 'FISTULA', 1);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (17, 4, 'Abscess', null, 8, 'ABSCESS', 1);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (18, 5, 'No', null, 1, 'NO', 0);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (19, 5, 'Possible', null, 2, 'POSSIBLE', 1);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (20, 5, 'Definite', null, 3, 'DEFINITE', 2);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (21, 5, 'Definite and tender', null, 4, 'DEFINITE_AND_TENDER', 3);

INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (22, 6, '0-3', null, 1, 'ZERO_TO_THREE', 0);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (23, 6, '4-6', null, 2, 'FOUR_TO_SIX', 1);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (24, 6, '7-9', null, 3, 'SEVEN_TO_NINE', 2);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (25, 6, '>9', null, 4, 'MORE_THAN_NINE', 3);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (26, 7, '0', null, 1, 'NONE', 0);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (27, 7, '1-3', null, 2, 'ONE_TO_THREE', 1);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (28, 7, '4-6', null, 3, 'FOUR_TO_SIX', 2);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (29, 8, 'I don''t need to hurry', null, 1, 'DONT_NEED_TO_RUSH', 0);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (30, 8, 'I need to hurry', null, 2, 'NEED_TO_HURRY', 1);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (31, 8, 'I need to go immediately', null, 3, 'NEED_TO_GO_IMMEDIATELY', 2);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (32, 8, 'I am having accidents', null, 4, 'HAVING_ACCIDENTS', 3);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (33, 9, 'None', null, 1, 'NONE', 0);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (34, 9, 'A trace', null, 2, 'A_TRACE', 1);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (35, 9, 'Occasional Blood', null, 3, 'OCCASIONAL', 2);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (36, 9, 'Blood is always present', null, 4, 'ALWAYS_PRESENT', 3);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (37, 10, 'I feel well', null, 1, 'WELL', 0);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (38, 10, 'Slightly below par', null, 2, 'BELOW_PAR', 1);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (39, 10, 'Poor', null, 3, 'POOR', 2);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (40, 10, 'Very poor', null, 4, 'VERY_POOR', 3);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (41, 10, 'Terrible', null, 5, 'TERRIBLE', 4);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (42, 11, 'None', null, 1, 'NONE', 0);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (43, 11, 'Joint Problems (arthralgia)', null, 2, 'JOINT_PROBLEMS', 1);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (44, 11, 'Skin Problems (erythema nodusum, pyoderma gangrenosum)', null, 3, 'SKIN_PROBLEMS', 1);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (45, 11, 'Eye Problems (uveitis, scleritis)', null, 4, 'EYE_PROBLEMS', 1);

INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (46, 12, 'Yes', null, 1, 'YES_NO_NOT_SURE', 1);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (47, 12, 'No', null, 2, 'YES_NO_NOT_SURE', 2);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (48, 12, 'Not Sure', null, 3, 'YES_NO_NOT_SURE', 3);