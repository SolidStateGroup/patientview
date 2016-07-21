'use strict';

angular.module('patientviewApp').factory('CategoryService', ['$q', 'Restangular', 'UtilService',
    function ($q, Restangular, UtilService) {
    return {
        create: function (category) {
            var deferred = $q.defer();
            // POST /categories
            Restangular.all('categories').post(category).then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        get: function (categoryId) {
            var deferred = $q.defer();
            // GET /categories/{categoryId}
            Restangular.one('categories', categoryId).get().then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        getAll: function (getParameters) {
            var deferred = $q.defer();
            // GET /categories?filterText=something&page=0&size=5&sortDirection=ASC&sortField=code
            Restangular.one('categories').get(getParameters).then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        remove: function (categoryId) {
            var deferred = $q.defer();
            // DELETE /categories/{categoryId}
            Restangular.one('categories', categoryId).remove().then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        save: function (category) {
            var deferred = $q.defer();
            category = UtilService.cleanObject(category, 'category');

            // PUT /categories
            Restangular.all('categories').customPUT(category).then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        }
    };
}]);
