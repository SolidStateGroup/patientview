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
            },
            save: function (inputCode) {
                var deferred = $q.defer();
                Restangular.one('code', inputCode.id).get().then(function(code) {
                    code.type = inputCode.type;
                    code.standard = inputCode.standard;
                    code.number = inputCode.number;
                    code.description = inputCode.description;
                    code.links = inputCode.links;

                    code.post().then(function(res) {
                        deferred.resolve(res);
                    })
                });
                return deferred.promise;
            }
        };
    }]);
