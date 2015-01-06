INSERT INTO "pv_result_cluster" ( "id", "name") 
VALUES (1 , 'Blood Pressure, Glucose, Weight' );
INSERT INTO "pv_result_cluster" ( "id", "name") 
VALUES (2 , 'U + E test' );

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