var express = require('express');
var bodyParser = require('body-parser')
var handlers = require('./handlers/handlers.js');

var app = express();

app.use(bodyParser());

app.all('/*', function(req, res, next) {
    res.header("Access-Control-Allow-Origin", "*");
    res.header("Access-Control-Allow-Headers", "X-Requested-With");
    next();
});
app.all('/user', function(req, res) {
    res.redirect('http://diabetes-pv.dev.solidstategroup.com/api' + req.url);
    return;
});
app.all('/group*', function(req, res) {
    res.redirect('http://diabetes-pv.dev.solidstategroup.com/api' + req.url);
    return;
});
app.all('/features*', function(req, res) {
    res.redirect('http://diabetes-pv.dev.solidstategroup.com/api' + req.url);
    return;
});
app.all('/roles*', function(req, res) {
    res.redirect('http://diabetes-pv.dev.solidstategroup.com/api' + req.url);
    return;
});
app.post('/resource', handlers.handle_resource);
app.get('/resource', handlers.handle_test);
app.get('/patient/:uuid/observations', handlers.handle_patient_observations);
app.get('/patient/:uuid', handlers.handle_patient_by_id);
app.get('/patient*', handlers.handle_patient_by_nhs_number);
app.all('*', function(req, res) {
    res.redirect('http://patientview201.apiary-mock.com' + req.url);
    return;
});

app.listen(8082);