\ir '00_spec_helper.sql'

BEGIN;

\ir ../sql/01_extensions.sql
\ir ../sql/03_meta.sql
\ir ../sql/04_load_meta.sql
\ir ../sql/05_functions.sql
\ir ../sql/06_datatypes.sql
\ir ../sql/07_schema.sql
\ir ../sql/08_generate_schema.sql
\ir ../sql/09_views.sql
\ir ../sql/10__insert_helpers.sql
\ir ../sql/10_insert.sql

\set pt_json `cat $FHIRBASE_HOME/test/fixtures/patient.json`

SELECT plan(9);

select fhir.insert_resource(:'pt_json'::json) as logical_id \gset
select _version_id as version_id from fhir.resource where _logical_id = :'logical_id' \gset

\echo :'logical_id';
\echo :'version_id';

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

SELECT is(_type, 'patient')
       FROM fhir.resource
       WHERE _logical_id = :'logical_id';

SELECT is(count(*)::int, 2)
       FROM fhir.patient_gender_cd
       WHERE _version_id = :'version_id';

SELECT is_empty(
  'SELECT *
  FROM fhir.resource_component
  WHERE _unknown_attributes IS NOT NULL',
  'should not contain any _unknown_attributes'
);

SELECT * FROM fhir.organization;

SELECT is((SELECT array_agg(name ORDER BY name) FROM fhir.organization
       WHERE _container_id = :'version_id'),
       ARRAY['ACME', 'Foobar']::varchar[],
       'contained resource was correctly saved');

SELECT is((SELECT array_agg(id order by id) FROM fhir.organization
       WHERE _container_id = :'version_id'),
       ARRAY['#org1', '#org2']::varchar[],
       'id should be correct');

SELECT is((SELECT array_agg(ot.value ORDER BY value) FROM fhir.organization_telecom ot
       JOIN fhir.organization o ON o._version_id = ot._version_id
       WHERE o._container_id = :'version_id'),
       ARRAY['+31612234000', '+31612234322']::varchar[],
       'contained resource was correctly saved');

SELECT * FROM finish();
ROLLBACK;
