'use strict';

angular.module('patientviewApp').factory('AuthService', ['$q', 'Restangular',
    function ($q, Restangular) {
    return {
        login: function (usernameAndPassword) {
            var deferred = $q.defer();
            Restangular.all('auth/login').post(usernameAndPassword).then(function(res) {
                deferred.resolve(res);
            }, function(res) {
                deferred.reject(res);
            });
            return deferred.promise;
        },
        getUserInformation: function (token) {
            var deferred = $q.defer();
            Restangular.one('auth', token).customGET('userinformation').then(function(res) {
                deferred.resolve(res);
            }, function(res) {
                deferred.reject(res);
            });
            return deferred.promise;
        },
        // Change user's password, sets the change flag to false
        forgottenPassword: function (usernameAndEmail) {
            var deferred = $q.defer();
            Restangular.all('auth/forgottenpassword').post(usernameAndEmail).then(function(res) {
                deferred.resolve(res);
            }, function(res) {
                deferred.reject(res);
            });
            return deferred.promise;
        },
        logout: function (token) {
            var deferred = $q.defer();
            Restangular.all('auth/logout').customDELETE(token).then(function(successResult) {
                deferred.resolve(successResult);
            }, function (failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        // switch from current user to another based on userId
        switchUser: function (userId, token) {
            var deferred = $q.defer();
            if (token) {
                // POST /auth/{token}/switchuser/{userId}
                Restangular.one('auth', token).one('switchuser', userId).get().then(function (successResult) {
                    deferred.resolve(successResult);
                }, function (failureResult) {
                    deferred.reject(failureResult);
                });
            } else {
                // POST /auth/switchuser/{userId}
                Restangular.one('auth').one('switchuser', userId).get().then(function (successResult) {
                    deferred.resolve(successResult);
                }, function (failureResult) {
                    deferred.reject(failureResult);
                });
            }
            return deferred.promise;
        }
    };
}]);
