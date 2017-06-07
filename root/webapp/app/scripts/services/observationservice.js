'use strict';

angular.module('patientviewApp').factory('ObservationService', ['$q', 'Restangular', 'UtilService',
function ($q, Restangular, UtilService) {
    return {
        // Migration only
        startObservationMigration: function () {
            var deferred = $q.defer();
            Restangular.one('migrate/observationsfast').get().then(function(res) {
                deferred.resolve(res);
            });
            return deferred.promise;
        },
        getObservation: function (uuid, typeName) {
            var deferred = $q.defer();
            Restangular.one('patient',uuid).getList('observations', {type: typeName}).then(function(res) {
                deferred.resolve(res);
            });
            return deferred.promise;
        },
        getResultTypes: function (uuid) {
            var deferred = $q.defer();
            Restangular.one('patient',uuid).getList('resulttypes').then(function(res) {
                deferred.resolve(res);
            });
            return deferred.promise;
        },
        getByCode: function (userId, code) {
            var deferred = $q.defer();
            // GET /user/{userId}/observations/{code}
            Restangular.one('user', userId).one('observations').one(code).get().then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        getByCodePatientEntered: function (userId, code) {
            var deferred = $q.defer();
            // GET /user/{userId}/observations/{code}/patiententered
            Restangular.one('user', userId).one('observations').one(code).one('patiententered').get().then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        getHomeDialysisResults: function (userId) {
            var deferred = $q.defer();
            // GET /user/{userId}/observations/patiententered/home-dialysis
            Restangular.one('user', userId).one('observations').one('patiententered').one('home-dialysis').get().then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        getByCodes: function (userId, codes, limit, offset, orderDirection) {
            var deferred = $q.defer();
            // GET /user/{userId}/observations
            Restangular.one('user', userId).one('observations')
                .get({'code':codes, 'limit':limit, 'offset':offset, 'orderDirection':orderDirection})
                .then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        getSummary: function (userId) {
            var deferred = $q.defer();
            // GET /user/{userId}/observations/summary
            Restangular.one('user', userId).one('observations').one('summary').get().then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        saveResultClusters: function (userId, resultClusters, isDialysis) {
            var toSend = [];

            if(!isDialysis){
                for (var i=0;i<resultClusters.length;i++) {
                    var userResultCluster = _.clone(resultClusters[i]);
                    var values = [];                 

                    for (var key in userResultCluster.values) {
                        if (userResultCluster.values.hasOwnProperty(key)) {
                            values.push({'id':key, 'value':userResultCluster.values[key]});
                        }
                    }

                    userResultCluster.values = values;
                    toSend.push(UtilService.cleanObject(userResultCluster, 'resultCluster'));
                }

                var deferred = $q.defer();
                // POST /user/{userId}/observations/resultclusters
                Restangular.one('user', userId).one('observations').one('resultclusters').customPOST(toSend)
                    .then(function(successResult) {
                    deferred.resolve(successResult);
                }, function(failureResult) {
                    deferred.reject(failureResult);
                });
                return deferred.promise;
            }else{
                var userResultCluster = _.clone(resultClusters[0]);
                var deferred = $q.defer();

               var cleanedObj = UtilService.cleanObject(userResultCluster, 'dialysisTreatment');

                // POST /user/{userId}/observations/resultclusters/custom
                Restangular.one('user', userId).one('observations').one('resultclusters').one('custom').customPOST(cleanedObj)
                    .then(function(successResult) {
                    deferred.resolve(successResult);
                }, function(failureResult) {
                    deferred.reject(failureResult);
                });
                return deferred.promise;
            }
            
        },        
        saveResultCluster: function (adminId, userId, resultCluster) {

            var date = new Date(resultCluster.year, resultCluster.month - 1, resultCluster.day, resultCluster.hour, resultCluster.minute, 0, 0);

            var observation = {};
            observation.applies = date;
            observation.logicalId = resultCluster.logicalId;
            observation.name = resultCluster.name;
            observation.value = resultCluster.value;
            observation.group = resultCluster.group;

            var deferred = $q.defer();
            // POST /user/{userId}/observations/patiententered
            Restangular.one('user', userId).one('observations').one('patiententered').customPOST(observation, "?adminId="+adminId)
                .then(function (successResult) {
                    deferred.resolve(successResult);
                }, function (failureResult) {
                    deferred.reject(failureResult);
                });
            return deferred.promise;

        },
        // Remove a single result based on userId and uuid
        remove: function (adminId, userId, observation) {
            var deferred = $q.defer();
            // DELETE /user/{userId}/observations/{uuid}
            Restangular.one('user', userId).one('observations', observation.logicalId).remove({"adminId":adminId})
                .then(function (successResult) {
                    deferred.resolve(successResult);
                }, function (failureResult) {
                    deferred.reject(failureResult);
                });
            return deferred.promise;
        }

    };
}]);
