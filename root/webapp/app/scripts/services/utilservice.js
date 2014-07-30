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
            fields.user = ['id', 'username', 'password', 'email', 'forename', 'surname', 'changePassword', 'locked', 'userFeatures', 'verified', 'verificationCode', 'identifiers', 'contactNumber'];
            fields.role = ['id','name','description','routes'];
            fields.group = ['id','name','code','sftpUser','groupType','groupFeatures','routes','links','locations','contactPoints','childGroups','parentGroups','children','parents','visible','visibleToJoin','address1','address2','address3','postcode'];
            fields.code = ['id','code','codeType','standardType','description','links'];
            fields.codeType = ['id','value','lookupType','surname','dateOfBirth', 'nhsNumber', 'specialty', 'unit', 'email'];
            fields.joinRequest = ['id','forename','surname', 'nhsNumber', 'dateOfBirth', 'email'];
            fields.standardType = ['id','value','lookupType'];
            fields.groupType = ['id','value','lookupType'];
            fields.identifierType = ['id','value','lookupType'];
            fields.contactPoint = ['id','contactPointType','content'];
            fields.contactPointType = ['id','value','lookupType'];
            fields.link = ['id','displayOrder','link','linkType','name'];
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
        },

        generateDays: function () {
            var days = [];
            for (var i=1;i<=31;i++) {
                days.push(i);
            }
            return days
        },

        generateMonths: function () {
            var months = [];
            for (var i=1;i<=13;i++) {
                months.push(i);
            }
            return months;
        },

        generateYears: function () {
            var years = [];
            for (var i=new Date().getFullYear();i>=1920;i--) {
                years.push(i);
            }
            return years;
        }

    };
}]);
