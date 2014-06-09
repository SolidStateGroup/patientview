\ir '00_spec_helper.sql'

BEGIN;

\ir ../install.sql

SELECT plan(3);

\set pt_json `cat $FHIRBASE_HOME/test/fixtures/patient.json`

SELECT fhir.insert_resource(:'pt_json'::json) as resource_id \gset

SELECT is(count(*)::integer, 1, 'patient was inserted')
       FROM fhir.patient
       WHERE _logical_id = :'resource_id';

SELECT is((((data->'name')->0)->>'text')::varchar, 'Roel', 'patient name is correct')
       FROM fhir.patient
       WHERE _logical_id = :'resource_id';

SELECT is((((data->'name')->0)->>'use')::varchar, 'official', 'patient name.use is correctly saved and restored from DB')
       FROM fhir.patient
       WHERE _logical_id = :'resource_id';

SELECT * FROM finish();
ROLLBACK;
