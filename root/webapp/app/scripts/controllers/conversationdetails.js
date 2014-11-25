'use strict';

angular.module('patientviewApp').controller('ConversationDetailsCtrl', ['$scope', 'ConversationService',
function ($scope, ConversationService) {

    $scope.selectGroup = function(conversation, groupId) {
        $scope.modalLoading = true;

        ConversationService.getRecipients($scope.loggedInUser.id, groupId).then(function (recipientMap) {
            var availableRecipients = [];
            var restangularObjects
                = ['route','reqParams','fromServer','parentResource','restangularCollection','singleOne'];
            var sortOrder = ['Unit Admin', 'Unit Staff', 'Patient'];
            var keys = [];
            var i, j;

            for (var key in recipientMap) {
                if (recipientMap.hasOwnProperty(key) && typeof(recipientMap[key]) !== 'function'
                    && !(restangularObjects.indexOf(key) > -1)) {
                    keys.push(key);
                }
            }

            //var options = $("#conversation-add-recipient");

            // order keys accordingly
            var result = [];
            for(i=0; i<sortOrder.length; i++) {
                for (j=0; j<keys.length; j++) {
                    if (keys[j] == sortOrder[i] && !(result.indexOf(keys[j]) > -1)) {
                        result.push(keys[j]);
                    }
                }
            }

            // add any remaining keys
            for (i=0; i<keys.length; i++) {
                if (!(result.indexOf(keys[i]) > -1)) {
                    result.push(keys[i]);
                }
            }

            var optionString = '';

            // add in order to recipients, with disabled option describing role
            for (i=0; i<result.length; i++) {
                if (recipientMap[result[i]] !== undefined) {
                    if (recipientMap[result[i]].length) {
                        //var element = {};
                        //element.description = ' ';

                        //availableRecipients.push(element);
                        //element = {};
                        //element.description = result[i];
                        optionString += '<option></option><option class="option-header">' + result[i] + '</option>';
                        //options.append($("<option />"));
                        //options.append($("<option />").val(null).text(result[i]).addClass('option-header'));
                        //availableRecipients.push(element);
                    }

                    //var sorted = _.sortBy(recipientMap[result[i]], 'forename');

                    for (j = 0; j < recipientMap[result[i]].length; j++) {

                        optionString += '<option value="'+ recipientMap[result[i]][j].id +'">' + recipientMap[result[i]][j].forename + ' ' + recipientMap[result[i]][j].surname + '</option>';
                        //options.append($("<option />").val(recipientMap[result[i]][j].id).text(' ' + recipientMap[result[i]][j].forename + ' ' + recipientMap[result[i]][j].surname));
                        //availableRecipients.push(sorted[j]);
                    }
                }
            }

            $("#conversation-add-recipient").html(optionString);

            /*$.each(availableRecipients, function() {
                if (this.description !== undefined) {
                    options.append($("<option />").val(null).text(this.description).addClass('option-header'));
                } else {
                    options.append($("<option />").val(this.id).text(' ' + this.forename + ' ' + this.surname));
                }
            });*/

            //conversation.availableRecipients = availableRecipients;
            $scope.modalLoading = false;
        }, function (failureResult) {
            if (failureResult.status === 404) {
                $scope.modalLoading = false;
            } else {
                $scope.modalLoading = false;
                alert('Error loading message recipients');
            }
        });
    };

    $scope.addRecipient = function (form, conversation, userId) {

        var found = false;
        var i;

        // check not already added
        for (i = 0; i < conversation.recipients.length; i++) {
            // need to cast string to number using == not === for id
            if (conversation.recipients[i].id == userId) {
                found = true;
            }
        }

        if (!found && userId !== undefined) {
            for (i = 0; i < conversation.availableRecipients.length; i++) {
                if (conversation.availableRecipients[i].id == userId) {
                    conversation.recipients.push(conversation.availableRecipients[i]);
                }
            }
        }
    };

    $scope.removeRecipient = function (form, conversation, user) {
        for (var i = 0; i < conversation.recipients.length; i++) {
            if (conversation.recipients[i].id === user.id) {
                conversation.recipients.splice(i, 1);
            }
        }
    };
}]);
