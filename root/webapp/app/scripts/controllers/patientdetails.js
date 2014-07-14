'use strict';

angular.module('patientviewApp').controller('PatientDetailsCtrl', ['$scope', function ($scope) {
    var i, j;

    // add group to current group, remove from allowed
    $scope.addGroup = function (form, user, groupId) {
        if(_.findWhere(user.availableGroups, {id: groupId}) && _.findWhere($scope.allowedRoles, {id: user.selectedRole})) {
            user.availableGroups = _.without(user.availableGroups, _.findWhere(user.availableGroups, {id: groupId}));
            var newGroup = _.findWhere($scope.allGroups, {id: groupId});
            newGroup.role = _.findWhere($scope.allowedRoles, {id: user.selectedRole});
            user.groups.push(newGroup);
            user.selectedRole = '';

            // for REST compatibility
            user.groupRoles = [];
            for(var i=0;i<user.groups.length;i++) {
                var group = user.groups[i];
                user.groupRoles.push({'group': group, 'role': group.role});
            }

            form.$setDirty(true);
        }
    };

    // remove group from current groups, add to allowed groups
    $scope.removeGroup = function (form, user, group) {
        user.groups = _.without(user.groups, _.findWhere(user.groups, {id: group.id}));
        user.availableGroups.push(group);

        // for REST compatibility
        user.groupRoles = [];
        for(i=0;i<user.groups.length;i++) {
            var tempGroup = user.groups[i];
            user.groupRoles.push({'group': tempGroup, 'role': tempGroup.role});
        }

        form.$setDirty(true);
    };

    // add feature to current feature, remove from allowed
    $scope.addFeature = function (form, user, featureId) {
        for (i=0; i < user.availableFeatures.length; i++) {
            if (user.availableFeatures[i].feature.id === featureId) {
                user.userFeatures.push(user.availableFeatures[i]);
                user.availableFeatures.splice(i, 1);
            }
        }
        form.$setDirty(true);
    };

    // remove feature from current features, add to allowed features
    $scope.removeFeature = function (form, user, feature) {
        for (i=0; i < user.userFeatures.length; i++) {
            if (user.userFeatures[i].feature.id === feature.feature.id) {
                user.availableFeatures.push(user.userFeatures[i]);
                user.userFeatures.splice(i, 1);
            }
        }
        form.$setDirty(true);
    };

    $scope.validateNHSNumber = function(txtNhsNumber) {
        var isValid = false;

        if (txtNhsNumber.length == 10) {
            var total = 0, i;
            for (i = 0; i <= 8; i++) {
                var digit = txtNhsNumber.substr(i, 1);
                var factor = 10 - i;
                total += (digit * factor);
            }

            var checkDigit = (11 - (total % 11));
            if (checkDigit == 11) { checkDigit = 0; }
            if (checkDigit == txtNhsNumber.substr(9, 1)) { isValid = true; }
        }

        return isValid;
    };

    $scope.addIdentifier = function (form, user, identifier) {

        if (identifier.identifierType !== undefined) {
            identifier.identifierType = _.findWhere($scope.identifierTypes, {id: identifier.identifierType});

            // validate NHS_NUMBER
            var valid = true, errorMessage = '';

            if (identifier.identifierType.value === 'NHS_NUMBER') {
                valid = $scope.validateNHSNumber(identifier.identifier);
                errorMessage = 'Invalid NHS Number, please check format';
            }

            if (valid) {
                identifier.id = Math.floor(Math.random() * (9999)) - 10000;
                user.identifiers.push(_.clone(identifier));
                identifier.identifier = '';
                form.$setDirty(true);
            } else {
                identifier.identifierType = identifier.identifierType.id;
                alert(errorMessage);
            }
        }
    };

    $scope.removeIdentifier = function (form, user, identifier) {
        for (i = 0; i < user.identifiers.length; i++) {
            if (user.identifiers[i].id === identifier.id) {
                user.identifiers.splice(i, 1);
            }
        }
        form.$setDirty(true);
    };
}]);
