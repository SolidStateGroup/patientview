-- мы хотим знать среднее время вставки одного ресурса
-- мы хотим его сравнить со временем одного инсерта как json

-- мы хотим знать, зависит ли время выборки жсона из вьюх от кол-ва
-- существующих ресурсов.

-- мы хотим знать, насколько замедляют выборку жсона контейнеды

\set pt_json `cat ./data/patient.json`
\set pt_num 10000

CREATE OR REPLACE FUNCTION bench_run(pt_num integer, pt_json text)
RETURNS void
LANGUAGE plpythonu as $$
  import json;
  import timeit;
  import copy;

  def log(x):
    plpy.notice(x)

  def insert_fhir_patients(d):
    plpy.execute('SELECT fhir.insert_resource(\'%s\'::json) FROM generate_series(1, %d)' % (d, pt_num))

  def select_fhir_patients():
    plpy.execute('SELECT * FROM fhir.view_patient')

  def select_json_patients():
    plpy.execute('SELECT * FROM bench_json_patients')

  def insert_json_patients(d):
    plpy.execute('INSERT INTO bench_json_patients SELECT uuid_generate_v4() as "id", \'%s\'::json as "json" FROM generate_series(1, %d)' % (d, pt_num))

  def bench(data):
    sdata = json.dumps(data)
    log('INSERTING DATA')
    log("numer of inserts in all tests: %d" % (pt_num))

    fhir_insert_time = timeit.timeit(lambda: insert_fhir_patients(sdata), number=1)

    log("fhir.insert_resource(): %f s" % (fhir_insert_time))
    log("mean time per 1 fhir.insert_resource(): %f ms" % (fhir_insert_time / pt_num * 1000))
    log('')

    json_insert_time = timeit.timeit(lambda: insert_json_patients(sdata), number=1)

    log("inset resource as JSON column: %f s" % (json_insert_time))
    log("mean time per 1 JSON insert: %f ms" % (json_insert_time / pt_num * 1000))
    log("fhir.insert_resource() / JSON insert: %f" % (fhir_insert_time / json_insert_time))

    log('------------------')
    log('SELECTING DATA')

    fhir_select_time = timeit.timeit(lambda: select_fhir_patients(), number=1)
    log("select * from fhir.view_patient: %f s" % (fhir_select_time))

    json_select_time = timeit.timeit(lambda: select_json_patients(), number=1)
    log("select * from bench_json_patients: %f s" % (json_select_time))

    log("fhir.view_patient / plain json: %f" % (fhir_select_time / json_select_time))

  data_with_containeds = json.loads(pt_json)
  data_without_containeds = copy.deepcopy(data_with_containeds)
  del data_without_containeds['contained']

  log("WITH CONTAINEDS:")
  bench(data_with_containeds)

  log('')
  log('')
  log("WITHOUT CONTAINEDS:")
  bench(data_without_containeds)
$$;

CREATE TABLE IF NOT EXISTS bench_json_patients (
  id uuid,
  content json
);

TRUNCATE TABLE bench_json_patients;


-- get rid of CONTEXT messages
\set VERBOSITY 'terse'
BEGIN;

SELECT bench_run(:pt_num, :'pt_json');
ROLLBACK;
