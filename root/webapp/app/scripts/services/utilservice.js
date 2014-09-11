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

        validationDate: function (day, month, year) {
            // strip preceding 0 on dates if present
            day = parseInt(day.toString());
            month = parseInt(month.toString());

            var valid = true;
            if ((month < 1) || (month > 12)) {
                valid = false;
            }
            else if ((day < 1) || (day > 31)) {
                valid = false;
            }
            else if (((month === 4) || (month === 6) || (month === 9) || (month === 11)) && (day > 30)) {
                valid = false;
            }
            else if ((month === 2) && (((year % 400) === 0) || ((year % 4) === 0)) && ((year % 100) !== 0) && (day > 29)) {
                valid = false;
            }
            else if ((month === 2) && ((year % 100) === 0) && (day > 29)) {
                valid = false;
            }
            return valid;
        },

        // Used when cleaning objects before they are passed to REST service, object fields to keep
        getFields: function (objectType) {
            var fields = [];
            fields.user = ['id', 'username', 'password', 'email', 'forename', 'surname', 'changePassword', 'locked', 'userFeatures', 'emailVerified', 'verificationCode', 'identifiers', 'contactNumber', 'locked', 'dummy'];
            fields.userDetails = ['id', 'username', 'email', 'forename', 'surname', 'locked', 'emailVerified', 'dummy', 'contactNumber'];
            fields.role = ['id','name','description','routes'];
            fields.group = ['id','name','code','sftpUser','groupType','groupFeatures','routes','links','locations','contactPoints','childGroups','parentGroups','children','parents','visible','visibleToJoin','address1','address2','address3','postcode'];
            fields.groupDetails = ['id','name','code','sftpUser','groupType','visibleToJoin','address1','address2','address3','postcode'];
            fields.code = ['id','code','codeType','standardType','description','links'];
            fields.codeType = ['id','value','description','lookupType'];
            fields.joinRequest = ['id','forename','surname', 'nhsNumber', 'status', 'notes', 'dateOfBirth', 'email'];
            fields.standardType = ['id','value','description','lookupType'];
            fields.groupType = ['id','value','lookupType'];
            fields.identifierType = ['id','value','description','lookupType'];
            fields.contactPoint = ['id','contactPointType','content'];
            fields.contactPointType = ['id','value','lookupType'];
            fields.link = ['id','displayOrder','link','linkType','name'];
            fields.location = ['id','label','name','phone','address','web','email'];
            fields.identifier = ['id','identifier','identifierType'];
            fields.newsItem = ['id','heading','story','newsLinks'];
            fields.unitRequest = ['forename','surname', 'nhsNumber','dateOfBirth', 'email'];
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
            days.push('');
            for (var i=1;i<=31;i++) {
                if (i<10) {
                    days.push('0' + i);
                } else {
                    days.push(i);
                }
            }
            return days;
        },

        generateMonths: function () {
            var months = [];
            months.push('');
            for (var i=1;i<=12;i++) {
                if (i<10) {
                    months.push('0' + i);
                } else {
                    months.push(i);
                }
            }
            return months;
        },

        generateYears: function () {
            var years = [];
            years.push('');
            for (var i=new Date().getFullYear();i>=1920;i--) {
                years.push(i);
            }
            return years;
        }
    };
}]);
