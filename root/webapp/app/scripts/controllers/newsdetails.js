'use strict';

angular.module('patientviewApp').controller('NewsDetailsCtrl', ['$scope', 'NewsService',
    function ($scope, NewsService) {

        $scope.addGroup = function (form, newsItem, groupId) {
            var i;
            // only do POST if in edit mode, otherwise just add to object
            if ($scope.editMode) {
                //todo
                GroupService.addGroup(group, groupId).then(function () {
                    // added Group
                    for (var j = 0; j < group.availableGroups.length; j++) {
                        if (group.availableGroups[j].id === groupId) {
                            group.groups.push(group.availableGroups[j]);
                            group.availableGroups.splice(j, 1);
                        }
                    }

                    // update accordion header with data from GET
                    GroupService.get(group.id).then(function (successResult) {
                        for(var i=0;i<$scope.list.length;i++) {
                            if($scope.list[i].id == successResult.id) {
                                var headerDetails = $scope.list[i];
                                headerDetails.groups = successResult.groups;
                            }
                        }
                    }, function () {
                        // failure
                        alert('Error updating header (saved successfully)');
                    });

                    form.$setDirty(true);
                }, function () {
                    // failure
                    alert('Error saving Group');
                });
            } else {
                for (i = 0; i < newsItem.availableGroups.length; i++) {
                    if (newsItem.availableGroups[i].id === groupId) {
                        //newsItem.groups.push(newsItem.availableGroups[j]);
                        newsItem.groups.push(newsItem.allGroups[groupId]);
                        newsItem.availableGroups.splice(i, 1);
                    }
                }
                form.$setDirty(true);
            }
        };

        $scope.removeGroup = function (form, newsItem, group) {
            // only do DELETE if in edit mode, otherwise just remove from object
            if ($scope.editMode) {
                //todo
                GroupService.deleteGroup(group, group).then(function () {
                    // deleted Group
                    for (var j = 0; j < group.groups.length; j++) {
                        if (group.groups[j].id === group.id) {
                            group.availableGroups.push(group.groups[j]);
                            group.groups.splice(j, 1);
                        }
                    }

                    // update accordion header with data from GET
                    GroupService.get(group.id).then(function (successResult) {
                        for(var i=0;i<$scope.list.length;i++) {
                            if($scope.list[i].id == successResult.id) {
                                var headerDetails = $scope.list[i];
                                headerDetails.groups = successResult.groups;
                            }
                        }
                    }, function () {
                        // failure
                        alert('Error updating header (saved successfully)');
                    });

                    form.$setDirty(true);
                }, function () {
                    // failure
                    alert('Error deleting Group');
                });
            } else {
                for (var j = 0; j < newsItem.groups.length; j++) {
                    if (newsItem.groups[j].id === group.id) {
                        newsItem.availableGroups.push(newsItem.allGroups[group.id]);
                        newsItem.groups.splice(j, 1);
                    }
                }

                form.$setDirty(true);
            }
        };

        $scope.addRole = function (form, newsItem, roleId) {
            var i;
            // only do POST if in edit mode, otherwise just add to object
            if ($scope.editMode) {
                //todo
                RoleService.addRole(role, roleId).then(function () {
                    // added Role
                    for (var j = 0; j < role.availableRoles.length; j++) {
                        if (role.availableRoles[j].id === roleId) {
                            role.roles.push(role.availableRoles[j]);
                            role.availableRoles.splice(j, 1);
                        }
                    }

                    // update accordion header with data from GET
                    RoleService.get(role.id).then(function (successResult) {
                        for(var i=0;i<$scope.list.length;i++) {
                            if($scope.list[i].id == successResult.id) {
                                var headerDetails = $scope.list[i];
                                headerDetails.roles = successResult.roles;
                            }
                        }
                    }, function () {
                        // failure
                        alert('Error updating header (saved successfully)');
                    });

                    form.$setDirty(true);
                }, function () {
                    // failure
                    alert('Error saving Role');
                });
            } else {
                for (i = 0; i < newsItem.availableRoles.length; i++) {
                    if (newsItem.availableRoles[i].id === roleId) {
                        //newsItem.roles.push(newsItem.availableRoles[j]);
                        newsItem.roles.push(newsItem.allRoles[roleId]);
                        newsItem.availableRoles.splice(i, 1);
                    }
                }
                form.$setDirty(true);
            }
        };

        $scope.removeRole = function (form, newsItem, role) {
            // only do DELETE if in edit mode, otherwise just remove from object
            if ($scope.editMode) {
                //todo
                RoleService.deleteRole(role, role).then(function () {
                    // deleted Role
                    for (var j = 0; j < role.roles.length; j++) {
                        if (role.roles[j].id === role.id) {
                            role.availableRoles.push(role.roles[j]);
                            role.roles.splice(j, 1);
                        }
                    }

                    // update accordion header with data from GET
                    RoleService.get(role.id).then(function (successResult) {
                        for(var i=0;i<$scope.list.length;i++) {
                            if($scope.list[i].id == successResult.id) {
                                var headerDetails = $scope.list[i];
                                headerDetails.roles = successResult.roles;
                            }
                        }
                    }, function () {
                        // failure
                        alert('Error updating header (saved successfully)');
                    });

                    form.$setDirty(true);
                }, function () {
                    // failure
                    alert('Error deleting Role');
                });
            } else {
                for (var j = 0; j < newsItem.roles.length; j++) {
                    if (newsItem.roles[j].id === role.id) {
                        newsItem.availableRoles.push(newsItem.allRoles[role.id]);
                        newsItem.roles.splice(j, 1);
                    }
                }

                form.$setDirty(true);
            }
        };
}]);
