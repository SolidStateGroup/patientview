'use strict';

angular.module('patientviewApp').factory('CodeService', ['$q', 'Restangular', 'UtilService', function ($q, Restangular, UtilService) {
    return {
        getAll: function (getParameters) {
            var deferred = $q.defer();
            // GET /code?codeTypes=1&filterText=something&page=0&size=5&sortDirection=ASC&sortField=code&standardTypes=2
            Restangular.one('code').get(getParameters).then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        getPatientViewStandardCodes: function (searchTerm) {
            var deferred = $q.defer();
            // GET /codes/patientviewstandard/{searchTerm}
            Restangular.one('codes/patientviewstandard', searchTerm).getList().then(function(successResult) {
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
        getByCategory: function(categoryId) {
            var deferred = $q.defer();
            // GET /codes/category/{categoryId}
            Restangular.one('codes/category').getList(categoryId).then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        getCategories: function() {
            var deferred = $q.defer();
            // GET /categories
            Restangular.one('categories').getList().then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        // create new code
        create: function (code, codeTypes, standardTypes) {
            var deferred = $q.defer();

            // convert code and standard type ids to actual objects and clean
            code.codeType = UtilService.cleanObject(_.findWhere(codeTypes, {id: code.codeTypeId}),'codeType');
            code.standardType = UtilService.cleanObject(_.findWhere(standardTypes, {id: code.standardTypeId}),'standardType');
            var codeToPost = _.clone(code);
            delete codeToPost.codeTypeId;
            delete codeToPost.standardTypeId;

            // clean negative number IDs
            for (var i=0;i<codeToPost.links.length;i++) {
                var link = codeToPost.links[i];
                if (link.id < 0) {
                    delete link.id;
                }
            }

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
        clone: function (codeId) {
            var deferred = $q.defer();
            Restangular.one('code',codeId).post('clone').then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        // Remove a single code based on userId
        remove: function (code) {
            var deferred = $q.defer();
            // GET then DELETE /user/{userId}
            Restangular.one('code', code.id).remove().then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        // Add new link to code
        addLink: function (code, link) {
            var deferred = $q.defer();
            // POST /code/{codeId}/links
            Restangular.one('code', code.id).all('links').post(link).then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        // Add new externalStandard to code
        addCodeExternalStandard: function (code, codeExternalStandard) {
            var deferred = $q.defer();
            // POST /code/{codeId}/externalstandards
            Restangular.one('code', code.id).all('externalstandards').post(codeExternalStandard)
                .then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        }
    };
}]);
