'use strict';

angular.module('patientviewApp').factory('DonorPathwayService', ['$q', 'Restangular', function ($q, Restangular) {
    var user;

    return {
        setUser(donor) {
          user = donor;
        },
        getUser() {
          return user;
        },
        addNote: function (note) {
            var deferred = $q.defer();
            // POST /user/{userId}/notes
            Restangular.one('user', user.id).one('notes').customPOST(note).then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        getNote: function (noteId) {
            var deferred = $q.defer();
            // GET /user/{userId}/notes/{noteId}
            Restangular.one('user', user.id).one('notes', noteId).get().then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        updateNote: function (note) {
            var deferred = $q.defer();
            // PUT /user/{userId}/notes
            Restangular.one('user', user.id).one('notes').customPUT(note).then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        deleteNote: function (noteId) {
            var deferred = $q.defer();
            // DELETE /user/{userId}/notes/{noteId}
            Restangular.one('user', user.id).one('notes', noteId).remove().then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        getNotes: function (noteType) {
            var deferred = $q.defer();
            // GET /user/{userId}/notes/{noteType}/type
            Restangular.one('user', user.id).one('notes', noteType).one('type').get().then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        getPathway: function (pathwayType) {
            var deferred = $q.defer();
            // GET /user/{userId}/pathway/{pathwayType}
            Restangular.one('user', user.id).one('pathway', pathwayType).get().then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        updatePathway: function (pathway, notify) {
            var deferred = $q.defer();
            // PUT /user/{userId}/pathway/{notify}
            Restangular.one('user', user.id).one('pathway', notify).customPUT(pathway).then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        getStageStatusTooltipText: function (stageStatus) {
            switch (stageStatus) {
                case 'PENDING':
                    return 'Pending';
                case 'STARTED':
                    return 'Started'
                case 'ON_HOLD':
                    return 'On Hold';
                case 'STOPPED':
                    return 'Stopped';
                case 'COMPLETED':
                    return 'Completed';
                default:
                    return '';
            }
        },
        getDate: function (ts) {
            return moment(ts).format('DD/MM/YYYY');
        },
        getStageStatusColour: function (stageStatus) {
            var colour;
            switch (stageStatus) {
                case 'PENDING':
                case 'STARTED':
                default:
                    colour = 'green';
                    break;

                case 'ON_HOLD':
                    colour = '#ffbf00';
                    break;

                case 'STOPPED':
                    colour = 'red';
                    break;

                case 'COMPLETED':
                    colour = 'gray';
                    break;
            }
            return {'background-color': colour};
        }
    };
}]);
