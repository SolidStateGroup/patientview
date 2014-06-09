CREATE OR REPLACE FUNCTION fhir.update_resource(id uuid, resource_data json)
RETURNS integer LANGUAGE plpgsql AS $$
BEGIN
  IF NOT EXISTS(select 1 FROM fhir.resource WHERE _state = 'current' and _logical_id = update_resource.id) THEN
    RAISE EXCEPTION 'Resource with id % not found', id;
  ELSE
    UPDATE fhir.resource SET _state = 'archived' WHERE _state = 'current' and _logical_id = update_resource.id;
    PERFORM fhir.insert_resource(resource_data, id);
  END IF;
  RETURN 0::integer;
END
$$;
