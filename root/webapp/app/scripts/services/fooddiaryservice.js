// not currently used

'use strict';

angular.module('patientviewApp').factory('FoodDiaryService', ['$q', 'Restangular', function ($q, Restangular) {
    return {
        add: function (userId, foodDiary) {
            var deferred = $q.defer();
            Restangular.one('user', userId).all('fooddiary').customPOST(foodDiary).then(function(res) {
                deferred.resolve(res);
            });
            return deferred.promise;
        },
        get: function (userId) {
            var deferred = $q.defer();
            Restangular.one('user', userId).all('fooddiary').getList().then(function(res) {
                deferred.resolve(res);
            });
            return deferred.promise;
        },
        remove: function (userId, foodDiaryId) {
            var deferred = $q.defer();
            Restangular.one('user', userId).one('fooddiary', foodDiaryId).remove().then(function(res) {
                deferred.resolve(res);
            });
            return deferred.promise;
        },
        update: function (userId, foodDiary) {
            var deferred = $q.defer();
            Restangular.one('user', userId).all('fooddiary').customPUT(foodDiary).then(function(res) {
                deferred.resolve(res);
            });
            return deferred.promise;
        }
    };
}]);
