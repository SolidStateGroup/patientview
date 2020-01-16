'use strict';

angular.module('patientviewApp').factory('ImmunisationService', ['$q', 'Restangular', 'UtilService', function ($q, Restangular, UtilService) {
    return {
        getAll: function (userid) {
            var deferred = $q.defer();
            // POST /user/{userId}/immunisations?adminId=123
            Restangular.one('user', userid).all('immunisations').getList().then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        // GET /user/{userId}/immunisations/{recordId}
        get: function (userid, recordid) {
            var deferred = $q.defer();
            Restangular.one('user', userid).one('immunisations', recordid).get().then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        save: function (userid, recordid, record, adminid) {
            var deferred = $q.defer();
            // PUT /user/{userId}/immunisations/{recordId}?adminId=123 
            Restangular.one('user', userid).one('immunisations', recordid).customPUT(record, '?adminId=' + adminid).then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        post: function (userid, record, adminid) {
            var deferred = $q.defer();
            // POST /user/{userId}/immunisations?adminId=123 
            Restangular.one('user', userid).one('immunisations').customPOST(record, '?adminId=' + adminid).then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        remove: function (userid, recordid, adminid) {
            var deferred = $q.defer();
            // DELETE /user/{userId}/immunisations/{recordId}?adminId=123 
            Restangular.one('user', userid).one('immunisations', recordid).remove({ 'adminId': adminid }).then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        }
    };
}]);
