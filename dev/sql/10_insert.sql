--db: fff
--{{{
select row_to_json(json_each(c.value))
from json_array_elements('{"contained":[{"id":"id1", "foo":"bar"}, {"id":"id2", "name":"Joe"}]}'::json->'contained') c;
--}}}

-- get_nested_entity_from_json(max, path)
CREATE OR REPLACE
FUNCTION fhir.json_extract_value_ddl(max varchar, key varchar)
RETURNS text
AS $$
  SELECT CASE WHEN max='*'
    THEN 'json_array_elements((p.value::json)->''' || key || ''')'
    ELSE '((p.value::json)->''' || key || ''')'
  END;
$$ IMMUTABLE LANGUAGE sql;

--{{{

/* DROP VIEW IF EXISTS insert_ctes cascade; */
CREATE OR REPLACE VIEW insert_ctes AS (
SELECT
  path,
  CASE
  WHEN array_length(path,1) = 1 THEN
     fhir.eval_template($SQL$
       _{{table_name}}  AS (
         SELECT path, value, logical_id, version_id, null::uuid as id, null::uuid as parent_id
            FROM (
              SELECT coalesce(_logical_id, uuid_generate_v4()) as logical_id , uuid_generate_v4() as version_id, ARRAY['{{resource}}'] as path, _data as value
         ) _
      )
     $SQL$,
     'resource', path[1],
     'table_name', table_name)
  WHEN max = '*' THEN
    fhir.eval_template($SQL$
      _{{table_name}}  AS (
        SELECT
          {{path}}::text[] as path,
          ar.value as value,
          null::uuid as logical_id,
          p.version_id,
          uuid_generate_v4() as id,
          {{parent_id}} as parent_id
        FROM _{{parent_table}} p, json_array_elements((p.value::json)->{{key}}) ar
        WHERE p.value IS NOT NULL
      )
      $SQL$,
      'table_name', table_name,
      'path', quote_literal(path::text),
      'key', '''' || fhir.array_last(path) || '''',
      'parent_table', fhir.table_name(fhir.array_pop(path)),
      'parent_id', case when array_length(path, 1) = 2 then 'null::uuid' else 'p.id' end
    )
  ELSE
    fhir.eval_template($SQL$
      _{{table_name}}  AS (
        SELECT
          {{path}}::text[] as path,
          {{value}} as value,
          null::uuid as logical_id,
          p.version_id,
          uuid_generate_v4() as id,
          {{parent_id}} as parent_id
        FROM _{{parent_table}} p
        WHERE p.value IS NOT NULL
      )
      $SQL$,
      'table_name', table_name,
      'path', quote_literal(path::text),
      'value', '((p.value::json)->''' || fhir.array_last(path) || ''')',
      'parent_table', fhir.table_name(fhir.array_pop(path)),
      'parent_id', case when array_length(path, 1) = 2 then 'null::uuid' else 'p.id' end
    )
  END as cte
FROM meta.resource_tables
ORDER BY PATH
);
--}}}

CREATE OR REPLACE VIEW insert_ddls AS (
SELECT
  path[1] as resource,
  fhir.eval_template($SQL$
     --DROP FUNCTION IF EXISTS fhir.insert_{{fn_name}}(json, uuid, uuid, integer);
     CREATE OR REPLACE FUNCTION fhir.insert_{{fn_name}}(_data json, _logical_id uuid default null)
     RETURNS TABLE(path text[], value json, logical_id uuid, version_id uuid, id uuid, parent_id uuid) AS
     $fn$
        WITH {{ctes}}
        {{selects}};
     $fn$
     LANGUAGE sql;
  $SQL$,
   'fn_name', fhir.underscore(path[1]),
   'ctes',array_to_string(array_agg(cte order by path), E',\n'),
   'selects', array_to_string(array_agg(('SELECT * FROM _' || fhir.table_name(path)) order by path), E'\nUNION ALL\n')
  ) as ddl
 FROM insert_ctes
 GROUP BY path[1]
 HAVING path[1] <> 'Profile' -- fix profile
);

-- generate insert functions

SELECT 'create insert functions...', count(*)  FROM (
   SELECT meta.eval_function(ddl) FROM insert_ddls
) _;

CREATE OR REPLACE FUNCTION
fhir.insert_resource(_resource JSON, _logical_id UUID DEFAULT NULL)
RETURNS UUID AS
$BODY$
  DECLARE
    logical_id uuid;
    version_id uuid;
    r record;
    sql text;
  BEGIN
    EXECUTE fhir.eval_template($SQL$
      SELECT DISTINCT version_id
      FROM (
        SELECT version_id,
               meta.eval_insert(build_insert_statment(
                  fhir.table_name(path)::text, value, logical_id::text, version_id::text, id::text, parent_id::text))
        FROM fhir.insert_{{resource}}($1, $2)
        WHERE value IS NOT NULL
        ORDER BY path
      ) _;
      $SQL$, 'resource', fhir.underscore(_resource->>'resourceType'))
    INTO version_id USING _resource, _logical_id;

    EXECUTE fhir.eval_template($SQL$
      SELECT _logical_id
      FROM fhir.{{resource}}
      WHERE _version_id = $1;
      $SQL$, 'resource', fhir.underscore(_resource->>'resourceType'))
    INTO logical_id USING version_id;

    -- PERFORM build_tags(_resource->'category', version_id, logical_id);
    EXECUTE fhir.eval_template($$
        UPDATE fhir.{{table_name}} SET data = {{data}}::json WHERE _version_id = {{version_id}}::uuid
      $$,
      'table_name', fhir.table_name(array[_resource->>'resourceType']::varchar[]),
      'data', quote_literal(_resource),
      'version_id', quote_literal(version_id)
    );
    RETURN logical_id;
  END;
$BODY$
LANGUAGE plpgsql VOLATILE;
--}}}
