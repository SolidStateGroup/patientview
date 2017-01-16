'use strict';
var EditNoteModalInstanceCtrl = ['$scope', '$rootScope', '$modalInstance', 'note',
    function ($scope, $rootScope, $modalInstance, note) {
        var init = function () {
            $scope.note = note;
            $scope.edit = true;

            delete $scope.errorMessage;
        };

        $scope.ok = function () {
            delete $scope.errorMessage;

            if (!$scope.note.body) {
                $scope.errorMessage = 'Please complete the note field';
                return;
            }

            $scope.note.lastUpdate = Date.now();
            $scope.note.lastUpdater = {
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
