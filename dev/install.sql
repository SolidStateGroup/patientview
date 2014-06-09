--db:testfhir
--{{{
--DROP SCHEMA IF EXISTS meta CASCADE;
--DROP SCHEMA IF EXISTS fhir CASCADE;

-- Revert all changes on failure.
\set ON_ERROR_ROLLBACK 1
\set ON_ERROR_STOP true
\set QUIET 1

\ir sql/01_extensions.sql
\ir sql/03_meta.sql
\ir sql/04_load_meta.sql
\ir sql/05_functions.sql
\ir sql/06_datatypes.sql
\ir sql/07_schema.sql
\ir sql/08_generate_schema.sql
\ir sql/10__insert_helpers.sql
\ir sql/10_insert.sql
\ir sql/11_delete.sql
\ir sql/12_update.sql

--}}}
