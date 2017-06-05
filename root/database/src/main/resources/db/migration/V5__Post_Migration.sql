/* result clusters (requires observation headings)
 Using Custom form for Dialysis Treatment hence not adding any observation headings to
*/
INSERT INTO "pv_result_cluster" ( "id", "name")
VALUES (1 , 'Blood Pressure, Glucose, Weight' );
INSERT INTO "pv_result_cluster" ( "id", "name")
VALUES (2 , 'U + E test' );
INSERT INTO "pv_result_cluster" ( "id", "name")
VALUES (3 , 'Dialysis Treatment' );

INSERT INTO "pv_result_cluster_observation_heading" ( "display_order", "id", "observation_heading_id", "result_cluster_id")
VALUES ( 1, 1, (SELECT id FROM pv_observation_heading WHERE pv_observation_heading.code ='bpsys'),  1);
INSERT INTO "pv_result_cluster_observation_heading" ( "display_order", "id", "observation_heading_id", "result_cluster_id")
VALUES ( 2, 2, (SELECT id FROM pv_observation_heading WHERE pv_observation_heading.code ='bpdia'),  1);
INSERT INTO "pv_result_cluster_observation_heading" ( "display_order", "id", "observation_heading_id", "result_cluster_id")
VALUES ( 3, 3, (SELECT id FROM pv_observation_heading WHERE pv_observation_heading.code ='glucose'), 1);
INSERT INTO "pv_result_cluster_observation_heading" ( "display_order", "id", "observation_heading_id", "result_cluster_id")
VALUES ( 4, 4, (SELECT id FROM pv_observation_heading WHERE pv_observation_heading.code ='weight'), 1);

INSERT INTO "pv_result_cluster_observation_heading" ( "display_order", "id", "observation_heading_id", "result_cluster_id")
VALUES ( 1, 5, (SELECT id FROM pv_observation_heading WHERE pv_observation_heading.code ='sodium'),  2);
INSERT INTO "pv_result_cluster_observation_heading" ( "display_order", "id", "observation_heading_id", "result_cluster_id")
VALUES ( 2, 6, (SELECT id FROM pv_observation_heading WHERE pv_observation_heading.code ='potassium'),  2);
INSERT INTO "pv_result_cluster_observation_heading" ( "display_order", "id", "observation_heading_id", "result_cluster_id")
VALUES ( 3, 7, (SELECT id FROM pv_observation_heading WHERE pv_observation_heading.code ='urea'),  2);
INSERT INTO "pv_result_cluster_observation_heading" ( "display_order", "id", "observation_heading_id", "result_cluster_id")
VALUES ( 4, 8, (SELECT id FROM pv_observation_heading WHERE pv_observation_heading.code ='creatinine'),  2);
INSERT INTO "pv_result_cluster_observation_heading" ( "display_order", "id", "observation_heading_id", "result_cluster_id")
VALUES ( 5, 9, (SELECT id FROM pv_observation_heading WHERE pv_observation_heading.code ='hco3'),  2);

/* pulse */
INSERT INTO "pv_result_cluster_observation_heading" ( "display_order", "id", "observation_heading_id", "result_cluster_id")
VALUES ( 5, 10, (SELECT id FROM pv_observation_heading WHERE pv_observation_heading.code ='pulse'), 1);

/* default messaging contacts (function must be created as postgres) */
CREATE OR REPLACE FUNCTION featureIDdmc() RETURNS INT AS
$BODY$
DECLARE feature_id1 INT;
BEGIN
 SELECT ID FROM pv_feature WHERE feature_name = 'DEFAULT_MESSAGING_CONTACT' INTO feature_id1;
 return feature_id1;
END
$BODY$
LANGUAGE plpgsql;

INSERT INTO pv_feature_user (feature_id, user_id, creation_date, created_by, id)
SELECT DISTINCT featureIDdmc(), pv_user.id, now(), 2, nextval( 'hibernate_sequence' )
FROM pv_contact_point
JOIN pv_group ON group_id = pv_group.id
JOIN pv_user_group_role ON pv_user_group_role.group_id = pv_group.id
JOIN pv_user ON pv_user.id = pv_user_group_role.user_id
WHERE upper(pv_user.email) = upper(pv_contact_point.content)
AND pv_contact_point.type_id IN (SELECT id FROM pv_lookup_value WHERE VALUE = 'PV_ADMIN_EMAIL')
AND pv_group.type_id = 1
AND pv_user.id NOT IN (
SELECT user_id FROM pv_feature_user WHERE feature_id = featureIDdmc()
);

/* messaging for all groups */
CREATE OR REPLACE FUNCTION featureIDmsg() RETURNS INT AS
$BODY$
DECLARE feature_id1 INT;
BEGIN
 SELECT ID FROM pv_feature WHERE feature_name = 'MESSAGING' INTO feature_id1;
 return feature_id1;
END
$BODY$
LANGUAGE plpgsql;

INSERT INTO pv_feature_group (feature_id,group_id,creation_date,created_by)
SELECT DISTINCT featureIDmsg(), pv_group.id, now(), 1
FROM pv_group
WHERE type_id = 1
AND visible = TRUE
AND id NOT IN(SELECT group_id FROM pv_feature_group WHERE feature_id = featureIDmsg())

-- Cannot rely on admin to populate this correctly so having this script to insert all new headings
INSERT INTO "pv_observation_heading"(
    "id","code", "heading", "name", "normal_range", "units", "min_graph", "max_graph", "info_link", "default_panel",
    "default_panel_order", "decimal_places", "creation_date", "created_by", "last_update_date", "last_updated_by") VALUES
     (nextval('hibernate_sequence'), 'HdHours','Hours', 'in hours',   NULL, NULL, NULL, NULL,'', NULL, 1, NULL, NOW(), NULL, NULL, NULL),
     (nextval('hibernate_sequence'),'HdLocation','Location', 'in hours',   NULL, NULL, NULL, NULL,'', NULL, 1, NULL, NOW(), NULL, NULL, NULL),
     (nextval('hibernate_sequence'),'eprex','Eprex', 'in iu',   NULL, NULL, NULL, NULL,'', NULL, 1, NULL, NOW(), NULL, NULL, NULL),
     (nextval('hibernate_sequence'),'TargetWeight','Target Weight', 'in kg',   NULL, NULL, NULL, NULL,'', NULL, 1, NULL, NOW(), NULL, NULL, NULL),
     (nextval('hibernate_sequence'),'UfVolume','UF Volume', 'in mL',   NULL, NULL, NULL, NULL,'', NULL, 1, NULL, NOW(), NULL, NULL, NULL),
     (nextval('hibernate_sequence'),'BodyTemperature','Body Temp', 'in °C',   NULL, NULL, NULL, NULL,'', NULL, 1, NULL, NOW(), NULL, NULL, NULL),
     (nextval('hibernate_sequence'),'hypotension','Symptomatic Hypotension', '',   NULL, NULL, NULL, NULL,'', NULL, 1, NULL, NOW(), NULL, NULL, NULL),
     (nextval('hibernate_sequence'),'bps','BPS', '',   NULL, NULL, NULL, NULL,'', NULL, 1, NULL, NOW(), NULL, NULL, NULL),
     (nextval('hibernate_sequence'),'DialFlow','Dial Flow', '',   NULL, NULL, NULL, NULL,'', NULL, 1, NULL, NOW(), NULL, NULL, NULL),
     (nextval('hibernate_sequence'),'LitresProcessed','Litres Processed', '',   NULL, NULL, NULL, NULL,'', NULL, 1, NULL, NOW(), NULL, NULL, NULL);