'use strict';

angular.module('patientviewApp').factory('CodeService', ['$q', 'Restangular',
    function ($q, Restangular) {
        return {
            getGroupCodes: function (groupId) {
                var deferred = $q.defer();
                Restangular.one('group',groupId).getList('codes').then(function(res) {
                    deferred.resolve(res);
                });
                return deferred.promise;
            },
            post: function (group, code) {
                var deferred = $q.defer();

                Restangular.one('group', group.id).post('codes',code).then(function(res) {
                    deferred.resolve(res);
                });
                return deferred.promise;
            }
        };
    }]);
