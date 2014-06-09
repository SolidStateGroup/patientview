--create schema functions;
set search_path = fhir, pg_catalog;

CREATE OR REPLACE
FUNCTION underscore(str varchar)
  returns varchar
  language sql
  as $$
  SELECT
   lower(
    replace(
      regexp_replace(
        regexp_replace(str, '([a-z\d])([A-Z]+)', '\1_\2', 'g'),
        '[-\s]+', '_', 'g'),
      '.', '')); -- problem with path: {schedule,repeat} with type Schedule.repeat
$$ IMMUTABLE;

CREATE OR REPLACE
FUNCTION camelize(_str varchar)
  returns varchar
  language sql
  as $$
  select string_agg(replace((upper(substring(str from 1 for 1)) || substring(str from 2)), 'Xxx', ''), '') from unnest(string_to_array('xxx' || _str, '_')) as str;
$$ IMMUTABLE;

-- remove last item from array
CREATE OR REPLACE
FUNCTION array_pop(ar varchar[])
  RETURNS varchar[] language sql AS $$
    SELECT ar[array_lower(ar,1) : array_upper(ar,1) - 1];
$$ IMMUTABLE;

-- remove last item from array
CREATE OR REPLACE
FUNCTION array_tail(ar varchar[])
  RETURNS varchar[] language sql AS $$
    SELECT ar[2 : array_upper(ar,1)];
$$ IMMUTABLE;

CREATE OR REPLACE
FUNCTION array_last(ar varchar[])
  RETURNS varchar language sql AS $$
    SELECT ar[array_length(ar,1)];
$$ IMMUTABLE;


CREATE TABLE short_names (name varchar, alias varchar);
INSERT INTO short_names (name, alias)
VALUES
    ('capabilities', 'cap'),
    ('chanel', 'chnl'),
    ('codeable_concept', 'cc'),
    ('coding', 'cd'),
    ('identifier', 'idn'),
    ('immunization', 'imm'),
    ('immunization_recommendation', 'imm_rec'),
    ('location', 'loc'),
    ('medication', 'med'),
    ('medication_administration', 'med_adm'),
    ('medication_dispense', 'med_disp'),
    ('medication_prescription', 'med_prs'),
    ('medication_statement', 'med_st'),
    ('observation', 'obs'),
    ('prescription', 'prs'),
    ('recommentdaton', 'rcm'),
    ('resource_reference', 'res_ref'),
    ('value', 'val'),
    ('value_set', 'vs')
;

CREATE OR REPLACE
FUNCTION table_name(path varchar[])
RETURNS varchar AS $$
  SELECT string_agg(part, '_') FROM (
    SELECT fhir.underscore(coalesce(sn.alias, pth.unnest)) as part
      FROM (SELECT *, row_number() OVER () FROM unnest(path)) pth
      LEFT JOIN fhir.short_names sn
      ON sn.name = fhir.underscore(pth.unnest)
      ORDER BY row_number
    ) _;
$$ LANGUAGE sql IMMUTABLE;

CREATE OR REPLACE
FUNCTION resource_table_name(path varchar[])
RETURNS varchar AS $$
  SELECT fhir.table_name(ARRAY[path[1]]);
$$ LANGUAGE sql IMMUTABLE;

CREATE OR REPLACE
FUNCTION parent_table_name(path varchar[])
RETURNS varchar AS $$
  SELECT fhir.table_name(ARRAY[fhir.array_pop(path)]);
$$ LANGUAGE sql IMMUTABLE;

-- used for view sql generation
CREATE OR REPLACE FUNCTION indent(t text, l integer)
  RETURNS text LANGUAGE sql AS $$
    SELECT regexp_replace(t, '^', repeat('  ', l), 'gm');
$$;

CREATE OR REPLACE
FUNCTION column_name(name varchar, type varchar)
  RETURNS varchar language sql AS $$
    SELECT replace(name, '[x]', '_' || type);
$$  IMMUTABLE;

CREATE OR REPLACE
FUNCTION column_ddl(column_name varchar, pg_type varchar, min varchar, max varchar)
  RETURNS varchar LANGUAGE sql AS $$
    SELECT '"' || fhir.underscore(column_name) || '" ' || pg_type
      || case
        when max = '*' then '[]'
        else ''
      end
      || case
        when min = '1' then ' not null'
        else ''
      end;
$$ IMMUTABLE;

CREATE OR REPLACE FUNCTION
eval_template(_tpl text, variadic _bindings varchar[])
RETURNS text LANGUAGE plpgsql AS $$
DECLARE
result text := _tpl;
BEGIN
  FOR i IN 1..(array_upper(_bindings, 1)/2) LOOP
    result := replace(result, '{{' || _bindings[i*2 - 1] || '}}', _bindings[i*2]);
  END LOOP;
  RETURN result;
END
$$;

set search_path = public, pg_catalog;
