'use strict';

angular.module('patientviewApp').factory('UserService', ['$q', 'Restangular',
    function ($q, Restangular) {
        return {
            get: function (userId) {
                var deferred = $q.defer();
                Restangular.one('user', userId).get().then(function(res) {
                    deferred.resolve(res);
                });
                return deferred.promise;
            },
            getFeatures: function (userId) {
                var deferred = $q.defer();
                Restangular.one('user', userId).getList('features').then(function(res) {
                    deferred.resolve(res);
                });
                return deferred.promise;
            }
        };
    }]);
