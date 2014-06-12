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
            },
            getByRole: function (roleId) {
                var deferred = $q.defer();
                Restangular.all('user').getList({role: roleId[0]}).then(function(res) {
                    deferred.resolve(res);
                });
                return deferred.promise;
            },
            save: function (input) {
                var deferred = $q.defer();
                Restangular.one('user', input.id).get().then(function(user) {

                    var toUpdate = ['username','email','groups','features'];

                    for (var name in input) {
                        if (input.hasOwnProperty(name) && _.contains(toUpdate, name)) {
                            user[name] = input[name];
                        }
                    }

                    user.post().then(function(res) {
                        deferred.resolve(res);
                    });
                });
                return deferred.promise;
            }
        };
    }]);
