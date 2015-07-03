'use strict';

angular.module('patientviewApp').factory('SurveyService', ['$q', 'Restangular',
    function ($q, Restangular) {
        return {
            getByType: function (type) {
                var deferred = $q.defer();
                // GET /surveys/type/{type}
                Restangular.one('surveys/type', type).get().then(function(successResult) {
                    deferred.resolve(successResult);
                }, function(failureResult) {
                    deferred.reject(failureResult);
                });
                return deferred.promise;
            }
        };
    }]);
