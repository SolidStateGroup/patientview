// not currently used

'use strict';

angular.module('patientviewApp').factory('FeatureService', ['$q', 'Restangular', function ($q, Restangular) {
    return {
        getAll: function () {
            var deferred = $q.defer();
            Restangular.all('feature').getList().then(function(res) {
                deferred.resolve(res);
            });
            return deferred.promise;
        },
        getAllStaffFeatures: function () {
            var deferred = $q.defer();
            Restangular.all('feature').getList({'type':'STAFF'}).then(function(res) {
                deferred.resolve(res);
            });
            return deferred.promise;
        },
        getAllPatientFeatures: function () {
            var deferred = $q.defer();
            Restangular.all('feature').getList({'type':'PATIENT'}).then(function(res) {
                deferred.resolve(res);
            });
            return deferred.promise;
        },
        getAllGroupFeatures: function () {
            var deferred = $q.defer();
            Restangular.all('feature').getList({'type':'GROUP'}).then(function(res) {
                deferred.resolve(res);
            });
            return deferred.promise;
        }
    };
}]);
