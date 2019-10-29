'use strict';

angular.module('patientviewApp').factory('DiaryRecordingService', ['$q', 'Restangular', 'UtilService', function ($q, Restangular, UtilService) {
    return {
        /*getAll: function (userid) {
            var deferred = $q.defer(); 
            // POST /user/{userId}/hospitalisations?adminId=123
            Restangular.one('user', userid).all('insdiary').getList().then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },*/
        getPaged: function (userid, page, size) {
            var deferred = $q.defer(); 
            // POST /user/{userId}/insdiary?page=0&size=10
            Restangular.one('user', userid).one('insdiary').get({page: page, size: size}).then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        // GET /user/{userId}/hospitalisations/{recordId}
        get: function (userid, recordid) {
            var deferred = $q.defer();
            Restangular.one('user', userid).one('insdiary', recordid).get().then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        save: function (userid, record, adminid) {
            var deferred = $q.defer();
            // PUT /user/{userId}/insdiary?adminId=123 
            Restangular.one('user', userid).one('insdiary').customPUT(record, '?adminId=' + adminid).then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        post: function (userid, record, adminid) {
            var deferred = $q.defer();
            // POST /user/{userId}/insdiary?adminId=123
            Restangular.one('user', userid).one('insdiary').customPOST(record, '?adminId=' + adminid).then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        remove: function (userid, recordid, adminid) {
            var deferred = $q.defer();
            // DELETE /user/{userId}/insdiary/{recordId}?adminId=123 
            Restangular.one('user', userid).one('insdiary', recordid).remove({ 'adminId': adminid }).then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        }
    };
}]);
