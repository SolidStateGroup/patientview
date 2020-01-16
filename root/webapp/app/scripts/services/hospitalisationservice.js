'use strict';

angular.module('patientviewApp').factory('HostpitalisationService', ['$q', 'Restangular', 'UtilService', function ($q, Restangular, UtilService) {
    return {
        getAll: function (userid) {
            var deferred = $q.defer();
            // POST /user/{userId}/hospitalisations?adminId=123
            Restangular.one('user', userid).all('hospitalisations').getList().then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        // GET /user/{userId}/hospitalisations/{recordId}
        get: function (userid, recordid) {
            var deferred = $q.defer();
            Restangular.one('user', userid).one('hospitalisations', recordid).get().then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        save: function (userid, recordid, record, adminid) {
            var deferred = $q.defer();
            // PUT /user/{userId}/hospitalisations/{recordId}?adminId=123 
            Restangular.one('user', userid).one('hospitalisations', recordid).customPUT(record, '?adminId=' + adminid).then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        post: function (userid, record, adminid) {
            var deferred = $q.defer();
            // POST /user/{userId}/hospitalisations?adminId=123 
            Restangular.one('user', userid).one('hospitalisations').customPOST(record, '?adminId=' + adminid).then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        remove: function (userid, recordid, adminid) {
            var deferred = $q.defer();
            // DELETE /user/{userId}/hospitalisations/{recordId}?adminId=123 
            Restangular.one('user', userid).one('hospitalisations', recordid).remove({ 'adminId': adminid }).then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        }
    };
}]);
