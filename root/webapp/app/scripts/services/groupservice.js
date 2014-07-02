'use strict';

angular.module('patientviewApp').factory('GroupService', ['$q', 'Restangular', 'UtilService',
function ($q, Restangular, UtilService) {
    return {
        get: function (groupId) {
            var deferred = $q.defer();
            Restangular.one('group', groupId).get().then(function(res) {
                deferred.resolve(res);
            });
            return deferred.promise;
        },
        getAll: function () {
            var deferred = $q.defer();
            Restangular.all('group').getList().then(function(res) {
                deferred.resolve(res);
            });
            return deferred.promise;
        },
        getUsersByType: function (groupId, roleType) {
            var deferred = $q.defer();
            Restangular.one('group', groupId).all('user').getList({'roleType': roleType}).then(function(res) {
                deferred.resolve(res);
            });
            return deferred.promise;
        },
        // save group
        save: function (inputGroup, groupTypes) {
            var deferred = $q.defer();
            var i = 0;

            // clean group features
            var cleanGroupFeatures = [];
            for (i=0;i<inputGroup.groupFeatures.length;i++) {
                var groupFeature = inputGroup.groupFeatures[i];
                var feature = {'id':groupFeature.feature.id,'name':groupFeature.feature.name,'description':''};
                cleanGroupFeatures.push({'feature':feature});
            }

            // clean childGroups
            var cleanChildGroups = [];
            for (i=0;i<inputGroup.childGroups.length;i++) {
                cleanChildGroups.push(UtilService.cleanObject(inputGroup.childGroups[i], 'group'));
            }

            // clean parentGroups
            var cleanParentGroups = [];
            for (i=0;i<inputGroup.parentGroups.length;i++) {
                cleanParentGroups.push(UtilService.cleanObject(inputGroup.parentGroups[i], 'group'));
            }

            var groupType = UtilService.cleanObject(_.findWhere(groupTypes, {id: inputGroup.groupTypeId}),'groupType');
            var group = UtilService.cleanObject(inputGroup, 'group');

            // add cleaned objects
            group.groupFeatures = cleanGroupFeatures;
            group.childGroups = cleanChildGroups;
            group.parentGroups = cleanParentGroups;
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
            var i = 0;

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
                cleanChildGroups.push(UtilService.cleanObject(inputGroup.childGroups[i], 'group'));
            }

            // clean parentGroups
            var cleanParentGroups = [];
            for (i=0;i<inputGroup.parentGroups.length;i++) {
                cleanParentGroups.push(UtilService.cleanObject(inputGroup.parentGroups[i], 'group'));
            }

            // convert group and standard type ids to actual objects and clean
            var groupType = UtilService.cleanObject(_.findWhere(groupTypes, {id: inputGroup.groupTypeId}),'groupType');
            var group = UtilService.cleanObject(inputGroup, 'group');

            // add cleaned objects
            group.groupFeatures = cleanGroupFeatures;
            group.childGroups = cleanChildGroups;
            group.parentGroups = cleanParentGroups;
            group.groupType = groupType;

            Restangular.all('group').post(group).then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        }
    };
}]);
