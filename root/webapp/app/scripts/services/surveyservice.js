'use strict';

angular.module('patientviewApp').factory('SurveyService', ['$q', 'Restangular',
    function ($q, Restangular) {
        return {
            addFeedback: function (userId, surveyFeedback) {
                var deferred = $q.defer();
                // POST /user/{userId}/surveys/feedback
                Restangular.one('user', userId).one('surveys/feedback').customPOST(surveyFeedback).then(function(successResult) {
                    deferred.resolve(successResult);
                }, function(failureResult) {
                    deferred.reject(failureResult);
                });
                return deferred.promise;
            },
            getByType: function (type) {
                var deferred = $q.defer();
                // GET /surveys/type/{type}
                Restangular.one('surveys/type', type).get().then(function(successResult) {
                    deferred.resolve(successResult);
                }, function(failureResult) {
                    deferred.reject(failureResult);
                });
                return deferred.promise;
            },
            getFeedback: function (userId, surveyId) {
                var deferred = $q.defer();
                // GET /user/{userId}/surveys/{surveyId}/feedback/
                Restangular.one('user', userId).one('surveys', surveyId).getList('feedback').then(function(successResult) {
                    deferred.resolve(successResult);
                }, function(failureResult) {
                    deferred.reject(failureResult);
                });
                return deferred.promise;
            }
        };
    }]);
