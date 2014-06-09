var utilities = require('../utilities/utilities.js');
var pg = require('pg');
var conString = "postgres://eatek:eatek@localhost/mydb";

function execute_query(sql, param, callback) {

    console.error(JSON.stringify(param));

    var client = new pg.Client(conString);
    client.connect(function (err) {

        if (err) {
            return console.error('could not connect to postgres', err);
        }
        client.query(sql, param,
            function (err, result) {
                client.end();

                if (err) {
                    console.error('error running query', err);
                    callback(err, null);
                } else {
                    callback(err, result);
                }

            });
    });
    return;
}

function get_patient_by_id(uuid, callback) {

    var sql = "SELECT ptt.data " +
              "FROM   fhir.patient_idn idn " +
              ",      fhir.patient ptt " +
              "WHERE  ptt._version_id = idn._version_id " +
              "AND    ptt._logical_id = $1";

    execute_query(sql, [uuid], callback);
};



function get_patient_by_nhs_number(nhs_number, callback) {

    var sql =   "SELECT ptt.data " +
                "FROM   fhir.patient_idn idn " +
                ",      fhir.patient ptt " +
                "WHERE  ptt._version_id = idn._version_id " +
                "AND    idn.label = 'nhsno' " +
                "AND    idn.value = $1";

    execute_query(sql, [nhs_number], callback);
};


function get_patient_observations_by_name(uuid, name, callback) {

    var sql = "SELECT  ob.data " +
        "FROM    fhir.obs_subject sub " +
        ",       fhir.obs ob " +
        ",       fhir.obs_name obn " +
        "WHERE   ob._version_id = sub._version_id " +
        "AND     obn._version_id = ob._version_id " +
        "AND     sub.display = $1 " +
        "AND     obn.text = $2 "

    execute_query(sql, [uuid, name], callback);
};


function get_patient_observations(uuid, callback) {

    var sql = "SELECT  ob.data " +
              "FROM    fhir.obs_subject sub " +
              ",       fhir.obs ob " +
              "WHERE   ob._version_id = sub._version_id " +
              "AND     sub.display = $1"

    execute_query(sql, [uuid], callback);
};

function load_resource(req, callback) {

    var sql = "SELECT fhir.insert_resource($1::json)";
    console.log(JSON.stringify(req.body));
    execute_query(sql,JSON.stringify(req.body), callback);

};

var self = {

    handle_test: function (req, res) {
        console.log("Manage to handle");
        utilities.send_success(res, "Test Success");
    },

    handle_resource: function (req, res) {
        load_resource(req, function (err, resource) {

            if (err) {
                console.log(err);
                utilities.send_failure(res, 500, err);
                return console.error('error running query', err);
            } else {
                console.log('Request successful ' + JSON.stringify(resource.rows[0]));
            }

            utilities.send_success(res, resource.rows[0]);
        });
    },

    handle_patient_by_id: function (req, res) {

        console.log(JSON.stringify(req.params));

        var uuid = req.params.uuid;

        get_patient_by_id(uuid, function (err, patient) {

            if (err) {
                console.log(err);
                utilities.send_failure(res, 500, err);
                return console.error('error running query', err);
            } else {
                console.log('Request successful ' + JSON.stringify(patient.rows[0]));
            }

            utilities.send_success(res, patient.rows[0].data);
        });
    },

    handle_patient_by_nhs_number: function (req, res) {

        console.log(JSON.stringify(req.params));

        var nhs_number = req.query.nhsnumber;

        get_patient_by_nhs_number(nhs_number, function (err, patient) {

            if (err) {
                console.log(err);
                utilities.send_failure(res, 500, err);
                return console.error('error running query', err);
            } else {
                console.log('Request successful ' + JSON.stringify(patient.rows[0]));
            }

            utilities.send_success(res, patient.rows[0]);
        });
    },

    handle_patient_observations: function (req, res) {

        var uuid = req.params.uuid;

        if (req.query.type) {
            get_patient_observations_by_name(uuid, req.query.type, function (err, observations) {

                if (err) {
                    console.log(err);
                    utilities.send_failure(res, 500, err);
                    return console.error('error running query', err);
                } else {
                    console.log('Request successful ' + JSON.stringify(observations.rows));
                }

                utilities.send_success(res, observations.rows);
            });
        } else {
            get_patient_observations(uuid, function (err, observations) {

                if (err) {
                    console.log(err);
                    utilities.send_failure(res, 500, err);
                    return console.error('error running query', err);
                } else {
                    console.log('Request successful ' + JSON.stringify(observations.rows));
                }

                utilities.send_success(res, observations.rows);
            });
        }
    },

};

module.exports = self;
