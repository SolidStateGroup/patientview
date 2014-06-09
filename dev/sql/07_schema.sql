set search_path = fhir, pg_catalog;
-- FIXME: may be use underscore

-- expand polimorphic types
CREATE
VIEW meta.expanded_resource_elements as (
  SELECT
    array_pop(path) || ARRAY[column_name(array_last(path), type)] as path,
    type,
    min,
    max
  FROM (
    SELECT
      path,
      CASE WHEN array_length(type, 1) is null
        THEN '_NestedResource_'
        ELSE unnest(type)
      END as type,
      min,
      max
    FROM meta.resource_elements
  ) e
  WHERE type not in ('Extension', 'contained') OR type is null
);

-- get all elements wich have a children
-- all parent path is coumpound (teorema Bodnarchuka)
CREATE
VIEW meta.compound_resource_elements as (
  SELECT a.*
         ,ere.min
         ,ere.max
    FROM (
            SELECT DISTINCT
              array_pop(path) as path
            FROM meta.expanded_resource_elements
            WHERE array_length(path,1) > 1
         ) a
    LEFT JOIN meta.expanded_resource_elements ere
    ON ere.path = a.path
);

-- Two ways of calculate columns:
-- 1. OR primitive OR enum (we suggest that no enums in resource elements)
-- TODO: check invariant
-- 2. expanded_resource_elements - compound_resource_elements - complex_types - resource_references
CREATE MATERIALIZED VIEW meta.resource_columns as (
    SELECT
      e.path as path,
      tt.pg_type as pg_type,
      column_ddl(array_last(e.path), tt.pg_type, e.min::varchar, e.max) as column_ddl,
      e.min,
      e.max
    FROM meta.expanded_resource_elements e
    JOIN meta.type_to_pg_type tt ON tt.type = underscore(e.type)
);


-- elements recursively expanded with complex datatypes
CREATE
VIEW meta.expanded_with_dt_resource_elements as (
    SELECT
      e.path || array_tail(t.path) as path,
      table_name(t.path) as base_table,
      CASE WHEN array_length(t.path,1) = 1
        THEN e.min
        ELSE t.min
      END AS min,
      CASE WHEN array_length(t.path,1) = 1
        THEN e.max
        ELSE t.max
      END AS max
    FROM meta.expanded_resource_elements e
    JOIN meta.unified_complex_datatype t
    ON t.path[1] = e.type
);


-- select all tables will be created
-- for resource, compound element and complex type
CREATE
VIEW meta.resource_tables as (
  SELECT
    path as path,
    table_name(path) as table_name,
    resource_table_name(path) as resource_table_name,
    parent_table_name(path) as parent_table_name,
    case
      when array_length(path, 1) > 1 then 'resource_component'
      else 'resource'
    end as base_table,
    coalesce((
      SELECT array_agg(column_ddl)
        FROM meta.resource_columns rc
       WHERE array_pop(rc.path) = e.path
    ), ARRAY[]::varchar[]) as columns
    ,min
    ,max
  FROM meta.compound_resource_elements e
  UNION
  SELECT
    path as path
    ,table_name(path) as table_name
    ,resource_table_name(path) as resource_table_name
    ,parent_table_name(path) as parent_table_name
    ,base_table
    ,array[]::varchar[] as columns
    ,min
    ,max
  FROM meta.expanded_with_dt_resource_elements
);

set search_path = public, pg_catalog;
