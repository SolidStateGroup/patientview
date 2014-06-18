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
            save: function (inputUser) {
                var deferred = $q.defer();

                // clean user object
                var user = {};
                var userFields = ['id', 'username', 'password', 'email', 'name', 'groupRoles', 'changePassword', 'locked'];
                for (var field in inputUser) {
                    if (inputUser.hasOwnProperty(field) && _.contains(userFields, field)) {
                        user[field] = inputUser[field];
                    }
                }

                // PUT
                Restangular.all('user').customPUT(user).then(function(successResult) {
                    deferred.resolve(successResult);
                }, function(failureResult) {
                    deferred.reject(failureResult);
                });

                return deferred.promise;
            },
            new: function (inputUser) {
                var deferred = $q.defer();

                var userFields = ['username','email','name','groupRoles'];
                inputUser.groupRoles = [];

                for (var i=0;i<inputUser.groups.length;i++) {
                    var inputGroup = inputUser.groups[i];
                    var groupRole = {};

                    // clean role
                    var role = {}, roleFields = ['id','name','description','routes'];
                    for (var field in inputGroup.role) {
                        if (inputGroup.role.hasOwnProperty(field) && _.contains(roleFields, field)) {
                            role[field] = inputGroup.role[field];
                        }
                    }
                    groupRole.role = role;

                    // clean group
                    var group = {}, groupFields = ['id','name','code','description','groupType','groupFeatures','routes'];
                    for (var field in inputGroup) {
                        if (inputGroup.hasOwnProperty(field) && _.contains(groupFields, field)) {
                            group[field] = inputGroup[field];
                        }
                    }
                    groupRole.group = group;

                    inputUser.groupRoles.push(groupRole);
                }

                var user = {};

                for (var name in inputUser) {
                    if (inputUser.hasOwnProperty(name) && _.contains(userFields, name)) {
                        user[name] = inputUser[name];
                    }
                }

                user.groupRoles = inputUser.groupRoles;
                user.password = 'password';
                user.changePassword = 'false';
                user.locked = 'false';

                Restangular.all('user').post(user).then(function(successResult) {
                    deferred.resolve(successResult);
                }, function(failureResult) {
                    deferred.reject(failureResult);
                });
                return deferred.promise;
            }
        };
    }]);
