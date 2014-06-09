CREATE OR REPLACE FUNCTION meta.eval_ddl(str text)
RETURNS text AS
$BODY$
  begin
    EXECUTE str;
    RETURN str;
  end;
$BODY$
LANGUAGE plpgsql VOLATILE;


CREATE VIEW meta.enums_ddl AS(
  SELECT
    'CREATE TYPE'
    || ' fhir."' || enum  || '"'
    || ' AS ENUM ('
    || array_to_string( ( SELECT array_agg('$quote$' || unnest || '$quote$') FROM unnest(options)) , ',')
    ||  ')' as ddl
  FROM meta.enums
);

SELECT  meta.eval_ddl(ddl) FROM meta.enums_ddl;

CREATE TYPE fhir.resource_state AS ENUM (
  'current',
  'archived',
  'deleted'
);

CREATE TABLE fhir.resource (
  _version_id UUID NOT NULL,
  _logical_id UUID NOT NULL,
  _last_modified_date TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
  _state fhir.resource_state NOT NULL DEFAULT 'current',

  resource_type varchar,
  language VARCHAR,
  data JSON
);

CREATE TABLE fhir.resource_component (
  _id UUID DEFAULT uuid_generate_v4() PRIMARY KEY,
  _version_id UUID NOT NULL,
  _parent_id UUID
);

CREATE TABLE fhir.tag (
  _id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  _version_id UUID NOT NULL,
  _logical_id UUID NOT NULL,
  scheme VARCHAR NOT NULL,
  term VARCHAR NOT NULL,
  label VARCHAR
);

CREATE VIEW meta.datatypes_ddl AS (
SELECT
     'CREATE TABLE'
    ||  ' fhir."' || table_name  || '"'
    ||  '(' || array_to_string(columns, ',') || ')'
    ||  ' INHERITS (fhir.' || base_table || ')'
    -- TODO: add constraints!
  AS ddl
  FROM meta.datatype_tables
 WHERE table_name NOT IN ('resource', 'backbone_element')
);

SELECT meta.eval_ddl(ddl) FROM meta.datatypes_ddl;

CREATE TABLE fhir._resources_list AS
  WITH RECURSIVE tables_tree (table_oid, path) AS (
    SELECT I.inhparent AS table_oid, '{}'::oid[] AS path
      FROM pg_inherits I
      LEFT JOIN pg_inherits I2 ON I.inhparent = I2.inhrelid
      WHERE I2.inhparent IS NULL

    UNION

    SELECT I.inhrelid, TT.path || I.inhparent
      FROM pg_inherits I
      JOIN tables_tree TT ON TT.table_oid  = I.inhparent
  )
  SELECT
    C.relname AS table_name
  FROM tables_tree TT
  JOIN pg_class C ON TT.table_oid = C.oid
  JOIN pg_class P ON TT.path[1] = P.oid
  JOIN pg_namespace N ON P.relnamespace = N.oid
  WHERE P.relname = 'resource' AND N.nspname = 'fhir';

CREATE VIEW meta.resources_ddl AS (
SELECT
  ARRAY[
       'CREATE TABLE'
   || ' fhir."' || table_name  || '"'
    || '(' || array_to_string(columns, ',') || ')'
    || ' INHERITS (fhir.' || base_table || ')'
  ,CASE
   WHEN base_table = 'resource' THEN
     'ALTER TABLE fhir.' || table_name || ' ADD PRIMARY KEY (_version_id);' ||
     'CREATE UNIQUE INDEX ON fhir.' || table_name || '(_logical_id) WHERE _state = ''current'''
   ELSE
        'ALTER TABLE fhir.' || table_name || ' ADD FOREIGN KEY (_version_id) REFERENCES fhir.' || resource_table_name || ' (_version_id) ON DELETE CASCADE DEFERRABLE;'
     || 'CREATE INDEX ON fhir.' || table_name || ' (_version_id);'
   END
  ] AS ddls
  FROM meta.resource_tables
  WHERE table_name !~ '^profile'
);

SELECT meta.eval_ddl(unnest)
  FROM ( SELECT unnest(ddls) FROM meta.resources_ddl) _;
