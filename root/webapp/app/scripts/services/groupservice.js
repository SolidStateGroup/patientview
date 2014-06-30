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

            // convert group and standard type ids to actual objects and clean
            var groupType = UtilService.cleanObject(_.findWhere(groupTypes, {id: inputGroup.groupTypeId}),'groupType');
            var group = UtilService.cleanObject(inputGroup, 'group');
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
