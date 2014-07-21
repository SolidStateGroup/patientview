INSERT INTO pv_user (id, username, password, change_password, locked, email, fullname, start_date, creation_date, created_by) VALUES
  (1, 'system','pppppp', false, false, 'system@patientview.org', 'system', now(), now(), 1);

INSERT INTO pv_user (id, username, password, change_password, locked, email, fullname, start_date, creation_date, created_by) VALUES
  (2, 'migration','pppppp', false, false, 'migration@patientview.org', 'migration', now(), now(), 1);

INSERT INTO pv_user (id, username, password, change_password, locked, email, fullname, start_date, creation_date, created_by) VALUES
  (3, 'globaladmin','pppppp', false, false, 'migration@patientview.org', 'migration', now(), now(), 1);

INSERT INTO pv_lookup_type(id, creation_date, description, lookup_type, created_by)  VALUES (1, now(), 'Type of group','GROUP', '1');
INSERT INTO pv_lookup_type(id, creation_date, description, lookup_type, created_by)  VALUES (2, now(), 'Type of menu','MENU', '1');
INSERT INTO pv_lookup_type(id, creation_date, description, lookup_type, created_by)  VALUES (3, now(), 'Type of role','ROLE', '1');
INSERT INTO pv_lookup_type(id, creation_date, description, lookup_type, created_by)  VALUES (4, now(), 'Type of external coding standard','CODE_STANDARD', '1');
INSERT INTO pv_lookup_type(id, creation_date, description, lookup_type, created_by)  VALUES (5, now(), 'Type of code','CODE_TYPE', '1');
INSERT INTO pv_lookup_type(id, creation_date, description, lookup_type, created_by)  VALUES (6, now(), 'Type of feature','FEATURE_TYPE', '1');
INSERT INTO pv_lookup_type(id, creation_date, description, lookup_type, created_by)  VALUES (7, now(), 'Group relationship type','RELATIONSHIP_TYPE', '1');
INSERT INTO pv_lookup_type(id, creation_date, description, lookup_type, created_by)  VALUES (8, now(), 'Identifier','IDENTIFIER', '1');

INSERT INTO pv_lookup_value(id, creation_date, value, created_by, lookup_type_id) VALUES (1, now(), 'UNIT','1', '1');
INSERT INTO pv_lookup_value(id, creation_date, value, created_by, lookup_type_id) VALUES (2, now(), 'SPECIALTY','1','1');
INSERT INTO pv_lookup_value(id, creation_date, value, created_by, lookup_type_id) VALUES (3, now(), 'TOP_RIGHT','1','2');
INSERT INTO pv_lookup_value(id, creation_date, value, created_by, lookup_type_id) VALUES (4, now(), 'TOP','1','2');
INSERT INTO pv_lookup_value(id, creation_date, value, created_by, lookup_type_id) VALUES (5, now(), 'NOT_DISPLAYED','1','2');
INSERT INTO pv_lookup_value(id, creation_date, value, created_by, lookup_type_id) VALUES (6, now(), 'STAFF','1','3');
INSERT INTO pv_lookup_value(id, creation_date, value, created_by, lookup_type_id) VALUES (7, now(), 'PATIENT','1','3');
INSERT INTO pv_lookup_value(id, creation_date, value, created_by, lookup_type_id) VALUES (8, now(), 'EDTA','1','4');
INSERT INTO pv_lookup_value(id, creation_date, value, created_by, lookup_type_id) VALUES (9, now(), 'READ','1','4');
INSERT INTO pv_lookup_value(id, creation_date, value, created_by, lookup_type_id) VALUES (10, now(), 'ICD','1','4');
INSERT INTO pv_lookup_value(id, creation_date, value, created_by, lookup_type_id) VALUES (11, now(), 'SNOMED','1','4');
INSERT INTO pv_lookup_value(id, creation_date, value, created_by, lookup_type_id) VALUES (12, now(), 'DIAGNOSIS','1','5');
INSERT INTO pv_lookup_value(id, creation_date, value, created_by, lookup_type_id) VALUES (13, now(), 'TREATMENT','1','5');
INSERT INTO pv_lookup_value(id, creation_date, value, created_by, lookup_type_id) VALUES (14, now(), 'GROUP','1','6');
INSERT INTO pv_lookup_value(id, creation_date, value, created_by, lookup_type_id) VALUES (15, now(), 'STAFF','1','6');
INSERT INTO pv_lookup_value(id, creation_date, value, created_by, lookup_type_id) VALUES (16, now(), 'PATIENT','1','6');
INSERT INTO pv_lookup_value(id, creation_date, value, created_by, lookup_type_id) VALUES (17, now(), 'PARENT','1','7');
INSERT INTO pv_lookup_value(id, creation_date, value, created_by, lookup_type_id) VALUES (18, now(), 'CHILD','1','7');
INSERT INTO pv_lookup_value(id, creation_date, value, created_by, lookup_type_id) VALUES (19, now(), 'DISEASE_GROUP','1','1');
INSERT INTO pv_lookup_value(id, creation_date, value, created_by, lookup_type_id) VALUES (20, now(), 'NHS_NUMBER','1','8');
INSERT INTO pv_lookup_value(id, creation_date, value, created_by, lookup_type_id) VALUES (21, now(), 'CHI_NUMBER','1','8');

INSERT INTO pv_group(id, Group_Name, Code, Sftp_User, Type_Id, Visible, Creation_Date,Created_By) VALUES (1, 'Generic', 'Generic', null, 2, false, now(),1 );
INSERT INTO pv_group(id, Group_Name, Code, Sftp_User, Type_Id, Visible, Creation_Date,Created_By) VALUES (2, 'Renal', 'Renal', null, 2, true, now(),1 );
INSERT INTO pv_group(id, Group_Name, Code, Sftp_User, Type_Id, Visible, Creation_Date,Created_By) VALUES (3, 'Diabetes', 'Diabetes', null, 2, true, now(), 1);
INSERT INTO pv_group(id, Group_Name, Code, Sftp_User, Type_Id, Visible, Creation_Date,Created_By) VALUES (4, 'IBD', 'IBD', null, 2, true, now(), 1);

INSERT INTO pv_role(id, role_name, type_id, level, visible, description, creation_date, created_by) VALUES (1, 'PATIENT', '7', '2', true, 'Patient', now(), '1');
INSERT INTO pv_role(id, role_name, type_id, level, visible, description, creation_date, created_by) VALUES (2, 'UNIT_ADMIN', '6', '4', true, 'Unit Admin', now(), '1');
INSERT INTO pv_role(id, role_name, type_id, level, visible, description, creation_date, created_by) VALUES (3, 'STAFF_ADMIN', '6', '3', true, 'Unit Staff', now(), '1');
INSERT INTO pv_role(id, role_name, type_id, level, visible, description, creation_date, created_by) VALUES (4, 'SPECIALTY_ADMIN', '6', '5', true, 'Specialty Admin', now(), '1');
INSERT INTO pv_role(id, role_name, type_id, level, visible, description, creation_date, created_by) VALUES (5, 'GLOBAL_ADMIN', '6', '6', true, 'Global Admin', now(), '1');
INSERT INTO pv_role(id, role_name, type_id, level, visible, description, creation_date, created_by) VALUES (6, 'GP_ADMIN ', '6', '2', true, 'Secondary Login', now(), '1');
INSERT INTO pv_role(id, role_name, type_id, level, visible, description, creation_date, created_by) VALUES (7, 'MEMBER', '7', '1', false, 'Member', now(), '1');

INSERT INTO pv_user_group_role VALUES(1, 3, 1, 5, now(), null, now(), 1, null, null);

INSERT INTO pv_feature (id, feature_name, description, start_date, creation_date, created_by) VALUES (1, 'MESSAGING', 'Messaging', now(), now(), 1);
INSERT INTO pv_feature (id, feature_name, description, start_date, creation_date, created_by) VALUES (2, 'SHARING_THOUGHTS', 'Sharing Thoughts', now(), now(), 1);
INSERT INTO pv_feature (id, feature_name, description, start_date, creation_date, created_by) VALUES (3, 'FEEDBACK', 'Feedback', now(), now(), 1);
INSERT INTO pv_feature (id, feature_name, description, start_date, creation_date, created_by) VALUES (4, 'ECS', 'Emergency Care Summary', now(), now(), 1);

INSERT INTO pv_feature_feature_type (id, feature_id, type_id) VALUES (1, 1, 14);
INSERT INTO pv_feature_feature_type (id, feature_id, type_id) VALUES (2, 1, 15);
INSERT INTO pv_feature_feature_type (id, feature_id, type_id) VALUES (4, 2, 14);
INSERT INTO pv_feature_feature_type (id, feature_id, type_id) VALUES (5, 2, 15);
INSERT INTO pv_feature_feature_type (id, feature_id, type_id) VALUES (7, 3, 14);
INSERT INTO pv_feature_feature_type (id, feature_id, type_id) VALUES (9, 4, 14);

INSERT INTO PV_Route (Id, Type_Id, Group_Id, Role_Id, Feature_Id, Display_Order, Url, Template_Url, Controller, Title, Creation_Date, Created_By) VALUES
  (2, 4, 1, null, null, 1,  '/dashboard', 'views/dashboard.html','DashboardCtrl', 'Home', now(), 1 );
INSERT INTO PV_Route (Id, Type_Id, Group_Id, Role_Id, Feature_Id, Display_Order, Url, Template_Url, Controller, Title, Creation_Date, Created_By) VALUES
  (4, 3, null, null, null, 1,  '/messages', 'views/messages.html','MessagesCtrl', 'Messages', now(), 1 );
INSERT INTO PV_Route (Id, Type_Id, Group_Id, Role_Id, Feature_Id, Display_Order, Url, Template_Url, Controller, Title, Creation_Date, Created_By) VALUES
  (6, 3, null, null, null, 1,  '/settings', 'views/settings.html','SettingsCtrl', 'Settings', now(), 1 );
INSERT INTO PV_Route (Id, Type_Id, Group_Id, Role_Id, Feature_Id, Display_Order, Url, Template_Url, Controller, Title, Creation_Date, Created_By) VALUES
  (7, 3, null, null, null, 1,  '/feedback', 'views/feedback.html','FeedbackCtrl', 'Feedback', now(), 1 );
INSERT INTO PV_Route (Id, Type_Id, Group_Id, Role_Id, Feature_Id, Display_Order, Url, Template_Url, Controller, Title, Creation_Date, Created_By) VALUES
  (8, 3, null, null, null, 1,  '/help', 'views/help.html','HelpCtrl', 'Help', now(), 1 );
INSERT INTO PV_Route (Id, Type_Id, Group_Id, Role_Id, Feature_Id, Display_Order, Url, Template_Url, Controller, Title, Creation_Date, Created_By) VALUES
  (10, 4, null, null, null, 2,  '/mydetails', 'views/mydetails.html','MydetailsCtrl', 'My Details', now(), 1 );
INSERT INTO PV_Route (Id, Type_Id, Group_Id, Role_Id, Feature_Id, Display_Order, Url, Template_Url, Controller, Title, Creation_Date, Created_By) VALUES
  (11, 4, null, null, null, 3,  '/results', 'views/results.html','ResultsCtrl', 'My Results', now(), 1 );
INSERT INTO PV_Route (Id, Type_Id, Group_Id, Role_Id, Feature_Id, Display_Order, Url, Template_Url, Controller, Title, Creation_Date, Created_By) VALUES
  (12, 5, null, null, null, 4,  '/resultsdetail', 'views/resultsdetail.html','ResultsDetailCtrl', 'Results Detail', now(), 1 );
INSERT INTO PV_Route (Id, Type_Id, Group_Id, Role_Id, Feature_Id, Display_Order, Url, Template_Url, Controller, Title, Creation_Date, Created_By) VALUES
  (13, 4, null, null, null, 5,  '/medicines', 'views/medicines.html','MedicinesCtrl', 'Medicines', now(), 1 );
INSERT INTO PV_Route (Id, Type_Id, Group_Id, Role_Id, Feature_Id, Display_Order, Url, Template_Url, Controller, Title, Creation_Date, Created_By) VALUES
  (14, 4, null, null, null, 6,  '/letters', 'views/letters.html','LettersCtrl', 'Letters', now(), 1 );
INSERT INTO PV_Route (Id, Type_Id, Group_Id, Role_Id, Feature_Id, Display_Order, Url, Template_Url, Controller, Title, Creation_Date, Created_By) VALUES
  (15, 4, null, null, 3, 7,  '/sharingthoughts', 'views/sharingthoughts.html','SharingthoughtsCtrl', 'Sharing Thoughts', now(), 1 );
INSERT INTO PV_Route (Id, Type_Id, Group_Id, Role_Id, Feature_Id, Display_Order, Url, Template_Url, Controller, Title, Creation_Date, Created_By) VALUES
  (16, 4, null, null, null, 8,  '/contact', 'views/contact.html','ContactCtrl', 'Contact', now(), 1 );
INSERT INTO PV_Route (Id, Type_Id, Group_Id, Role_Id, Feature_Id, Display_Order, Url, Template_Url, Controller, Title, Creation_Date, Created_By) VALUES
  (17, 4, null, 2, null, 9,  '/codes', 'views/codes.html','CodesCtrl', 'Codes', now(), 1 );
INSERT INTO PV_Route (Id, Type_Id, Group_Id, Role_Id, Feature_Id, Display_Order, Url, Template_Url, Controller, Title, Creation_Date, Created_By) VALUES
  (18, 4, null, 2, null, 11,  '/staff', 'views/staff.html','StaffCtrl', 'Staff', now(), 1 );
INSERT INTO PV_Route (Id, Type_Id, Group_Id, Role_Id, Feature_Id, Display_Order, Url, Template_Url, Controller, Title, Creation_Date, Created_By) VALUES
  (19, 4, null, 2, null, 10,  '/groups', 'views/groups.html','GroupsCtrl', 'Groups', now(), 1 );
INSERT INTO PV_Route (Id, Type_Id, Group_Id, Role_Id, Feature_Id, Display_Order, Url, Template_Url, Controller, Title, Creation_Date, Created_By) VALUES
  (20, 4, null, 5, null, 9,  '/codes', 'views/codes.html','CodesCtrl', 'Codes', now(), 1 );
INSERT INTO PV_Route (Id, Type_Id, Group_Id, Role_Id, Feature_Id, Display_Order, Url, Template_Url, Controller, Title, Creation_Date, Created_By) VALUES
  (21, 4, null, 5, null, 11,  '/staff', 'views/staff.html','StaffCtrl', 'Staff', now(), 1 );
INSERT INTO PV_Route (Id, Type_Id, Group_Id, Role_Id, Feature_Id, Display_Order, Url, Template_Url, Controller, Title, Creation_Date, Created_By) VALUES
  (22, 4, null, 5, null, 10,  '/groups', 'views/groups.html','GroupsCtrl', 'Groups', now(), 1 );
INSERT INTO PV_Route (Id, Type_Id, Group_Id, Role_Id, Feature_Id, Display_Order, Url, Template_Url, Controller, Title, Creation_Date, Created_By) VALUES
  (23, 4, null, 2, null, 12,  '/patients', 'views/patients.html','PatientsCtrl', 'Patients', now(), 1 );
INSERT INTO PV_Route (Id, Type_Id, Group_Id, Role_Id, Feature_Id, Display_Order, Url, Template_Url, Controller, Title, Creation_Date, Created_By) VALUES
  (24, 4, null, 5, null, 12,  '/patients', 'views/patients.html','PatientsCtrl', 'Patients', now(), 1 );
INSERT INTO PV_Route (Id, Type_Id, Group_Id, Role_Id, Feature_Id, Display_Order, Url, Template_Url, Controller, Title, Creation_Date, Created_By) VALUES
  (25, 4, null, 4, null, 9,  '/codes', 'views/codes.html','CodesCtrl', 'Codes', now(), 1 );
INSERT INTO PV_Route (Id, Type_Id, Group_Id, Role_Id, Feature_Id, Display_Order, Url, Template_Url, Controller, Title, Creation_Date, Created_By) VALUES
  (26, 4, null, 4, null, 11,  '/staff', 'views/staff.html','StaffCtrl', 'Staff', now(), 1 );
INSERT INTO PV_Route (Id, Type_Id, Group_Id, Role_Id, Feature_Id, Display_Order, Url, Template_Url, Controller, Title, Creation_Date, Created_By) VALUES
  (27, 4, null, 4, null, 10,  '/groups', 'views/groups.html','GroupsCtrl', 'Groups', now(), 1 );
INSERT INTO PV_Route (Id, Type_Id, Group_Id, Role_Id, Feature_Id, Display_Order, Url, Template_Url, Controller, Title, Creation_Date, Created_By) VALUES
  (28, 4, null, 4, null, 12,  '/patients', 'views/patients.html','PatientsCtrl', 'Patients', now(), 1 );
INSERT INTO PV_Route (Id, Type_Id, Group_Id, Role_Id, Feature_Id, Display_Order, Url, Template_Url, Controller, Title, Creation_Date, Created_By) VALUES
  (29, 4, null, 6, null, 9,  '/codes', 'views/codes.html','CodesCtrl', 'Codes', now(), 1 );
INSERT INTO PV_Route (Id, Type_Id, Group_Id, Role_Id, Feature_Id, Display_Order, Url, Template_Url, Controller, Title, Creation_Date, Created_By) VALUES
  (30, 4, null, 6, null, 11,  '/staff', 'views/staff.html','StaffCtrl', 'Staff', now(), 1 );
INSERT INTO PV_Route (Id, Type_Id, Group_Id, Role_Id, Feature_Id, Display_Order, Url, Template_Url, Controller, Title, Creation_Date, Created_By) VALUES
  (31, 4, null, 6, null, 10,  '/groups', 'views/groups.html','GroupsCtrl', 'Groups', now(), 1 );
INSERT INTO PV_Route (Id, Type_Id, Group_Id, Role_Id, Feature_Id, Display_Order, Url, Template_Url, Controller, Title, Creation_Date, Created_By) VALUES
  (32, 4, null, 6, null, 12,  '/patients', 'views/patients.html','PatientsCtrl', 'Patients', now(), 1 );
