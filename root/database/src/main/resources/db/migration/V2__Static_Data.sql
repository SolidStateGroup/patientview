INSERT INTO pv_user (id, username, password, change_password, locked, email, name, start_date, creation_date, created_by) VALUES
  (1, 'system','pppppp', false, false, 'system@patientview.org', 'system', now(), now(), 1);

INSERT INTO pv_user (id, username, password, change_password, locked, email, name, start_date, creation_date, created_by) VALUES
  (2, 'migration','pppppp', false, false, 'migration@patientview.org', 'migration', now(), now(), 1);

INSERT INTO pv_lookup_type(id, creation_date, description, lookup_type, created_by)  VALUES (1, now(), 'Type of group','GROUP', '1');

INSERT INTO pv_lookup_value(id, creation_date, value, created_by, lookup_type_id) VALUES (1, now(), 'UNIT','1', '1');
INSERT INTO pv_lookup_value(id, creation_date, value, created_by, lookup_type_id) VALUES (2, now(), 'SPECIALTY','1','1');


INSERT INTO pv_role(id, name, description, creation_date, created_by) VALUES (1, 'PATIENT', 'A standard patient user', now(), '1');
INSERT INTO pv_role(id, name, description, creation_date, created_by) VALUES (2, 'UNIT_ADMIN', 'A unit administrator', now(), '1');
INSERT INTO pv_role(id, name, description, creation_date, created_by) VALUES (3, 'STAFF_ADMIN', 'A radar administrator', now(), '1');
INSERT INTO pv_role(id, name, description, creation_date, created_by) VALUES (4, 'SPECIALTY_ADMIN', 'A specialty administrator', now(), '1');
INSERT INTO pv_role(id, name, description, creation_date, created_by) VALUES (5, 'SUPER_ADMIN', 'A superamdmin', now(), '1');
INSERT INTO pv_role(id, name, description, creation_date, created_by) VALUES (6, 'GP', 'A doctor', now(), '1');


INSERT INTO pv_feature (id, feature_name, description, start_date, creation_date, created_by) VALUES (1, 'MESSAGING', 'Messaging other users and admins', now(), now(), 1);
INSERT INTO pv_feature (id, feature_name, description, start_date, creation_date, created_by) VALUES (2, 'SHARING_THOUGHTS', 'Sharing thoughts of a patient on care', now(), now(), 1);
INSERT INTO pv_feature (id, feature_name, description, start_date, creation_date, created_by) VALUES (3, 'FEEDBACK', 'Ability to feedback problems on patient pages', now(), now(), 1);
INSERT INTO pv_feature (id, feature_name, description, start_date, creation_date, created_by) VALUES (4, 'ECS', 'Emergency Care Summary', now(), now(), 1);


