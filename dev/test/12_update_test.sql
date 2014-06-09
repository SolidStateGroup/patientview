\ir '00_spec_helper.sql'

BEGIN;

\ir ../install.sql

\set pt_json `cat $FHIRBASE_HOME/test/fixtures/patient.json`
\set new_pt_json `cat $FHIRBASE_HOME/test/fixtures/updated_patient.json`

SELECT plan(6);

SELECT fhir.insert_resource(:'pt_json'::json) AS logical_id \gset
SELECT _version_id as version_id FROM fhir.patient WHERE _logical_id = :'logical_id' \gset

SELECT is(COUNT(*)::integer, 1, 'patient was inserted')
       FROM fhir.patient WHERE _logical_id = :'logical_id';

SELECT is(
       (SELECT text::varchar
       FROM fhir.patient_name WHERE _version_id = :'version_id'),
       'Roel'::varchar,
       'patient data was placed in correct tables');

SELECT fhir.update_resource(:'logical_id', :'new_pt_json'::json);
SELECT _version_id as new_version_id FROM fhir.patient WHERE _state = 'current' and _logical_id = :'logical_id' \gset

SELECT is((
    SELECT count(1)::integer
    FROM fhir.patient
    WHERE _version_id = :'version_id' and _state = 'archived'),
  1, 'old patient record marked as archived');

SELECT is(
       (SELECT text::varchar
       FROM fhir.patient_name WHERE _version_id = :'version_id'),
       'Roel'::varchar,
       'old patient data is the same');

SELECT is(
       (SELECT text::varchar
       FROM fhir.patient_name WHERE _version_id = :'new_version_id'),
       'Gavrila'::varchar,
       'patient data was correctly updated');

-- test if error was thrown when update_resource is called with
-- unknown resource ID
SELECT uuid_generate_v4() AS random_uuid \gset
PREPARE incorrect_update_resource_call AS SELECT fhir.update_resource(:'random_uuid', :'pt_json'::json);
SELECT throws_ok(
  'incorrect_update_resource_call',
  'Resource with id ' || :'random_uuid'::varchar || ' not found'
);

SELECT * FROM finish();
ROLLBACK;
