'use strict';

angular.module('patientviewApp').factory('ResultService', ['$q', 'Restangular',
    function ($q, Restangular) {
        return {
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
            }
        };
    }]);
