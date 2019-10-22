'use strict';

angular.module('patientviewApp').controller('DiaryRecordingsCtrl', ['$scope', 'UtilService', 'DiaryRecordingService', '$rootScope',
function ($scope, UtilService, HostpitalisationService, $rootScope) {

    function formatHostpitalisation(d){
        d.originalAdmitted = d.dateAdmitted;
        d.originalDischarged = d.dateDischarged;

        d.dateAdmitted = moment(new Date(d.dateAdmitted)).format('ll');
        d.dateDischarged = d.dateDischarged ? moment(new Date(d.dateDischarged)).format('ll') : '(Ongoing)';

        return d;
    }

    function getDateDropdownVals(date){
        const vals = {};
        for (var i=0;i<$scope.days.length;i++) {
            if (parseInt($scope.days[i]) === date.getDate()) {
                vals.day = $scope.days[i];
            }
        }
        for (var i=0;i<$scope.months.length;i++) {
            if (parseInt($scope.months[i]) === date.getMonth() + 1) {
                vals.month = $scope.months[i];
            }
        }
        for (var i=0;i<$scope.years.length;i++) {
            if (parseInt($scope.years[i]) === date.getFullYear()) {
                vals.year = $scope.years[i];
            }
        }
        for (var i=0;i<$scope.mins.length;i++) {
            if (parseInt($scope.mins[i]) === date.getMinutes() + 1) {
                vals.mins = $scope.mins[i];
            }
        }
        for (var i=0;i<$scope.hrs.length;i++) {
            if (parseInt($scope.hrs[i]) === date.getHours()) {
                vals.hrs = $scope.hrs[i];
            }
        }
        return vals;
    }

    $scope.addMedication = function(form){
        form.newMedicationCount ++;
        form.medications.push({
            id: form.newMedicationCount,
            name: null,
            other: null,
            doseQty: null,
            doseUnits: null,
            doseFrequency: null,
            route: null,
            started: getDateDropdownVals(new Date()),
            stopped: getDateDropdownVals(new Date()),
        });
    }

    $scope.removeMedication = function(form, id){
        form.medications = form.medications.filter(function(m){
            console.log(m.id, id);
            return m.id !== id;
        });
    }

    $scope.addOedema = function(form){
        if(form.oedema.filter(function(o){return o === form.newOedema}).length === 0){
            form.oedema.push(form.newOedema);
            delete form.errors.newOedema;
        } else {
            form.errors.newOedema = "Already Exists!";
        }
    };

     $scope.removeOedema = function(form, oedem){
        form.oedema = form.oedema.filter(function(o){return o !== oedem});
        delete form.errors.newOedema;
    };

    $scope.init = function(){
        $scope.showEdit = null;

        $scope.recordings = [];


        /*  ---- SELECT OPTIONS  ---- */

        $scope.days = UtilService.generateDays().filter(function(x){return !!x});
        $scope.months = UtilService.generateMonths().filter(function(x){return !!x});
        $scope.years = UtilService.generateYears2000().filter(function(x){return !!x});

        $scope.hrs = UtilService.generateHours().filter(function(x){return !!x});
        $scope.mins = UtilService.generateMinutes().filter(function(x){return !!x});

        $scope.yesNo = [{key: 'Yes', val: true}, {key: 'No', val: false}];


        /* - Diary Options - */

        $scope.proteinDipsticks = [{val: 'NEGATIVE', key:'Negative'}, {val: 'TRACE', key:'Trace'}, {val: 'ONE_PLUS', key:'One+'},
            {val: 'TWO_PLUS', key:'Two+'}, {val: 'THREE_PLUS', key:'Three+'}, {val: 'FOUR_PLUS', key:'Four+'}];

        $scope.oedemas = [{val: 'NONE', key:'None'}, {val: 'ANKLES', key:'Ankles'}, {val: 'LEGS', key:'Legs'}, {val: 'HANDS', key:'Hands'}, 
            {val: 'ABDOMEN', key:'Abdomen'}, {val: 'NECK', key:'Neck'}, {val: 'EYES', key:'Eyes'}];
        
        $scope.oedemasLookup = {};

        $scope.oedemas.forEach(function(obj){
            $scope.oedemasLookup[obj.val] = obj.key;
        });

        /*  - Medication Options - */

        $scope.doseUnits = ['Mg', 'g', 'iU'];

        $scope.medicationNames = [{val: 'ORAL_PREDNISOLONE', key: 'Oral Prednisolone'}, {val: 'METHYL_ORAL_PREDNISOLONE', key: 'Methyl Prednisolone'}, {val: 'OTHER', key: 'Other'}]; 

        $scope.doseFrequencies = [{val: 'ONE_DAY', key: 'once a day'}, {val: 'TWO_DAY', key: 'X2 a day'}, {val: 'THREE_DAY', key: 'X2 a day'}, {val: 'FOUR_DAY', key: 'X4 a day'}]; 

        $scope.routes = [{val: 'ORAL', key: 'Oral'}, {val: 'IV', key: 'IV'}, {val: 'IM', key: 'IM'}]; 

        $scope.weightChanged = function(){
            // tests for 1 d.p.
            if(! /^(\d+)?([.]?\d?)?$/g.test($scope.newForm.weight + '')){
                $scope.newForm.errors.weight = 'Must be 1dp'
            } else {
                delete $scope.newForm.errors.weight;
            }
        };


        $scope.newForm = {
            errors: {},
            newMedicationCount: 0,

            reason: '',
            date: getDateDropdownVals(new Date()),
            ongoing: false,
            weight: '0.0',
            relapse: false,

            oedema: [],
            newOedema: 'NONE',

            commonCold: false,
            hayFever: false,
            allergicReaction: false,
            allergicSkinRash: false,
            foodIntolerance: false,

            medications: [],

            remissionDate: getDateDropdownVals(new Date()),
        }


        $scope.editForm = {
            reason: '',
            dateAdmitted: {},
            dateDischarged: {},
            ongoing: false,
        }

        $scope.getRecordings();
    }

    
    $scope.getRecordings = function() {
        $scope.loading = true;

        HostpitalisationService.getAll($scope.loggedInUser.id).then(function(data) {
            data.forEach(function(d){
                $scope.recordings.push(formatHostpitalisation(d));
            });
            $scope.loading = false;

            delete $scope.errorMessage;
        }, function() {
            $scope.loading = false;
            $scope.errorMessage = error.data;
        });
    }
    
    $scope.postHostpitalisation = function() {
        $scope.newForm.errors = $scope.validate($scope.newForm);
        if($scope.recordings.filter(function(h){
                return !h.originalDischarged && (!$scope.newForm.dateDischarged || new Date(h.originalAdmitted) <= new Date($scope.newForm.dateAdmitted));
            }).length > 0){
                $scope.newForm.errors.existingOngoing = 'Please complete currently active recording first';
                $scope.newForm.errors.isValid = false;
        }
        if(!$scope.newForm.errors.isValid) return;

        $scope.loading = true;
        var dateAdmitted = new Date(), dateDischarged = new Date();

        dateAdmitted.setFullYear(
            $scope.newForm.dateAdmitted.year,
            $scope.newForm.dateAdmitted.month - 1,
            $scope.newForm.dateAdmitted.day);

        dateDischarged.setFullYear(
            $scope.newForm.dateDischarged.year,
            $scope.newForm.dateDischarged.month - 1,
            $scope.newForm.dateDischarged.day);

        HostpitalisationService.post($scope.loggedInUser.id, {
            dateAdmitted: dateAdmitted.toISOString(),
            dateDischarged: $scope.newForm.ongoing ? null : dateDischarged.toISOString(),
            reason: $scope.newForm.reason,
        }, $rootScope.previousLoggedInUser.id).then(function(data) {
            $scope.loading = false;
            $scope.recordings.push(formatHostpitalisation(data));

            delete $scope.errorMessage;
        }, function(error) {
            $scope.loading = false;
            $scope.errorMessage = error.data;
        });
    }

    $scope.updateHostpitalisation = function(id) {
        $scope.editForm.errors = $scope.validate($scope.editForm);
        if(!$scope.editForm.errors.isValid) return;

        $scope.loading = true;

        var dateAdmitted = new Date(), dateDischarged = new Date();

        dateAdmitted.setFullYear(
            $scope.editForm.dateAdmitted.year,
            $scope.editForm.dateAdmitted.month - 1,
            $scope.editForm.dateAdmitted.day);

        dateDischarged.setFullYear(
            $scope.editForm.dateDischarged.year,
            $scope.editForm.dateDischarged.month - 1,
            $scope.editForm.dateDischarged.day);

        HostpitalisationService.save($scope.loggedInUser.id, id, {
            dateAdmitted: dateAdmitted.toISOString(),
            dateDischarged: $scope.editForm.ongoing ? null : dateDischarged.toISOString(),
            reason: $scope.editForm.reason,
        }, $rootScope.previousLoggedInUser.id).then(function(data) {
            $scope.loading = false;
            $scope.recordings = $scope.recordings.filter(function(val){
                return val.id !== id;
            });
            $scope.recordings.push(formatHostpitalisation(data));
            $scope.openEdit(null);
            
            $('.faux-row').removeClass('highlight');
            $('.highlight').removeClass('highlight');
            $('.item-header').removeClass('open');
            $('.faux-row').removeClass('dull');
            $('.edit-button').removeClass('editing');

            delete $scope.errorMessage;
        }, function(error) {
            $scope.loading = false;
            $scope.errorMessage = error.data;
        });
    }

    $scope.validate = function (form) {
        const errors = {};

        if (!UtilService.validationDate(form.dateAdmitted.day,
            form.dateAdmitted.month, form.dateAdmitted.year)) {
            errors.dateAdmitted = 'Non-existent date';
        }

        if (!form.ongoing && !UtilService.validationDate(form.dateDischarged.day,
            form.dateDischarged.month, form.dateDischarged.year)) {
                errors.dateDischarged =  'Non-existant date';
        }
        
        if (
            new Date(form.dateDischarged.month + '/' + form.dateDischarged.day + '/' + form.dateDischarged.year) < 
                new Date(form.dateAdmitted.month + '/' + form.dateAdmitted.day + '/' + form.dateAdmitted.year)) {
                    errors.dateDischarged =  'After admission date';
        }

        if(!form.reason || form.reason.length === 0){
            errors.reason = 'Required';
        }

        errors.isValid = Object.keys(errors).length === 0;

        return errors;
    };

    $scope.deleteHostpitalisation = function(id) {
        if(!confirm('Permanantly Delete Record?')) return; 

        $scope.loading = true;

        HostpitalisationService.remove($scope.loggedInUser.id, id, $rootScope.previousLoggedInUser.id).then(function(data) {
            $scope.loading = false;
            $scope.recordings = $scope.recordings.filter(function(val){
                return val.id !== id;
            });
            delete $scope.errorMessage;
        }, function() {
            $scope.loading = false;
            $scope.errorMessage = error.data;
        });
    }

    $scope.getHostpitalisation = function(id) {
        $scope.loading = true;

        HostpitalisationService.get($scope.loggedInUser.id, id).then(function(data) {
            $scope.loading = false;
            delete $scope.errorMessage;
        }, function() {
            $scope.loading = false;
            $scope.errorMessage = error.data;
        });
    }

    $scope.openEdit = function(row){
        if(row && row.id !== $scope.showEdit) {
            $scope.showEdit = row ? row.id : null;
            $scope.editForm.dateAdmitted = getDateDropdownVals(new Date(row.originalAdmitted));
            $scope.editForm.dateDischarged = getDateDropdownVals(row.originalDischarged ? new Date(row.originalDischarged) : new Date());
            $scope.editForm.reason = row.reason;
            $scope.editForm.ongoing = !row.originalDischarged;
            delete $scope.errorMessage;
        } else {
            $scope.showEdit = null;
        }
    };

    $scope.debugThing = function(){
        console.log($scope)
    };

    $scope.init();
}]);