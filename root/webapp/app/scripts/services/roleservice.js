'use strict';

angular.module('patientviewApp').factory('RoleService', ['$q', 'Restangular',
    function ($q, Restangular) {
        return {
            getAll: function () {
                var deferred = $q.defer();
                Restangular.all('role').getList().then(function(res) {
                    deferred.resolve(res);
                });
                return deferred.promise;
            },
            getByType: function (roleType) {
                var deferred = $q.defer();
                Restangular.all('role').getList({'type':roleType}).then(function(res) {
                    deferred.resolve(res);
                });
                return deferred.promise;
            }
        };
    }]);
