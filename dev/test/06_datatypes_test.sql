\ir '00_spec_helper.sql'
BEGIN;

\ir ../sql/01_extensions.sql
\ir ../sql/03_meta.sql
\ir ../sql/04_load_meta.sql
\ir ../sql/05_functions.sql
\ir ../sql/06_datatypes.sql

SELECT plan(7);

SELECT is(pg_type,'varchar','convert string') FROM meta.primitive_types
WHERE type = 'string';

SELECT is(pg_type, 'fhir."AddressUse"' ,'convert enum') FROM meta.primitive_types
WHERE type = 'AddressUse';

SELECT is(type, 'dateTime', 'should expand datatypes') FROM meta.datatype_unified_elements
where path[1]='Address' and path[2] = 'period' and path[3]='end'
order by path;

SELECT is(type, 'ResourceReference', 'should expand datatypes')
FROM meta.unified_complex_datatype
where path[1]='CodeableConcept' and path[2] = 'coding' and path[3]='valueSet'
order by path;

SELECT is(pg_type, 'fhir."AddressUse"', 'should collect columns')
FROM meta.unified_datatype_columns
where path[1]='Address' and path[2] = 'use'
order by path;

select
  is(array_length(columns,1), 7, 'columns')
  from meta.datatype_tables
  where table_name = 'attachment';

select
  is(base_table, 'resource_component', 'base table')
  from meta.datatype_tables
  where table_name = 'attachment';

SELECT * FROM finish();
ROLLBACK;
