'use strict';

angular.module('patientviewApp').factory('AuthService', ['$q', 'Restangular',
    function ($q, Restangular) {
    return {
        login: function (usernameAndPassword) {
            var deferred = $q.defer();
            Restangular.all('auth/login').post(usernameAndPassword).then(function(res) {
                deferred.resolve(res);
            });
            return deferred.promise;
        }
    };
}]);
