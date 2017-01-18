'use strict';
var EditNoteModalInstanceCtrl = ['$scope', '$rootScope', '$modalInstance', 'note', '$modal',
    function ($scope, $rootScope, $modalInstance, note, $modal) {
        var init = function () {
            $scope.note = note;
            $scope.originalBody = note.body;
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
            if ($scope.originalBody !== note.body) {
            // open modal and pass in required objects for use in modal scope
                var modalInstance = $modal.open({
                    templateUrl: 'discardChangesModal.html',
                    controller: DiscardChangesModalInstanceCtrl,
                    size: 'lg',
                    backdrop: 'static'
                });

                // handle modal close (via button click)
                modalInstance.result.then(function () {
                    delete $scope.errorMessage;
                    $modalInstance.dismiss('cancel');
                });
            } else {
                delete $scope.errorMessage;
                $modalInstance.dismiss('cancel');
            }
        };

        init();
    }];
