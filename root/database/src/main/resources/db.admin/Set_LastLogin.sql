DROP TABLE IF EXISTS random_ids;
DROP TABLE IF EXISTS random_ids_three_months;
DROP TABLE IF EXISTS random_ids_before_three;
DROP TABLE IF EXISTS random_ids_never;

/* clear last and current login */
UPDATE pv_user SET last_login = NULL, current_login = NULL;

/* 44709 total */
CREATE TEMP TABLE random_ids AS SELECT id FROM pv_user ORDER BY random();

/* 30% = 13413 */
CREATE TEMP TABLE random_ids_three_months AS SELECT * FROM random_ids LIMIT 13413;
DELETE FROM random_ids WHERE id IN (SELECT * FROM random_ids_three_months);

/* 50% = 22355 */
CREATE TEMP TABLE random_ids_before_three AS SELECT * FROM random_ids LIMIT 22355;
DELETE FROM random_ids WHERE id IN (SELECT * FROM random_ids_before_three);

/* 20% = 8942 */
CREATE TEMP TABLE random_ids_never AS SELECT * FROM random_ids LIMIT 8942;
DELETE FROM random_ids WHERE id IN (SELECT * FROM random_ids_never);

UPDATE pv_user 
SET current_login = (SELECT TIMESTAMP '2016-06-19 00:00:00' + random() * (TIMESTAMP '2016-09-19 00:00:00' - TIMESTAMP '2016-06-19 00:00:00'))
WHERE id IN (SELECT * FROM random_ids_three_months);

UPDATE pv_user 
SET current_login = (SELECT TIMESTAMP '2015-06-19 00:00:00' + random() * (TIMESTAMP '2016-06-19 00:00:00' - TIMESTAMP '2015-06-19 00:00:00'))
WHERE id IN (SELECT * FROM random_ids_before_three);

UPDATE pv_user 
SET last_login = NULL, current_login = NULL
WHERE id IN (SELECT * FROM random_ids_never);