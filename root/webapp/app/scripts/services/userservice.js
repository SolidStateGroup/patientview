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
            delete: function (user) {
                var deferred = $q.defer();
                Restangular.one('user', user.id).get().then(function(user) {
                    user.remove().then(function(res) {
                        deferred.resolve(res);
                    });
                });
                return deferred.promise;
            },
            resetPassword: function (user) {
                var deferred = $q.defer();
                Restangular.one('user', user.id).get().then(function(user) {
                    user.put('reset').then(function(res) {
                        deferred.resolve(res);
                    });
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
            getStaffByGroup: function (groupId) {
                var deferred = $q.defer();
                //console.log(groupId);
                Restangular.one('group', groupId).getList('staff').then(function(res) {
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

                    var toInclude = ['username','email','fullname','groups','features'];

                    for (var name in input) {
                        if (input.hasOwnProperty(name) && _.contains(toInclude, name)) {
                            user[name] = input[name];
                        }
                    }

                    user.post().then(function(res) {
                        deferred.resolve(res);
                    });
                });
                return deferred.promise;
            },
            new: function (input) {
                var deferred = $q.defer();

                var toInclude = ['username','email','fullname','groups','features'];
                var user = {};

                for (var name in input) {
                    if (input.hasOwnProperty(name) && _.contains(toInclude, name)) {
                        user[name] = input[name];
                    }
                }

                Restangular.all('user').post(user).then(function(res) {
                    deferred.resolve(res);
                });
                return deferred.promise;
            }
        };
    }]);
