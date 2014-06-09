--db:testfhir
--{{{
\ir '00_spec_helper.sql'

BEGIN;

\ir ../sql/01_extensions.sql
\ir ../sql/03_meta.sql
\ir ../sql/04_load_meta.sql
\ir ../sql/05_functions.sql

SELECT plan(9);

SELECT is(
  (SELECT fhir.array_last(ARRAY['a','b','c'])),
  'c',
  'fhir.array_last'
);

SELECT is(
  (SELECT fhir.array_pop(ARRAY['a','b','c'])),
  ARRAY['a','b']::varchar[],
  'fhir.array_pop'
);

SELECT is(
  (SELECT fhir.table_name(ARRAY['a','b','c']::varchar[])),
  'a_b_c',
  'table_name'
);

SELECT is(
  (SELECT fhir.table_name(ARRAY['abay','baran','cidr']::varchar[])),
  'abay_baran_cidr',
  'table_name'
);

SELECT is(
  (SELECT fhir.table_name(ARRAY['schedule.repeat']::varchar[])),
  'schedulerepeat',
  'table_name'
);

SELECT is(
  (SELECT fhir.table_name(ARRAY['immunization_recommendation','codeable_concept']::varchar[])),
  'imm_rec_cc',
  'table_name'
);

SELECT is(
  (SELECT fhir.table_name(ARRAY['xang','abay','baran','cidr']::varchar[])),
  'xang_abay_baran_cidr',
  'table_name'
);

SELECT is(
  (SELECT fhir.camelize('here_is_my_string')),
  'hereIsMyString',
  'camelize'
);

SELECT is(
  (SELECT fhir.eval_template(
      'SELECT * FROM {{name}} WHERE second={{second}}',
      'name','Max',
      'second', 'Second')
  ),
  'SELECT * FROM Max WHERE second=Second',
  'eval_template'
);

SELECT * FROM finish();
ROLLBACK;
--}}}
