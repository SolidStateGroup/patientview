INSERT INTO pv_user (id, username, password, change_password, locked, email, forename, surname, start_date, creation_date, created_by, dummy) VALUES
  (1, 'system','pppppp', false, false, 'system@patientview.org', 'system', 'system', now(), now(), 1, false);

INSERT INTO pv_user (id, username, password, change_password, locked, email, forename, surname, start_date, creation_date, created_by, dummy) VALUES
  (2, 'migration','pppppp', false, false, 'migration@patientview.org', 'migration', 'migration', now(), now(), 1, false);

INSERT INTO pv_user (id, username, password, change_password, locked, email, forename, surname, start_date, creation_date, created_by, dummy) VALUES
  (3, 'globaladmin','pppppp', false, false, 'globaladmin@patientview.org', 'globaladmin', 'globaladmin', now(), now(), 1, false);

INSERT INTO pv_user (id, username, password, change_password, locked, email, forename, surname, start_date, creation_date, created_by, dummy) VALUES
  (4, 'importer','pppppp', false, false, 'importer@patientview.org', 'importer', 'importer', now(), now(), 1, false);

INSERT INTO pv_user (id, username, password, change_password, locked, email, forename, surname, start_date, creation_date, created_by, dummy) VALUES
  (5, 'ecs','pppppp', false, false, 'ecs@patientview.org', 'ecs', 'ecs', now(), now(), 1, false);

INSERT INTO pv_user (id, username, password, change_password, locked, email, forename, surname, start_date, creation_date, created_by, dummy) VALUES
  (6, 'patientviewnotifications','pppppp', false, false, 'patientviewnotifications@patientview.org', 'PatientView', 'Notifications', now(), now(), 1, false);

INSERT INTO pv_lookup_type(id, creation_date, description, lookup_type, created_by)  VALUES (1, now(), 'Type of group','GROUP', '1');
INSERT INTO pv_lookup_type(id, creation_date, description, lookup_type, created_by)  VALUES (2, now(), 'Type of menu','MENU', '1');
INSERT INTO pv_lookup_type(id, creation_date, description, lookup_type, created_by)  VALUES (3, now(), 'Type of role','ROLE', '1');
INSERT INTO pv_lookup_type(id, creation_date, description, lookup_type, created_by)  VALUES (4, now(), 'Type of external coding standard','CODE_STANDARD', '1');
INSERT INTO pv_lookup_type(id, creation_date, description, lookup_type, created_by)  VALUES (5, now(), 'Type of code','CODE_TYPE', '1');
INSERT INTO pv_lookup_type(id, creation_date, description, lookup_type, created_by)  VALUES (6, now(), 'Type of feature','FEATURE_TYPE', '1');
INSERT INTO pv_lookup_type(id, creation_date, description, lookup_type, created_by)  VALUES (8, now(), 'Identifier','IDENTIFIER', '1');
INSERT INTO pv_lookup_type(id, creation_date, description, lookup_type, created_by)  VALUES (9, now(), 'Contact point type','CONTACT_POINT_TYPE', '1');
INSERT INTO pv_lookup_type(id, creation_date, description, lookup_type, created_by)  VALUES (10, now(), 'Types of statistic','STATISTIC_TYPE', '1');
INSERT INTO pv_lookup_type(id, creation_date, description, lookup_type, created_by)  VALUES (11, now(), 'Types of news items','NEWS_TYPE', '1');
/* IBD patient management */
INSERT INTO pv_lookup_type(id, creation_date, description, lookup_type, created_by)  VALUES (12, now(), 'Gender','GENDER', '1');
INSERT INTO pv_lookup_type(id, creation_date, description, lookup_type, created_by)  VALUES (13, now(), 'Crohns Location','IBD_CROHNSLOCATION', '1');
INSERT INTO pv_lookup_type(id, creation_date, description, lookup_type, created_by)  VALUES (14, now(), 'Disease Proximal to Terminal Ileum','IBD_CROHNSPROXIMALTERMINALILEUM', '1');
INSERT INTO pv_lookup_type(id, creation_date, description, lookup_type, created_by)  VALUES (15, now(), 'Perianal','IBD_CROHNSPERIANAL', '1');
INSERT INTO pv_lookup_type(id, creation_date, description, lookup_type, created_by)  VALUES (16, now(), 'Complications','IBD_CROHNSBEHAVIOUR', '1');
INSERT INTO pv_lookup_type(id, creation_date, description, lookup_type, created_by)  VALUES (17, now(), 'Other Location','IBD_UCEXTENT', '1');
INSERT INTO pv_lookup_type(id, creation_date, description, lookup_type, created_by)  VALUES (18, now(), 'Other Part(s) of The Body Affected','IBD_EGIMCOMPLICATION', '1');
INSERT INTO pv_lookup_type(id, creation_date, description, lookup_type, created_by)  VALUES (19, now(), 'Surgery Main Procedure','IBD_SURGERYMAINPROCEDURE', '1');
INSERT INTO pv_lookup_type(id, creation_date, description, lookup_type, created_by)  VALUES (20, now(), 'Smoking Status','IBD_SMOKINGSTATUS', '1');
INSERT INTO pv_lookup_type(id, creation_date, description, lookup_type, created_by)  VALUES (21, now(), 'Family History','IBD_FAMILYHISTORY', '1');
INSERT INTO pv_lookup_type(id, creation_date, description, lookup_type, created_by)  VALUES (22, now(), 'Code Link Types','LINK_TYPE', '1');

INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id) VALUES (1, now(), 'UNIT','Unit','1', '1');
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id) VALUES (2, now(), 'SPECIALTY','Specialty','1','1');
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id) VALUES (3, now(), 'TOP_RIGHT','Top Right','1','2');
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id) VALUES (4, now(), 'TOP','Top','1','2');
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id) VALUES (5, now(), 'NOT_DISPLAYED','Not Displayed','1','2');
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id) VALUES (6, now(), 'STAFF','Staff','1','3');
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id) VALUES (7, now(), 'PATIENT','Patient','1','3');
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id) VALUES (8, now(), 'EDTA','EDTA','1','4');
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id) VALUES (9, now(), 'READ','READ','1','4');
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id) VALUES (10, now(), 'ICD','ICD','1','4');
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id) VALUES (11, now(), 'SNOMED','SNOMED','1','4');
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id) VALUES (12, now(), 'DIAGNOSIS','Diagnosis','1','5');
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id) VALUES (13, now(), 'TREATMENT','Treatment','1','5');
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id) VALUES (14, now(), 'GROUP','Group','1','6');
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id) VALUES (15, now(), 'STAFF','Staff','1','6');
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id) VALUES (16, now(), 'PATIENT','Patient','1','6');
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id) VALUES (19, now(), 'DISEASE_GROUP','Disease Group','1','1');
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id) VALUES (20, now(), 'NHS_NUMBER','NHS Number','1','8');
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id) VALUES (21, now(), 'CHI_NUMBER','CHI Number','1','8');
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id) VALUES (22, now(), 'NAV','Nav','1','2');
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id) VALUES (23, now(), 'UNIT_WEB_ADDRESS','Unit Web Address','1','9');
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id) VALUES (24, now(), 'TRUST_WEB_ADDRESS','Trust Web Address','1','9');
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id) VALUES (25, now(), 'PV_ADMIN_NAME','PatientView Admin Name','1','9');
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id) VALUES (26, now(), 'PV_ADMIN_PHONE','PatientView Admin Phone','1','9');
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id) VALUES (27, now(), 'PV_ADMIN_EMAIL','PatientView Admin Email','1','9');
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id) VALUES (28, now(), 'UNIT_ENQUIRIES_PHONE','Unit Enquiries Phone','1','9');
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id) VALUES (29, now(), 'UNIT_ENQUIRIES_EMAIL','Unit Enquiries Email','1','9');
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id) VALUES (30, now(), 'APPOINTMENT_PHONE','Appointment Phone','1','9');
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id) VALUES (31, now(), 'APPOINTMENT_EMAIL','Appointment Email','1','9');
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id) VALUES (32, now(), 'OUT_OF_HOURS_INFO','Out of Hours Information','1','9');
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id) VALUES (33, now(), 'PUBLIC','Public','1','3');
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id) VALUES (47, now(), 'HSC_NUMBER','H&SC Number','1','8');
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id) VALUES (48, now(), 'HOSPITAL_NUMBER','Hospital Number','1','8');
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id) VALUES (49, now(), 'RADAR_NUMBER','Radar Number','1','8');
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id) VALUES (57, now(), 'NON_UK_UNIQUE','Non UK Unique Identifier','1','8');
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id) VALUES (60, now(), 'CENTRAL_SUPPORT','Central Support','1','1');
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id) VALUES (61, now(), 'REGULAR','Regular News Item','1','11');
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id) VALUES (62, now(), 'DASHBOARD','Dashboard News Item','1','11');
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id) VALUES (63, now(), 'ALL','All News Items','1','11');
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id) VALUES (64, now(), 'GENERAL_PRACTICE','General Practice','1', '1');
/* IBD patient management */
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id, display_order) VALUES (65, now(), '0', 'Not Known', 1, 12, 1);
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id, display_order) VALUES (66, now(), '1', 'Male', 1, 12, 2);
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id, display_order) VALUES (67, now(), '2', 'Female', 1, 12, 3);
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id, display_order) VALUES (68, now(), 'L1', 'Terminal Ileum +/- limited caecal disease', 1, 13, 1);
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id, display_order) VALUES (69, now(), 'L2', 'Colonic', 1, 13, 2);
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id, display_order) VALUES (70, now(), 'L3', 'Ileocolonic', 1, 13, 3);
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id, display_order) VALUES (71, now(), 'None', 'None of the above', 1, 13, 4);
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id, display_order) VALUES (72, now(), 'YES', 'Yes', 1, 14, 1);
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id, display_order) VALUES (73, now(), 'NO', 'No', 1, 14, 2);
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id, display_order) VALUES (74, now(), 'YES', 'Yes', 1, 15, 1);
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id, display_order) VALUES (75, now(), 'NO', 'No', 1, 15, 2);
INSERT INTO pv_lookup_value(id, creation_date, value, description, description_friendly, created_by, lookup_type_id, display_order) VALUES (76, now(), 'B1', 'Non-Stricturing, Non-Penetrating', 'Inflammation Only', 1, 16, 1);
INSERT INTO pv_lookup_value(id, creation_date, value, description, description_friendly, created_by, lookup_type_id, display_order) VALUES (77, now(), 'B2', 'Stricturing', 'Stricturing Disease', 1, 16, 2);
INSERT INTO pv_lookup_value(id, creation_date, value, description, description_friendly, created_by, lookup_type_id, display_order) VALUES (78, now(), 'B3', 'Penetrating', 'Fistulating Disease', 1, 16, 3);
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id, display_order) VALUES (79, now(), 'E1', 'Ulcerative Proctitis', 1, 17, 1);
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id, display_order) VALUES (80, now(), 'E2', 'Left Sided UC (Distal UC)', 1, 17, 2);
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id, display_order) VALUES (81, now(), 'E3', 'Extensive UC (Pancolitis)', 1, 17, 3);
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id, display_order) VALUES (82, now(), '00', 'None', 1, 18, 1);
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id, display_order) VALUES (83, now(), '01', 'Bone: Osteopenia', 1, 18, 2);
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id, display_order) VALUES (84, now(), '02', 'Bone: Osteoporosis', 1, 18, 3);
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id, display_order) VALUES (85, now(), '03', 'Bone: Fracture', 1, 18, 4);
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id, display_order) VALUES (86, now(), '04', 'Joints: Arthralgia', 1, 18, 5);
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id, display_order) VALUES (87, now(), '05', 'Joints: Monoarthritis', 1, 18, 6);
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id, display_order) VALUES (88, now(), '06', 'Joints: Polyarthritis', 1, 18, 7);
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id, display_order) VALUES (89, now(), '25', 'Joints: Spondyloarthropathy', 1, 18, 8);
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id, display_order) VALUES (90, now(), '07', 'Joints: Ankylosing Spondylitis', 1, 18, 9);
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id, display_order) VALUES (91, now(), '08', 'Joints: Sacro-iliitis', 1, 18, 10);
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id, display_order) VALUES (92, now(), '09', 'Skin: Erythema Nodosum', 1, 18, 11);
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id, display_order) VALUES (93, now(), '10', 'Skin: Pyoderma gangrenosum', 1, 18, 12);
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id, display_order) VALUES (94, now(), '11', 'Skin: Metastatic Crohn''s Disease', 1, 18, 13);
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id, display_order) VALUES (95, now(), '26', 'Skin: Oro-facial granulomatosis', 1, 18, 14);
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id, display_order) VALUES (96, now(), '24', 'Skin: Aphthous ulcers', 1, 18, 15);
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id, display_order) VALUES (97, now(), '12', 'Eye: Uveitis', 1, 18, 16);
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id, display_order) VALUES (98, now(), '13', 'Eye: Episcleritis', 1, 18, 17);
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id, display_order) VALUES (99, now(), '14', 'Eye: Iritis', 1, 18, 18);
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id, display_order) VALUES (100, now(), '15', 'Vascular: DVT', 1, 18, 19);
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id, display_order) VALUES (101, now(), '16', 'Vascular: PE', 1, 18, 20);
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id, display_order) VALUES (102, now(), '17', 'Hepatobiliary: Sclerosing cholangitis', 1, 18, 21);
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id, display_order) VALUES (103, now(), '18', 'Hepatobiliary: Hepatitis (auto-immune)', 1, 18, 22);
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id, display_order) VALUES (104, now(), '19', 'Hepatobiliary: Raised Transaminases', 1, 18, 23);
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id, display_order) VALUES (105, now(), '20', 'Hepatobiliary: Gallstones', 1, 18, 24);
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id, display_order) VALUES (106, now(), '21', 'Renal: Glomerulopathy', 1, 18, 25);
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id, display_order) VALUES (107, now(), '22', 'Renal: Stones', 1, 18, 26);
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id, display_order) VALUES (108, now(), '23', 'Other', 1, 18, 27);
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id, display_order) VALUES (109, now(), '01.1', 'Apendicectomy', 1, 19, 1);
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id, display_order) VALUES (110, now(), '04.1', 'Total proctocolectomy', 1, 19, 2);
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id, display_order) VALUES (111, now(), '04.2', 'Ileonal pouch', 1, 19, 3);
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id, display_order) VALUES (112, now(), '05.1', 'Partial colectomy & colostomy with retained rectal stump', 1, 19, 4);
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id, display_order) VALUES (113, now(), '05.2', 'Colectomy ileostomy with retained rectal stump', 1, 19, 5);
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id, display_order) VALUES (114, now(), '05.3', 'Pancolectomy', 1, 19, 6);
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id, display_order) VALUES (115, now(), '06.1', 'Partial (segmental) colectomy', 1, 19, 7);
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id, display_order) VALUES (116, now(), '07.1', 'Right hemicolectomy', 1, 19, 8);
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id, display_order) VALUES (117, now(), '09.1', 'Left hemicolectomy', 1, 19, 9);
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id, display_order) VALUES (118, now(), '55.4', 'Insertion of seton', 1, 19, 10);
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id, display_order) VALUES (119, now(), '55.5', 'Fistulectomy', 1, 19, 11);
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id, display_order) VALUES (120, now(), '58.2', 'Drainage of perianal sepsis', 1, 19, 12);
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id, display_order) VALUES (121, now(), '27.2', 'Gastric surgery', 1, 19, 13);
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id, display_order) VALUES (122, now(), '58.1', 'Small bowel resection', 1, 19, 14);
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id, display_order) VALUES (123, now(), '73.3', 'Permanent ileostomy', 1, 19, 15);
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id, display_order) VALUES (124, now(), '78.2', 'Stricturoplasty', 1, 19, 16);
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id, display_order) VALUES (125, now(), 'J18.1', 'Cholecystectomy', 1, 19, 17);
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id, display_order) VALUES (126, now(), 'Y53.1', 'Radiological drainage of abscess', 1, 19, 18);
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id, display_order) VALUES (127, now(), '99', 'Other surgery (not specified)', 1, 19, 19);
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id, display_order) VALUES (128, now(), '1', 'Current Smoker', 1, 20, 1);
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id, display_order) VALUES (129, now(), '2', 'Ex-Smoker', 1, 20, 2);
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id, display_order) VALUES (130, now(), '3', 'Non-Smoker - History Unknown', 1, 20, 3);
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id, display_order) VALUES (131, now(), '4', 'Never Smoked', 1, 20, 4);
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id, display_order) VALUES (132, now(), 'YES', 'Yes', 1, 21, 3);
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id, display_order) VALUES (133, now(), 'NO', 'No', 1, 21, 4);

/* for vanilla PV */
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id) VALUES (134, now(), 'PATIENTVIEW','PatientView','1','4');

INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id, display_order) VALUES (135, now(), 'NHS_CHOICES', 'NHS Choices Information', 1, 22, 1);
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id, display_order) VALUES (136, now(), 'MEDLINE_PLUS', 'Medline Plus (USA)', 1, 22, 2);
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id, display_order) VALUES (137, now(), 'CUSTOM', 'User link', 1, 22, 3);

/* Group Statistics (see AuditActions.java) */
/* pv1 "admin add" */
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id) VALUES (34, now(),
'ADMIN_GROUP_ROLE_ADD_COUNT', 'SELECT COUNT(adt.id) FROM pv_audit adt WHERE adt.group_id = :groupId AND adt.action = ''ADMIN_GROUP_ROLE_ADD'' AND adt.creation_date BETWEEN :startDate AND :endDate','1','10');

/* pv1 "email verified" */
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id) VALUES (35, now(),
'EMAIL_VERIFY_COUNT', 'SELECT COUNT(adt.id) FROM pv_audit adt, pv_user_group_role upr WHERE adt.source_object_id = upr.user_id AND upr.group_id = :groupId AND adt.action = ''EMAIL_VERIFY'' AND adt.creation_date BETWEEN :startDate AND :endDate','1','10');

/* pv1 "email changed" */
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id) VALUES (36, now(),
'EMAIL_CHANGED_COUNT', 'SELECT COUNT(adt.id) FROM pv_audit adt, pv_user_group_role upr WHERE adt.source_object_id = upr.user_id AND upr.group_id = :groupId AND adt.action = ''EMAIL_CHANGED'' AND adt.creation_date BETWEEN :startDate AND :endDate','1','10');

/* pv1 "logon" */
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id) VALUES (37, now(),
'LOGGED_ON_COUNT', 'SELECT COUNT(adt.id) FROM pv_audit adt, pv_user_group_role upr WHERE adt.source_object_id = upr.user_id AND upr.group_id = :groupId AND adt.action = ''LOGGED_ON'' AND adt.creation_date BETWEEN :startDate AND :endDate','1','10');

/* pv1 "password change" */
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id) VALUES (38, now(),
'PASSWORD_CHANGE_COUNT', 'SELECT COUNT(adt.id) FROM pv_audit adt, pv_user_group_role upr WHERE adt.source_object_id = upr.user_id AND upr.group_id = :groupId AND adt.action = ''PASSWORD_CHANGE'' AND adt.creation_date BETWEEN :startDate AND :endDate','1','10');

/* pv1 "password locked" */
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id) VALUES (39, now(),
'ACCOUNT_LOCKED_COUNT', 'SELECT COUNT(adt.id) FROM pv_audit adt, pv_user_group_role upr WHERE adt.source_object_id = upr.user_id AND upr.group_id = :groupId AND adt.action = ''ACCOUNT_LOCKED'' AND adt.creation_date BETWEEN :startDate AND :endDate','1','10');

/* pv1 "password reset" */
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id) VALUES (40, now(),
'PASSWORD_RESET_COUNT', 'SELECT COUNT(adt.id) FROM pv_audit adt, pv_user_group_role upr WHERE adt.source_object_id = upr.user_id AND upr.group_id = :groupId AND adt.action = ''PASSWORD_RESET'' AND adt.creation_date BETWEEN :startDate AND :endDate','1','10');

/* pv1 "password reset forgotten" */
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id) VALUES (41, now(),
'PASSWORD_RESET_FORGOTTEN_COUNT', 'SELECT COUNT(adt.id) FROM pv_audit adt, pv_user_group_role upr WHERE adt.source_object_id = upr.user_id AND upr.group_id = :groupId AND adt.action = ''PASSWORD_RESET_FORGOTTEN'' AND adt.creation_date BETWEEN :startDate AND :endDate','1','10');

/* pv1 "password unlocked" */
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id) VALUES (42, now(),
'ACCOUNT_UNLOCKED_COUNT', 'SELECT COUNT(adt.id) FROM pv_audit adt, pv_user_group_role upr WHERE adt.source_object_id = upr.user_id AND upr.group_id = :groupId AND adt.action = ''ACCOUNT_UNLOCKED'' AND adt.creation_date BETWEEN :startDate AND :endDate','1','10');

/* pv1 "patient add" */
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id) VALUES (43, now(),
'PATIENT_GROUP_ROLE_ADD_COUNT', 'SELECT COUNT(adt.id) FROM pv_audit adt WHERE adt.group_id = :groupId AND adt.action = ''PATIENT_GROUP_ROLE_ADD'' AND adt.creation_date BETWEEN :startDate AND :endDate','1','10');

/* pv1 "patient data fail" */
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id) VALUES (44, now(),
'PATIENT_DATA_FAIL_COUNT', 'SELECT COUNT(adt.id) FROM pv_audit adt WHERE adt.group_id = :groupId AND adt.action = ''PATIENT_DATA_FAIL'' AND adt.creation_date BETWEEN :startDate AND :endDate','1','10');

/* pv1 "patient data load" */
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id) VALUES (45, now(),
'PATIENT_DATA_SUCCESS_COUNT','SELECT COUNT(adt.id) FROM pv_audit adt WHERE adt.group_id = :groupId AND adt.action = ''PATIENT_DATA_SUCCESS'' AND adt.creation_date BETWEEN :startDate AND :endDate','1','10');

/* pv1 "patient delete" */
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id) VALUES (46, now(),
'PATIENT_GROUP_ROLE_DELETE_COUNT', 'SELECT COUNT(adt.id) FROM pv_audit adt WHERE adt.group_id = :groupId AND adt.action = ''PATIENT_GROUP_ROLE_DELETE'' AND adt.creation_date BETWEEN :startDate AND :endDate','1','10');

/* pv1 "patient view" */
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id) VALUES (50, now(),
'PATIENT_VIEW_COUNT','SELECT COUNT(adt.id) FROM pv_audit adt, pv_user_group_role upr WHERE adt.source_object_id = upr.user_id AND upr.group_id = :groupId AND adt.action = ''PATIENT_VIEW'' AND adt.creation_date BETWEEN :startDate AND :endDate','1','10');

/* pv1 "unique data load" */
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id) VALUES (51, now(),
'UNIQUE_PATIENT_DATA_SUCCESS_COUNT','SELECT COUNT(DISTINCT source_object_id) FROM pv_audit adt, pv_user_group_role upr WHERE adt.source_object_id = upr.user_id AND upr.group_id = :groupId AND adt.action = ''PATIENT_DATA_SUCCESS'' AND adt.creation_date BETWEEN :startDate AND :endDate','1','10');

/* pv1 "unique logon" */
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id) VALUES (52, now(),
'UNIQUE_LOGGED_ON_COUNT', 'SELECT COUNT(DISTINCT source_object_id) FROM pv_audit adt, pv_user_group_role upr WHERE adt.source_object_id = upr.user_id AND upr.group_id = :groupId AND adt.action = ''LOGGED_ON'' AND adt.creation_date BETWEEN :startDate AND :endDate','1','10');

/* new in pv2 */
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id) VALUES (53, now(),
'PATIENT_COUNT','SELECT COUNT(DISTINCT(user_id)) FROM pv_user_group_role WHERE role_id = 1 AND group_id = :groupId','1','10');

/* new in pv2 */
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id) VALUES (54, now(),
'INACTIVE_USER_COUNT', 'SELECT COUNT(DISTINCT(u.id)) FROM pv_user u, pv_user_group_role gr WHERE gr.group_id = :groupId AND gr.user_id = u.id AND (NOT (u.current_login BETWEEN LOCALTIMESTAMP - INTERVAL ''3 months'' AND LOCALTIMESTAMP) OR u.current_login IS NULL) AND u.deleted = false','1','10');

/* new in pv2 */
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id) VALUES (55, now(),
'LOCKED_USER_COUNT', 'SELECT COUNT(DISTINCT(u.id)) FROM pv_user u, pv_user_group_role gr WHERE gr.group_id = :groupId AND gr.user_id = u.id AND u.locked = true AND u.deleted = false','1','10');

/* new in pv2 */
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id) VALUES (56, now(),
'USER_COUNT','SELECT COUNT(DISTINCT(ugr.user_id)) FROM pv_user_group_role ugr, pv_user u WHERE ugr.group_id = :groupId AND ugr.user_id = u.id AND u.deleted = false','1','10');

/* new in pv2 */
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id) VALUES (58, now(),
'INACTIVE_PATIENT_COUNT', 'SELECT COUNT(DISTINCT(u.id)) FROM pv_user u, pv_user_group_role gr WHERE gr.group_id = :groupId AND gr.user_id = u.id AND (NOT (u.current_login BETWEEN LOCALTIMESTAMP - INTERVAL ''3 months'' AND LOCALTIMESTAMP) OR u.current_login IS NULL) AND u.deleted = false AND gr.role_id = 1','1','10');

/* new in pv2 */
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id) VALUES (59, now(),
'LOCKED_PATIENT_COUNT', 'SELECT COUNT(DISTINCT(u.id)) FROM pv_user u, pv_user_group_role gr WHERE gr.group_id = :groupId AND gr.user_id = u.id AND u.locked = true AND u.deleted = false AND gr.role_id = 1','1','10');

INSERT INTO pv_group(id, Group_Name, Group_Short_Name, Code, Sftp_User, Type_Id, Visible, Creation_Date,Created_By, Visible_To_Join) VALUES (1, 'Generic', 'Generic', 'Generic', null, 2, false, now(),1, false );
INSERT INTO pv_group(id, Group_Name, Group_Short_Name, Code, Sftp_User, Type_Id, Visible, Creation_Date,Created_By, Visible_To_Join) VALUES (2, 'Renal', 'Renal', 'Renal', null, 2, true, now(),1 , true);
INSERT INTO pv_group(id, Group_Name, Group_Short_Name, Code, Sftp_User, Type_Id, Visible, Creation_Date,Created_By, Visible_To_Join) VALUES (3, 'Diabetes', 'Diabetes', 'Diabetes', null, 2, true, now(), 1, true);
INSERT INTO pv_group(id, Group_Name, Group_Short_Name, Code, Sftp_User, Type_Id, Visible, Creation_Date,Created_By, Visible_To_Join) VALUES (4, 'IBD', 'IBD', 'IBD', null, 2, true, now(), 1, true);
INSERT INTO pv_group(id, Group_Name, Group_Short_Name, Code, Sftp_User, Type_Id, Visible, Creation_Date,Created_By, Visible_To_Join) VALUES (5, 'ECS', 'ECS', 'ECS', null, 1, false, now(), 1, false);
/* used for storing user entered results */
INSERT INTO pv_group(id, Group_Name, Code, Sftp_User, Type_Id, Visible, Creation_Date,Created_By, Visible_To_Join, Group_Short_Name) VALUES (6, 'Patient Entered Data', 'PATIENT_ENTERED', null, 2, false, now(), 1, false, 'Patient Entered');
/* used for storing staff entered results */
INSERT INTO pv_group(id, Group_Name, Code, Sftp_User, Type_Id, Visible, Creation_Date,Created_By, Visible_To_Join, Group_Short_Name) VALUES (7, 'Staff Entered Data', 'STAFF_ENTERED', null, 2, false, now(), 1, false, 'Staff Entered');
/* General Practice specialty, used as parent for all groups of type GENERAL_PRACTICE */
INSERT INTO pv_group(id, Group_Name, Group_Short_Name, Code, Sftp_User, Type_Id, Visible, Creation_Date,Created_By, Visible_To_Join) VALUES (8, 'General Practice', 'GP', 'GENERAL_PRACTICE', null, 2, false, now(),1 , true);

INSERT INTO pv_role(id, role_name, type_id, level, visible, description, creation_date, created_by) VALUES (1, 'PATIENT', '7', '20', true, 'Patient', now(), '1');
INSERT INTO pv_role(id, role_name, type_id, level, visible, description, creation_date, created_by) VALUES (2, 'UNIT_ADMIN', '6', '40', true, 'Unit Admin', now(), '1');
INSERT INTO pv_role(id, role_name, type_id, level, visible, description, creation_date, created_by) VALUES (3, 'STAFF_ADMIN', '6', '30', true, 'Unit Staff', now(), '1');
INSERT INTO pv_role(id, role_name, type_id, level, visible, description, creation_date, created_by) VALUES (4, 'SPECIALTY_ADMIN', '6', '50', true, 'Specialty Admin', now(), '1');
INSERT INTO pv_role(id, role_name, type_id, level, visible, description, creation_date, created_by) VALUES (5, 'GLOBAL_ADMIN', '6', '60', true, 'Global Admin', now(), '1');
INSERT INTO pv_role(id, role_name, type_id, level, visible, description, creation_date, created_by) VALUES (7, 'MEMBER', '7', '10', false, 'Logged-in Users', now(), '1');
INSERT INTO pv_role(id, role_name, type_id, level, visible, description, creation_date, created_by) VALUES (8, 'PUBLIC', '7', '10', false, 'Non-Logged on Visitors', now(), '1');
INSERT INTO pv_role(id, role_name, type_id, level, visible, description, creation_date, created_by) VALUES (9, 'DISEASE_GROUP_ADMIN', '6', '30', true, 'Disease Group Admin', now(), '1');
INSERT INTO pv_role(id, role_name, type_id, level, visible, description, creation_date, created_by) VALUES (10, 'UNIT_ADMIN_API', '6', '40', false, 'Unit Admin (API)', now(), '1');
INSERT INTO pv_role(id, role_name, type_id, level, visible, description, creation_date, created_by) VALUES (11, 'GP_ADMIN', '6', '25', true, 'GP Admin', now(), '1');
INSERT INTO pv_role(id, role_name, type_id, level, visible, description, creation_date, created_by) VALUES (12, 'IMPORTER', '6', '25', false, 'Importer', now(), '1');

/* global admin */
INSERT INTO pv_user_group_role VALUES(1, 3, 1, 5, now(), null, now(), 1, null, null);
/* migration (same rights as global admin) */
INSERT INTO pv_user_group_role VALUES(2, 2, 1, 5, now(), null, now(), 1, null, null);

INSERT INTO pv_feature (id, feature_name, description, start_date, creation_date, created_by) VALUES (1, 'MESSAGING', 'Messaging', now(), now(), 1);
INSERT INTO pv_feature (id, feature_name, description, start_date, creation_date, created_by) VALUES (3, 'FEEDBACK', 'Feedback', now(), now(), 1);
INSERT INTO pv_feature (id, feature_name, description, start_date, creation_date, created_by) VALUES (4, 'GP_MEDICATION', 'GP Medication', now(), now(), 1);
INSERT INTO pv_feature (id, feature_name, description, start_date, creation_date, created_by) VALUES (5, 'UNIT_TECHNICAL_CONTACT', 'Unit Technical Contact', now(), now(), 1);
INSERT INTO pv_feature (id, feature_name, description, start_date, creation_date, created_by) VALUES (6, 'PATIENT_SUPPORT_CONTACT', 'Patient Support Contact', now(), now(), 1);
INSERT INTO pv_feature (id, feature_name, description, start_date, creation_date, created_by) VALUES (7, 'DEFAULT_MESSAGING_CONTACT', 'Default Messaging Contact', now(), now(), 1);
INSERT INTO pv_feature (id, feature_name, description, start_date, creation_date, created_by) VALUES (8, 'KEEP_ALL_DATA', 'Keep All Data', now(), now(), 1);
INSERT INTO pv_feature (id, feature_name, description, start_date, creation_date, created_by) VALUES (9, 'CENTRAL_SUPPORT_CONTACT', 'Central Support Contact', now(), now(), 1);
INSERT INTO pv_feature (id, feature_name, description, start_date, creation_date, created_by) VALUES (10, 'IBD_SCORING_ALERTS', 'IBD Scoring Alerts (needs Messaging)', now(), now(), 1);
INSERT INTO pv_feature (id, feature_name, description, start_date, creation_date, created_by) VALUES (11, 'IBD_PATIENT_MANAGEMENT', 'IBD Patient Management', now(), now(), 1);
INSERT INTO pv_feature (id, feature_name, description, start_date, creation_date, created_by) VALUES (12, 'RENAL_SURVEY_FEEDBACK_RECIPIENT', 'Renal Survey Feedback Recipient', now(), now(), 1);
INSERT INTO pv_feature (id, feature_name, description, start_date, creation_date, created_by) VALUES (13, 'RENAL_HEALTH_SURVEYS', 'Renal Health Surveys', now(), now(), 1);
INSERT INTO pv_feature (id, feature_name, description, start_date, creation_date, created_by) VALUES (14, 'ENTER_OWN_DIAGNOSES', 'Enter Own Diagnoses', now(), now(), 1);

INSERT INTO pv_feature_feature_type (id, feature_id, type_id) VALUES (1, 1, 14);
INSERT INTO pv_feature_feature_type (id, feature_id, type_id) VALUES (2, 1, 15);
INSERT INTO pv_feature_feature_type (id, feature_id, type_id) VALUES (7, 3, 14);
INSERT INTO pv_feature_feature_type (id, feature_id, type_id) VALUES (9, 4, 14);
INSERT INTO pv_feature_feature_type (id, feature_id, type_id) VALUES (10, 5, 15);
INSERT INTO pv_feature_feature_type (id, feature_id, type_id) VALUES (11, 6, 15);
INSERT INTO pv_feature_feature_type (id, feature_id, type_id) VALUES (12, 7, 15);
INSERT INTO pv_feature_feature_type (id, feature_id, type_id) VALUES (13, 8, 14);
INSERT INTO pv_feature_feature_type (id, feature_id, type_id) VALUES (14, 4, 16);
INSERT INTO pv_feature_feature_type (id, feature_id, type_id) VALUES (15, 9, 15);
INSERT INTO pv_feature_feature_type (id, feature_id, type_id) VALUES (16, 10, 15);
INSERT INTO pv_feature_feature_type (id, feature_id, type_id) VALUES (17, 11, 14);
INSERT INTO pv_feature_feature_type (id, feature_id, type_id) VALUES (18, 12, 15);
INSERT INTO pv_feature_feature_type (id, feature_id, type_id) VALUES (19, 13, 14);
INSERT INTO pv_feature_feature_type (id, feature_id, type_id) VALUES (20, 14, 14);

INSERT INTO PV_Route (Id, Type_Id, Display_Order, Url, Template_Url, Controller, Title, Creation_Date, Created_By) VALUES
  (1, 4, 10,  '/dashboard', 'views/dashboard.html','DashboardCtrl', 'Home', now(), 1 );
INSERT INTO PV_Route (Id, Type_Id, Display_Order, Url, Template_Url, Controller, Title, Creation_Date, Created_By) VALUES
  (2, 22, 19,  '/conversations', 'views/conversations.html','ConversationsCtrl', 'Messages', now(), 1 );
INSERT INTO PV_Route (Id, Type_Id, Display_Order, Url, Template_Url, Controller, Title, Creation_Date, Created_By) VALUES
  (3, 3, 1,  '/settings', 'views/settings.html','SettingsCtrl', 'Settings', now(), 1 );
INSERT INTO PV_Route (Id, Type_Id, Display_Order, Url, Template_Url, Controller, Title, Creation_Date, Created_By) VALUES
  (4, 5, 17,  '/feedback', 'views/feedback.html','FeedbackCtrl', 'Feedback', now(), 1 );
INSERT INTO PV_Route (Id, Type_Id, Display_Order, Url, Template_Url, Controller, Title, Creation_Date, Created_By) VALUES
  (5, 22, 18,  '/help', 'views/help.html','HelpCtrl', 'Help', now(), 1 );
INSERT INTO PV_Route (Id, Type_Id, Display_Order, Url, Template_Url, Controller, Title, Creation_Date, Created_By) VALUES
  (6, 4, 30,  '/mydetails', 'views/mydetails.html','MydetailsCtrl', 'My Details', now(), 1 );
INSERT INTO PV_Route (Id, Type_Id, Display_Order, Url, Template_Url, Controller, Title, Creation_Date, Created_By) VALUES
  (7, 4, 50,  '/results', 'views/results.html','ResultsCtrl', 'Results', now(), 1 );
INSERT INTO PV_Route (Id, Type_Id, Display_Order, Url, Template_Url, Controller, Title, Creation_Date, Created_By) VALUES
  (8, 5, 51,  '/resultsdetail', 'views/resultsdetail.html','ResultsDetailCtrl', 'Results', now(), 1 );
INSERT INTO PV_Route (Id, Type_Id, Display_Order, Url, Template_Url, Controller, Title, Creation_Date, Created_By) VALUES
  (9, 4, 60,  '/medicines', 'views/medicines.html','MedicinesCtrl', 'Medicines', now(), 1 );
INSERT INTO PV_Route (Id, Type_Id, Display_Order, Url, Template_Url, Controller, Title, Creation_Date, Created_By) VALUES
  (10, 4, 70,  '/letters', 'views/letters.html','LettersCtrl', 'Letters', now(), 1 );
INSERT INTO PV_Route (Id, Type_Id, Display_Order, Url, Template_Url, Controller, Title, Creation_Date, Created_By) VALUES
  (12, 4, 90,  '/contact', 'views/contact.html','ContactCtrl', 'Contact', now(), 1 );
INSERT INTO PV_Route (Id, Type_Id, Display_Order, Url, Template_Url, Controller, Title, Creation_Date, Created_By) VALUES
  (13, 4, 100,  '/codes', 'views/codes.html','CodesCtrl', 'Codes', now(), 1 );
INSERT INTO PV_Route (Id, Type_Id, Display_Order, Url, Template_Url, Controller, Title, Creation_Date, Created_By) VALUES
  (14, 4, 120,  '/staff', 'views/staff.html','StaffCtrl', 'Staff', now(), 1 );
INSERT INTO PV_Route (Id, Type_Id, Display_Order, Url, Template_Url, Controller, Title, Creation_Date, Created_By) VALUES
  (15, 4, 110,  '/groups', 'views/groups.html','GroupsCtrl', 'Groups', now(), 1 );
INSERT INTO PV_Route (Id, Type_Id, Display_Order, Url, Template_Url, Controller, Title, Creation_Date, Created_By) VALUES
  (16, 4, 130,  '/patients', 'views/patients.html','PatientsCtrl', 'Patients', now(), 1 );
INSERT INTO PV_Route (Id, Type_Id, Display_Order, Url, Template_Url, Controller, Title, Creation_Date, Created_By) VALUES
  (17, 4, 140,  '/requestadmin', 'views/requestadmin.html','RequestAdminCtrl', 'Requests', now(), 1 );
INSERT INTO PV_Route (Id, Type_Id, Display_Order, Url, Template_Url, Controller, Title, Creation_Date, Created_By) VALUES
  (18, 4, 15,  '/news', 'views/news.html','NewsCtrl', 'News', now(), 1 );
INSERT INTO PV_Route (Id, Type_Id, Display_Order, Url, Template_Url, Controller, Title, Creation_Date, Created_By) VALUES
  (19, 4, 40,  '/myconditions', 'views/myconditions.html','MyconditionsCtrl', 'My Conditions', now(), 1 );
INSERT INTO PV_Route (Id, Type_Id, Display_Order, Url, Template_Url, Controller, Title, Creation_Date, Created_By) VALUES
  (20, 4, 105,  '/resultheadings', 'views/observationheadings.html','ObservationHeadingsCtrl', 'Result Headings', now(), 1 );
INSERT INTO PV_Route (Id, Type_Id, Display_Order, Url, Template_Url, Controller, Title, Creation_Date, Created_By) VALUES
  (21, 5, 55,  '/resultsenter', 'views/resultsenter.html','ResultsEnterCtrl', 'Results', now(), 1 );
INSERT INTO PV_Route (Id, Type_Id, Display_Order, Url, Template_Url, Controller, Title, Creation_Date, Created_By) VALUES
  (22, 5, 56,  '/diagnostics', 'views/diagnostics.html','DiagnosticsCtrl', 'Results', now(), 1 );
INSERT INTO PV_Route (Id, Type_Id, Display_Order, Url, Template_Url, Controller, Title, Creation_Date, Created_By) VALUES
  (23, 4, 150,  '/log', 'views/log.html','LogCtrl', 'Log', now(), 1 );
INSERT INTO PV_Route (Id, Type_Id, Display_Order, Url, Template_Url, Controller, Title, Creation_Date, Created_By) VALUES
  (24, 5, 52,  '/resultstable', 'views/resultstable.html','ResultsTableCtrl', 'Results', now(), 1 );
INSERT INTO PV_Route (Id, Type_Id, Display_Order, Url, Template_Url, Controller, Title, Creation_Date, Created_By) VALUES
  (25, 5, 132,  '/newpatient', 'views/newpatient.html','NewUserCtrl', 'Patients', now(), 1 );
INSERT INTO PV_Route (Id, Type_Id, Display_Order, Url, Template_Url, Controller, Title, Creation_Date, Created_By) VALUES
  (26, 5, 122,  '/newstaff', 'views/newstaff.html','NewUserCtrl', 'Staff', now(), 1 );
INSERT INTO PV_Route (Id, Type_Id, Display_Order, Url, Template_Url, Controller, Title, Creation_Date, Created_By) VALUES
  (27, 4, 160,  '/admin', 'views/siteadmin.html','SiteAdminCtrl', 'Site Administration', now(), 1 );
INSERT INTO PV_Route (Id, Type_Id, Display_Order, Url, Template_Url, Controller, Title, Creation_Date, Created_By) VALUES
  (28, 5, 170,  '/surveys/managing', 'views/surveys/managing.html','SurveysManagingCtrl', 'Managing Your Health', now(), 1);
INSERT INTO PV_Route (Id, Type_Id, Display_Order, Url, Template_Url, Controller, Title, Creation_Date, Created_By) VALUES
  (29, 5, 180,  '/surveys/symptoms', 'views/surveys/symptoms.html','SurveysSymptomsCtrl', 'Your Symptoms', now(), 1);
INSERT INTO PV_Route (Id, Type_Id, Display_Order, Url, Template_Url, Controller, Title, Creation_Date, Created_By) VALUES
  (30, 5, 190,  '/surveys/overall', 'views/surveys/overall.html','SurveysOverallCtrl', 'Your Overall Health', now(), 1);
INSERT INTO PV_Route (Id, Type_Id, Display_Order, Url, Template_Url, Controller, Title, Creation_Date, Created_By) VALUES
  (31, 5, 200,  '/categories', 'views/categories.html','CategoriesCtrl', 'Categories', now(), 1);
INSERT INTO PV_Route (Id, Type_Id, Display_Order, Url, Template_Url, Controller, Title, Creation_Date, Created_By) VALUES
  (32, 5, 210,  '/nhsindicators', 'views/nhsindicators.html','NhsIndicatorsCtrl', 'NHS Indicators', now(), 1);
INSERT INTO PV_Route (Id, Type_Id, Display_Order, Url, Template_Url, Controller, Title, Creation_Date, Created_By) VALUES
  (33, 5, 220,  '/resultsedit', 'views/resultsedit.html','ResultsEditCtrl', 'Edit Own Results', now(), 1);
INSERT INTO PV_Route (Id, Type_Id, Display_Order, Url, Template_Url, Controller, Title, Creation_Date, Created_By) VALUES
  (34, 4, 80,  '/research', 'views/research.html','ResearchCtrl', 'Research', now(), 1);
INSERT INTO PV_Route (Id, Type_Id, Display_Order, Url, Template_Url, Controller, Title, Creation_Date, Created_By) VALUES
  (35, 5, 160,  '/siteadmin', 'views/admin.html','AdminCtrl', 'Admin', now(), 1 );
INSERT INTO PV_Route (Id, Type_Id, Display_Order, Url, Template_Url, Controller, Title, Creation_Date, Created_By) VALUES
  (36, 4, 80,  '/mymedia', 'views/mymedia.html','MyMediaCtrl', 'My Media', now(), 1 );



INSERT INTO PV_Route_Link (Id, Route_Id, Group_Id, Role_Id, Feature_Id, Creation_Date, Created_By) VALUES
  (1, 1, 1, null, null, now(), 1 );
INSERT INTO PV_Route_Link (Id, Route_Id, Group_Id, Role_Id, Feature_Id, Creation_Date, Created_By) VALUES
  (2, 2, 1, null, null, now(), 1 );
INSERT INTO PV_Route_Link (Id, Route_Id, Group_Id, Role_Id, Feature_Id, Creation_Date, Created_By) VALUES
  (4, 13, null, 5, null, now(), 1 );
INSERT INTO PV_Route_Link (Id, Route_Id, Group_Id, Role_Id, Feature_Id, Creation_Date, Created_By) VALUES
  (5, 13, null, 4, null, now(), 1 );
INSERT INTO PV_Route_Link (Id, Route_Id, Group_Id, Role_Id, Feature_Id, Creation_Date, Created_By) VALUES
  (6, 14, null, 4, null, now(), 1 );
INSERT INTO PV_Route_Link (Id, Route_Id, Group_Id, Role_Id, Feature_Id, Creation_Date, Created_By) VALUES
  (8, 14, null, 5, null, now(), 1 );
INSERT INTO PV_Route_Link (Id, Route_Id, Group_Id, Role_Id, Feature_Id, Creation_Date, Created_By) VALUES
  (9, 14, null, 2, null, now(), 1 );
INSERT INTO PV_Route_Link (Id, Route_Id, Group_Id, Role_Id, Feature_Id, Creation_Date, Created_By) VALUES
  (10, 15, null, 5, null, now(), 1 );
INSERT INTO PV_Route_Link (Id, Route_Id, Group_Id, Role_Id, Feature_Id, Creation_Date, Created_By) VALUES
  (11, 15, null, 4, null, now(), 1 );
INSERT INTO PV_Route_Link (Id, Route_Id, Group_Id, Role_Id, Feature_Id, Creation_Date, Created_By) VALUES
  (13, 15, null, 2, null, now(), 1 );
INSERT INTO PV_Route_Link (Id, Route_Id, Group_Id, Role_Id, Feature_Id, Creation_Date, Created_By) VALUES
  (14, 16, null, 2, null, now(), 1 );
INSERT INTO PV_Route_Link (Id, Route_Id, Group_Id, Role_Id, Feature_Id, Creation_Date, Created_By) VALUES
  (15, 16, null, 5, null, now(), 1 );
INSERT INTO PV_Route_Link (Id, Route_Id, Group_Id, Role_Id, Feature_Id, Creation_Date, Created_By) VALUES
  (16, 16, null, 4, null, now(), 1 );
INSERT INTO PV_Route_Link (Id, Route_Id, Group_Id, Role_Id, Feature_Id, Creation_Date, Created_By) VALUES
  (18, 16, null, 3, null, now(), 1 );
INSERT INTO PV_Route_Link (Id, Route_Id, Group_Id, Role_Id, Feature_Id, Creation_Date, Created_By) VALUES
  (19, 17, null, 2, null, now(), 1 );
INSERT INTO PV_Route_Link (Id, Route_Id, Group_Id, Role_Id, Feature_Id, Creation_Date, Created_By) VALUES
  (20, 18, 1, null, null, now(), 1 );
INSERT INTO PV_Route_Link (Id, Route_Id, Group_Id, Role_Id, Feature_Id, Creation_Date, Created_By) VALUES
  (21, 17, null, 4, null, now(), 1 );
INSERT INTO PV_Route_Link (Id, Route_Id, Group_Id, Role_Id, Feature_Id, Creation_Date, Created_By) VALUES
  (22, 17, null, 5, null, now(), 1 );
INSERT INTO PV_Route_Link (Id, Route_Id, Group_Id, Role_Id, Feature_Id, Creation_Date, Created_By) VALUES
  (23, 5, 1, null, null, now(), 1 );
INSERT INTO PV_Route_Link (Id, Route_Id, Group_Id, Role_Id, Feature_Id, Creation_Date, Created_By) VALUES
  (24, 4, 1, null, null, now(), 1 );
INSERT INTO PV_Route_Link (Id, Route_Id, Group_Id, Role_Id, Feature_Id, Creation_Date, Created_By) VALUES
  (25, 12, null, 1, null, now(), 1 );
INSERT INTO PV_Route_Link (Id, Route_Id, Group_Id, Role_Id, Feature_Id, Creation_Date, Created_By) VALUES
  (26, 6, null, 1, null, now(), 1 );
INSERT INTO PV_Route_Link (Id, Route_Id, Group_Id, Role_Id, Feature_Id, Creation_Date, Created_By) VALUES
  (27, 19, null, 1, null, now(), 1 );
INSERT INTO PV_Route_Link (Id, Route_Id, Group_Id, Role_Id, Feature_Id, Creation_Date, Created_By) VALUES
  (28, 7, null, 1, null, now(), 1 );
INSERT INTO PV_Route_Link (Id, Route_Id, Group_Id, Role_Id, Feature_Id, Creation_Date, Created_By) VALUES
  (29, 8, null, 1, null, now(), 1 );
INSERT INTO PV_Route_Link (Id, Route_Id, Group_Id, Role_Id, Feature_Id, Creation_Date, Created_By) VALUES
  (30, 20, null, 4, null, now(), 1 );
INSERT INTO PV_Route_Link (Id, Route_Id, Group_Id, Role_Id, Feature_Id, Creation_Date, Created_By) VALUES
  (31, 20, null, 5, null, now(), 1 );
INSERT INTO PV_Route_Link (Id, Route_Id, Group_Id, Role_Id, Feature_Id, Creation_Date, Created_By) VALUES
  (32, 9, null, 1, null, now(), 1 );
INSERT INTO PV_Route_Link (Id, Route_Id, Group_Id, Role_Id, Feature_Id, Creation_Date, Created_By) VALUES
  (33, 21, null, 1, null, now(), 1 );
INSERT INTO PV_Route_Link (Id, Route_Id, Group_Id, Role_Id, Feature_Id, Creation_Date, Created_By) VALUES
  (34, 22, null, 1, null, now(), 1 );
INSERT INTO PV_Route_Link (Id, Route_Id, Group_Id, Role_Id, Feature_Id, Creation_Date, Created_By) VALUES
  (35, 10, null, 1, null, now(), 1 );
/* log */
INSERT INTO PV_Route_Link (Id, Route_Id, Group_Id, Role_Id, Feature_Id, Creation_Date, Created_By) VALUES
  (36, 23, null, 2, null, now(), 1 );
INSERT INTO PV_Route_Link (Id, Route_Id, Group_Id, Role_Id, Feature_Id, Creation_Date, Created_By) VALUES
  (37, 23, null, 4, null, now(), 1 );
INSERT INTO PV_Route_Link (Id, Route_Id, Group_Id, Role_Id, Feature_Id, Creation_Date, Created_By) VALUES
  (38, 23, null, 5, null, now(), 1 );
/* disease group admin */
INSERT INTO PV_Route_Link (Id, Route_Id, Group_Id, Role_Id, Feature_Id, Creation_Date, Created_By) VALUES
  (39, 16, null, 9, null, now(), 1 );
INSERT INTO PV_Route_Link (Id, Route_Id, Group_Id, Role_Id, Feature_Id, Creation_Date, Created_By) VALUES
  (40, 24, null, 1, null, now(), 1 );
/* new patient */
INSERT INTO PV_Route_Link (Id, Route_Id, Group_Id, Role_Id, Feature_Id, Creation_Date, Created_By) VALUES
  (41, 25, null, 2, null, now(), 1 );
INSERT INTO PV_Route_Link (Id, Route_Id, Group_Id, Role_Id, Feature_Id, Creation_Date, Created_By) VALUES
  (42, 25, null, 4, null, now(), 1 );
INSERT INTO PV_Route_Link (Id, Route_Id, Group_Id, Role_Id, Feature_Id, Creation_Date, Created_By) VALUES
  (43, 25, null, 5, null, now(), 1 );
/* new staff */
INSERT INTO PV_Route_Link (Id, Route_Id, Group_Id, Role_Id, Feature_Id, Creation_Date, Created_By) VALUES
  (44, 26, null, 2, null, now(), 1 );
INSERT INTO PV_Route_Link (Id, Route_Id, Group_Id, Role_Id, Feature_Id, Creation_Date, Created_By) VALUES
  (45, 26, null, 4, null, now(), 1 );
INSERT INTO PV_Route_Link (Id, Route_Id, Group_Id, Role_Id, Feature_Id, Creation_Date, Created_By) VALUES
  (46, 26, null, 5, null, now(), 1 );
/* site administration */
INSERT INTO PV_Route_Link (Id, Route_Id, Group_Id, Role_Id, Feature_Id, Creation_Date, Created_By) VALUES
  (47, 27, null, 5, null, now(), 1 );

/* GP admins, similar to unit admins */
INSERT INTO PV_Route_Link (Id, Route_Id, Group_Id, Role_Id, Feature_Id, Creation_Date, Created_By) VALUES
  (48, 1, null, 11, null, now(), 1);
INSERT INTO PV_Route_Link (Id, Route_Id, Group_Id, Role_Id, Feature_Id, Creation_Date, Created_By) VALUES
  (49, 2, null, 11, null, now(), 1);
INSERT INTO PV_Route_Link (Id, Route_Id, Group_Id, Role_Id, Feature_Id, Creation_Date, Created_By) VALUES
  (50, 5, null, 11, null, now(), 1);
INSERT INTO PV_Route_Link (Id, Route_Id, Group_Id, Role_Id, Feature_Id, Creation_Date, Created_By) VALUES
  (51, 14, null, 11, null, now(), 1);
INSERT INTO PV_Route_Link (Id, Route_Id, Group_Id, Role_Id, Feature_Id, Creation_Date, Created_By) VALUES
  (52, 16, null, 11, null, now(), 1);
INSERT INTO PV_Route_Link (Id, Route_Id, Group_Id, Role_Id, Feature_Id, Creation_Date, Created_By) VALUES
  (53, 18, null, 11, null, now(), 1);
INSERT INTO PV_Route_Link (Id, Route_Id, Group_Id, Role_Id, Feature_Id, Creation_Date, Created_By) VALUES
  (54, 23, null, 11, null, now(), 1);
INSERT INTO PV_Route_Link (Id, Route_Id, Group_Id, Role_Id, Feature_Id, Creation_Date, Created_By) VALUES
  (55, 25, null, 11, null, now(), 1);
INSERT INTO PV_Route_Link (Id, Route_Id, Group_Id, Role_Id, Feature_Id, Creation_Date, Created_By) VALUES
  (56, 26, null, 11, null, now(), 1);
INSERT INTO PV_Route_Link (Id, Route_Id, Group_Id, Role_Id, Feature_Id, Creation_Date, Created_By) VALUES
  (57, 15, null, 11, null, now(), 1);

/* Surveys */
INSERT INTO PV_Route_Link (Id, Route_Id, Group_Id, Role_Id, Feature_Id, Creation_Date, Created_By) VALUES
  (58, 28, null, 1, null, now(), 1 );
INSERT INTO PV_Route_Link (Id, Route_Id, Group_Id, Role_Id, Feature_Id, Creation_Date, Created_By) VALUES
  (59, 29, null, 1, null, now(), 1 );
INSERT INTO PV_Route_Link (Id, Route_Id, Group_Id, Role_Id, Feature_Id, Creation_Date, Created_By) VALUES
  (60, 30, null, 1, null, now(), 1 );

/* edit Code Categorys */
INSERT INTO PV_Route_Link (Id, Route_Id, Group_Id, Role_Id, Feature_Id, Creation_Date, Created_By) VALUES
  (61, 31, null, 5, null, now(), 1 );
INSERT INTO PV_Route_Link (Id, Route_Id, Group_Id, Role_Id, Feature_Id, Creation_Date, Created_By) VALUES
  (62, 31, null, 4, null, now(), 1 );

/* NHS Indicators (global admin, specialty admin, unit admin, unit staff */
INSERT INTO PV_Route_Link (Id, Route_Id, Group_Id, Role_Id, Feature_Id, Creation_Date, Created_By) VALUES
  (63, 32, null, 2, null, now(), 1 );
INSERT INTO PV_Route_Link (Id, Route_Id, Group_Id, Role_Id, Feature_Id, Creation_Date, Created_By) VALUES
  (64, 32, null, 3, null, now(), 1 );
INSERT INTO PV_Route_Link (Id, Route_Id, Group_Id, Role_Id, Feature_Id, Creation_Date, Created_By) VALUES
  (65, 32, null, 4, null, now(), 1 );
INSERT INTO PV_Route_Link (Id, Route_Id, Group_Id, Role_Id, Feature_Id, Creation_Date, Created_By) VALUES
  (66, 32, null, 5, null, now(), 1 );

/* Edit patient entered results */
INSERT INTO PV_Route_Link (Id, Route_Id, Group_Id, Role_Id, Feature_Id, Creation_Date, Created_By) VALUES
  (67, 33, null, 1, null, now(), 1 );

/* Research studies */
INSERT INTO PV_Route_Link (Id, Route_Id, Group_Id, Role_Id, Feature_Id, Creation_Date, Created_By) VALUES
  (69, 34, null, 1, null, now(), 1 );

/* site administration */
INSERT INTO PV_Route_Link (Id, Route_Id, Group_Id, Role_Id, Feature_Id, Creation_Date, Created_By) VALUES
  (70, 35, null, 5, null, now(), 1 );

/** MyMedia **/
INSERT INTO PV_Route_Link (Id, Route_Id, Group_Id, Role_Id, Feature_Id, Creation_Date, Created_By) VALUES
  (71, 36, null, 1, null, now(), 1 );

/* External Standards, used by Codes */
INSERT INTO PV_External_Standard (Id, Name, Description) VALUES
  (1, 'ICD-10', 'ICD-10');
INSERT INTO PV_External_Standard (Id, Name, Description) VALUES
  (2, 'SNOMED-CT', 'SNOMED-CT');