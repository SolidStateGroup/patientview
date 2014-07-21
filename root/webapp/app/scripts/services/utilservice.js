'use strict';

angular.module('patientviewApp').factory('UtilService', [function () {
    return {
        generatePassword: function () {
            var password = '';
            var possible = 'ABCDEFGHKMNPQRSTUVWXYZabcdefghkmnopqrstuvwxyz123456789';
            for (var k=0;k<9;k++) {
                password += possible.charAt(Math.floor(Math.random() * possible.length));
            }
            return password;
        },
        generateVerificationCode: function () {
            var code = '';
            var possible = 'ABCDEFGHKMNPQRSTUVWXYZabcdefghkmnopqrstuvwxyz123456789';
            for (var k=0;k<50;k++) {
                code += possible.charAt(Math.floor(Math.random() * possible.length));
            }
            return code;
        },

        validateEmail: function (email)
        {
            var re = /\S+@\S+\.\S+/;
            return !re.test(email);
        },

        // Used when cleaning objects before they are passed to REST service, object fields to keep
        getFields: function (objectType) {
            var fields = [];
            fields.user = ['id', 'username', 'password', 'email', 'name', 'changePassword', 'locked', 'userFeatures', 'verified', 'verificationCode', 'identifiers', 'contactNumber'];
            fields.role = ['id','name','description','routes'];
            fields.group = ['id','name','code','sftpUser','groupType','groupFeatures','routes','links','locations','childGroups','parentGroups','children','parents','visible','visibleToJoin'];
            fields.code = ['id','code','codeType','standardType','description','links'];
            fields.codeType = ['id','value','lookupType'];
            fields.standardType = ['id','value','lookupType'];
            fields.groupType = ['id','value','lookupType'];
            fields.identifierType = ['id','value','lookupType'];
            return fields[objectType];
        },
        // used when converting from angular objects to those suitable for REST
        cleanObject: function (object, objectType) {
            var cleanObject = {}, fields = this.getFields(objectType);
            for (var field in object) {
                if (object.hasOwnProperty(field) && _.contains(fields, field)) {
                    cleanObject[field] = object[field];
                }
            }
            return cleanObject;
        }
    };
}]);
