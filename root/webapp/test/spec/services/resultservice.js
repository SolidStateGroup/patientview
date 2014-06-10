'use strict';

describe('Service: ResultsService', function () {

    // load the service's module
    beforeEach(module('patientviewApp'));

    // instantiate service
    var ResultService,
        httpBackend,
        scope;

    beforeEach(inject(function (_$httpBackend_, _ResultService_, $rootScope) {
        ResultService = _ResultService_;
        httpBackend = _$httpBackend_;
        scope = $rootScope.$new();

        var url1 = '/api/patient/1/observations?type=calcium';
        var url1a = 'http://patientview201.apiary-mock.com/patient/1/observations?type=calcium';
        var response1 = [
            {
                'data': {
                    'resourceType': 'Observation',
                    'name': {
                        'text': 'adjustedcalcium'
                    },
                    'valueQuantity': {
                        'value': 1.98
                    },
                    'issued': '2012-01-26T00:00:00-00:00',
                    'status': 'preliminary',
                    'reliability': 'ok',
                    'subject': {
                        'reference': 'patientUuid',
                        'display': 'c26c145e-46bf-4e7e-bbcb-81457f93bcec'
                    }
                }
            },
            {
                'data': {
                    'resourceType': 'Observation',
                    'name': {
                        'text': 'adjustedcalcium'
                    },
                    'valueQuantity': {
                        'value': 2.24
                    },
                    'issued': '2012-01-28T00:00:00-00:00',
                    'status': 'preliminary',
                    'reliability': 'ok',
                    'subject': {
                        'reference': 'patientUuid',
                        'display': 'c26c145e-46bf-4e7e-bbcb-81457f93bcec'
                    }
                }
            },
            {
                'data': {
                    'resourceType': 'Observation',
                    'name': {
                        'text': 'adjustedcalcium'
                    },
                    'valueQuantity': {
                        'value': 2.25
                    },
                    'issued': '2012-01-30T15:10:00-00:00',
                    'status': 'preliminary',
                    'reliability': 'ok',
                    'subject': {
                        'reference': 'patientUuid',
                        'display': 'c26c145e-46bf-4e7e-bbcb-81457f93bcec'
                    }
                }
            },
            {
                'data': {
                    'resourceType': 'Observation',
                    'name': {
                        'text': 'adjustedcalcium'
                    },
                    'valueQuantity': {
                        'value': 2.7
                    },
                    'issued': '2012-02-02T00:00:00-00:00',
                    'status': 'preliminary',
                    'reliability': 'ok',
                    'subject': {
                        'reference': 'patientUuid',
                        'display': 'c26c145e-46bf-4e7e-bbcb-81457f93bcec'
                    }
                }
            },
            {
                'data': {
                    'resourceType': 'Observation',
                    'name': {
                        'text': 'adjustedcalcium'
                    },
                    'valueQuantity': {
                        'value': 2.57
                    },
                    'issued': '2012-02-04T14:00:00-00:00',
                    'status': 'preliminary',
                    'reliability': 'ok',
                    'subject': {
                        'reference': 'patientUuid',
                        'display': 'c26c145e-46bf-4e7e-bbcb-81457f93bcec'
                    }
                }
            },
            {
                'data': {
                    'resourceType': 'Observation',
                    'name': {
                        'text': 'adjustedcalcium'
                    },
                    'valueQuantity': {
                        'value': 2.41
                    },
                    'issued': '2012-02-04T16:45:00-00:00',
                    'status': 'preliminary',
                    'reliability': 'ok',
                    'subject': {
                        'reference': 'patientUuid',
                        'display': 'c26c145e-46bf-4e7e-bbcb-81457f93bcec'
                    }
                }
            },
            {
                'data': {
                    'resourceType': 'Observation',
                    'name': {
                        'text': 'adjustedcalcium'
                    },
                    'valueQuantity': {
                        'value': 2.67
                    },
                    'issued': '2012-02-06T12:00:00-00:00',
                    'status': 'preliminary',
                    'reliability': 'ok',
                    'subject': {
                        'reference': 'patientUuid',
                        'display': 'c26c145e-46bf-4e7e-bbcb-81457f93bcec'
                    }
                }
            }
        ];

        var url2 = '/api/patient/1/resulttypes';
        var url2a = 'http://patientview201.apiary-mock.com/patient/1/resulttypes';
        var response2 = [
            {
                'data': {
                    'type': 'Observation',
                    'name': {
                        'text': 'adjustedcalcium'
                    },
                    'color': '#fe1100'
                }
            },
            {
                'data': {
                    'type': 'Observation',
                    'name': {
                        'text': 'calcium'
                    },
                    'color': '#0011fe'
                }
            }
        ];

        httpBackend.whenGET(url1).respond(response1);
        httpBackend.whenGET(url1a).respond(response1);
        httpBackend.whenGET(url2).respond(response2);
        httpBackend.whenGET(url2a).respond(response2);

        //http://stackoverflow.com/questions/15927919/using-ngmock-to-simulate-http-calls-in-service-unit-tests
        //http://stackoverflow.com/questions/14761045/jasmine-tests-angularjs-directives-with-templateurl
        httpBackend.whenGET('views/main.html').respond('');
    }));

    afterEach(function () {
        httpBackend.verifyNoOutstandingExpectation();
        httpBackend.verifyNoOutstandingRequest();
    });

    it('should do something', function () {
        expect(!!ResultService).toBe(true);
        httpBackend.flush();
    });

    it('should get observations for uuid and typeName ', function () {
        var prom = ResultService.getObservation('1','calcium');
        var result;
        prom.then(function (pr) {
            result = pr;
        });
        httpBackend.flush();

        expect(result[0].data.resourceType).toEqual('Observation');
    });

    it('should get result types ', function () {
        var prom = ResultService.getResultTypes('1');
        var result;
        prom.then(function (pr) {
            result = pr;
        });
        httpBackend.flush();

        expect(result[0].data.type).toEqual('Observation');
    });
});
