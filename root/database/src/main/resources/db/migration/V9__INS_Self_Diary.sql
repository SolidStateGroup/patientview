INSERT INTO pv_feature (id, feature_name, description, start_date, creation_date, created_by)
VALUES (16, 'INS_DIARY', 'INS Diary Recording', now(), now(), 1);

INSERT INTO pv_feature_feature_type (id, feature_id, type_id) values (22, 16, 14);
