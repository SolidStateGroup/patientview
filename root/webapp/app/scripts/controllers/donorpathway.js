'use strict';

angular.module('patientviewApp').controller('DonorPathwayCtrl', ['localStorageService', 'UserService', 'DonorPathwayService', '$scope', '$rootScope', 'UtilService', 'FileUploader',
    '$routeParams', '$location', '$route', '$modal',
    function (localStorageService, UserService, DonorPathwayService, $scope, $rootScope, UtilService, FileUploader, $routeParams, $location, $route, $modal) {

        $scope.user = DonorPathwayService.getUser();

        $scope.state = {};
        $scope.loading = true;
        $scope.state.notesLoading = true;
        $scope.stages = {Consultation: {}, Testing: {}, Review: {}};
        $scope.editStages = {Consultation: {}, Testing: {}, Review: {}};
        $scope.editStage = {};
        $scope.state.moveToNextPoint = false;
        // $scope.state.devAllowBack = true;

        var init = function () {
            if (!$scope.user) {
                $scope.fatalErrorMessage = 'Error retrieving pathway';
                return;
            }

            // Get the donor pathway
            DonorPathwayService.getPathway('DONORPATHWAY')
                .then(function (pathway) {
                    if (!pathway) {
                        $scope.fatalErrorMessage = 'Error retrieving pathway';
                        return;
                    }
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
                        $scope.state.reviewStatus = $scope.editStage.stageStatus;
                    }
                    $scope.loading = false;
                }, function () {
                    $scope.fatalErrorMessage = 'Error retrieving pathway';
                });

            DonorPathwayService.getNotes('DONORVIEW')
                .then(function (notes) {
                    $scope.notes = notes;
                    $scope.state.notesLoading = false;
                }, function () {
                    $scope.state.noteErrorMessage = 'Error retrieving notes';
                });
        };

        var checkPending = function () {
            if ($scope.editStage.stageStatus == 'PENDING') {
                $scope.editStage.version++;
                $scope.editStage.stageStatus = 'STARTED';
                updatePathway(false, true);
            }
        };

        $scope.showPoint = function (point) {
            $scope.state.saved = false;
            $scope.editStage = $scope.editStages[point];
            $scope.state.moveToNextPoint = $scope.editStage.stageStatus == 'COMPLETED';
            if ($scope.editStage.stageType == 'REVIEW') {
                if ($scope.editStage.furtherInvestigation) {
                    $scope.state.reviewStatus = 'STARTED';
                } else {
                    if ($scope.editStage.stageStatus != 'STARTED') {
                        $scope.state.reviewStatus = $scope.editStage.stageStatus;
                    }
                }
            }
        };

        $scope.save = function (notify) {
            delete $scope.state.saveErrorMessage;

            // open modal and pass in required objects for use in modal scope
            if (notify) {
                var modalInstance = $modal.open({
                    templateUrl: 'notifyDonorModal.html',
                    controller: ConfirmModalInstanceCtrl,
                    size: 'lg',
                    backdrop: 'static'
                });

                modalInstance.result.then(function () {
                    savePathway(true);
                });
            } else {
                savePathway(false);
            }
        };

        var savePathway = function (notify) {
            var editStage = $scope.editStage;
            if (editStage.stageType == 'CONSULTATION' || editStage.stageType == 'TESTING') {
                if (!editStage.data.caregiverText || !editStage.data.carelocationText) {
                    $scope.state.saveErrorMessage = 'Please fill out all fields.';
                    angular.element(!editStage.data.caregiverText ? '#caregiver' : '#carelocation').focus();
                    return;
                }

                // Check whether we should move to next point
                if ($scope.state.moveToNextPoint) {
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
                    $scope.editStage.stageStatus = 'COMPLETED';

                    // Go to next stage
                    $scope.editStage = $scope.editStages[nextStage];
                    if (nextStage == 'Testing') {
                        $scope.state.moveToNextPoint = false;
                    }
                }
            } else {
                if (editStage.backToPreviousPoint) {
                    if (editStage.backToPreviousPoint == 'CHOOSE') {
                        $scope.state.saveErrorMessage = 'Please select the point on the pathway that should be reverted to.';
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
                    if (!editStage.furtherInvestigation && (!$scope.state.reviewStatus || $scope.state.reviewStatus == 'STARTED' || $scope.state.reviewStatus == 'PENDING')) {
                        $scope.state.saveErrorMessage = 'Please select a status for this point.';
                        return;
                    }
                    editStage.stageStatus = $scope.state.reviewStatus;
                    $scope.editStages.Review.stageStatus = $scope.state.reviewStatus;
                }
            }

            // Update the pathway
            updatePathway(notify);
        };

        var updatePathway = function (notify, silent) {
            DonorPathwayService.updatePathway($scope.editPathway, notify)
                .then(function () {
                    if (!silent) {
                        $scope.state.saved = true;
                    }
                    $scope.pathway = _.cloneDeep($scope.editPathway);
                    $scope.stages = $scope.pathway.stages;
                }, function (failureResult) {
                    $scope.state.saveErrorMessage = 'There was an error updating the pathway';
                });
        };

        $scope.cancel = function () {
            // open modal and pass in required objects for use in modal scope
            var modalInstance = $modal.open({
                templateUrl: 'discardChangesModal.html',
                controller: ConfirmModalInstanceCtrl,
                size: 'lg',
                backdrop: 'static'
            });

            // handle modal close (via button click)
            modalInstance.result.then(function () {
                $scope.editPathway = _.cloneDeep($scope.pathway);
                $scope.editStages = $scope.editPathway.stages;
                $scope.editStage = $scope.editStages[$scope.getStageKeyFromType($scope.editStage.stageType)];
            });
        };

        $scope.getDate = function (ts) {
            return DonorPathwayService.getDate(ts);
        };

        $scope.addNote = function () {
            delete $scope.state.noteErrorMessage;

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
                        $scope.state.noteErrorMessage = 'There was an error adding the note';
                    });
            });
        };

        $scope.editNote = function (note) {
            delete $scope.state.noteErrorMessage;

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
                        $scope.state.noteErrorMessage = 'There was an error updating the note';
                    });
            });
        };

        $scope.onReviewStatus = function () {
            $scope.state.saved = false;
            $scope.editStage.backToPreviousPoint = null;
            $scope.editStage.furtherInvestigation = false;
            $scope.editStage.stageStatus = 'STARTED';
        };

        $scope.onBackToPreviousPoint = function () {
            $scope.state.saved = false;
            $scope.state.reviewStatus = 'PENDING';
            $scope.editStage.stageStatus = 'STARTED';
            $scope.editStage.furtherInvestigation = false;
        };

        $scope.onFurtherInvestigation = function () {
            $scope.state.saved = false;
            $scope.editStage.backToPreviousPoint = null;
            $scope.editStage.stageStatus = 'STARTED';
            $scope.state.reviewStatus = 'STARTED';
        };

        $scope.getStageStatusColour = function (stageStatus) {
            return DonorPathwayService.getStageStatusColour(stageStatus);
        };

        $scope.getStageStatusTooltipText = function (stageStatus) {
            return DonorPathwayService.getStageStatusTooltipText(stageStatus);
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

        $scope.openPointsModal = function () {
            // open modal and pass in required objects for use in modal scope
            var modalInstance = $modal.open({
                templateUrl: 'views/modal/donorPathwayPointsModal.html',
                controller: ConfirmModalInstanceCtrl,
                size: 'lg',
                backdrop: 'static'
            });
        };

        init();
    }]);
