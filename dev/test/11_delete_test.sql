\ir '00_spec_helper.sql'

BEGIN;

\ir ../install.sql

\set pt_json `cat $FHIRBASE_HOME/test/fixtures/patient.json`

SELECT plan(4);

select fhir.insert_resource(:'pt_json'::json) as logical_id \gset

SELECT :'logical_id';

SELECT is((
    SELECT count(*)::integer
    FROM fhir.patient p
    WHERE p._state = 'current' and p._logical_id = :'logical_id'),
  1, 'insert current patient record');

SELECT is((
    SELECT count(*)::integer
    FROM fhir.patient_name n
    JOIN fhir.patient p on p._version_id = n._version_id
    WHERE p._state = 'current' and p._logical_id = :'logical_id'),
  1, 'insert current patient name record');

select fhir.delete_resource(:'logical_id');

SELECT is((
    SELECT count(*)::integer
    FROM fhir.patient p
    WHERE p._state = 'current' and p._logical_id = :'logical_id'),
  0, 'leave no current patient record');

SELECT is((
    SELECT count(*)::integer
    FROM fhir.patient p
    WHERE p._state = 'deleted' and p._logical_id = :'logical_id'),
  1, 'mark patient record as deleted');

SELECT * FROM finish();
ROLLBACK;
