'use strict';

angular.module('patientviewApp').factory('UserService', ['$q', 'Restangular', 'UtilService',
function ($q, Restangular, UtilService) {
    return {
        // Add new feature to user
        addFeature: function (user, featureId) {
            var deferred = $q.defer();
            // POST /user/{userId}/features/{featureId}
            Restangular.one('user', user.id).one('features',featureId).post().then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        // add new grouprole
        addGroupRole: function (user, groupId, roleId) {
            var deferred = $q.defer();
            // POST /user/{userId}/group/{groupId}/role/{roleId}
            Restangular.one('user', user.id).one('group',groupId).one('role',roleId).post().then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        // Add new identifier to user
        addIdentifier: function (user, identifier) {
            var deferred = $q.defer();
            identifier.identifierType = UtilService.cleanObject(identifier.identifierType, 'identifierType');
            // POST /user/{userId}/identifiers
            Restangular.one('user', user.id).all('identifiers').post(identifier).then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        // Change user's password, sets the change flag to false
        changePassword: function (userId, password) {
            var deferred = $q.defer();
            // POST /user/{userId}/changePassword
            Restangular.one('user', userId).post('changePassword', {'password': password}).then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        // Change user's secret word
        changeSecretWord: function (userId, secretWordInput) {
            var deferred = $q.defer();
            // POST /user/{userId}/changeSecretWord
            Restangular.one('user', userId).post('changeSecretWord', secretWordInput).then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        // check user has a certain role type in any GroupRole
        checkRoleExists: function(role, user) {
            var i;
            if (user.groupRoles) {
                for (i = 0; i < user.groupRoles.length; i++) {
                    if (user.groupRoles[i].role.name === role) {
                        return true;
                    }
                }
            } else {
                return false;
            }
        },
        // check username exists (returns boolean true if does exist, false if doesn't)
        checkUsernameExists: function (username) {
            var deferred = $q.defer();
            // GET /user/usernameexists/{username}
            Restangular.one('user/usernameexists').customGET(username).then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        // Create new user
        create: function (inputUser) {
            var deferred = $q.defer();

            // clean group roles
            for (var i=0;i<inputUser.groupRoles.length;i++) {
                delete inputUser.groupRoles[i].id;
                inputUser.groupRoles[i].group = UtilService.cleanObject(inputUser.groupRoles[i].group, 'group');
                inputUser.groupRoles[i].role = UtilService.cleanObject(inputUser.groupRoles[i].role, 'role');
            }

            // clean user features
            var cleanUserFeatures = [];
            for (var j=0;j<inputUser.userFeatures.length;j++) {
                var userFeature = inputUser.userFeatures[j];
                var feature = {'id':userFeature.feature.id,'name':userFeature.feature.name,'description':''};
                cleanUserFeatures.push({'feature':feature});
            }

            // clean identifiers
            var cleanIdentifiers = [];
            for (i=0;i<inputUser.identifiers.length;i++) {
                var identifier = inputUser.identifiers[i];
                identifier.identifierType = UtilService.cleanObject(identifier.identifierType, 'identifierType');
                if (identifier.id < 0) {
                    delete identifier.id;
                }
                cleanIdentifiers.push(identifier);
            }

            // set date of birth if available
            if (inputUser.selectedDay && inputUser.selectedMonth && inputUser.selectedYear) {
                inputUser.dateOfBirth = new Date(inputUser.selectedYear, inputUser.selectedMonth - 1, inputUser.selectedDay);
            }

            // patient management
            var patientManagement = inputUser.patientManagement;

            // clean base user object
            var user = UtilService.cleanObject(inputUser, 'user');

            // add cleaned objects
            user.userFeatures = cleanUserFeatures;
            user.identifiers = cleanIdentifiers;
            user.groupRoles = inputUser.groupRoles;

            user.changePassword = 'true';
            user.emailVerified = 'false';
            user.verificationCode = UtilService.generateVerificationCode();

            user.patientManagement = patientManagement;

            // POST /user
            Restangular.all('user').post(user).then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        // Delete feature from user
        deleteFeature: function (user, feature) {
            var deferred = $q.defer();
            // DELETE /user/{userId}/features/{featureId}
            Restangular.one('user', user.id).one('features',feature.id).remove().then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        // Delete grouprole
        deleteGroupRole: function (user, groupId, roleId) {
            var deferred = $q.defer();
            // DELETE /user/{userId}/group/{groupId}/role/{roleId}
            Restangular.one('user', user.id).one('group',groupId).one('role',roleId).remove().then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        // Delete picture associated with user
        deletePicture: function (userId) {
            var deferred = $q.defer();
            // DELETE /user/{userId}/picture
            Restangular.one('user', userId).one('picture').remove().then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        // find by email
        findByEmail: function (email) {
            //email = email.replace(/\./g,'[DOT]');
            var deferred = $q.defer();
            // GET /user/email?email=
            Restangular.one('user/email').get({'email': email}).then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        // find by identifier
        findByIdentifier: function (identifier) {
            var deferred = $q.defer();
            // GET /user/identifier/{identifier}
            Restangular.one('user/identifier').customGET(identifier).then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        // find by username
        findByUsername: function (username) {
            username = username.replace(/\./g,'[DOT]');
            var deferred = $q.defer();
            // GET /user/username/{username}
            Restangular.one('user/username').customGET(username).then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
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
        // gets users by group, role IDs passed in
        getByGroupsAndRoles: function (getParameters) {
            var deferred = $q.defer();
            // GET /user?groupId=1&groupId=2&roleId=1 etc
            Restangular.one('user').get(getParameters).then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        // get user information
        getInformation: function (userId) {
            var deferred = $q.defer();
            // GET /user/{userId}/information
            Restangular.one('user', userId).one('information').get().then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        // hide notification for secret word
        hideSecretWordNotification: function (userId) {
            var deferred = $q.defer();
            // POST /user/{userId}/hideSecretWordNotification
            Restangular.one('user', userId).post('hideSecretWordNotification').then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        // Remove a single user based on userId
        remove: function (user) {
            var deferred = $q.defer();
            // GET then DELETE /user/{userId}
            Restangular.one('user', user.id).get().then(function(user) {
                user.remove().then(function(res) {
                    deferred.resolve(res);
                });
            });
            return deferred.promise;
        },
        // Delete all group roles
        removeAllGroupRoles: function (user) {
            var deferred = $q.defer();
            // DELETE /user/{userId}/removeallgrouproles
            Restangular.one('user', user.id).one('removeallgrouproles').remove().then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        // Remove user's secret word
        removeSecretWord: function (userId) {
            var deferred = $q.defer();
            // DELETE /user/{userId}/secretword
            Restangular.one('user', userId).all('secretword').remove().then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
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
        // create or generate new api key
        generateApiKey: function (userId) {
            var deferred = $q.defer();
            // POST /user/{userId}/generateApiKey
            Restangular.one('user', userId).post('generateApiKey').then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        // Save existing user
        save: function (inputUser) {
            var deferred = $q.defer();

            // set date of birth if available
            if (inputUser.selectedDay && inputUser.selectedMonth && inputUser.selectedYear) {
                inputUser.dateOfBirth = new Date(inputUser.selectedYear, inputUser.selectedMonth - 1, inputUser.selectedDay);
            }

            // clean user object
            var user = UtilService.cleanObject(inputUser, 'userDetails');

            // PUT /user
            Restangular.all('user').customPUT(user).then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });

            return deferred.promise;
        },
        // Save More About Me details (currently SHOULD_KNOW and TALK_ABOUT fields)
        saveMoreAboutMe: function (user, moreAboutMe) {

            var userInformation = [];
            var shouldKnow = {}, talkAbout = {};

            shouldKnow.type = 'SHOULD_KNOW';
            shouldKnow.value = moreAboutMe.shouldKnow;
            userInformation.push(shouldKnow);
            talkAbout.type = 'TALK_ABOUT';
            talkAbout.value = moreAboutMe.talkAbout;
            userInformation.push(talkAbout);

            var deferred = $q.defer();
            // POST /user/{userId}/information
            Restangular.one('user', user.id).post('information', userInformation).then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        // Save own user settings
        saveOwnSettings: function (userId, inputUser) {
            var deferred = $q.defer();

            // clean user object
            var user = UtilService.cleanObject(inputUser, 'userDetails');

            // PUT /user/{userId}/settings
            Restangular.one('user', userId).all('settings').customPUT(user).then(function(successResult) {
                deferred.resolve(successResult);
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
        // Undelete a staff user
        undelete: function (user) {
            var deferred = $q.defer();
            // POST
            Restangular.one('user', user.id).post('undelete').then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        // validate Identifier, including if already in use etc
        validateIdentifier: function (userId, identifier, dummy) {
            identifier = UtilService.cleanObject(identifier, 'identifier');
            identifier.identifierType = UtilService.cleanObject(identifier.identifierType, 'identifierType');

            var userIdentifier = {};
            userIdentifier.userId = userId;
            userIdentifier.identifier = identifier;

            if (dummy !== undefined) {
                userIdentifier.dummy = dummy;
            } else {
                userIdentifier.dummy = false;
            }

            var deferred = $q.defer();

            // POST /identifier/validate
            Restangular.one('identifier/validate').customPOST(userIdentifier)
                .then(function(successResult) {
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
        }
    };
}]);
