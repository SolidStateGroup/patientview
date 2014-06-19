'use strict';

angular.module('patientviewApp').factory('SecurityService', ['$q', 'Restangular',
    function ($q, Restangular) {
        return {
            getSecurityRolesByUser: function (userId, roleId) {
                var deferred = $q.defer();
                Restangular.all('security').one('user',userId).getList('roles').then(function(res) {
                    deferred.resolve(res);
                });
                return deferred.promise;
            },
            getSecurityGroupsByUserAndRole: function (userId, roleId) {
                var deferred = $q.defer();
                Restangular.all('security').one('user',userId).one('role',roleId).getList('groups').then(function(res) {
                    deferred.resolve(res);
                });
                return deferred.promise;
            }
        };
    }]);
