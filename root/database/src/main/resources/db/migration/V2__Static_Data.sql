INSERT INTO pv_user (id, username, password, change_password, locked, email, forename, surname, start_date, creation_date, created_by, dummy) VALUES
  (1, 'system','pppppp', false, false, 'system@patientview.org', 'system', 'system', now(), now(), 1, false);

INSERT INTO pv_user (id, username, password, change_password, locked, email, forename, surname, start_date, creation_date, created_by, dummy) VALUES
  (2, 'migration','pppppp', false, false, 'migration@patientview.org', 'migration', 'migration', now(), now(), 1, false);

INSERT INTO pv_user (id, username, password, change_password, locked, email, forename, surname, start_date, creation_date, created_by, dummy) VALUES
  (3, 'globaladmin','pppppp', false, false, 'migration@patientview.org', 'globaladmin', 'globaladmin', now(), now(), 1, false);

INSERT INTO pv_lookup_type(id, creation_date, description, lookup_type, created_by)  VALUES (1, now(), 'Type of group','GROUP', '1');
INSERT INTO pv_lookup_type(id, creation_date, description, lookup_type, created_by)  VALUES (2, now(), 'Type of menu','MENU', '1');
INSERT INTO pv_lookup_type(id, creation_date, description, lookup_type, created_by)  VALUES (3, now(), 'Type of role','ROLE', '1');
INSERT INTO pv_lookup_type(id, creation_date, description, lookup_type, created_by)  VALUES (4, now(), 'Type of external coding standard','CODE_STANDARD', '1');
INSERT INTO pv_lookup_type(id, creation_date, description, lookup_type, created_by)  VALUES (5, now(), 'Type of code','CODE_TYPE', '1');
INSERT INTO pv_lookup_type(id, creation_date, description, lookup_type, created_by)  VALUES (6, now(), 'Type of feature','FEATURE_TYPE', '1');
INSERT INTO pv_lookup_type(id, creation_date, description, lookup_type, created_by)  VALUES (8, now(), 'Identifier','IDENTIFIER', '1');
INSERT INTO pv_lookup_type(id, creation_date, description, lookup_type, created_by)  VALUES (9, now(), 'Contact point type','CONTACT_POINT_TYPE', '1');
INSERT INTO pv_lookup_type(id, creation_date, description, lookup_type, created_by)  VALUES (10, now(), 'Types of statistic','STATISTIC_TYPE', '1');

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

INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id) VALUES (34, now(), 'PATIENT_COUNT','SELECT  COUNT(1) FROM   pv_user_group_role WHERE   role_id = 1 AND     group_id = :groupId AND creation_date BETWEEN :startDate AND :endDate','1','10');
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id) VALUES (35, now(), 'LOGON_COUNT', 'SELECT  COUNT(1) FROM    pv_audit adt,       pv_user_group_role upr WHERE   adt.source_object_id = upr.user_id AND     upr.group_id = :groupId AND     adt.action = ''LOGON_SUCCESS'' AND adt.creation_date BETWEEN :startDate AND :endDate','1','10');
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id) VALUES (36, now(), 'UNIQUE_LOGON_COUNT', 'SELECT  COUNT(DISTINCT source_object_id) FROM    pv_audit adt,       pv_user_group_role upr WHERE   adt.source_object_id = upr.user_id AND     upr.group_id = :groupId AND     adt.action = ''LOGON_SUCCESS'' AND adt.creation_date BETWEEN :startDate AND :endDate','1','10');

INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id) VALUES (37, now(), 'VIEW_PATIENT_COUNT','SELECT  COUNT(1) FROM    pv_user_group_role WHERE   role_id = 1 AND     group_id = :groupId AND creation_date BETWEEN :startDate AND :endDate','1','10');
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id) VALUES (38, now(), 'ADD_PATIENT_COUNT', 'SELECT  COUNT(1) FROM    pv_audit adt,       pv_user_group_role upr WHERE   adt.source_object_id = upr.user_id AND     upr.group_id = :groupId AND     adt.action = ''LOGON_SUCCESS'' AND adt.creation_date BETWEEN :startDate AND :endDate','1','10');
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id) VALUES (39, now(), 'IMPORT_FAIL_COUNT', 'SELECT  COUNT(DISTINCT source_object_id) FROM    pv_audit adt,       pv_user_group_role upr WHERE   adt.source_object_id = upr.user_id AND     upr.group_id = :groupId AND     adt.action = ''LOGON_SUCCESS'' AND adt.creation_date BETWEEN :startDate AND :endDate','1','10');

INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id) VALUES (40, now(), 'IMPORT_COUNT','SELECT  COUNT(1) FROM    pv_user_group_role WHERE   role_id = 1 AND     group_id = :groupId AND creation_date BETWEEN :startDate AND :endDate','1','10');
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id) VALUES (41, now(), 'PASSWORD_CHANGE_COUNT', 'SELECT  COUNT(1) FROM    pv_audit adt,       pv_user_group_role upr WHERE   adt.source_object_id = upr.user_id AND     upr.group_id = :groupId AND     adt.action = ''RESET_PASSWORD'' AND adt.creation_date BETWEEN :startDate AND :endDate','1','10');
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id) VALUES (42, now(), 'ACCOUNT_LOCKED_COUNT', 'SELECT  COUNT(DISTINCT source_object_id) FROM    pv_audit adt,       pv_user_group_role upr WHERE   adt.source_object_id = upr.user_id AND     upr.group_id = :groupId AND     adt.action = ''LOGON_SUCCESS'' AND adt.creation_date BETWEEN :startDate AND :endDate','1','10');

INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id) VALUES (43, now(), 'REMOVE_PATIENT_COUNT', 'SELECT  COUNT(DISTINCT source_object_id) FROM    pv_audit adt,       pv_user_group_role upr WHERE   adt.source_object_id = upr.user_id AND     upr.group_id = :groupId AND     adt.action = ''LOGON_SUCCESS'' AND adt.creation_date BETWEEN :startDate AND :endDate','1','10');
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id) VALUES (44, now(), 'DELETE_PATIENT_COUNT', 'SELECT  COUNT(DISTINCT source_object_id) FROM    pv_audit adt,       pv_user_group_role upr WHERE   adt.source_object_id = upr.user_id AND     upr.group_id = :groupId AND     adt.action = ''LOGON_SUCCESS'' AND adt.creation_date BETWEEN :startDate AND :endDate','1','10');

INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id) VALUES (45, now(), 'INACTIVE_USER_COUNT', 'SELECT COUNT(u) FROM pv_user u, pv_user_group_role gr WHERE gr.group_id = :groupId AND gr.user_id = u.id AND (NOT (u.last_login BETWEEN :startDate AND :endDate) OR u.last_login IS NULL)','1','10');
INSERT INTO pv_lookup_value(id, creation_date, value, description, created_by, lookup_type_id) VALUES (46, now(), 'LOCKED_USER_COUNT',   'SELECT COUNT(u) FROM pv_user u, pv_user_group_role gr WHERE gr.group_id = :groupId AND gr.user_id = u.id AND u.locked = true AND :startDate = :startDate AND :endDate = :endDate','1','10');


INSERT INTO pv_group(id, Group_Name, Code, Sftp_User, Type_Id, Visible, Creation_Date,Created_By, Visible_To_Join) VALUES (1, 'Generic', 'Generic', null, 2, false, now(),1, false );
INSERT INTO pv_group(id, Group_Name, Code, Sftp_User, Type_Id, Visible, Creation_Date,Created_By, Visible_To_Join) VALUES (2, 'Renal', 'Renal', null, 2, true, now(),1 , true);
INSERT INTO pv_group(id, Group_Name, Code, Sftp_User, Type_Id, Visible, Creation_Date,Created_By, Visible_To_Join) VALUES (3, 'Diabetes', 'Diabetes', null, 2, true, now(), 1, true);
INSERT INTO pv_group(id, Group_Name, Code, Sftp_User, Type_Id, Visible, Creation_Date,Created_By, Visible_To_Join) VALUES (4, 'IBD', 'IBD', null, 2, true, now(), 1, true);

INSERT INTO pv_role(id, role_name, type_id, level, visible, description, creation_date, created_by) VALUES (1, 'PATIENT', '7', '2', true, 'Patient', now(), '1');
INSERT INTO pv_role(id, role_name, type_id, level, visible, description, creation_date, created_by) VALUES (2, 'UNIT_ADMIN', '6', '4', true, 'Unit Admin', now(), '1');
INSERT INTO pv_role(id, role_name, type_id, level, visible, description, creation_date, created_by) VALUES (3, 'STAFF_ADMIN', '6', '3', true, 'Unit Staff', now(), '1');
INSERT INTO pv_role(id, role_name, type_id, level, visible, description, creation_date, created_by) VALUES (4, 'SPECIALTY_ADMIN', '6', '5', true, 'Specialty Admin', now(), '1');
INSERT INTO pv_role(id, role_name, type_id, level, visible, description, creation_date, created_by) VALUES (5, 'GLOBAL_ADMIN', '6', '6', true, 'Global Admin', now(), '1');
INSERT INTO pv_role(id, role_name, type_id, level, visible, description, creation_date, created_by) VALUES (6, 'GP_ADMIN ', '6', '2', true, 'Secondary Login', now(), '1');
INSERT INTO pv_role(id, role_name, type_id, level, visible, description, creation_date, created_by) VALUES (7, 'MEMBER', '7', '1', false, 'Logged-in Users', now(), '1');
INSERT INTO pv_role(id, role_name, type_id, level, visible, description, creation_date, created_by) VALUES (8, 'PUBLIC', '7', '1', false, 'General Public', now(), '1');

INSERT INTO pv_user_group_role VALUES(1, 3, 1, 5, now(), null, now(), 1, null, null);

INSERT INTO pv_feature (id, feature_name, description, start_date, creation_date, created_by) VALUES (1, 'MESSAGING', 'Messaging', now(), now(), 1);
INSERT INTO pv_feature (id, feature_name, description, start_date, creation_date, created_by) VALUES (2, 'SHARING_THOUGHTS', 'Sharing Thoughts', now(), now(), 1);
INSERT INTO pv_feature (id, feature_name, description, start_date, creation_date, created_by) VALUES (3, 'FEEDBACK', 'Feedback', now(), now(), 1);
INSERT INTO pv_feature (id, feature_name, description, start_date, creation_date, created_by) VALUES (4, 'ECS', 'Emergency Care Summary', now(), now(), 1);
INSERT INTO pv_feature (id, feature_name, description, start_date, creation_date, created_by) VALUES (5, 'UNIT_TECHNICAL_CONTACT', 'Unit Technical Contact', now(), now(), 1);
INSERT INTO pv_feature (id, feature_name, description, start_date, creation_date, created_by) VALUES (6, 'PATIENT_SUPPORT_CONTACT', 'Patient Support Contact', now(), now(), 1);
INSERT INTO pv_feature (id, feature_name, description, start_date, creation_date, created_by) VALUES (7, 'DEFAULT_MESSAGING_CONTACT', 'Default Messaging Contact', now(), now(), 1);

INSERT INTO pv_feature_feature_type (id, feature_id, type_id) VALUES (1, 1, 14);
INSERT INTO pv_feature_feature_type (id, feature_id, type_id) VALUES (2, 1, 15);
INSERT INTO pv_feature_feature_type (id, feature_id, type_id) VALUES (4, 2, 14);
INSERT INTO pv_feature_feature_type (id, feature_id, type_id) VALUES (5, 2, 15);
INSERT INTO pv_feature_feature_type (id, feature_id, type_id) VALUES (7, 3, 14);
INSERT INTO pv_feature_feature_type (id, feature_id, type_id) VALUES (9, 4, 14);
INSERT INTO pv_feature_feature_type (id, feature_id, type_id) VALUES (10, 5, 15);
INSERT INTO pv_feature_feature_type (id, feature_id, type_id) VALUES (11, 6, 15);
INSERT INTO pv_feature_feature_type (id, feature_id, type_id) VALUES (12, 7, 15);

INSERT INTO PV_Route (Id, Type_Id, Display_Order, Url, Template_Url, Controller, Title, Creation_Date, Created_By) VALUES
  (1, 4, 1,  '/dashboard', 'views/dashboard.html','DashboardCtrl', 'Home', now(), 1 );
INSERT INTO PV_Route (Id, Type_Id, Display_Order, Url, Template_Url, Controller, Title, Creation_Date, Created_By) VALUES
  (2, 22, 19,  '/messages', 'views/messages.html','MessagesCtrl', 'Messages', now(), 1 );
INSERT INTO PV_Route (Id, Type_Id, Display_Order, Url, Template_Url, Controller, Title, Creation_Date, Created_By) VALUES
  (3, 3, 1,  '/settings', 'views/settings.html','SettingsCtrl', 'Settings', now(), 1 );
INSERT INTO PV_Route (Id, Type_Id, Display_Order, Url, Template_Url, Controller, Title, Creation_Date, Created_By) VALUES
  (4, 3, 17,  '/feedback', 'views/feedback.html','FeedbackCtrl', 'Feedback', now(), 1 );
INSERT INTO PV_Route (Id, Type_Id, Display_Order, Url, Template_Url, Controller, Title, Creation_Date, Created_By) VALUES
  (5, 22, 18,  '/help', 'views/help.html','HelpCtrl', 'Help', now(), 1 );
INSERT INTO PV_Route (Id, Type_Id, Display_Order, Url, Template_Url, Controller, Title, Creation_Date, Created_By) VALUES
  (6, 4, 2,  '/mydetails', 'views/mydetails.html','MydetailsCtrl', 'My Details', now(), 1 );
INSERT INTO PV_Route (Id, Type_Id, Display_Order, Url, Template_Url, Controller, Title, Creation_Date, Created_By) VALUES
  (7, 4, 3,  '/results', 'views/results.html','ResultsCtrl', 'My Results', now(), 1 );
INSERT INTO PV_Route (Id, Type_Id, Display_Order, Url, Template_Url, Controller, Title, Creation_Date, Created_By) VALUES
  (8, 5, 4,  '/resultsdetail', 'views/resultsdetail.html','ResultsDetailCtrl', 'Results Detail', now(), 1 );
INSERT INTO PV_Route (Id, Type_Id, Display_Order, Url, Template_Url, Controller, Title, Creation_Date, Created_By) VALUES
  (9, 4, 5,  '/medicines', 'views/medicines.html','MedicinesCtrl', 'Medicines', now(), 1 );
INSERT INTO PV_Route (Id, Type_Id, Display_Order, Url, Template_Url, Controller, Title, Creation_Date, Created_By) VALUES
  (10, 4, 6,  '/letters', 'views/letters.html','LettersCtrl', 'Letters', now(), 1 );
INSERT INTO PV_Route (Id, Type_Id, Display_Order, Url, Template_Url, Controller, Title, Creation_Date, Created_By) VALUES
  (11, 4, 7,  '/sharingthoughts', 'views/sharingthoughts.html','SharingthoughtsCtrl', 'Sharing Thoughts', now(), 1 );
INSERT INTO PV_Route (Id, Type_Id, Display_Order, Url, Template_Url, Controller, Title, Creation_Date, Created_By) VALUES
  (12, 4, 8,  '/contact', 'views/contact.html','ContactCtrl', 'Contact', now(), 1 );
INSERT INTO PV_Route (Id, Type_Id, Display_Order, Url, Template_Url, Controller, Title, Creation_Date, Created_By) VALUES
  (13, 4, 9,  '/codes', 'views/codes.html','CodesCtrl', 'Codes', now(), 1 );
INSERT INTO PV_Route (Id, Type_Id, Display_Order, Url, Template_Url, Controller, Title, Creation_Date, Created_By) VALUES
  (14, 4, 11,  '/staff', 'views/staff.html','StaffCtrl', 'Staff', now(), 1 );
INSERT INTO PV_Route (Id, Type_Id, Display_Order, Url, Template_Url, Controller, Title, Creation_Date, Created_By) VALUES
  (15, 4, 10,  '/groups', 'views/groups.html','GroupsCtrl', 'Groups', now(), 1 );
INSERT INTO PV_Route (Id, Type_Id, Display_Order, Url, Template_Url, Controller, Title, Creation_Date, Created_By) VALUES
  (16, 4, 12,  '/patients', 'views/patients.html','PatientsCtrl', 'Patients', now(), 1 );
INSERT INTO PV_Route (Id, Type_Id, Display_Order, Url, Template_Url, Controller, Title, Creation_Date, Created_By) VALUES
  (17, 4, 13,  '/joinrequestadmin', 'views/joinrequestadmin.html','JoinRequestAdminCtrl', 'Join Requests', now(), 1 );
INSERT INTO PV_Route (Id, Type_Id, Display_Order, Url, Template_Url, Controller, Title, Creation_Date, Created_By) VALUES
  (18, 4, 15,  '/news', 'views/news.html','NewsCtrl', 'News', now(), 1 );


INSERT INTO PV_Route_Link (Id, Route_Id, Group_Id, Role_Id, Feature_Id, Creation_Date, Created_By) VALUES
  (1, 1, 1, null, null, now(), 1 );
 INSERT INTO PV_Route_Link (Id, Route_Id, Group_Id, Role_Id, Feature_Id, Creation_Date, Created_By) VALUES
  (2, 2, 1, null, 2, now(), 1 );
INSERT INTO PV_Route_Link (Id, Route_Id, Group_Id, Role_Id, Feature_Id, Creation_Date, Created_By) VALUES
  (3, 13, null, 6, null, now(), 1 );
INSERT INTO PV_Route_Link (Id, Route_Id, Group_Id, Role_Id, Feature_Id, Creation_Date, Created_By) VALUES
  (4, 13, null, 5, null, now(), 1 );
INSERT INTO PV_Route_Link (Id, Route_Id, Group_Id, Role_Id, Feature_Id, Creation_Date, Created_By) VALUES
  (5, 13, null, 4, null, now(), 1 );
INSERT INTO PV_Route_Link (Id, Route_Id, Group_Id, Role_Id, Feature_Id, Creation_Date, Created_By) VALUES
  (6, 14, null, 4, null, now(), 1 );
INSERT INTO PV_Route_Link (Id, Route_Id, Group_Id, Role_Id, Feature_Id, Creation_Date, Created_By) VALUES
  (7, 14, null, 6, null, now(), 1 );
INSERT INTO PV_Route_Link (Id, Route_Id, Group_Id, Role_Id, Feature_Id, Creation_Date, Created_By) VALUES
  (8, 14, null, 5, null, now(), 1 );
INSERT INTO PV_Route_Link (Id, Route_Id, Group_Id, Role_Id, Feature_Id, Creation_Date, Created_By) VALUES
  (9, 14, null, 2, null, now(), 1 );
INSERT INTO PV_Route_Link (Id, Route_Id, Group_Id, Role_Id, Feature_Id, Creation_Date, Created_By) VALUES
  (10, 15, null, 5, null, now(), 1 );
INSERT INTO PV_Route_Link (Id, Route_Id, Group_Id, Role_Id, Feature_Id, Creation_Date, Created_By) VALUES
  (11, 15, null, 4, null, now(), 1 );
INSERT INTO PV_Route_Link (Id, Route_Id, Group_Id, Role_Id, Feature_Id, Creation_Date, Created_By) VALUES
  (12, 15, null, 6, null, now(), 1 );
INSERT INTO PV_Route_Link (Id, Route_Id, Group_Id, Role_Id, Feature_Id, Creation_Date, Created_By) VALUES
  (13, 15, null, 2, null, now(), 1 );
INSERT INTO PV_Route_Link (Id, Route_Id, Group_Id, Role_Id, Feature_Id, Creation_Date, Created_By) VALUES
  (14, 16, null, 2, null, now(), 1 );
INSERT INTO PV_Route_Link (Id, Route_Id, Group_Id, Role_Id, Feature_Id, Creation_Date, Created_By) VALUES
  (15, 16, null, 5, null, now(), 1 );
INSERT INTO PV_Route_Link (Id, Route_Id, Group_Id, Role_Id, Feature_Id, Creation_Date, Created_By) VALUES
  (16, 16, null, 4, null, now(), 1 );
INSERT INTO PV_Route_Link (Id, Route_Id, Group_Id, Role_Id, Feature_Id, Creation_Date, Created_By) VALUES
  (17, 16, null, 6, null, now(), 1 );
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


INSERT INTO PV_User_Token(Id, User_Id, Token, Creation_Date) VALUES
  (1, 2, 'pppppp', now())




