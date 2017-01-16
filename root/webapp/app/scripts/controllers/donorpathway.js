'use strict';

angular.module('patientviewApp').controller('DonorPathwayCtrl', ['localStorageService', 'UserService', 'DonorPathwayService', '$scope', '$rootScope', 'UtilService', 'FileUploader',
    '$routeParams', '$location', '$route', '$modal',
    function (localStorageService, UserService, DonorPathwayService, $scope, $rootScope, UtilService, FileUploader, $routeParams, $location, $route, $modal) {

        var DiscardChangesModalInstanceCtrl = ['$scope', '$modalInstance',
            function ($scope, $modalInstance) {
                $scope.ok = function () {
                    $modalInstance.close();
                };
                $scope.cancel = function () {
                    $modalInstance.dismiss('cancel');
                };
            }];

        $scope.userId = DonorPathwayService.getUserId();

        $scope.loading = true;
        $scope.notesLoading = true;
        $scope.stages = {Consultation: {}, Testing: {}, Review: {}};
        $scope.editStages = {Consultation: {}, Testing: {}, Review: {}};
        $scope.editStage = {};
        $scope.moveToNextPoint = false;
        // $scope.devAllowBack = true;

        var init = function () {
            if (!$scope.userId) {
                $scope.fatalErrorMessage = 'Error retrieving pathway';
                return;
            }

            // Get the donor pathway
            DonorPathwayService.getPathway('DONORPATHWAY')
                .then(function (pathway) {
                    $scope.pathway = pathway;
                    $scope.stages = pathway.stages;
                    $scope.editPathway = _.cloneDeep($scope.pathway);
                    $scope.editStages = $scope.editPathway.stages;
                    if ($scope.stages.Consultation.stageStatus != 'COMPLETED') {
                        $scope.editStage = $scope.editStages.Consultation;
                        checkPending();
                    } else if ($scope.stages.Testing.stageStatus != 'COMPLETED') {
                        $scope.editStage = $scope.editStages.Testing;
                        checkPending();
                    } else {
                        $scope.editStage = $scope.editStages.Review;
                        checkPending();
                        $scope.reviewStatus = $scope.editStage.stageStatus;
                    }
                    $scope.loading = false;
                }, function () {
                    $scope.fatalErrorMessage = 'Error retrieving pathway';
                });

            DonorPathwayService.getNotes('DONORVIEW')
                .then(function (notes) {
                    $scope.notes = notes;
                    $scope.notesLoading = false;
                }, function () {
                    $scope.noteErrorMessage = 'Error retrieving notes';
                });
        };

        var checkPending = function () {
            if ($scope.editStage.stageStatus == 'PENDING') {
                $scope.editStage.version++;
                $scope.editStage.stageStatus = 'STARTED';
                updatePathway();
            }
        };

        $scope.showPoint = function (point) {
            $scope.editStage = $scope.editStages[point];
            $scope.moveToNextPoint = $scope.editStage.stageStatus == 'COMPLETED';
            if ($scope.editStage.stageType == 'REVIEW') {
                if ($scope.editStage.furtherInvestigation) {
                    $scope.reviewStatus = 'STARTED';
                } else {
                    if ($scope.editStage.stageStatus != 'STARTED') {
                        $scope.reviewStatus = $scope.editStage.stageStatus;
                    }
                }
            }
        };

        $scope.save = function (notify) {
            var editStage = $scope.editStage;

            delete $scope.saveErrorMessage;

            if (editStage.stageType == 'CONSULTATION' || editStage.stageType == 'TESTING') {
                if (!editStage.data.caregiverText || !editStage.data.carelocationText) {
                    $scope.saveErrorMessage = 'Please fill out all fields.';
                    angular.element(!editStage.data.caregiverText ? '#caregiver' : '#carelocation').focus();
                    return;
                }
                // Set next stage to started and increase version number
                var nextStage = editStage.stageType == 'CONSULTATION' ? 'Testing' : 'Review';
                $scope.editStages[nextStage].stageStatus = 'STARTED';
                $scope.editStages[nextStage].version++;
                if (editStage.stageType == 'CONSULTATION') {
                    $scope.editStages.Review.stageStatus = 'PENDING';
                } else {
                    $scope.editStages.Review.backToPreviousPoint = null;
                }

                // Set current stage to completed
                $scope.editStages[$scope.getStageKeyFromType(editStage.stageType)].stageStatus = 'COMPLETED';

                // Check whether we should move to next point
                if ($scope.moveToNextPoint) {
                    // Go to next stage
                    $scope.editStage = $scope.editStages[nextStage];
                    if (nextStage == 'TESTING') {
                        $scope.moveToNextPoint = false;
                    }
                } else {
                    // Set edit stage to completed
                    editStage.stageStatus = 'COMPLETED';
                }
            } else {
                if (editStage.backToPreviousPoint) {
                    if (editStage.backToPreviousPoint == 'CHOOSE') {
                        $scope.saveErrorMessage = 'Please select the point on the pathway that should be reverted to.';
                        angular.element('#selBackToPreviousPoint').focus();
                        return;
                    }
                    // Set selected point to started and increase version number
                    $scope.editStage = $scope.editStages[$scope.getStageKeyFromType(editStage.backToPreviousPoint)];
                    $scope.editStage.stageStatus = 'STARTED';
                    $scope.editStage.version++;
                    if (editStage.backToPreviousPoint == 'CONSULTATION') {
                        $scope.editStages.Testing.stageStatus = 'PENDING';
                    }

                    // Set review point back to pending
                    editStage.stageStatus = 'PENDING';
                } else {
                    if (!editStage.furtherInvestigation && (!$scope.reviewStatus || $scope.reviewStatus == 'STARTED' || $scope.reviewStatus == 'PENDING')) {
                        $scope.saveErrorMessage = 'Please select a status for this point.';
                        return;
                    }
                    editStage.stageStatus = $scope.reviewStatus;
                    $scope.editStages.Review.stageStatus = $scope.reviewStatus;
                }
            }

            // Update the pathway
            updatePathway();
        };

        var updatePathway = function () {
            DonorPathwayService.updatePathway($scope.editPathway)
                .then(function () {
                    $scope.pathway = _.cloneDeep($scope.editPathway);
                    $scope.stages = $scope.pathway.stages;
                }, function (failureResult) {
                    $scope.saveErrorMessage = 'There was an error updating the pathway';

                    // @TODO review
                    // Restore original data
                    //$scope.editStages = _.cloneDeep($scope.stages);
                });
        };

        $scope.cancel = function () {
            // open modal and pass in required objects for use in modal scope
            var modalInstance = $modal.open({
                templateUrl: 'discardChangesModal.html',
                controller: DiscardChangesModalInstanceCtrl,
                size: 'lg',
                backdrop: 'static'
            });

            // handle modal close (via button click)
            modalInstance.result.then(function () {
                $scope.editStage = _.cloneDeep($scope.stages[$scope.getStageKeyFromType($scope.editStage.stageType)]);
            });
        };

        $scope.getDate = function (ts) {
            return moment(ts).format('DD/MM/YYYY');
        };

        $scope.addNote = function () {
            delete $scope.noteErrorMessage;

            // open modal and pass in required objects for use in modal scope
            var modalInstance = $modal.open({
                templateUrl: 'noteModal.html',
                controller: AddNoteModalInstanceCtrl,
                size: 'lg',
                backdrop: 'static',
            });

            // handle modal close (via button click)
            modalInstance.result.then(function (note) {
                note.noteType = 'DONORVIEW';
                DonorPathwayService.addNote(note)
                    .then(function (note) {
                        $scope.notes.push(note);
                    }, function (failureResult) {
                        $scope.noteErrorMessage = 'There was an error adding the note';
                    });
            });
        };

        $scope.editNote = function (note) {
            delete $scope.noteErrorMessage;

            // open modal and pass in required objects for use in modal scope
            var modalInstance = $modal.open({
                templateUrl: 'noteModal.html',
                controller: EditNoteModalInstanceCtrl,
                size: 'lg',
                backdrop: 'static',
                resolve: {
                    note: function () {
                        return _.cloneDeep(note);
                    }
                }
            });

            // handle modal close (via button click)
            modalInstance.result.then(function (updatedNote) {
                DonorPathwayService.updateNote(updatedNote)
                    .then(function () {
                        note.body = updatedNote.body;
                        note.lastUpdate = updatedNote.lastUpdate;
                        note.lastUpdater = updatedNote.lastUpdater;
                    }, function (failureResult) {
                        $scope.noteErrorMessage = 'There was an error updating the note';
                    });
            });
        }

        $scope.onReviewStatus = function () {
            $scope.editStage.backToPreviousPoint = null;
            $scope.editStage.furtherInvestigation = false;
            $scope.editStage.stageStatus = 'STARTED';
        };

        $scope.onBackToPreviousPoint = function () {
            $scope.reviewStatus = 'PENDING';
            $scope.editStage.stageStatus = 'STARTED';
            $scope.editStage.furtherInvestigation = false;
        };

        $scope.onFurtherInvestigation = function () {
            $scope.editStage.backToPreviousPoint = null;
            $scope.editStage.stageStatus = 'STARTED';
            $scope.reviewStatus = 'STARTED';
        };

        $scope.getReviewStatusColour = function () {
            var colour;
            switch ($scope.stages.Review.stageStatus) {
                case 'PENDING':
                case 'STARTED':
                default:
                    colour = 'green';
                    break;

                case 'ON_HOLD':
                    colour = '#ffbf00';
                    break;

                case 'STOPPED':
                    colour = 'red';
                    break;

                case 'COMPLETED':
                    colour = 'gray';
                    break;
            }
            return {'background-color': colour};
        };

        $scope.getStageKeyFromType = function (type) {
            switch (type) {
                case 'CONSULTATION':
                    return 'Consultation';
                case 'TESTING':
                    return 'Testing';
                case 'REVIEW':
                    return 'Review';
            }
        };

        init();
    }]);
