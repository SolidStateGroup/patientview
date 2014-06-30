'use strict';

angular.module('patientviewApp').factory('CodeService', ['$q', 'Restangular', 'UtilService', function ($q, Restangular, UtilService) {
    return {
        getAll: function () {
            var deferred = $q.defer();
            Restangular.all('code').getList().then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        get: function (codeId) {
            var deferred = $q.defer();
            Restangular.one('code',codeId).get().then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        // create new code
        new: function (code, codeTypes, standardTypes) {
            var deferred = $q.defer();

            // convert code and standard type ids to actual objects and clean
            code.codeType = UtilService.cleanObject(_.findWhere(codeTypes, {id: code.codeTypeId}),'codeType');
            code.standardType = UtilService.cleanObject(_.findWhere(standardTypes, {id: code.standardTypeId}),'standardType');
            var codeToPost = _.clone(code);
            delete codeToPost.codeTypeId;
            delete codeToPost.standardTypeId;

            Restangular.all('code').post(codeToPost).then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        // save code
        save: function (inputCode, codeTypes, standardTypes) {
            var deferred = $q.defer();

            var codeType = UtilService.cleanObject(_.findWhere(codeTypes, {id: inputCode.codeTypeId}),'codeType');
            var standardType = UtilService.cleanObject(_.findWhere(standardTypes, {id: inputCode.standardTypeId}),'standardType');
            var code = UtilService.cleanObject(inputCode, 'code');
            code.codeType = codeType;
            code.standardType = standardType;

            Restangular.all('code').customPUT(code).then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        // clone code
        clone: function (codeId, codeTypes, standardTypes) {
            var deferred = $q.defer();
            Restangular.one('code',codeId).post('clone').then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        // Remove a single code based on userId
        delete: function (code) {
            var deferred = $q.defer();
            // GET then DELETE /user/{userId}
            Restangular.one('code', code.id).remove().then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        }
    };
}]);
