'use strict';

angular.module('patientviewApp').factory('GroupService', ['$q', 'Restangular',
    function ($q, Restangular) {
        return {
            get: function (groupId) {
                var deferred = $q.defer();
                Restangular.one('group', groupId).get().then(function(res) {
                    deferred.resolve(res);
                });
                return deferred.promise;
            },
            getAll: function () {
                var deferred = $q.defer();
                Restangular.all('group').getList().then(function(res) {
                    deferred.resolve(res);
                });
                return deferred.promise;
            },
            getUsersByType: function (groupId, roleType) {
                var deferred = $q.defer();
                Restangular.one('group', groupId).all('user').getList({'roleType': roleType}).then(function(res) {
                    deferred.resolve(res);
                });
                return deferred.promise;
            }
        };
    }]);
