'use strict';

angular.module('patientviewApp').factory('CodeService', ['$q', 'Restangular', 'UtilService',
    function ($q, Restangular, UtilService) {
    return {
        // Add category to code
        addCodeCategory: function (codeId, categoryId) {
            var deferred = $q.defer();
            // POST /code/{codeId}/categories/{categoryId}
            Restangular.one('code', codeId).one('categories', categoryId).post().then(function(successResult) {
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
        addCodeExternalStandard: function (codeId, codeExternalStandard) {
            var deferred = $q.defer();
            // POST /code/{codeId}/externalstandards
            Restangular.one('code', codeId).all('externalstandards').post(codeExternalStandard)
                .then(function(successResult) {
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
        // create new code
        create: function (code, codeTypes, standardTypes) {
            var deferred = $q.defer();

            // convert code and standard type ids to actual objects and clean
            code.codeType = UtilService.cleanObject(_.findWhere(codeTypes, {id: code.codeTypeId}),'codeType');
            code.standardType = UtilService.cleanObject(_.findWhere(standardTypes, {id: code.standardTypeId}),'standardType');

            // clean categories
            for (i=0; i<code.codeCategories.length; i++) {
                code.codeCategories[i].category = UtilService.cleanObject(code.codeCategories[i].category, 'category');
            }

            var codeToPost = _.clone(code);
            delete codeToPost.codeTypeId;
            delete codeToPost.standardTypeId;
            delete codeToPost.availableCategories;

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
        // Delete category from code
        deleteCodeCategory: function (codeId, categoryId) {
            var deferred = $q.defer();
            // DELETE /code/{codeId}/categories/{categoryId}
            Restangular.one('code', codeId).one('categories', categoryId).remove().then(function(successResult) {
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
        getAllDiagnosisCodes: function () {
            var deferred = $q.defer();
            // GET /code/diagnosis
            Restangular.one('code/diagnosis').getList().then(function(successResult) {
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
            // GET /categories/all
            Restangular.one('categories/all').getList().then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        getPublic: function (codeId) {
            var deferred = $q.defer();
            // GET /code/public/{codeId}
            Restangular.one('code/public', codeId).get().then(function(successResult) {
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
        searchDiagnosisCodesByStandard: function (searchTerm, standardType) {
            var deferred = $q.defer();
            // GET /codes/diagnosis/{searchTerm}/standard/{standardType}
            Restangular.one('codes/diagnosis', searchTerm).one('standard', standardType).get()
                .then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        searchDiagnosisCodes: function (searchTerm) {
            var deferred = $q.defer();
            // GET /codes/diagnosis/{searchTerm}/standard/{standardType}
            Restangular.one('codes/diagnosis', searchTerm).get()
                .then(function(successResult) {
                    deferred.resolve(successResult);
                }, function(failureResult) {
                    deferred.reject(failureResult);
                });
            return deferred.promise;
        },
        searchTreatmentCodes: function (searchTerm) {
            var deferred = $q.defer();
            // GET /codes/diagnosis/{searchTerm}/standard/{standardType}
            Restangular.one('codes/treatment', searchTerm).get()
                .then(function(successResult) {
                    deferred.resolve(successResult);
                }, function(failureResult) {
                    deferred.reject(failureResult);
                });
            return deferred.promise;
        }
    };
}]);
