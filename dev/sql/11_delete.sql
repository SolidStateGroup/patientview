CREATE OR REPLACE
FUNCTION fhir.delete_resource(logical_id uuid)
  returns void
  language sql
  as $$
    UPDATE fhir.resource SET _state = 'deleted' WHERE _logical_id = delete_resource.logical_id;
  $$;
