'use strict';

angular.module('patientviewApp').factory('GroupService', ['$q', 'Restangular', 'UtilService',
function ($q, Restangular, UtilService) {
    return {
        get: function (groupId) {
            var deferred = $q.defer();
            Restangular.one('group', groupId).get().then(function(successResult) {
                deferred.resolve(successResult);
            }, function (failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        getStatistics: function (groupId) {
            var deferred = $q.defer();
            Restangular.one('group', groupId).one('statistics').get().then(function(successResult) {
                deferred.resolve(successResult);
            }, function (failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        getAllByType: function (typeId) {
            var deferred = $q.defer();
            Restangular.all('group').one('type', typeId).getList().then(function(successResult) {
                deferred.resolve(successResult);
            }, function (failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        getGroupsForUser: function (userId, getParameters) {
            var deferred = $q.defer();
            // GET /security/user/{userId}/groups?filterText=something&groupTypes=1&page=0&size=5&sortDirection=ASC&sortField=code
            Restangular.all('security').one('user',userId).customGET('groups', getParameters).then(function(successResult) {
                deferred.resolve(successResult);
            }, function (failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        getAllowedRelationshipGroups: function (userId) {
            var deferred = $q.defer();
            // GET /security/user/allowedrelationshipgroups
            Restangular.all('security').one('user',userId).customGET('allowedrelationshipgroups').then(function(successResult) {
                deferred.resolve(successResult);
            }, function (failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        getUsersByType: function (groupId, roleType) {
            var deferred = $q.defer();
            Restangular.one('group', groupId).all('user').getList({'roleType': roleType}).then(function(successResult) {
                deferred.resolve(successResult);
            }, function (failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        // save group
        save: function (inputGroup, groupTypes) {
            var deferred = $q.defer();

            var groupType = UtilService.cleanObject(_.findWhere(groupTypes, {id: inputGroup.groupTypeId}),'groupType');
            var group = UtilService.cleanObject(inputGroup, 'group');

            group.groupType = groupType;

            // PUT /group
            Restangular.all('group').customPUT(group).then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        // create new group
        new: function (inputGroup, groupTypes) {
            var deferred = $q.defer();
            var i;

            // clean group features
            var cleanGroupFeatures = [];
            for (var j=0;j<inputGroup.groupFeatures.length;j++) {
                var groupFeature = inputGroup.groupFeatures[j];
                var feature = {'id':groupFeature.feature.id,'name':groupFeature.feature.name,'description':''};
                cleanGroupFeatures.push({'feature':feature});
            }

            // clean childGroups
            var cleanChildGroups = [];
            for (i=0;i<inputGroup.childGroups.length;i++) {
                var childGroup = UtilService.cleanObject(inputGroup.childGroups[i], 'group');
                delete childGroup.childGroups;
                delete childGroup.parentGroups;
                delete childGroup.groupFeatures;
                cleanChildGroups.push(childGroup);
            }

            // clean parentGroups
            var cleanParentGroups = [];
            for (i=0;i<inputGroup.parentGroups.length;i++) {
                var parentGroup = UtilService.cleanObject(inputGroup.parentGroups[i], 'group');
                delete parentGroup.childGroups;
                delete parentGroup.parentGroups;
                delete parentGroup.groupFeatures;
                cleanParentGroups.push(parentGroup);
            }

            // clean contactPoints
            var cleanContactPoints = [];
            for (i=0;i<inputGroup.contactPoints.length;i++) {
                var contactPoint = UtilService.cleanObject(inputGroup.contactPoints[i], 'contactPoint');
                contactPoint.contactPointType = UtilService.cleanObject(contactPoint.contactPointType, 'contactPointType');
                if (contactPoint.id < 0) {
                    delete contactPoint.id;
                }
                cleanContactPoints.push(contactPoint);
            }

            // convert group and standard type ids to actual objects and clean
            var groupType = UtilService.cleanObject(_.findWhere(groupTypes, {id: inputGroup.groupTypeId}),'groupType');
            var group = UtilService.cleanObject(inputGroup, 'group');

            // add cleaned objects
            group.groupFeatures = cleanGroupFeatures;
            group.contactPoints = cleanContactPoints;
            group.childGroups = cleanChildGroups;
            group.parentGroups = cleanParentGroups;
            group.groupType = groupType;

            // clean negative number IDs
            for (i=0;i<group.links.length;i++) {
                var link = group.links[i];
                if (link.id < 0) {
                    delete link.id;
                }
            }
            for (i=0;i<group.locations.length;i++) {
                var location = group.locations[i];
                if (location.id < 0) {
                    delete location.id;
                }
            }

            Restangular.all('group').post(group).then(function(successResult) {
                deferred.resolve(successResult);
                successResult.parentGroups = successResult.parents;
                successResult.childGroups = successResult.children;
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        getChildren: function (groupId) {
            var deferred = $q.defer();
            Restangular.all('group/' + groupId + '/children').getList().then(function(successResult) {
                deferred.resolve(successResult);
            }, function (failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        // Add new link to group
        addLink: function (group, link) {
            var deferred = $q.defer();
            // POST /group/{groupId}/links
            Restangular.one('group', group.id).all('links').post(link).then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        // Add new location to group
        addLocation: function (group, location) {
            var deferred = $q.defer();
            // POST /group/{groupId}/locations
            Restangular.one('group', group.id).all('locations').post(location).then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        // Add new feature to group
        addFeature: function (group, featureId) {
            var deferred = $q.defer();
            // PUT /group/{groupId}/features/{featureId}
            Restangular.one('group', group.id).one('features',featureId).put().then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        // Delete feature from group
        deleteFeature: function (group, feature) {
            var deferred = $q.defer();
            // DELETE /group/{groupId}/features/{featureId}
            Restangular.one('group', group.id).one('features',feature.id).remove().then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        // Add new parent group to group relationships
        addParentGroup: function (group, parentGroupId) {
            var deferred = $q.defer();
            // PUT /group/{groupId}/parent/{parentGroupId}
            Restangular.one('group', group.id).one('parent',parentGroupId).put().then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        // Delete parent from group relationships
        deleteParentGroup: function (group, parentGroup) {
            var deferred = $q.defer();
            // DELETE /group/{groupId}/parent/{parentGroupId}
            Restangular.one('group', group.id).one('parent',parentGroup.id).remove().then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        // Add new child group to group relationships
        addChildGroup: function (group, childGroupId) {
            var deferred = $q.defer();
            // PUT /group/{groupId}/child/{childGroupId}
            Restangular.one('group', group.id).one('child',childGroupId).put().then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        // Delete child from group relationships
        deleteChildGroup: function (group, childGroup) {
            var deferred = $q.defer();
            // DELETE /group/{groupId}/child/{childGroupId}
            Restangular.one('group', group.id).one('child',childGroup.id).remove().then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        // Add new contactPoint to group
        addContactPoint: function (group, contactPoint) {
            var deferred = $q.defer();
            contactPoint.contactPointType = UtilService.cleanObject(contactPoint.contactPointType, 'contactPointType');
            delete contactPoint.id;
            // POST /group/{groupId}/contactpoints
            Restangular.one('group', group.id).all('contactpoints').post(UtilService.cleanObject(contactPoint,
                'contactPoint')).then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },        // save join request
        contactUnit: function (groupId, unitRequest) {
            unitRequest = UtilService.cleanObject(unitRequest, 'unitRequest');

            // correctly format DOB
            unitRequest.dateOfBirth = unitRequest.dateOfBirth.split('-')[2] + '-'
                + unitRequest.dateOfBirth.split('-')[1] + '-' + unitRequest.dateOfBirth.split('-')[0];

            var deferred = $q.defer();
            Restangular.all('group/' + groupId + '/contactunit').customPOST(unitRequest).then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        }
    };
}]);
