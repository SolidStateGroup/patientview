'use strict';

angular.module('patientviewApp').factory('SecurityService', ['$q', 'Restangular',
    function ($q, Restangular) {
        return {
            // when creating a user, based on my security access, get list of roles I can choose from
            getSecurityRolesByUser: function (userId) {
                var deferred = $q.defer();
                Restangular.all('security').one('user',userId).getList('roles').then(function(res) {
                    deferred.resolve(res);
                });
                return deferred.promise;
            },
            // when creating a user, based on my security access, after choosing a role, get list of available groups the user can be put in
            getSecurityGroupsByUserAndRole: function (userId, roleId) {
                var deferred = $q.defer();
                Restangular.all('security').one('user',userId).one('role',roleId).getList('groups').then(function(res) {
                    deferred.resolve(res);
                });
                return deferred.promise;
            }
        };
    }]);
