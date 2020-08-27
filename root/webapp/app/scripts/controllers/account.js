'use strict';

angular.module('patientviewApp').controller('AccountCtrl', ['localStorageService', 'UserService', 'AuthService', '$scope', '$rootScope', 'UtilService', 'FileUploader',
    function (localStorageService, UserService, AuthService, $scope, $rootScope, UtilService, FileUploader) {

    $scope.pw ='';
    $scope.userPicture = '/api/user/' + $rootScope.loggedInUser.id + '/picture';

    if ($rootScope.loggedInUser === null) {
        $rootScope.logout();
    }

    var getUser = function() {
        UserService.get($rootScope.loggedInUser.id).then(function (data) {
            $scope.userdetails = data;
            $scope.userdetails.confirmEmail = $scope.userdetails.email;
            // use date parameter (not used in Spring controller) to force refresh of picture by angular after upload
            $scope.datedUserPicture = $scope.userPicture + '?date=' + (new Date()).toString();

            rootScope.loggedInUser.secretWordIsSet = data.secretWordIsSet;
        });
    };

    getUser();

    $scope.removeSecretWord = function() {
        delete $scope.secretWordErrorMessage;
        delete $scope.secretWordSuccessMessage;
        UserService.removeSecretWord($rootScope.loggedInUser.id).then(function () {
            getUser();
            $scope.secretWordSuccessMessage = 'Your secret word has been successfully removed.';
        }, function () {
            $scope.secretWordErrorMessage = 'Error removing secret word';
        });
    };

    $scope.updateSecretWordDisabled = function () {
        if ($rootScope.loggedInUser.secretWordIsSet && !$scope.oldSecretWord) {
            return true;
        }

        return !($scope.secretWord1 && $scope.secretWord1.length > 6
            && $scope.secretWord2 && $scope.secretWord2.length > 6
            && ($scope.secretWord1 === $scope.secretWord2));
    }

    $scope.saveSettings = function () {
        // If the email field has been changed validate emails
        $scope.settingsSuccessMessage = null;
        $scope.settingsErrorMessage = null;

        if (!$scope.userdetails.confirmEmail) {
            $scope.settingsErrorMessage = 'Please confirm the email address';
        } else {
            // Email equal and correct
            if (($scope.userdetails.confirmEmail === $scope.userdetails.email)) {
                if (UtilService.validateEmail($scope.userdetails.email)) {
                    $scope.settingsErrorMessage = 'Invalid format for email';
                } else {
                    UserService.saveOwnSettings($scope.loggedInUser.id, $scope.userdetails).then(function () {
                        $scope.settingsSuccessMessage = 'The settings have been saved';
                        AuthService.getUserInformation({'token' : $scope.loggedInUser.userInformation.token})
                            .then(function (userInformation) {

                            // get user information, store in session
                            var user = userInformation.user;
                            delete userInformation.user;
                            user.userInformation = userInformation;

                            $rootScope.loggedInUser = user;
                            localStorageService.set('loggedInUser', user);

                        }, function(result) {
                            if (result.data) {
                                $scope.settingsErrorMessage = result.data;
                            } else {
                                delete $scope.settingsErrorMessage;
                            }
                            $scope.loading = false;
                        });

                    }, function (result) {
                        $scope.settingsErrorMessage = 'The settings have not been saved ' + result.data;
                    });
                }
            } else {
                $scope.settingsErrorMessage = 'The emails do not match';
            }
        }
    };

    $scope.savePassword = function () {
        $scope.passwordSuccessMessage = null;
        $scope.passwordErrorMessage = null;

        if ($scope.pw !== $scope.confirmPassword) {
            $scope.passwordErrorMessage = 'The passwords do not match';
        } else {
            UserService.changePassword($rootScope.loggedInUser.id, $scope.pw).then(function () {
                $scope.passwordSuccessMessage = 'The password has been saved';
            }, function () {
                $scope.passwordErrorMessage = 'There was an error';
            });
        }
    };

    $scope.saveSecretWord = function () {
        $scope.secretWordSuccessMessage = null;
        $scope.secretWordErrorMessage = null;

        if ($rootScope.loggedInUser.secretWordIsSet && !$scope.oldSecretWord) {
            $scope.secretWordErrorMessage = 'Please enter your current secret word';
        } else if ($scope.secretWord1 !== $scope.secretWord2) {
            $scope.secretWordErrorMessage = 'The secret words do not match';
        } else {
            var secretWordInput = {};
            if ($rootScope.loggedInUser.secretWordIsSet) {
                secretWordInput.oldSecretWord = $scope.oldSecretWord;
            }
            secretWordInput.secretWord1 = $scope.secretWord1;
            secretWordInput.secretWord2 = $scope.secretWord2;
            UserService.changeSecretWord($rootScope.loggedInUser.id, secretWordInput).then(function () {
                $scope.secretWordSuccessMessage = 'Your secret word has been saved';
                $rootScope.loggedInUser.hideSecretWordNotification = true;
                $rootScope.loggedInUser.secretWordIsSet = true;
            }, function () {
                $scope.secretWordErrorMessage = 'There was an error changing your secret word';
            });
        }
    };

    $scope.generateKey = function () {
        $scope.apiKeySuccessMessage = null;
        $scope.apiKeyErrorMessage = null;

        UserService.generateApiKey($rootScope.loggedInUser.id).then(function () {
            getUser();
            $scope.apiKeySuccessMessage = 'Your api key has been successfully generated';
        }, function (result) {
            $scope.apiKeyErrorMessage = result.data;
        });
    };

    // configure basic angular-file-upload
    var uploader = $scope.uploader = new FileUploader({
        // note: ie8 cannot pass custom headers so must be added as query parameter
        url: $scope.userPicture,
        headers: {'X-Auth-Token': $rootScope.authToken}
    });

    var isImageFiletype = function(item) {
        var type = '|' + item.file.type.slice(item.file.type.lastIndexOf('/') + 1) + '|';
        return '|jpg|png|jpeg|bmp|gif|'.indexOf(type) !== -1;
    };

    // callback after user selects a file
    uploader.onAfterAddingFile = function(item) {
        delete $scope.uploadErrorMessage;
        delete $scope.pictureChangeSuccessMessage;
        $scope.uploadingPicture = true;
        $scope.uploadError = false;

        if (isImageFiletype(item)) {
            uploader.uploadAll();
            uploader.queue = [];
        } else {
            $scope.uploadError = true;
            $scope.uploadingPicture = false;
            $scope.uploadErrorMessage = 'There was an error processing this file - please choose an image file.';
            uploader.queue = [];
        }
    };

    // callback if there is a problem with an image
    uploader.onErrorItem = function(fileItem, response, status, headers) {
        console.log(response);
        $scope.uploadError = true;
        delete $scope.pictureChangeSuccessMessage;
        $scope.uploadErrorMessage = 'There was an error uploading your image file.';
        $scope.uploadingPicture = false;
    };

    // when all uploads complete, if no error then force refresh of image by appending current date as parameter
    uploader.onCompleteAll = function() {
        if (!$scope.uploadError) {
            $rootScope.loggedInUser.picture = 'new';
            $scope.pictureChangeSuccessMessage = 'Your photo has been uploaded successfully. Thank you.';
            $scope.uploadingPicture = false;
            getUser();
        } else {
            // error during upload
            $scope.uploadingPicture = false;
            delete $scope.pictureChangeSuccessMessage;
        }
    };

    $scope.deletePicture = function() {
        delete $scope.uploadErrorMessage;
        delete $scope.pictureChangeSuccessMessage;
        UserService.deletePicture($rootScope.loggedInUser.id).then(function () {
            delete $rootScope.loggedInUser.picture;
            getUser();
            $scope.pictureChangeSuccessMessage = 'Your photo has been successfully removed.';
        }, function () {
            $scope.uploadErrorMessage = 'Error removing photo';
        });
    };
}]);