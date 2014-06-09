\ir '00_spec_helper.sql'
BEGIN;

\ir ../sql/03_meta.sql
\ir ../sql/04_load_meta.sql


SELECT plan(1);

SELECT  is(max,'*','should be multiple')
  FROM  meta.resource_elements
 WHERE  path[1] = 'Patient'
   AND  path[2] = 'name';

SELECT  *
  FROM  finish();

ROLLBACK;
