'use strict';

angular.module('patientviewApp').factory('UserService', ['$q', 'Restangular', 'UtilService',
function ($q, Restangular, UtilService) {
    return {
        // Get a single user based on userId
        get: function (userId) {
            var deferred = $q.defer();
            // GET /user/{userId}
            Restangular.one('user', userId).get().then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        // gets staff by groups, api assumes staff if no role IDs passed in
        getStaffByGroups: function (groupIds) {
            var deferred = $q.defer();
            // GET /user?groupId=1&groupId=2 etc
            Restangular.all('user').getList({'groupId':groupIds}).then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        // Remove a single user based on userId
        delete: function (user) {
            var deferred = $q.defer();
            // GET then DELETE /user/{userId}
            Restangular.one('user', user.id).get().then(function(user) {
                user.remove().then(function(res) {
                    deferred.resolve(res);
                });
            });
            return deferred.promise;
        },
        // Reset user's password
        resetPassword: function (user) {
            var deferred = $q.defer();
            var generatedPassword = UtilService.generatePassword();
            // POST /user/{userId}/resetPassword
            Restangular.one('user', user.id).post('resetPassword', {'password':generatedPassword}).then(function(successResult) {
                deferred.resolve(successResult);
                successResult.password = generatedPassword;
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        // Send user a verification email
        sendVerificationEmail: function (user) {
            var deferred = $q.defer();
            // POST
            Restangular.one('user', user.id).post('sendVerificationEmail').then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        // Verify user based on userId and verificationCode
        verify: function (userId, verificationCode) {
            var deferred = $q.defer();
            // POST
            Restangular.one('user', userId).all('verify').all(verificationCode).post().then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        // Save existing user
        save: function (inputUser) {
            var deferred = $q.defer();

            // clean user object
            var user = UtilService.cleanObject(inputUser, 'user');

            // clean group roles (clean role and group then add to groupRoles)
            user.groupRoles = [];
            for (var i=0;i<inputUser.groupRoles.length;i++) {
                var inputGroupRole = inputUser.groupRoles[i];
                var role = UtilService.cleanObject(inputGroupRole.role, 'role');
                var group = UtilService.cleanObject(inputGroupRole.group, 'group');
                user.groupRoles.push({'group':group,'role':role});
            }

            // clean user features
            var cleanUserFeatures = [];
            for (var j=0;j<inputUser.userFeatures.length;j++) {
                var userFeature = inputUser.userFeatures[j];
                var feature = {'id':userFeature.feature.id,'name':userFeature.feature.name,'description':''};
                cleanUserFeatures.push({'feature':feature});
            }
            user.userFeatures = cleanUserFeatures;

            // PUT /user
            Restangular.all('user').customPUT(user).then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });

            return deferred.promise;
        },
        // Create new user
        new: function (inputUser) {
            var deferred = $q.defer();

            // clean group roles (clean role and group then add to groupRoles)
            inputUser.groupRoles = [];
            for (var i=0;i<inputUser.groups.length;i++) {
                var inputGroup = inputUser.groups[i];
                var role = UtilService.cleanObject(inputGroup.role, 'role');
                var group = UtilService.cleanObject(inputGroup, 'group');
                inputUser.groupRoles.push({'group':group,'role':role});
            }

            // clean user features
            var cleanUserFeatures = [];
            for (var j=0;j<inputUser.userFeatures.length;j++) {
                var userFeature = inputUser.userFeatures[j];
                var feature = {'id':userFeature.feature.id,'name':userFeature.feature.name,'description':''};
                cleanUserFeatures.push({'feature':feature});
            }

            // clean base user object
            var user = UtilService.cleanObject(inputUser, 'user');

            // add cleaned objects
            user.userFeatures = cleanUserFeatures;
            user.groupRoles = inputUser.groupRoles;

            // generate password
            user.password = UtilService.generatePassword();
            user.changePassword = 'false';

            // lock and generate verification code
            user.locked = 'false';
            user.verified = 'false';
            user.verificationCode = UtilService.generateVerificationCode();

            // POST /user
            Restangular.all('user').post(user).then(function(successResult) {
                deferred.resolve(successResult);
                successResult.password = user.password;
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        }
    };
}]);
