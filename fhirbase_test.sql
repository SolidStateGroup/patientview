BEGIN;

\set pt1 `cat ./test/pt1.json`
\set pt2 `cat ./test/pt2.json`

CREATE EXTENSION pgtap;

SELECT plan(7);

SELECT COUNT(*) FROM (SELECT fhir.insert_resource(:'pt1') FROM generate_series(1,20)) gen;

SELECT has_schema('fhir'::name);
SELECT has_table('fhir'::name, 'patient'::name);
SELECT has_table('fhir'::name, 'patient_name'::name);

SELECT is(COUNT(*)::integer, 20, 'total 20 patients inserted')
       FROM fhir.patient;

SELECT _logical_id AS first_id FROM fhir.patient LIMIT 1 \gset
SELECT _logical_id AS second_id FROM fhir.patient LIMIT 1 OFFSET 1 \gset

SELECT fhir.delete_resource(:'first_id');

SELECT is_empty($$select * from fhir.patient p where _logical_id =  '$$ || :'first_id' || $$' and _state = 'current'$$, '');

SELECT ok(fhir.update_resource(:'second_id', :'pt2'::json) = 0,
       'second patient was updated');

SELECT is((SELECT (((data)->'identifier')->0->>'value')::varchar
       FROM fhir.patient
       WHERE _logical_id = :'second_id' AND _state = 'current'),
       '12345'::varchar,
       'second patient''s data was actualy changed');

ROLLBACK;
