'use strict';

angular.module('patientviewApp').factory('UtilService', [function () {
    return {
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
        generateAlphabet: function () {
            var arr = [];
            var letters = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ';
            for (var i=0; i<letters.length; i++) {
                arr.push(letters[i])
            }
            return letters;
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
        generateHours: function () {
            var hours = [];
            for (var i=0;i<=23;i++) {
                if (i<10) {
                    hours.push('0' + i);
                } else {
                    hours.push(i);
                }
            }
            return hours;
        },
        generateMinutes: function () {
            var minutes = [];
            for (var i=0;i<60;i++) {
                if (i<10) {
                    minutes.push('0' + i);
                } else {
                    minutes.push(i);
                }
            }
            return minutes;
        },
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
        generateYears: function (minYear, maxYear) {
            var years = [];
            years.push('');
            if (maxYear === undefined || maxYear === null) {
                maxYear = new Date().getFullYear();
            }
            if (minYear === undefined || minYear === null) {
                minYear = 1900;
            }

            for (var i=maxYear;i>=minYear;i--) {
                years.push(i);
            }
            return years;
        },
        generateYears2000: function () {
            var years = [];
            years.push('');
            for (var i=new Date().getFullYear();i>=2000;i--) {
                years.push(i);
            }
            return years;
        },
        generateYears1990: function () {
            var years = [];
            years.push('');
            for (var i=new Date().getFullYear();i>=1990;i--) {
                years.push(i);
            }
            return years;
        },
        // Used when cleaning objects before they are passed to REST service, object fields to keep
        getFields: function (objectType) {
            var fields = [];
            fields.user = ['id', 'username', 'password', 'email', 'forename', 'surname', 'changePassword', 'locked', 'userFeatures', 'emailVerified', 'verificationCode', 'identifiers', 'contactNumber', 'locked', 'dummy', 'dateOfBirth', 'roleDescription'];
            fields.userDetails = ['id', 'username', 'email', 'forename', 'surname', 'locked', 'emailVerified', 'dummy', 'contactNumber', 'dateOfBirth', 'roleDescription'];
            fields.role = ['id','name','description','routes'];
            fields.group = ['id','name','shortName','code','sftpUser','groupType','groupFeatures','routes','links','locations','contactPoints','childGroups','parentGroups','children','parents','visible','visibleToJoin','address1','address2','address3','postcode'];
            fields.groupDetails = ['id','name','code','sftpUser','groupType','visibleToJoin','address1','address2','address3','postcode'];
            fields.code = ['id','code','codeType','standardType','description','links','fullDescription', 'patientFriendlyName', 'hideFromPatients'];
            fields.codeType = ['id','value','description','lookupType'];
            fields.request = ['id','forename','surname', 'nhsNumber', 'status', 'type', 'notes', 'dateOfBirth', 'email', 'captcha'];
            fields.standardType = ['id','value','description','lookupType'];
            fields.groupType = ['id','value','lookupType'];
            fields.identifierType = ['id','value','description','lookupType'];
            fields.contactPoint = ['id','contactPointType','content'];
            fields.contactPointType = ['id','value','lookupType'];
            fields.link = ['id','displayOrder','link','linkType','name'];
            fields.location = ['id','label','name','phone','address','web','email'];
            fields.identifier = ['id','identifier','identifierType'];
            fields.newsItem = ['id','heading','story','newsLinks', 'newsType'];
            fields.unitRequest = ['forename','surname', 'nhsNumber','dateOfBirth', 'email'];
            fields.observationHeading = ['id', 'code','heading', 'name','normalRange', 'units', 'minGraph', 'maxGraph', 'infoLink', 'defaultPanel', 'defaultPanelOrder', 'observationHeadingGroups', 'decimalPlaces'];
            fields.observationHeadingGroup = ['id', 'group','panel', 'panelOrder'];
            fields.resultCluster = ['id', 'day','month', 'year', 'hour', 'minute', 'values', 'comments'];
            fields.externalStandard = ['id', 'name','description'];
            fields.category = ['id', 'friendlyDescription', 'icd10Description', 'number', 'hidden'];
            fields.dialysisTreatment = ['day', 'month', 'year', 'hour', 'minute', 'HdHours', 'HdLocation', 'eprex', 'TargetWeight', 'PreWeight',  'PostWeight', 'UfVolume', 'pulse', 'PreBpsys', 'PreBpdia', 'PostBpdia', 'PostBpsys', 'hypotension', 'BodyTemperature', 'bps','DialFlow', 'LitresProcessed', 'comments'];
            return fields[objectType];
        },
        getMonthText: function(month) {
            var monthNames = [ 'January', 'February', 'March', 'April', 'May', 'June',
                'July', 'August', 'September', 'October', 'November', 'December' ];
            return monthNames[parseInt(month)];
        },
        getMonthTextShort: function(month) {
            var monthNames = [ 'Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun',
                'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec' ];
            return monthNames[parseInt(month)];
        },
        validationDate: function (day, month, year) {
            if (day === undefined || month === undefined || year === undefined) {
                return false;
            }

            // strip preceding 0 on dates if present
            day = parseInt(day.toString(), 10);
            month = parseInt(month.toString(), 10);
            year = parseInt(year.toString(), 10);

            if (isNaN(day) || isNaN(month) || isNaN(year)) {
                return false;
            }
            if ((month < 1) || (month > 12)) {
                return false;
            }
            else if ((day < 1) || (day > 31)) {
                return false;
            }
            else if (((month === 4) || (month === 6) || (month === 9) || (month === 11)) && (day > 30)) {
                return false;
            }
            else if ((month === 2) && (((year % 400) === 0) || ((year % 4) === 0)) && ((year % 100) !== 0) && (day > 29)) {
                return false;
            }
            else if ((month === 2) && ((year % 100) === 0) && (day > 29)) {
                return false;
            }

            // validate leap years (more robust)
            var input = new Date(month+'/'+day+'/'+year);
            if (month !== input.getMonth()+1) {
                return false;
            }

            return true;
        },
        validationDateNoFuture: function (day, month, year) {
            if (day === undefined || month === undefined || year === undefined) {
                return false;
            }

            // strip preceding 0 on dates if present
            day = parseInt(day.toString(), 10);
            month = parseInt(month.toString(), 10);
            year = parseInt(year.toString(), 10);

            if (isNaN(day) || isNaN(month) || isNaN(year)) {
                return false;
            }
            if ((month < 1) || (month > 12)) {
                return false;
            }
            else if ((day < 1) || (day > 31)) {
                return false;
            }
            else if (((month === 4) || (month === 6) || (month === 9) || (month === 11)) && (day > 30)) {
                return false;
            }
            else if ((month === 2) && (((year % 400) === 0) || ((year % 4) === 0)) && ((year % 100) !== 0) && (day > 29)) {
                return false;
            }
            else if ((month === 2) && ((year % 100) === 0) && (day > 29)) {
                return false;
            }

            // validate leap years (more robust)
            var input = new Date(month+'/'+day+'/'+year);
            if (month !== input.getMonth()+1) {
                return false;
            }

            var now = new Date();
            return now.getTime() > input.getTime();
        },
        validateEmail: function (email)
        {
            var re = /\S+@\S+\.\S+/;
            return !re.test(email);
        }
    };
}]);
