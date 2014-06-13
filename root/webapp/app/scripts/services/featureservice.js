// not currently used

'use strict';

angular.module('patientviewApp').factory('FeatureService', ['$q', 'Restangular',
    function ($q, Restangular) {
        return {
            getAll: function () {
                var deferred = $q.defer();
                Restangular.all('feature').getList().then(function(res) {
                    deferred.resolve(res);
                });
                return deferred.promise;
            }
        };
    }]);
