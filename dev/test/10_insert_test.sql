\ir '00_spec_helper.sql'

BEGIN;

\ir ../sql/01_extensions.sql
\ir ../sql/03_meta.sql
\ir ../sql/04_load_meta.sql
\ir ../sql/05_functions.sql
\ir ../sql/06_datatypes.sql
\ir ../sql/07_schema.sql
\ir ../sql/08_generate_schema.sql
\ir ../sql/10__insert_helpers.sql
\ir ../sql/10_insert.sql

\set pt_json `cat $FHIRBASE_HOME/test/fixtures/patient.json`

SELECT plan(6);

select fhir.insert_resource(:'pt_json'::json) as logical_id \gset
select _version_id as version_id from fhir.resource where _logical_id = :'logical_id' \gset

\echo :'logical_id';
\echo :'version_id';

SELECT is(count(*)::integer, 1, 'insert tag')
  FROM fhir.tag;

SELECT is(
        (SELECT term FROM fhir.tag WHERE _version_id = :'version_id'),
        'test term',
        'should save term');

SELECT is(count(*)::integer, 1, 'insert patient')
       FROM fhir.patient;

SELECT is(count(*)::integer, 1, 'insert patient name')
       FROM fhir.patient_name;

SELECT is(
       (SELECT family FROM fhir.patient_name
         WHERE text = 'Roel'
         AND _version_id = :'version_id'),
       ARRAY['Bor']::varchar[],
       'should record name');

SELECT is(count(*)::int, 2)
       FROM fhir.patient_gender_cd
       WHERE _version_id = :'version_id';

SELECT * FROM finish();
ROLLBACK;
