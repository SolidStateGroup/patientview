'use strict';
var AddNoteModalInstanceCtrl = ['$scope', '$rootScope', '$modalInstance', '$modal',
    function ($scope, $rootScope, $modalInstance, $modal) {
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
            if ($scope.note.body) {
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
