/* Survey, see SurveyTypes.java */
INSERT INTO "pv_survey" ("id", "type", "description")
VALUES (1, 'CROHNS_SYMPTOM_SCORE', 'Crohns Symptom Score');
INSERT INTO "pv_survey" ("id", "type", "description")
VALUES (2, 'COLITIS_SYMPTOM_SCORE', 'Colitis Symptom Score');
INSERT INTO "pv_survey" ("id", "type", "description")
VALUES (3, 'IBD_CONTROL', 'My Disease Control Rating');
INSERT INTO "pv_survey" ("id", "type", "description")
VALUES (4, 'HEART_SYMPTOM_SCORE', 'Heart Failure Symptom Score');
INSERT INTO "pv_survey" ("id", "type", "description")
VALUES (5, 'IBD_FATIGUE', 'IBD Fatigue');

/* Question Group */
INSERT INTO "pv_question_group" ("id", "survey_id", "text", "description", "number", "display_order")
VALUES (1, 1, 'Crohn''s Questions', null, null, 1);
INSERT INTO "pv_question_group" ("id", "survey_id", "text", "description", "number", "display_order")
VALUES (2, 2, 'Colitis Questions', null, null, 1);

INSERT INTO "pv_question_group" ("id", "survey_id", "text", "description", "number", "display_order")
VALUES (3, 3, 'Do you believe that:', null, 1, 1);
INSERT INTO "pv_question_group" ("id", "survey_id", "text", "description", "number", "display_order")
VALUES (4, 3, 'Over the past 2 weeks, have your bowel symptoms been getting worse, getting better or not changed?', null, 2, 2);
INSERT INTO "pv_question_group" ("id", "survey_id", "text", "description", "number", "display_order")
VALUES (5, 3, 'In the past 2 weeks, did you:', null, 3, 3);
INSERT INTO "pv_question_group" ("id", "survey_id", "text", "description", "number", "display_order")
VALUES (6, 3, 'At your next clinic visit, would you like to discuss:', null, 4, 4);
INSERT INTO "pv_question_group" ("id", "survey_id", "text", "description", "number", "display_order")
VALUES (7, 3, 'How would you rate the OVERALL control of your IBD in the past two weeks?', '(please use the slider)', 5, 5);

INSERT INTO "pv_question_group" ("id", "survey_id", "text", "description", "number", "display_order")
VALUES (8, 4, '', null, null, 1);

INSERT INTO "pv_question_group" ("id", "survey_id", "text", "description", "number", "display_order")
VALUES (9, 5, 'SECTION I - Fatigue Assessment Scale',
        'This section of the questionnaire will identify fatigue, its severity, frequency and duration.
        <br/>Sometimes people with inflammatory bowel disease feel fatigued. The term ‘fatigue’ is used throughout the
        questionnaire. Fatigue has been defined as a sense of continuing tiredness, with periods of sudden and
        overwhelming lack of energy or feeling of exhaustion that is not relieved following rest or sleep.
        <br/>Please choose one number for each question, score from 0 - 4 with 0 = no fatigue, 4 = severe fatigue.', null, 1);
INSERT INTO "pv_question_group" ("id", "survey_id", "text", "description", "number", "display_order")
VALUES (10, 5, 'SECTION II – IBD-Fatigue Impact on Daily Activities Scale',
        'This section assesses the perceived impact of fatigue on your daily activities in the past two weeks.
        <br/>Please answer all the questions. The possible answers to the questions are: None of the time - 0;
        Some of the time – 1; Often - 2; Most of the time - 3; All of the time - 4.
        <br/>If a particular activity does not apply to you, for example you do not drive, please select N/A.
        <br/>Please tick only one answer for each question reflecting on the past two weeks.', null, 2);
INSERT INTO "pv_question_group" ("id", "survey_id", "text", "description", "number", "display_order")
VALUES (11, 5, 'SECTION III – Additional Questions about your Fatigue', null, null, 3);

/* Questions, see QuestionTypes.java */
INSERT INTO "pv_question" ("id", "question_group_id", "element_type", "html_type", "text", "description", "number", "display_order", "range_start", "range_end", "type")
VALUES (1, 1, 'SINGLE_SELECT', 'SELECT', 'Do you have any abdominal pain at present?', null, null, 1, null, null, 'ABDOMINAL_PAIN');
INSERT INTO "pv_question" ("id", "question_group_id", "element_type", "html_type", "text", "description", "number", "display_order", "range_start", "range_end", "type")
VALUES (2, 1, 'SINGLE_SELECT_RANGE', 'SELECT', 'How many times are your bowels open a day?', null, null, 2, 0, 20, 'OPEN_BOWELS');
INSERT INTO "pv_question" ("id", "question_group_id", "element_type", "html_type", "text", "description", "number", "display_order", "range_start", "range_end", "type")
VALUES (3, 1, 'SINGLE_SELECT', 'SELECT', 'How are you feeling?', null, null, 3, null, null, 'FEELING');
INSERT INTO "pv_question" ("id", "question_group_id", "element_type", "html_type", "text", "description", "number", "display_order", "range_start", "range_end", "type", "help_link")
VALUES (4, 1, 'SINGLE_SELECT', 'SELECT', 'Do you have any complications from your IBD?', null, null, 4, null, null, 'COMPLICATION', 'http://www.myibdportal.org/crohns-disease#what-are-the-possible-complications-of-crohns');
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
INSERT INTO "pv_question" ("id", "question_group_id", "element_type", "html_type", "text", "description", "number", "display_order", "range_start", "range_end", "type", "help_link")
VALUES (11, 2, 'SINGLE_SELECT', 'SELECT', 'Do I have any further complications?', null, null, 5, null, null, 'COMPLICATION', 'http://www.myibdportal.org/ulcerative-colitis#can-uc-affect-other-parts-of-the-body');

INSERT INTO "pv_question" ("id", "question_group_id", "element_type", "html_type", "text", "description", "number", "display_order", "range_start", "range_end", "type")
VALUES (12, 3, 'SINGLE_SELECT', 'RADIO', 'a. Your IBD has been well controlled in the past two weeks?', null, null, 1, null, null, 'IBD_CONTROLLED_TWO_WEEKS');
INSERT INTO "pv_question" ("id", "question_group_id", "element_type", "html_type", "text", "description", "number", "display_order", "range_start", "range_end", "type")
VALUES (13, 3, 'SINGLE_SELECT', 'RADIO', 'b. Your current treatment is useful in controlling your IBD?', null, null, 2, null, null, 'IBD_CONTROLLED_CURRENT_TREATMENT');
INSERT INTO "pv_question" ("id", "question_group_id", "element_type", "html_type", "text", "description", "number", "display_order", "range_start", "range_end", "type")
VALUES (14, 3, 'SINGLE_SELECT', 'RADIO', 'Are you are currently taking any treatment?', null, null, 3, null, null, 'IBD_NO_TREATMENT');
INSERT INTO "pv_question" ("id", "question_group_id", "element_type", "html_type", "text", "description", "number", "display_order", "range_start", "range_end", "type")
VALUES (15, 4, 'SINGLE_SELECT', 'RADIO', '', null, null, 1, null, null, 'IBD_TWO_WEEKS_BOWEL');
INSERT INTO "pv_question" ("id", "question_group_id", "element_type", "html_type", "text", "description", "number", "display_order", "range_start", "range_end", "type")
VALUES (16, 5, 'SINGLE_SELECT', 'RADIO', 'a. Miss any planned activities because of IBD?', '(e.g. attending school/college, going to work or a social event)', null, 1, null, null, 'IBD_MISS_PLANNED_ACTIVITIES');
INSERT INTO "pv_question" ("id", "question_group_id", "element_type", "html_type", "text", "description", "number", "display_order", "range_start", "range_end", "type")
VALUES (17, 5, 'SINGLE_SELECT', 'RADIO', 'b. Wake up at night because of symptoms of IBD?', null, null, 2, null, null, 'IBD_WAKE_UP');
INSERT INTO "pv_question" ("id", "question_group_id", "element_type", "html_type", "text", "description", "number", "display_order", "range_start", "range_end", "type")
VALUES (18, 5, 'SINGLE_SELECT', 'RADIO', 'c. Suffer from significant pain or discomfort?', null, null, 3, null, null, 'IBD_SIGNIFICANT_PAIN');
INSERT INTO "pv_question" ("id", "question_group_id", "element_type", "html_type", "text", "description", "number", "display_order", "range_start", "range_end", "type")
VALUES (19, 5, 'SINGLE_SELECT', 'RADIO', 'd. Often feel lacking in energy (fatigued)', '(by often we mean more than half of the the time)', null, 4, null, null, 'IBD_LACKING_ENERGY');
INSERT INTO "pv_question" ("id", "question_group_id", "element_type", "html_type", "text", "description", "number", "display_order", "range_start", "range_end", "type")
VALUES (20, 5, 'SINGLE_SELECT', 'RADIO', 'e. Feel anxious or depressed because of your IBD?', null, null, 5, null, null, 'IBD_FEEL_ANXIOUS');
INSERT INTO "pv_question" ("id", "question_group_id", "element_type", "html_type", "text", "description", "number", "display_order", "range_start", "range_end", "type")
VALUES (21, 5, 'SINGLE_SELECT', 'RADIO', 'f. Think you needed a change to your treatment?', null, null, 6, null, null, 'IBD_NEED_CHANGE');
INSERT INTO "pv_question" ("id", "question_group_id", "element_type", "html_type", "text", "description", "number", "display_order", "range_start", "range_end", "type")
VALUES (22, 6, 'SINGLE_SELECT', 'RADIO', 'a. Alternative types of drug for controlling IBD', null, null, 1, null, null, 'IBD_ALTERNATIVE_DRUGS');
INSERT INTO "pv_question" ("id", "question_group_id", "element_type", "html_type", "text", "description", "number", "display_order", "range_start", "range_end", "type")
VALUES (23, 6, 'SINGLE_SELECT', 'RADIO', 'b. Ways to adjust your own treatment', null, null, 2, null, null, 'IBD_ADJUST_TREATMENT');
INSERT INTO "pv_question" ("id", "question_group_id", "element_type", "html_type", "text", "description", "number", "display_order", "range_start", "range_end", "type")
VALUES (24, 6, 'SINGLE_SELECT', 'RADIO', 'c. Side effects or difficulties with using your medicines', null, null, 3, null, null, 'IBD_SIDE_EFFECTS');
INSERT INTO "pv_question" ("id", "question_group_id", "element_type", "html_type", "text", "description", "number", "display_order", "range_start", "range_end", "type")
VALUES (25, 6, 'SINGLE_SELECT', 'RADIO', 'd. New symptoms that have developed since your last visit', null, null, 4, null, null, 'IBD_NEW_SYMPTOMS');
INSERT INTO "pv_question" ("id", "question_group_id", "element_type", "html_type", "text", "description", "number", "display_order", "range_start", "range_end", "range_start_description", "range_end_description", "type")
VALUES (26, 7, 'SINGLE_SELECT_RANGE', 'SLIDER', '', null, null, 1, 0, 100, 'Worst possible control', 'Best possible control', 'IBD_OVERALL_CONTROL');

INSERT INTO "pv_question" ("id", "question_group_id", "element_type", "html_type", "text", "description", "number", "display_order", "range_start", "range_end", "type")
VALUES (27, 8, 'SINGLE_SELECT', 'RADIO', 'Over the past 2 weeks, how many times did you have swelling in your feet, ankles or legs bothered you?', null, 1, 1, null, null, 'HEART_SWELLING');
INSERT INTO "pv_question" ("id", "question_group_id", "element_type", "html_type", "text", "description", "number", "display_order", "range_start", "range_end", "type")
VALUES (28, 8, 'SINGLE_SELECT', 'RADIO', 'Over the past 2 weeks, on average, how many times has fatigue limited your ability to do what you want?', null, 2, 2, null, null, 'HEART_FATIGUE');
INSERT INTO "pv_question" ("id", "question_group_id", "element_type", "html_type", "text", "description", "number", "display_order", "range_start", "range_end", "type")
VALUES (29, 8, 'SINGLE_SELECT', 'RADIO', 'Over the past 2 weeks, on average, how many times has shortness of breath limited your ability to do what you wanted?', null, 3, 3, null, null, 'HEART_SHORTNESS_OF_BREATH');
INSERT INTO "pv_question" ("id", "question_group_id", "element_type", "html_type", "text", "description", "number", "display_order", "range_start", "range_end", "type")
VALUES (30, 8, 'SINGLE_SELECT', 'RADIO', 'Over the past 2 weeks, on average how many times have you been forced to sleep sitting up in a chair or with least 3 pillows to prop you up because of shortness of breath?', null, 4, 4, null, null, 'HEART_SHORTNESS_OF_BREATH_SLEEP');

/* IBD fatigue, section 1 */
INSERT INTO "pv_question" ("id", "question_group_id", "element_type", "html_type", "text", "description", "number", "display_order", "range_start", "range_end", "type", "required")
VALUES (31, 9, 'SINGLE_SELECT', 'RADIO', 'What is your fatigue level right NOW?', null, 1, 1, null, null, 'IBD_FATIGUE_NOW', true);
INSERT INTO "pv_question" ("id", "question_group_id", "element_type", "html_type", "text", "description", "number", "display_order", "range_start", "range_end", "type", "required")
VALUES (32, 9, 'SINGLE_SELECT', 'RADIO', 'What was your HIGHEST fatigue level in the past two weeks?', null, 2, 2, null, null, 'IBD_FATIGUE_HIGHEST_TWO_WEEKS', true);
INSERT INTO "pv_question" ("id", "question_group_id", "element_type", "html_type", "text", "description", "number", "display_order", "range_start", "range_end", "type", "required")
VALUES (33, 9, 'SINGLE_SELECT', 'RADIO', 'What was your LOWEST fatigue level in the past two weeks?', null, 3, 3, null, null, 'IBD_FATIGUE_LOWEST_TWO_WEEKS', true);
INSERT INTO "pv_question" ("id", "question_group_id", "element_type", "html_type", "text", "description", "number", "display_order", "range_start", "range_end", "type", "required")
VALUES (34, 9, 'SINGLE_SELECT', 'RADIO', 'What was your AVERAGE fatigue level in the past two weeks?', null, 4, 4, null, null, 'IBD_FATIGUE_AVERAGE_TWO_WEEKS', true);
INSERT INTO "pv_question" ("id", "question_group_id", "element_type", "html_type", "text", "description", "number", "display_order", "range_start", "range_end", "type", "required")
VALUES (35, 9, 'SINGLE_SELECT', 'RADIO', 'How much of your waking time have you felt fatigued in the past two weeks?', null, 5, 5, null, null, 'IBD_FATIGUE_WAKING_TWO_WEEKS', true);
/* IBD fatigue, section 2 */
INSERT INTO "pv_question" ("id", "question_group_id", "element_type", "html_type", "text", "description", "number", "display_order", "range_start", "range_end", "type", "required")
VALUES (36, 10, 'SINGLE_SELECT', 'RADIO', 'I had to nap during the day because of fatigue', null, 1, 1, null, null, 'IBD_DAS_NAP', true);
INSERT INTO "pv_question" ("id", "question_group_id", "element_type", "html_type", "text", "description", "number", "display_order", "range_start", "range_end", "type", "required")
VALUES (37, 10, 'SINGLE_SELECT', 'RADIO', 'Fatigue stopped me from going out to social events', null, 2, 2, null, null, 'IBD_DAS_SOCIAL', true);
INSERT INTO "pv_question" ("id", "question_group_id", "element_type", "html_type", "text", "description", "number", "display_order", "range_start", "range_end", "type", "required")
VALUES (38, 10, 'SINGLE_SELECT', 'RADIO', 'I was not able to go to work or college because of fatigue', null, 3, 3, null, null, 'IBD_DAS_GO_WORK', true);
INSERT INTO "pv_question" ("id", "question_group_id", "element_type", "html_type", "text", "description", "number", "display_order", "range_start", "range_end", "type", "required")
VALUES (39, 10, 'SINGLE_SELECT', 'RADIO', 'My performance at work or education was affected by fatigue', null, 4, 4, null, null, 'IBD_DAS_PERFORMANCE_WORK', true);
INSERT INTO "pv_question" ("id", "question_group_id", "element_type", "html_type", "text", "description", "number", "display_order", "range_start", "range_end", "type", "required")
VALUES (40, 10, 'SINGLE_SELECT', 'RADIO', 'I had problems concentrating because of fatigue', null, 5, 5, null, null, 'IBD_DAS_CONCENTRATING', true);
INSERT INTO "pv_question" ("id", "question_group_id", "element_type", "html_type", "text", "description", "number", "display_order", "range_start", "range_end", "type", "required")
VALUES (41, 10, 'SINGLE_SELECT', 'RADIO', 'I had difficulty motivating myself because of fatigue', null, 6, 6, null, null, 'IBD_DAS_MOTIVATING', true);
INSERT INTO "pv_question" ("id", "question_group_id", "element_type", "html_type", "text", "description", "number", "display_order", "range_start", "range_end", "type", "required")
VALUES (42, 10, 'SINGLE_SELECT', 'RADIO', 'I could not wash and dress myself because of fatigue', null, 7, 7, null, null, 'IBD_DAS_WASH', true);
INSERT INTO "pv_question" ("id", "question_group_id", "element_type", "html_type", "text", "description", "number", "display_order", "range_start", "range_end", "type", "required")
VALUES (43, 10, 'SINGLE_SELECT', 'RADIO', 'I had difficulty with walking because of fatigue', null, 8, 8, null, null, 'IBD_DAS_WALK', true);
INSERT INTO "pv_question" ("id", "question_group_id", "element_type", "html_type", "text", "description", "number", "display_order", "range_start", "range_end", "type", "required")
VALUES (44, 10, 'SINGLE_SELECT', 'RADIO', 'I was unable to drive as much as I need to because of fatigue', null, 9, 9, null, null, 'IBD_DAS_DRIVE', true);
INSERT INTO "pv_question" ("id", "question_group_id", "element_type", "html_type", "text", "description", "number", "display_order", "range_start", "range_end", "type", "required")
VALUES (45, 10, 'SINGLE_SELECT', 'RADIO', 'I was not able to do as much physical exercise as I wanted to because of fatigue', null, 10, 10, null, null, 'IBD_DAS_EXERCISE', true);
INSERT INTO "pv_question" ("id", "question_group_id", "element_type", "html_type", "text", "description", "number", "display_order", "range_start", "range_end", "type", "required")
VALUES (46, 10, 'SINGLE_SELECT', 'RADIO', 'I had difficulty continuing with my hobbies/interests because of fatigue', null, 11, 11, null, null, 'IBD_DAS_HOBBIES', true);
INSERT INTO "pv_question" ("id", "question_group_id", "element_type", "html_type", "text", "description", "number", "display_order", "range_start", "range_end", "type", "required")
VALUES (47, 10, 'SINGLE_SELECT', 'RADIO', 'My emotional relationship with my partner was affected by fatigue', null, 12, 12, null, null, 'IBD_DAS_EMOTIONAL', true);
INSERT INTO "pv_question" ("id", "question_group_id", "element_type", "html_type", "text", "description", "number", "display_order", "range_start", "range_end", "type", "required")
VALUES (48, 10, 'SINGLE_SELECT', 'RADIO', 'My sexual relationship with my partner was affected by fatigue', null, 13, 13, null, null, 'IBD_DAS_SEXUAL', true);
INSERT INTO "pv_question" ("id", "question_group_id", "element_type", "html_type", "text", "description", "number", "display_order", "range_start", "range_end", "type", "required")
VALUES (49, 10, 'SINGLE_SELECT', 'RADIO', 'My relationship with my children was affected by fatigue', null, 14, 14, null, null, 'IBD_DAS_CHILDREN', true);
INSERT INTO "pv_question" ("id", "question_group_id", "element_type", "html_type", "text", "description", "number", "display_order", "range_start", "range_end", "type", "required")
VALUES (50, 10, 'SINGLE_SELECT', 'RADIO', 'I was low in mood because of fatigue', null, 15, 15, null, null, 'IBD_DAS_LOW_MOOD', true);
INSERT INTO "pv_question" ("id", "question_group_id", "element_type", "html_type", "text", "description", "number", "display_order", "range_start", "range_end", "type", "required")
VALUES (51, 10, 'SINGLE_SELECT', 'RADIO', 'I felt isolated because of fatigue', null, 16, 16, null, null, 'IBD_DAS_ISOLATED', true);
INSERT INTO "pv_question" ("id", "question_group_id", "element_type", "html_type", "text", "description", "number", "display_order", "range_start", "range_end", "type", "required")
VALUES (52, 10, 'SINGLE_SELECT', 'RADIO', 'My memory was affected because of fatigue', null, 17, 17, null, null, 'IBD_DAS_MEMORY', true);
INSERT INTO "pv_question" ("id", "question_group_id", "element_type", "html_type", "text", "description", "number", "display_order", "range_start", "range_end", "type", "required")
VALUES (53, 10, 'SINGLE_SELECT', 'RADIO', 'I made mistakes because of fatigue', null, 18, 18, null, null, 'IBD_DAS_MISTAKES', true);
INSERT INTO "pv_question" ("id", "question_group_id", "element_type", "html_type", "text", "description", "number", "display_order", "range_start", "range_end", "type", "required")
VALUES (54, 10, 'SINGLE_SELECT', 'RADIO', 'Fatigue made me irritable', null, 19, 19, null, null, 'IBD_DAS_IRRITABLE', true);
INSERT INTO "pv_question" ("id", "question_group_id", "element_type", "html_type", "text", "description", "number", "display_order", "range_start", "range_end", "type", "required")
VALUES (55, 10, 'SINGLE_SELECT', 'RADIO', 'Fatigue made me frustrated', null, 20, 20, null, null, 'IBD_DAS_FRUSTRATED', true);
INSERT INTO "pv_question" ("id", "question_group_id", "element_type", "html_type", "text", "description", "number", "display_order", "range_start", "range_end", "type", "required")
VALUES (56, 10, 'SINGLE_SELECT', 'RADIO', 'I got words mixed up because of fatigue', null, 21, 21, null, null, 'IBD_DAS_WORDS_MIXED', true);
INSERT INTO "pv_question" ("id", "question_group_id", "element_type", "html_type", "text", "description", "number", "display_order", "range_start", "range_end", "type", "required")
VALUES (57, 10, 'SINGLE_SELECT', 'RADIO', 'Fatigue stopped me from enjoying life', null, 22, 22, null, null, 'IBD_DAS_ENJOYING_LIFE', true);
INSERT INTO "pv_question" ("id", "question_group_id", "element_type", "html_type", "text", "description", "number", "display_order", "range_start", "range_end", "type", "required")
VALUES (58, 10, 'SINGLE_SELECT', 'RADIO', 'Fatigue stopped me from having a fulfilling life', null, 23, 23, null, null, 'IBD_DAS_FULFILLING_LIFE', true);
INSERT INTO "pv_question" ("id", "question_group_id", "element_type", "html_type", "text", "description", "number", "display_order", "range_start", "range_end", "type", "required")
VALUES (59, 10, 'SINGLE_SELECT', 'RADIO', 'My self-esteem was affected by fatigue', null, 24, 24, null, null, 'IBD_DAS_SELF_ESTEEM', true);
INSERT INTO "pv_question" ("id", "question_group_id", "element_type", "html_type", "text", "description", "number", "display_order", "range_start", "range_end", "type", "required")
VALUES (60, 10, 'SINGLE_SELECT', 'RADIO', 'Fatigue affected my confidence', null, 25, 25, null, null, 'IBD_DAS_CONFIDENCE', true);
INSERT INTO "pv_question" ("id", "question_group_id", "element_type", "html_type", "text", "description", "number", "display_order", "range_start", "range_end", "type", "required")
VALUES (61, 10, 'SINGLE_SELECT', 'RADIO', 'Fatigue made me feel unhappy', null, 26, 26, null, null, 'IBD_DAS_UNHAPPY', true);
INSERT INTO "pv_question" ("id", "question_group_id", "element_type", "html_type", "text", "description", "number", "display_order", "range_start", "range_end", "type", "required")
VALUES (62, 10, 'SINGLE_SELECT', 'RADIO', 'I had difficulties sleeping at night because of fatigue', null, 27, 27, null, null, 'IBD_DAS_SLEEPING', true);
INSERT INTO "pv_question" ("id", "question_group_id", "element_type", "html_type", "text", "description", "number", "display_order", "range_start", "range_end", "type", "required")
VALUES (63, 10, 'SINGLE_SELECT', 'RADIO', 'Fatigue affected my ability to do all my normal household activities', null, 28, 28, null, null, 'IBD_DAS_HOUSEHOLD', true);
INSERT INTO "pv_question" ("id", "question_group_id", "element_type", "html_type", "text", "description", "number", "display_order", "range_start", "range_end", "type", "required")
VALUES (64, 10, 'SINGLE_SELECT', 'RADIO', 'I had to ask others for help because of fatigue', null, 29, 29, null, null, 'IBD_DAS_OTHERS_HELP', true);
INSERT INTO "pv_question" ("id", "question_group_id", "element_type", "html_type", "text", "description", "number", "display_order", "range_start", "range_end", "type", "required")
VALUES (65, 10, 'SINGLE_SELECT', 'RADIO', 'Quality of my life was affected by fatigue', null, 30, 30, null, null, 'IBD_DAS_QUALITY_LIFE', true);
/* IBD fatigue, section 3 */
INSERT INTO "pv_question" ("id", "question_group_id", "element_type", "html_type", "text", "description", "number", "display_order", "range_start", "range_end", "type")
VALUES (66, 11, 'TEXT', 'TEXT', 'What do you think is the main cause of your fatigue apart from IBD?', null, 1, 1, null, null, 'IBD_FATIGUE_EXTRA_MAIN_CAUSE');
INSERT INTO "pv_question" ("id", "question_group_id", "element_type", "html_type", "text", "description", "number", "display_order", "range_start", "range_end", "type")
VALUES (67, 11, 'TEXT', 'TEXT', 'What do you think are the other causes of your fatigue?', null, 2, 2, null, null, 'IBD_FATIGUE_EXTRA_OTHER_CAUSE');
INSERT INTO "pv_question" ("id", "question_group_id", "element_type", "html_type", "text", "description", "number", "display_order", "range_start", "range_end", "type")
VALUES (68, 11, 'TEXT', 'TEXT', 'Have you found anything that helps with your fatigue?', null, 3, 3, null, null, 'IBD_FATIGUE_EXTRA_HELPS');
INSERT INTO "pv_question" ("id", "question_group_id", "element_type", "html_type", "text", "description", "number", "display_order", "range_start", "range_end", "type")
VALUES (69, 11, 'TEXT_NUMERIC', 'TEXT_NUMERIC', 'How long have you experienced fatigue? (years)', null, 4, 4, null, null, 'IBD_FATIGUE_EXTRA_LENGTH_YEARS');
INSERT INTO "pv_question" ("id", "question_group_id", "element_type", "html_type", "text", "description", "number", "display_order", "range_start", "range_end", "type")
VALUES (70, 11, 'TEXT_NUMERIC', 'TEXT_NUMERIC', 'How long have you experienced fatigue? (months)', null, null, 5, null, null, 'IBD_FATIGUE_EXTRA_LENGTH_MONTHS');
INSERT INTO "pv_question" ("id", "question_group_id", "element_type", "html_type", "text", "description", "number", "display_order", "range_start", "range_end", "type")
VALUES (71, 11, 'SINGLE_SELECT', 'RADIO', 'During this time has your fatigue been:', null, 5, 6, null, null, 'IBD_FATIGUE_EXTRA_STATUS');

/* Question Options, see QuestionOptionTypes.java */
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
VALUES (46, 12, 'Yes', null, 1, 'YES', 2);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (47, 12, 'No', null, 2, 'NO', 0);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (48, 12, 'Not Sure', null, 3, 'NOT_SURE', 1);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (49, 13, 'Yes', null, 1, 'YES', 2);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (50, 13, 'No', null, 2, 'NO', 0);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (51, 13, 'Not Sure', null, 3, 'NOT_SURE', 1);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (52, 14, 'Yes', null, 1, 'YES', 0);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (53, 14, 'No', null, 2, 'NO', 1);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (54, 15, 'Better', null, 1, 'BETTER', 0);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (55, 15, 'No Change', null, 2, 'NO_CHANGE', 0);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (56, 15, 'Worse', null, 3, 'WORSE', 0);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (57, 16, 'Yes', null, 1, 'YES', 0);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (58, 16, 'No', null, 2, 'NO', 2);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (59, 16, 'Not Sure', null, 3, 'NOT_SURE', 1);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (60, 17, 'Yes', null, 1, 'YES', 0);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (61, 17, 'No', null, 2, 'NO', 2);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (62, 17, 'Not Sure', null, 3, 'NOT_SURE', 1);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (63, 18, 'Yes', null, 1, 'YES', 0);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (64, 18, 'No', null, 2, 'NO', 2);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (65, 18, 'Not Sure', null, 3, 'NOT_SURE', 1);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (66, 19, 'Yes', null, 1, 'YES', 0);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (67, 19, 'No', null, 2, 'NO', 2);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (68, 19, 'Not Sure', null, 3, 'NOT_SURE', 1);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (69, 20, 'Yes', null, 1, 'YES', 0);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (70, 20, 'No', null, 2, 'NO', 2);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (71, 20, 'Not Sure', null, 3, 'NOT_SURE', 1);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (72, 21, 'Yes', null, 1, 'YES', 0);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (73, 21, 'No', null, 2, 'NO', 2);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (74, 21, 'Not Sure', null, 3, 'NOT_SURE', 1);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (75, 22, 'Yes', null, 1, 'YES', 0);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (76, 22, 'No', null, 2, 'NO', 0);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (77, 22, 'Not Sure', null, 3, 'NOT_SURE', 0);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (78, 23, 'Yes', null, 1, 'YES', 0);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (79, 23, 'No', null, 2, 'NO', 0);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (80, 23, 'Not Sure', null, 3, 'NOT_SURE', 0);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (81, 24, 'Yes', null, 1, 'YES', 0);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (82, 24, 'No', null, 2, 'NO', 0);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (83, 24, 'Not Sure', null, 3, 'NOT_SURE', 0);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (84, 25, 'Yes', null, 1, 'YES', 0);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (85, 25, 'No', null, 2, 'NO', 0);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (86, 25, 'Not Sure', null, 3, 'NOT_SURE', 0);

INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (87, 27, 'Every morning', null, 1, 'EVERY_MORNING', 1);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (88, 27, '3 or more times a week, but not every day', null, 2, 'THREE_OR_MORE_PER_WEEK', 2);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (89, 27, '1 - 2 times a week', null, 3, 'ONE_OR_TWO_PER_WEEK', 3);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (90, 27, 'Less than once a week', null, 4, 'LT_ONE_PER_WEEK', 4);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (91, 27, 'Never over the past 2 weeks', null, 5, 'NOT_IN_TWO_WEEKS', 5);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (92, 28, 'All the time', null, 1, 'ALL_THE_TIME', 1);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (93, 28, 'Several times per day', null, 2, 'SEVERAL_TIMES_DAILY', 2);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (94, 28, 'At least once a day', null, 3, 'AT_LEAST_ONCE_DAILY', 3);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (95, 28, '3 or more times per week but not every day', null, 4, 'THREE_OR_MORE_PER_WEEK', 4);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (96, 28, '1 - 2 times a week', null, 5, 'ONE_OR_TWO_PER_WEEK', 5);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (97, 28, 'Less than once a week', null, 6, 'LT_ONE_PER_WEEK', 6);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (98, 28, 'Never over the past 2 weeks', null, 7, 'NOT_IN_TWO_WEEKS', 7);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (99, 29, 'All the time', null, 1, 'ALL_THE_TIME', 1);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (100, 29, 'Several times per day', null, 2, 'SEVERAL_TIMES_DAILY', 2);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (101, 29, 'At least once a day', null, 3, 'AT_LEAST_ONCE_DAILY', 3);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (102, 29, '3 or more times per week but not every day', null, 4, 'THREE_OR_MORE_PER_WEEK', 4);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (103, 29, '1 - 2 times a week', null, 5, 'ONE_OR_TWO_PER_WEEK', 5);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (104, 29, 'Less than once a week', null, 6, 'LT_ONE_PER_WEEK', 6);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (105, 29, 'Never over the past 2 weeks', null, 7, 'NOT_IN_TWO_WEEKS', 7);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (106, 30, 'Every night', null, 1, 'EVERY_NIGHT', 1);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (107, 30, '3 or more times a week, but not every day', null, 2, 'THREE_OR_MORE_PER_WEEK', 2);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (108, 30, '1 - 2 times a week', null, 3, 'ONE_OR_TWO_PER_WEEK', 3);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (109, 30, 'Less than once a week', null, 4, 'LT_ONE_PER_WEEK', 4);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (110, 30, 'Never over the past 2 weeks', null, 5, 'NOT_IN_TWO_WEEKS', 5);

/* IBD fatigue, section 1 */
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (111, 31, '0', null, 1, 'ZERO', 0);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (112, 31, '1', null, 2, 'ONE', 1);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (113, 31, '2', null, 3, 'TWO', 2);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (114, 31, '3', null, 4, 'THREE', 3);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (115, 31, '4', null, 5, 'FOUR', 4);

INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (116, 32, '0', null, 1, 'ZERO', 0);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (117, 32, '1', null, 2, 'ONE', 1);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (118, 32, '2', null, 3, 'TWO', 2);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (119, 32, '3', null, 4, 'THREE', 3);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (120, 32, '4', null, 5, 'FOUR', 4);

INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (121, 33, '0', null, 1, 'ZERO', 0);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (122, 33, '1', null, 2, 'ONE', 1);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (123, 33, '2', null, 3, 'TWO', 2);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (124, 33, '3', null, 4, 'THREE', 3);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (125, 33, '4', null, 5, 'FOUR', 4);

INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (126, 34, '0', null, 1, 'ZERO', 0);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (127, 34, '1', null, 2, 'ONE', 1);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (128, 34, '2', null, 3, 'TWO', 2);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (129, 34, '3', null, 4, 'THREE', 3);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (130, 34, '4', null, 5, 'FOUR', 4);

INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (131, 35, '0', 'None of the time', 1, 'ZERO', 0);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (132, 35, '1', 'Some of the time', 2, 'ONE', 1);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (133, 35, '2', 'Often', 3, 'TWO', 2);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (134, 35, '3', 'Most of the time', 4, 'THREE', 3);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (135, 35, '4', 'All the time', 5, 'FOUR', 4);

/* IBD fatigue, section 2 */
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (136, 36, '0', null, 1, 'ZERO', 0);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (137, 36, '1', null, 2, 'ONE', 1);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (138, 36, '2', null, 3, 'TWO', 2);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (139, 36, '3', null, 4, 'THREE', 3);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (140, 36, '4', null, 5, 'FOUR', 4);

INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (141, 37, '0', null, 1, 'ZERO', 0);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (142, 37, '1', null, 2, 'ONE', 1);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (143, 37, '2', null, 3, 'TWO', 2);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (144, 37, '3', null, 4, 'THREE', 3);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (145, 37, '4', null, 5, 'FOUR', 4);

INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (146, 38, '0', null, 1, 'ZERO', 0);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (147, 38, '1', null, 2, 'ONE', 1);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (148, 38, '2', null, 3, 'TWO', 2);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (149, 38, '3', null, 4, 'THREE', 3);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (150, 38, '4', null, 5, 'FOUR', 4);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (151, 38, 'N/A', null, 6, 'NOT_APPLICABLE', 0);

INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (152, 39, '0', null, 1, 'ZERO', 0);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (153, 39, '1', null, 2, 'ONE', 1);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (154, 39, '2', null, 3, 'TWO', 2);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (155, 39, '3', null, 4, 'THREE', 3);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (156, 39, '4', null, 5, 'FOUR', 4);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (157, 39, 'N/A', null, 6, 'NOT_APPLICABLE', 0);

INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (158, 40, '0', null, 1, 'ZERO', 0);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (159, 40, '1', null, 2, 'ONE', 1);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (160, 40, '2', null, 3, 'TWO', 2);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (161, 40, '3', null, 4, 'THREE', 3);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (162, 40, '4', null, 5, 'FOUR', 4);

INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (163, 41, '0', null, 1, 'ZERO', 0);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (164, 41, '1', null, 2, 'ONE', 1);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (165, 41, '2', null, 3, 'TWO', 2);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (166, 41, '3', null, 4, 'THREE', 3);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (167, 41, '4', null, 5, 'FOUR', 4);

INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (168, 42, '0', null, 1, 'ZERO', 0);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (169, 42, '1', null, 2, 'ONE', 1);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (170, 42, '2', null, 3, 'TWO', 2);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (171, 42, '3', null, 4, 'THREE', 3);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (172, 42, '4', null, 5, 'FOUR', 4);

INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (173, 43, '0', null, 1, 'ZERO', 0);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (174, 43, '1', null, 2, 'ONE', 1);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (175, 43, '2', null, 3, 'TWO', 2);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (176, 43, '3', null, 4, 'THREE', 3);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (177, 43, '4', null, 5, 'FOUR', 4);

INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (178, 44, '0', null, 1, 'ZERO', 0);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (179, 44, '1', null, 2, 'ONE', 1);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (180, 44, '2', null, 3, 'TWO', 2);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (181, 44, '3', null, 4, 'THREE', 3);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (182, 44, '4', null, 5, 'FOUR', 4);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (183, 44, 'N/A', null, 6, 'NOT_APPLICABLE', 0);

INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (184, 45, '0', null, 1, 'ZERO', 0);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (185, 45, '1', null, 2, 'ONE', 1);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (186, 45, '2', null, 3, 'TWO', 2);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (187, 45, '3', null, 4, 'THREE', 3);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (188, 45, '4', null, 5, 'FOUR', 4);

INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (189, 46, '0', null, 1, 'ZERO', 0);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (190, 46, '1', null, 2, 'ONE', 1);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (191, 46, '2', null, 3, 'TWO', 2);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (192, 46, '3', null, 4, 'THREE', 3);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (193, 46, '4', null, 5, 'FOUR', 4);

INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (194, 47, '0', null, 1, 'ZERO', 0);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (195, 47, '1', null, 2, 'ONE', 1);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (196, 47, '2', null, 3, 'TWO', 2);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (197, 47, '3', null, 4, 'THREE', 3);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (198, 47, '4', null, 5, 'FOUR', 4);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (199, 47, 'N/A', null, 6, 'NOT_APPLICABLE', 0);

INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (200, 48, '0', null, 1, 'ZERO', 0);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (201, 48, '1', null, 2, 'ONE', 1);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (202, 48, '2', null, 3, 'TWO', 2);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (203, 48, '3', null, 4, 'THREE', 3);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (204, 48, '4', null, 5, 'FOUR', 4);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (205, 48, 'N/A', null, 6, 'NOT_APPLICABLE', 0);

INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (206, 49, '0', null, 1, 'ZERO', 0);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (207, 49, '1', null, 2, 'ONE', 1);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (208, 49, '2', null, 3, 'TWO', 2);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (209, 49, '3', null, 4, 'THREE', 3);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (210, 49, '4', null, 5, 'FOUR', 4);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (211, 49, 'N/A', null, 6, 'NOT_APPLICABLE', 0);

INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (212, 50, '0', null, 1, 'ZERO', 0);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (213, 50, '1', null, 2, 'ONE', 1);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (214, 50, '2', null, 3, 'TWO', 2);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (215, 50, '3', null, 4, 'THREE', 3);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (216, 50, '4', null, 5, 'FOUR', 4);

INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (217, 51, '0', null, 1, 'ZERO', 0);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (218, 51, '1', null, 2, 'ONE', 1);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (219, 51, '2', null, 3, 'TWO', 2);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (220, 51, '3', null, 4, 'THREE', 3);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (221, 51, '4', null, 5, 'FOUR', 4);

INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (222, 52, '0', null, 1, 'ZERO', 0);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (223, 52, '1', null, 2, 'ONE', 1);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (224, 52, '2', null, 3, 'TWO', 2);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (225, 52, '3', null, 4, 'THREE', 3);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (226, 52, '4', null, 5, 'FOUR', 4);

INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (227, 53, '0', null, 1, 'ZERO', 0);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (228, 53, '1', null, 2, 'ONE', 1);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (229, 53, '2', null, 3, 'TWO', 2);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (230, 53, '3', null, 4, 'THREE', 3);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (231, 53, '4', null, 5, 'FOUR', 4);

INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (232, 54, '0', null, 1, 'ZERO', 0);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (233, 54, '1', null, 2, 'ONE', 1);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (234, 54, '2', null, 3, 'TWO', 2);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (235, 54, '3', null, 4, 'THREE', 3);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (236, 54, '4', null, 5, 'FOUR', 4);

INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (237, 55, '0', null, 1, 'ZERO', 0);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (238, 55, '1', null, 2, 'ONE', 1);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (239, 55, '2', null, 3, 'TWO', 2);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (240, 55, '3', null, 4, 'THREE', 3);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (241, 55, '4', null, 5, 'FOUR', 4);

INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (242, 56, '0', null, 1, 'ZERO', 0);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (243, 56, '1', null, 2, 'ONE', 1);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (244, 56, '2', null, 3, 'TWO', 2);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (245, 56, '3', null, 4, 'THREE', 3);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (246, 56, '4', null, 5, 'FOUR', 4);

INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (247, 57, '0', null, 1, 'ZERO', 0);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (248, 57, '1', null, 2, 'ONE', 1);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (249, 57, '2', null, 3, 'TWO', 2);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (250, 57, '3', null, 4, 'THREE', 3);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (251, 57, '4', null, 5, 'FOUR', 4);

INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (252, 58, '0', null, 1, 'ZERO', 0);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (253, 58, '1', null, 2, 'ONE', 1);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (254, 58, '2', null, 3, 'TWO', 2);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (255, 58, '3', null, 4, 'THREE', 3);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (256, 58, '4', null, 5, 'FOUR', 4);

INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (257, 59, '0', null, 1, 'ZERO', 0);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (258, 59, '1', null, 2, 'ONE', 1);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (259, 59, '2', null, 3, 'TWO', 2);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (260, 59, '3', null, 4, 'THREE', 3);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (261, 59, '4', null, 5, 'FOUR', 4);

INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (262, 60, '0', null, 1, 'ZERO', 0);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (263, 60, '1', null, 2, 'ONE', 1);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (264, 60, '2', null, 3, 'TWO', 2);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (265, 60, '3', null, 4, 'THREE', 3);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (266, 60, '4', null, 5, 'FOUR', 4);

INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (267, 61, '0', null, 1, 'ZERO', 0);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (268, 61, '1', null, 2, 'ONE', 1);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (269, 61, '2', null, 3, 'TWO', 2);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (270, 61, '3', null, 4, 'THREE', 3);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (271, 61, '4', null, 5, 'FOUR', 4);

INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (272, 62, '0', null, 1, 'ZERO', 0);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (273, 62, '1', null, 2, 'ONE', 1);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (274, 62, '2', null, 3, 'TWO', 2);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (275, 62, '3', null, 4, 'THREE', 3);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (276, 62, '4', null, 5, 'FOUR', 4);

INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (277, 63, '0', null, 1, 'ZERO', 0);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (278, 63, '1', null, 2, 'ONE', 1);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (279, 63, '2', null, 3, 'TWO', 2);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (280, 63, '3', null, 4, 'THREE', 3);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (281, 63, '4', null, 5, 'FOUR', 4);

INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (282, 64, '0', null, 1, 'ZERO', 0);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (283, 64, '1', null, 2, 'ONE', 1);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (284, 64, '2', null, 3, 'TWO', 2);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (285, 64, '3', null, 4, 'THREE', 3);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (286, 64, '4', null, 5, 'FOUR', 4);

INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (287, 65, '0', null, 1, 'ZERO', 0);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (288, 65, '1', null, 2, 'ONE', 1);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (289, 65, '2', null, 3, 'TWO', 2);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (290, 65, '3', null, 4, 'THREE', 3);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (291, 65, '4', null, 5, 'FOUR', 4);

/* IBD fatigue, section 3 */
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (292, 71, 'Constant', null, 1, 'CONSTANT', 0);
INSERT INTO "pv_question_option" ("id", "question_id", "text", "description", "display_order", "type", "score")
VALUES (293, 71, 'Intermittent', null, 2, 'INTERMITTENT', 0);
