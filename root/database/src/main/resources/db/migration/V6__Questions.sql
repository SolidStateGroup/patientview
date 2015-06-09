/* Survey */
INSERT INTO "pv_survey" ("id", "type", "description")
VALUES (1, 'CROHNS', 'Crohns Survey');

/* Question Group */
INSERT INTO "pv_question_group" ("id", "survey_id", "text", "description", "number", "display_order")
VALUES (1, 1, 'Stools', 'Stool Questions', null, 1);

/* Questions */
INSERT INTO "pv_question" ("id", "question_group_id", "type", "html_type", "text", "description", "number", "display_order")
VALUES (1, 1, 'SINGLE_SELECT', 'SELECT', 'Number of Stools (Day)', null, 'a', 1);

/* Question Options */
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description")
VALUES (1, 1, '0-3', null );
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description")
VALUES (2, 1, '4-6', null );
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description")
VALUES (3, 1, '7-9', null );
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description")
VALUES (4, 1, '>9', null );
