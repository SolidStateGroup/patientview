var http = require('http');
var express = require('express');
var bodyParser = require('body-parser')
var handlers = require('./handlers/handlers.js');
var cors = require('cors');
var app = express();

// Enables CORS
var enableCORS = function(req, res, next) {
    res.header('Access-Control-Allow-Origin', '*');
    res.header('Access-Control-Allow-Methods', 'GET,PUT,POST,DELETE,OPTIONS');
    res.header('Access-Control-Allow-Headers', 'Content-Type, Authorization, Content-Length, X-Requested-With');

    // intercept OPTIONS method
    if ('OPTIONS' == req.method) {
        res.send(200);
    }
    else {
        next();
    }
};

app.use(enableCORS);

app.use(bodyParser());

app.all('/api/user*', function(req, res) {
    var options = {
        host: 'diabetes-pv.dev.solidstategroup.com',
        path: req.url,
        port: '80',
        method: req.method
    };

    callback = function(response) {
        var str = ''
        response.on('data', function (chunk) {
            str += chunk;
        });

        response.on('end', function () {
            res.end(str);
        });
    }

    req = http.request(options, callback);
    req.end();
    return;
});
app.all('/api/group*', function(req, res) {
    var options = {
        host: 'diabetes-pv.dev.solidstategroup.com',
        path: req.url,
        port: '80',
        method: req.method
    };

    callback = function(response) {
        var str = ''
        response.on('data', function (chunk) {
            str += chunk;
        });

        response.on('end', function () {
            res.end(str);
        });
    }

    req = http.request(options, callback);
    req.end();
    return;
});
app.all('/api/feature*', function(req, res) {
    var options = {
        host: 'patientview201.apiary-mock.com',
        path: req.url,
        port: '80',
        method: res.method
    };

    callback = function(response) {
        var str = ''
        response.on('data', function (chunk) {
            str += chunk;
        });

        response.on('end', function () {
            res.end(str);
        });
    }

    req = http.request(options, callback);
    req.end();

    return;
});
app.all('/api/roles*', function(req, res) {
    var options = {
        host: 'diabetes-pv.dev.solidstategroup.com',
        path: req.url,
        port: '80',
        method: req.method
    };

    callback = function(response) {
        var str = ''
        response.on('data', function (chunk) {
            str += chunk;
        });

        response.on('end', function () {
            res.end(str);
        });
    }

    req = http.request(options, callback);
    req.end();
    return;
});
app.all('/api/auth*', function(req, res) {
    var options = {
        host: 'diabetes-pv.dev.solidstategroup.com',
        path: req.url,
        port: '80',
        method: req.method
    };

    callback = function(response) {
        var str = ''
        response.on('data', function (chunk) {
            str += chunk;
        });

        response.on('end', function () {
            res.end(str);
        });
    }

    req = http.request(options, callback);
    req.end();
    return;
});
app.post('/api/resource', handlers.handle_resource);
app.get('/api/resource', handlers.handle_test);
app.get('/api/patient/:uuid/observations', handlers.handle_patient_observations);
app.get('/api/patient/:uuid', handlers.handle_patient_by_id);
app.get('/api/patient*', handlers.handle_patient_by_nhs_number);
app.all('*', function(req, res) {
    var options = {
        host: 'patientview201.apiary-mock.com',
        path: req.url,
        port: '80',
        method: req.method
    };

    callback = function(response) {
        var str = ''
        response.on('data', function (chunk) {
            str += chunk;
        });

        response.on('end', function () {
            res.end(str);
        });
    }

    req = http.request(options, callback);
    req.end();

    return;
});

app.listen(7865);