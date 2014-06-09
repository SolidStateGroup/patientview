\ir '00_spec_helper.sql'

BEGIN;

\ir ../sql/01_extensions.sql
\ir ../sql/03_meta.sql
\ir ../sql/04_load_meta.sql
\ir ../sql/05_functions.sql
\ir ../sql/06_datatypes.sql
\ir ../sql/07_schema.sql

SELECT plan(8);

SELECT is (
  (
    SELECT array_agg(type)
    FROM meta.expanded_resource_elements
    WHERE path[1] = 'Patient'
    and path[2] ~ '^deceased_'
  ),
  ARRAY['boolean','dateTime']::varchar[],
  'should expand polimorphic'
);

CREATE OR REPLACE
FUNCTION array_sort(arr anyarray)
  RETURNS anyarray language sql AS $$
    select array_agg(x)
      from
        (select unnest(arr) as x order by x) x;
$$  IMMUTABLE;

SELECT is (
  (
    select array_sort(array_agg(fhir.array_last(path))) as nm
    from meta.compound_resource_elements
    where path[1] = 'Encounter'
  ),
  array_sort(ARRAY['Encounter', 'hospitalization','accomodation','location','participant' ]::varchar[]),
  'should select only compaund elements'
);

SELECT is(
(
  SELECT count(*)::integer
  FROM meta.expanded_resource_elements e
  JOIN meta.enums en ON en.enum = e.type
 ),
 0, 'no enums in resource elements');

SELECT is(
  (
    SELECT array_agg(attr) FROM (
      SELECT
        path[2] as attr
      FROM meta.resource_columns
      WHERE path[1] = 'Encounter'
        AND array_length(path,1) = 2
      ORDER BY attr
    ) pp
  ),
  ARRAY['class','status']::varchar[],
  'only 3 columns in encounter');

SELECT is(column_ddl, '"comment" varchar[]', 'should be array')
FROM meta.resource_columns
WHERE
  path[1] = 'Specimen'
  AND path[2] = 'collection'
  AND path[3] = 'comment';


SELECT is(column_ddl, '"note" varchar not null', 'should be not null')
FROM meta.resource_columns
WHERE
  path[1] = 'Alert'
  AND path[2] = 'note';


SELECT is(
  (
    SELECT base_table
    FROM meta.expanded_with_dt_resource_elements
    where path[1] = 'Encounter'
    AND path[3] = 'accomodation'
    AND path[4] = 'period'
    order by path
  ),
  'period',
  'check base table'
);

SELECT is(
  (
    SELECT count(*)::integer
    FROM meta.resource_tables
    where table_name in ('encounter',
      'encounter_hospitalization',
      'encounter_hospitalization_accomodation',
      'encounter_idn_period')
  ),
  4,
  'check some tables in encounter'
);

SELECT * FROM finish();
ROLLBACK;
