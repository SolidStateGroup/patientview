set search_path = fhir, pg_catalog;

CREATE VIEW meta.enums AS (
  SELECT  replace(datatype, '-list','')
      AS  enum
          ,array_agg(value)
      AS  options
    FROM  meta.datatype_enums
GROUP BY  replace(datatype, '-list','')
);

CREATE VIEW meta.primitive_types as (
  SELECT type
         ,pg_type
    FROM meta.type_to_pg_type
   UNION SELECT enum, 'fhir."' || enum  || '"'
    FROM meta.enums
);

CREATE VIEW meta._datatype_unified_elements AS (
  SELECT ARRAY[datatype, name]
      AS path
         ,type
         ,min_occurs
      AS min
         ,CASE when max_occurs = 'unbounded'
           THEN '*'
           ELSE max_occurs
          END
      AS max
    FROM meta.datatype_elements
   WHERE datatype <> 'Resource'
);

CREATE VIEW meta.datatype_unified_elements as (
  WITH RECURSIVE tree(
    path
    ,type
    ,min
    ,max
  ) AS (
    SELECT r.* FROM meta._datatype_unified_elements r
    UNION
    SELECT t.path || ARRAY[array_last(r.path)] as path,
           r.type as type,
           t.min as min,
           t.max as max
      FROM meta._datatype_unified_elements r
      JOIN tree t on t.type = r.path[1]
  )
  SELECT * FROM tree t LIMIT 1000
);

CREATE VIEW meta.unified_complex_datatype AS (
  SELECT   ue.path as path
           ,coalesce(tp.type, ue.path[1]) as type, tp.min, tp.max
     FROM  (
              SELECT array_pop(path)
                  AS path
                FROM meta.datatype_unified_elements
            GROUP BY array_pop(path)
           )
       AS  ue
LEFT JOIN meta.datatype_unified_elements tp
       ON tp.path = ue.path
);

CREATE VIEW meta.unified_datatype_columns AS (
  SELECT  dt.*
          ,pt.pg_type as pg_type
          ,column_ddl(array_last(dt.path) ,pt.pg_type, dt.min, dt.max)
      AS  column_ddl
    FROM  meta.datatype_unified_elements dt
    JOIN  meta.primitive_types pt
      ON  underscore(pt.type) = underscore(dt.type)
   WHERE  array_length(dt.path,1) = 2
);

CREATE VIEW meta.datatype_tables AS (
  SELECT  table_name(path)
      AS  table_name
          ,CASE WHEN array_length(path, 1) = 1
            THEN 'resource_component'
            ELSE table_name(ARRAY[type])
          END
      AS  base_table
          ,(
              SELECT coalesce(
                      array_agg(column_ddl),
                      ARRAY[]::varchar[]
                     )
                 FROM meta.unified_datatype_columns cls
                WHERE array_pop(cls.path) = cd.path
            )
      AS  columns
          ,*
    FROM  meta.unified_complex_datatype cd
ORDER BY  array_length(cd.path,1)
          ,table_name
);

set search_path = public, pg_catalog;
