\ir '00_spec_helper.sql'

BEGIN;

\ir ../sql/01_extensions.sql
\ir ../sql/03_meta.sql
\ir ../sql/04_load_meta.sql
\ir ../sql/05_functions.sql
\ir ../sql/06_datatypes.sql
\ir ../sql/07_schema.sql
\ir ../sql/08_generate_schema.sql

SELECT plan(1);

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


SELECT * FROM finish();
ROLLBACK;
