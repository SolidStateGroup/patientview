/*
To be run against fhir database. To show existing materialized views:
  SELECT oid::regclass::TEXT
  FROM   pg_class
  WHERE  relkind = 'm';
 */
CREATE MATERIALIZED VIEW mve_pv_usage_obs AS
    SELECT
      (((observation.content -> 'subject'::text) ->> 'display'::text)) as id,
      MAX((observation.content ->> 'appliesDateTime')) as latest_test
   FROM
     observation
   GROUP BY
    (((observation.content -> 'subject'::text) ->> 'display'::text))
WITH DATA;

CREATE MATERIALIZED VIEW mve_pv_usage_transplant_status AS
    SELECT
      (((encounter.content -> 'subject'::text) ->> 'display'::text)) as id,
      (((encounter.content -> 'type'::text) ->0) ->> 'text'::text) as transplant_status
    FROM
      encounter
    WHERE
        (((encounter.content -> 'identifier'::text) -> 0) ->> 'value'::text) = 'TRANSPLANT_STATUS_KIDNEY'
WITH DATA;

CREATE MATERIALIZED VIEW mve_pv_usage_modality_status AS
    SELECT
      (((encounter.content -> 'subject'::text) ->> 'display'::text)) as id,
      (((encounter.content -> 'type'::text) ->0) ->> 'text'::text) as current_modality
    FROM
      encounter
    WHERE
        (((encounter.content -> 'identifier'::text) -> 0) ->> 'value'::text) = 'TREATMENT'
WITH DATA;

CREATE MATERIALIZED VIEW mve_pv_usage_diagnosis AS
    SELECT
      (((condition.content -> 'subject'::text) ->> 'display'::text)) as id,
      ((condition.content -> 'code'::text) ->> 'text'::text) as current_diagnosis
    FROM
      condition
    WHERE
        ((condition.content -> 'category'::text) ->> 'text'::text) = 'DIAGNOSIS'
WITH DATA;

CREATE MATERIALIZED VIEW mve_pv_usage_diagnosis_edta AS
    SELECT
      (((condition.content -> 'subject'::text) ->> 'display'::text)) as id,
      ((condition.content -> 'code'::text) ->> 'text'::text) as current_diagnosis_edta
    FROM
      condition
    WHERE
        ((condition.content -> 'category'::text) ->> 'text'::text) = 'DIAGNOSIS_EDTA'
WITH DATA;
