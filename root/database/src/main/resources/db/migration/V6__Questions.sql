/* Survey */
INSERT INTO "pv_survey" ("id", "type", "description")
VALUES (1, 'CROHNS_SYMPTOM_SCORE', 'Crohns Symptom Score');

/* Question Group */
INSERT INTO "pv_question_group" ("id", "survey_id", "text", "description", "number", "display_order")
VALUES (1, 1, 'Crohn''s Questions', 'Crohn''s Questions', null, 1);

/* Questions */
INSERT INTO "pv_question" ("id", "question_group_id", "type", "html_type", "text", "description", "number", "display_order", "range_start", "range_end")
VALUES (1, 1, 'SINGLE_SELECT', 'SELECT', 'Do you have any abdominal pain at present?', null, null, 1, null, null);
INSERT INTO "pv_question" ("id", "question_group_id", "type", "html_type", "text", "description", "number", "display_order", "range_start", "range_end")
VALUES (2, 1, 'SINGLE_SELECT_RANGE', 'SELECT', 'How many times are your bowels open a day?', null, null, 2, 0, 20);
INSERT INTO "pv_question" ("id", "question_group_id", "type", "html_type", "text", "description", "number", "display_order", "range_start", "range_end")
VALUES (3, 1, 'SINGLE_SELECT', 'SELECT', 'How are you feeling?', null, null, 3, null, null);
INSERT INTO "pv_question" ("id", "question_group_id", "type", "html_type", "text", "description", "number", "display_order", "range_start", "range_end")
VALUES (4, 1, 'SINGLE_SELECT', 'SELECT', 'Do you have any complications from your IBD?', null, null, 4, null, null);
INSERT INTO "pv_question" ("id", "question_group_id", "type", "html_type", "text", "description", "number", "display_order", "range_start", "range_end")
VALUES (5, 1, 'SINGLE_SELECT', 'SELECT', 'Has the Doctor informed you about any mass (lump) in your abdomen?', null, null, 5, null, null);

/* Question Options */
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order")
VALUES (1, 1, 'No', null, 1);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order")
VALUES (2, 1, 'Mild', null, 2);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order")
VALUES (3, 1, 'Moderate', null, 3);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order")
VALUES (4, 1, 'Severe', null, 4);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order")
VALUES (5, 3, 'I feel well', null, 1);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order")
VALUES (6, 3, 'Slightly below par', null, 2);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order")
VALUES (7, 3, 'Poor', null, 3);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order")
VALUES (8, 3, 'Very poor', null, 4);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order")
VALUES (9, 3, 'Terrible', null, 5);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order")
VALUES (10, 4, 'IBD related joint complications (inflammatory arthritis, sacroilelitis)', null, 1);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order")
VALUES (11, 4, 'IBD related skin complications (erythema nodosum, pyoderma gangrenosum)', null, 2);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order")
VALUES (12, 4, 'IBD related eye complications (uveitis, episclreitis)', null, 3);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order")
VALUES (13, 4, 'Mouth ulcers', null, 4);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order")
VALUES (14, 4, 'Anal fissure', null, 5);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order")
VALUES (15, 4, 'Fistula', null, 6);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order")
VALUES (16, 4, 'Abscess', null, 7);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order")
VALUES (17, 5, 'No', null, 1);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order")
VALUES (18, 5, 'Possible', null, 2);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order")
VALUES (19, 5, 'Definite', null, 3);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order")
VALUES (20, 5, 'Definite and tender', null, 4);
