'use strict';
var AddNoteModalInstanceCtrl = ['$scope', '$rootScope', '$modalInstance',
    function ($scope, $rootScope, $modalInstance) {
        var init = function () {
            $scope.note = {};

            delete $scope.errorMessage;
        };

        $scope.ok = function () {
            delete $scope.errorMessage;

            if (!$scope.note.body) {
                $scope.errorMessage = 'Please complete the note field';
                return;
            }

            $scope.note.created = Date.now();
            $scope.note.creator = {
                id: $rootScope.loggedInUser.id,
                forename: $rootScope.loggedInUser.forename,
                surname: $rootScope.loggedInUser.surname
            }
            $modalInstance.close($scope.note);
        };

        $scope.cancel = function () {
            delete $scope.errorMessage;
            $modalInstance.dismiss('cancel');
        };

        init();
    }];
