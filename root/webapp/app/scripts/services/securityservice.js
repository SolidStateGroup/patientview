'use strict';

angular.module('patientviewApp').factory('SecurityService', ['$q', 'Restangular',
    function ($q, Restangular) {
        return {
            getAvailableGroupsFromUserAndRole: function (userId, roleId) {
                var deferred = $q.defer();
                Restangular.all('security').one('user',userId).one('role',roleId).getList('groups').then(function(res) {
                    deferred.resolve(res);
                });
                return deferred.promise;
            }
        };
    }]);
