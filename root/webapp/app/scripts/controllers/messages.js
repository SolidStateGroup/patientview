'use strict';

// pagination following http://fdietz.github.io/recipes-with-angular-js/common-user-interface-patterns/paginating-through-server-side-data.html

angular.module('patientviewApp').controller('MessagesCtrl',['$scope', 'ConversationService', function ($scope, ConversationService) {

    $scope.itemsPerPage = 5;
    $scope.currentPage = 0;

    $scope.prevPage = function() {
        if ($scope.currentPage > 0) {
            $scope.currentPage--;
        }
    };

    $scope.prevPageDisabled = function() {
        return $scope.currentPage === 0 ? "disabled" : "";
    };

    $scope.nextPage = function() {
        if ($scope.currentPage < $scope.pageCount() - 1) {
            $scope.currentPage++;
        }
    };

    $scope.nextPageDisabled = function() {
        return $scope.currentPage === $scope.pageCount() - 1 ? "disabled" : "";
    };

    $scope.pageCount = function() {
        return Math.ceil($scope.total/$scope.itemsPerPage);
    };

    $scope.$watch("currentPage", function(newValue, oldValue) {
        $scope.loading = true;
        ConversationService.getAll($scope.loggedInUser, newValue*$scope.itemsPerPage, $scope.itemsPerPage).then(function(result) {
            $scope.pagedItems = result;
            $scope.loading = false;
        }, function() {
            $scope.loading = false;
            // error
        });
        //$scope.total = ConversationService.total();
    });

    $scope.init = function() {
    };

    $scope.init();

    $scope.addMessage = function(conversation) {
        ConversationService.addMessage($scope.loggedInUser, conversation, conversation.addMessageContent).then(function() {
            conversation.addMessageContent = '';

            ConversationService.get(conversation.id).then(function(successResult) {
                conversation = successResult;
            }, function() {
                // error
                alert('Error updating conversation (message added successfully)');
            });
        }, function() {
            // error
            alert('Error adding message');
        });
    }
}]);
