'use strict';

angular.module('patientviewApp').factory('SurveyResponseService', ['$q', 'Restangular',
function ($q, Restangular) {
    return {
        getSurveyResponse: function (userId, surveyResponseId) {
            var deferred = $q.defer();
            // GET /user/{userId}/surveyresponses/{surveyResponseId}
            Restangular.one('user', userId).one('surveyresponses', surveyResponseId).get().then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        getByUserAndSurveyType: function (userId, surveyType) {
            var deferred = $q.defer();
            // GET /user/{userId}/surveyresponses/type/{surveyType}
            Restangular.one('user', userId).one('surveyresponses/type', surveyType).getList().then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        add: function (userId, surveyResponse) {
            var deferred = $q.defer();
            // POST /user/{userId}/surveyresponses
            Restangular.one('user', userId).all('surveyresponses').post(surveyResponse).then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        }
    };
}]);
