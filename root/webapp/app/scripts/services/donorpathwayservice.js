'use strict';

angular.module('patientviewApp').factory('DonorPathwayService', ['$q', 'Restangular', function ($q, Restangular) {
    var userId;

    return {
        setUserId(id) {
          userId = id;
        },
        getUserId() {
          return userId;
        },
        addNote: function (note) {
            var deferred = $q.defer();
            // POST /user/{userId}/notes
            Restangular.one('user', userId).one('notes').customPOST(note).then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        getNote: function (noteId) {
            var deferred = $q.defer();
            // GET /user/{userId}/notes/{noteId}
            Restangular.one('user', userId).one('notes', noteId).get().then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        updateNote: function (note) {
            var deferred = $q.defer();
            // PUT /user/{userId}/notes
            Restangular.one('user', userId).one('notes').customPUT(note).then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        deleteNote: function (noteId) {
            var deferred = $q.defer();
            // DELETE /user/{userId}/notes/{noteId}
            Restangular.one('user', userId).one('notes', noteId).remove().then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        getNotes: function (noteType) {
            var deferred = $q.defer();
            // GET /user/{userId}/notes/{noteType}/type
            Restangular.one('user', userId).one('notes', noteType).one('type').get().then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        getPathway: function (pathwayType) {
            var deferred = $q.defer();
            // GET /user/{userId}/pathway/{pathwayType}
            Restangular.one('user', userId).one('pathway', pathwayType).get().then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        updatePathway: function (pathway) {
            var deferred = $q.defer();
            // PUT /user/{userId}/pathway
            Restangular.one('user', userId).one('pathway').customPUT(pathway).then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
    };
}]);
