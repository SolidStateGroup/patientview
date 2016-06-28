'use strict';
angular.module('patientviewApp').controller('ContactCtrl',['$scope', '$modal', 'GroupService', 'ConversationService',
function ($scope, $modal, GroupService, ConversationService) {

    $scope.init = function() {
        $scope.loading = true;
        GroupService.getGroupsForUserAllDetails($scope.loggedInUser.id).then(function(page) {
            $scope.groups = [];

            // set checkboxes
            for (var i=0;i<page.content.length;i++) {

                if (page.content[i].groupType.value === 'UNIT') {
                    page.content[i].selected = true;
                    $scope.groups.push(page.content[i]);
                }
            }

            $scope.loading = false;
        }, function () {
            alert('Error retrieving groups');
            $scope.loading = false;
        });
    };

    // open modal for contacting unit
    $scope.openModalContactUnit = function (group) {
        $scope.errorMessage = '';
        $scope.successMessage = '';

        // open modal
        var modalInstance = $modal.open({
            templateUrl: 'contactUnitModal.html',
            controller: ContactUnitModalInstanceCtrl,
            backdrop: 'static',
            resolve: {
                group: function () {
                    return group;
                },
                ConversationService: function () {
                    return ConversationService;
                }
            }
        });

        modalInstance.result.then(function () {
            $scope.successMessage = 'You have successfully contacted your unit';
        }, function () {
            $scope.conversation = '';
        });

    };

    var isValidUrl = function ValidURL(str) {
        var pattern = new RegExp('^(https?:\\/\\/)?'+ // protocol
            '((([a-z\\d]([a-z\\d-]*[a-z\\d])*)\\.)+[a-z]{2,}|'+ // domain name
            '((\\d{1,3}\\.){3}\\d{1,3}))'+ // OR ip (v4) address
            '(\\:\\d+)?(\\/[-a-z\\d%_.~+]*)*'+ // port and path
            '(\\?[;&a-z\\d%_.~+=-]*)?'+ // query string
            '(\\#[-a-z\\d_]*)?$','i'); // fragment locator
        if(!pattern.test(str)) {
            return false;
        } else {
            return true;
        }
    };

    $scope.addAIfRequired = function (text) {
        if (text !== null && text !== undefined) {
            if (text.indexOf('@') > -1) {
                text = '<a href="mailto:' + text + '">' + text + '</a>';
            } else if (isValidUrl(text)) {
                if (text.indexOf('http') > -1) {
                    text = '<a href="' + text + '" target="_blank">' + text + '</a>';
                } else {
                    text = '<a href="http://' + text + '" target="_blank">' + text + '</a>';
                }
            }
        }

        return text;
    };

    $scope.init();
}]);
