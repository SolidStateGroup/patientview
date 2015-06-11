'use strict';

angular.module('patientviewApp').factory('SymptomScoreService', ['$q', 'Restangular',
function ($q, Restangular) {
    return {
        getSymptomScore: function (userId, symptomScoreId) {
            var deferred = $q.defer();
            // GET /user/{userId}/symptomscore/{symptomScoreId}
            Restangular.one('user', userId).one('symptomscore', symptomScoreId).get().then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        getByUser: function (userId) {
            var deferred = $q.defer();
            // GET /user/{userId}/symptomscores
            Restangular.one('user', userId).one('symptomscores').getList().then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        getByUserAndSurveyType: function (userId, surveyType) {
            var deferred = $q.defer();
            // GET /user/{userId}/symptomscores/{surveyType}
            Restangular.one('user', userId).one('symptomscores', surveyType).getList().then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        add: function (userId, symptomScore) {
            var deferred = $q.defer();
            // POST /user/{userId}/symptomscores
            Restangular.one('user', userId).all('symptomscores').post(symptomScore).then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        }
    };
}]);
