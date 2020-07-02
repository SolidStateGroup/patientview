INSERT INTO pv_feature (id, feature_name, description, start_date, creation_date, created_by)
VALUES (17, 'MEDPIC', 'Image-based Results Entry (MedPic)', now(), now(), 1);

INSERT INTO pv_feature_feature_type (id, feature_id, type_id) values (23, 17, 14);