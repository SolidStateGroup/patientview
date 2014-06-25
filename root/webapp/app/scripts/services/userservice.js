'use strict';

angular.module('patientviewApp').factory('UserService', ['$q', 'Restangular', 'UtilService',
function ($q, Restangular, UtilService) {
    return {
        // Used when cleaning objects before they are passed to REST service, object fields to keep
        getFields: function (objectType) {
            if (objectType == "user") {
                return ['id', 'username', 'password', 'email', 'name', 'changePassword', 'locked', 'userFeatures', 'verified', 'verificationCode'];
            }
            if (objectType == "role") {
                return ['id','name','description','routes'];
            }
            if (objectType == "group") {
                return ['id','name','code','description','groupType','groupFeatures','routes'];
            }
            return null;
        },
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
            var user = {};
            var userFields = this.getFields('user');
            for (var userField in inputUser) {
                if (inputUser.hasOwnProperty(userField) && _.contains(userFields, userField)) {
                    user[userField] = inputUser[userField];
                }
            }

            // clean group roles
            user.groupRoles = [];
            for (var i=0;i<inputUser.groupRoles.length;i++) {
                var inputGroupRole = inputUser.groupRoles[i];

                // clean role
                var role = {}, roleFields = this.getFields('role');
                for (var roleField in inputGroupRole.role) {
                    if (inputGroupRole.role.hasOwnProperty(roleField) && _.contains(roleFields, roleField)) {
                        role[roleField] = inputGroupRole.role[roleField];
                    }
                }

                // clean group
                var group = {}, groupFields = this.getFields('group');
                for (var groupField in inputGroupRole.group) {
                    if (inputGroupRole.group.hasOwnProperty(groupField) && _.contains(groupFields, groupField)) {
                        group[groupField] = inputGroupRole.group[groupField];
                    }
                }

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

            // clean and create group roles
            inputUser.groupRoles = [];
            for (var i=0;i<inputUser.groups.length;i++) {
                var inputGroup = inputUser.groups[i];

                // clean role
                var role = {}, roleFields = this.getFields('role');
                for (var roleField in inputGroup.role) {
                    if (inputGroup.role.hasOwnProperty(roleField) && _.contains(roleFields, roleField)) {
                        role[roleField] = inputGroup.role[roleField];
                    }
                }

                // clean group
                var group = {}, groupFields = this.getFields('group');
                for (var groupField in inputGroup) {
                    if (inputGroup.hasOwnProperty(groupField) && _.contains(groupFields, groupField)) {
                        group[groupField] = inputGroup[groupField];
                    }
                }

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
            var user = {}, userFields =this.getFields('user');
            for (var field in inputUser) {
                if (inputUser.hasOwnProperty(field) && _.contains(userFields, field)) {
                    user[field] = inputUser[field];
                }
            }

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
