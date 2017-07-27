-- Duplicate user script

-- 1. Find users by identifiers, and record their ids
SELECT * FROM pv_user WHERE id in (SELECT user_id FROM pv_identifier where identifier = '4827362807' OR identifier = '1604645962');

-- 2. Select Identifier record for user to be copied, record id
SELECT * FROM pv_identifier where identifier = '0401603628';


-- 3. Record id and update the record with new user ID,
-- NOTE: we cannot copy the same identifier as they MUST be unique in the system
UPDATE  pv_identifier SET  user_id = 28863029 WHERE id = 444489;

-- 4. Copy over any missing groups and roles from user 2 to user 1
SELECT * FROM pv_user_group_role WHERE user_id = 28863029 OR user_id = 444482;
INSERT INTO "pv_user_group_role"("id","user_id","group_id","role_id", "start_date", "end_date","creation_date","created_by", "last_update_date","last_updated_by")
VALUES
-- 633(Airdrie) replace with correct user id
(nextval('hibernate_sequence'), 28863029, 633, 1,'2015-01-13', NULL,'2015-01-13 10:45:19.075', 2, NULL, NULL),
-- 5(ECS) replace with correct user id
 (nextval('hibernate_sequence'), 28863029, 5, 1,'2015-01-13', NULL,'2015-02-17 17:00:46.464', 444482, NULL, NULL);

 -- 5. Copy over fhir links from user 2 to user 1
SELECT * FROM pv_fhir_link WHERE user_id = 444482;
 -- Make sure updating user_id  and identifier_id with correct one
  INSERT INTO "pv_fhir_link"(
    "id", "user_id", "identifier_id", "group_id", "resource_id", "version_id", "resource_type", "active", "creation_date", "last_update_date")
    VALUES
    (nextval('hibernate_sequence'),28863029,444489,5,'1e615bb2-9b4b-469e-b3a9-57e6b2d2d3ab','bbbf0dc3-ccfb-4a40-8ce8-168f1b320df1','Patient','1','2015-02-18 15:22:04.326','2016-06-15 02:24:10.308'),
    (nextval('hibernate_sequence'),28863029,444489,633,'44bbd028-48e6-44b6-97ee-e8bc78ef6430','7d82ba0e-5280-401f-85e6-069f6bc6b21c','Patient','1','2015-01-13 10:45:20.598','2017-05-17 04:05:27.765'),
    (nextval('hibernate_sequence'),28863029,444489,6,'0abb37f4-8dd4-405b-b08e-7f8c462ef3bc','34a379b4-d9e2-4a1e-8d5e-203bc165fd88','Patient','1','2015-01-13 10:45:20.623','2015-01-13 10:45:20.623');

-- 6. User Features
SELECT * FROM pv_feature_user WHERE user_id = 444482;
INSERT INTO "pv_feature_user"(
    "id","user_id","feature_id","opt_in_status","opt_in_hidden","opt_out_hidden","opt_in_date","start_date","end_date","creation_date","created_by",
    "last_update_date", "last_updated_by"
) VALUES(
    nextval('hibernate_sequence'),28863029,4,'1','0','0','2015-02-17 17:06:44.282','2015-02-17',NULL,'2015-02-17 17:00:46.39',28863029,NULL,NULL);

-- 7. gp letter
SELECT * FROM pv_gp_letter WHERE patient_identifier = '1604645962';

-- 8. messages and conversations
SELECT * FROM pv_message WHERE user_id = 444482;
SELECT * FROM pv_conversation_user WHERE user_id = 444482;

-- 9. Alerts
SELECT * FROM pv_alert WHERE user_id = 444482;

-- 10. Lock user 2, NOTE: this does not seems to apply successfully, not sure if
UPDATE  pv_user SET locked = true WHERE id = 444482;
