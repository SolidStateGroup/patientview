/* Survey */
INSERT INTO "pv_survey" ("id", "type", "description")
VALUES (1, 'CROHNS_SYMPTOM_SCORE', 'Crohns Symptom Score');

/* Question Group */
INSERT INTO "pv_question_group" ("id", "survey_id", "text", "description", "number", "display_order")
VALUES (1, 1, 'Crohn''s Questions', 'Crohn''s Questions', null, 1);

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
