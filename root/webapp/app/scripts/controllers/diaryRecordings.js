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

    function is1dp(val){
        return /^(\d+)?([.]?\d?)?$/g.test('' + val);
    }

    function getDateDropdownVals(date){
        var vals = {};
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

    function getDateFromDropdowns(dropdownSet){
        var date = new Date();
        date.setFullYear(
            dropdownSet.year,
            dropdownSet.month - 1,
            dropdownSet.day);
        return date;
    }

    function getDateTimeFromDropdowns(dropdownSet){
        return new Date(
            dropdownSet.year,
            dropdownSet.month - 1,
            dropdownSet.day,
            dropdownSet.hrs,
            dropdownSet.mins,
            0);
    }

    $scope.addMedication = function(form){
        form.newMedicationCount ++;
        form.medications.push({
            id: form.newMedicationCount,
            name: null,
            other: null,
            doseQty: null,
            doseUnits: 'MG',
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

    $scope.canAddNewOedema = function(form) {
        if(form.oedema.filter(function(o){return o === form.newOedema}).length === 0){
            return true;
        }
        return false;
    };

    $scope.addOedema = function(form) {
        if($scope.canAddNewOedema(form)){
            form.oedema.push(form.newOedema);
            delete form.errors.newOedema;
        } else {
            form.errors.newOedema = "Already Exists!";
        }
    };

     $scope.removeOedema = function(form, oedem){
        form.oedema = form.oedema.filter(function(o){return o !== oedem});
        if(form.oedema.length === 0){
            form.newOedema = 'NONE';
        }
        delete form.errors.newOedema;
    };

    $scope.newOedemaChanged = function(form) {
        if(form.oedema.length === 0 && form.newOedema !== 'NONE'){
            $scope.addOedema(form);
        }
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

        $scope.proteinDipsticks = [{val: 'NEGATIVE', key:'Negative'}, {val: 'TRACE', key:'Trace'}, {val: 'ONE', key:'One+'},
            {val: 'TWO', key:'Two+'}, {val: 'THREE', key:'Three+'}, {val: 'FOUR', key:'Four+'}];

        $scope.oedemas = [{val: 'NONE', key:'None'}, {val: 'ANKLES', key:'Ankles'}, {val: 'LEGS', key:'Legs'}, {val: 'HANDS', key:'Hands'}, 
            {val: 'ABDOMEN', key:'Abdomen'}, {val: 'NECK', key:'Neck'}, {val: 'EYES', key:'Eyes'}];
        
        $scope.oedemasLookup = {};

        $scope.oedemas.forEach(function(obj){
            $scope.oedemasLookup[obj.val] = obj.key;
        });

        /*  - Medication Options - */

        $scope.doseUnits = [{key: 'Mg', val: 'MG'}, {key: 'g', val: 'G'}, {key:'iU', val:'IU'}];

        $scope.medicationNames = [{val: 'ORAL_PREDNISOLONE', key: 'Oral Prednisolone'}, {val: 'METHYL_ORAL_PREDNISOLONE', key: 'Methyl Prednisolone'}, {val: 'OTHER', key: 'Other'}]; 

        $scope.doseFrequencies = [{val: 'ONE_DAY', key: 'once a day'}, {val: 'TWO_DAY', key: 'X2 a day'}, {val: 'THREE_DAY', key: 'X2 a day'}, {val: 'FOUR_DAY', key: 'X4 a day'}]; 

        $scope.routes = [{val: 'ORAL', key: 'Oral'}, {val: 'IV', key: 'IV'}, {val: 'IM', key: 'IM'}]; 

        $scope.weightChanged = function(){
            // tests for 1 d.p.
            if(!is1dp($scope.newForm.weight)){
                $scope.newForm.errors.weight = 'Must be 1dp'
            } else {
                delete $scope.newForm.errors.weight;
            }
        };


        $scope.newForm = {
            errors: {},
            newMedicationCount: 0,

            date: getDateDropdownVals(new Date()),
            protein: null,

            systolic: null,
            systolicNotMeasured: true,

            diastolic: null,
            diastolicNotMeasured: true,

            weight: null,
            weightNotMeasured: true,

            oedema: [],
            newOedema: null,

            relapse: null,
            relapseDate: getDateDropdownVals(new Date()),
            remissionDate: getDateDropdownVals(new Date()),

            commonCold: false,
            hayFever: false,
            allergicReaction: false,
            allergicSkinRash: false,
            foodIntolerance: false,

            medications: [],

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
        var errors = {};
        var errorCount = 0;


        /* Main Recording Validation */

        if (!UtilService.validationDate(form.date.day,
            form.date.month, form.date.year)) {
            errors.date = 'Non-existent date';
        }
        else if(!form.date.hrs || !form.date.mins) {
            errors.time = 'Invalid'
        }
        else if (getDateTimeFromDropdowns(form.date) > moment().add(1, 'm').toDate()){
            errors.date = 'Date/Time cannot be in the future';
        }
        // TODO: < last entry's date
        else if (false && getDateTimeFromDropdowns(form.date)){
            errors.date = 'Date/Time must be after last diary entry';
        }

        if(!form.protein){
            errors.protein = "Required";
        }

        if(!form.systolic && !form.systolicNotMeasured){
            errors.systolic = "No value specified";
        }
        else if(!!form.systolic && !+form.systolic){
            errors.systolic = "Must be number or empty";
        }

        if(!form.diastolic && !form.diastolicNotMeasured){
            errors.diastolic = "No value specified";
        }
        else if(!!form.diastolic && !+form.diastolic){
            errors.diastolic = "Must be number";
        }

        if(!form.weight && !form.weightNotMeasured){
            errors.weight = "No value specified";
        }
        else if(!!form.weight && !is1dp(form.weight)){
            errors.weight = "Must be 1dp";
        }
        
        if((!form.oedema || form.oedema.length === 0) && form.newOedema !== 'NONE'){
            errors.newOedema = "Required";
        }

        if(form.relapse === null){
            errors.relapse = "Required";
        }


        /* Relapse Validation */

        if(form.relapse) {

            if (!UtilService.validationDate(form.relapseDate.day,
                form.relapseDate.month, form.relapseDate.year)) {
                errors.relapseDate = 'Non-existant Date';
            }
            else if (getDateFromDropdowns(form.relapseDate) > new Date()) {
                errors.relapseDate = 'Date must be in past';
            }

            if(!form.relapseOngoing){
                if (!UtilService.validationDate(form.remissionDate.day,
                    form.remissionDate.month, form.remissionDate.year)) {
                    errors.remissionDate = 'Non-existant Date';
                }
                else if (getDateFromDropdowns(form.relapseDate) > getDateFromDropdowns(form.remissionDate)) {
                    errors.remissionDate = 'Must be after Relapse';
                }
                else if (getDateFromDropdowns(form.remissionDate) > new Date()) {
                    errors.remissionDate = 'Date must be in past';
                }
            }

            //TODO overlap

            /* Medication Validation */
            
            for( var i = 0; i < form.medications.length; i++ ){
                var medication = form.medications[i];
                medication.errors = {};

                if (!medication.name) {
                    medication.errors.name = 'Required';
                }

                if (medication.name === 'OTHER' && !medication.other) {
                    medication.errors.other = 'Required';
                }

                if (!UtilService.validationDate(medication.started.day,
                    medication.started, medication.started.year)) {
                    medication.errors.started = 'Non-existant Date';
                }

                if (!UtilService.validationDate(medication.stopped.day,
                    medication.stopped, medication.stopped.year)) {
                    medication.errors.stopped = 'Non-existant Date';
                }
                if(medication.errors) console.log(medication.errors);
                errorCount += Object.keys(errors).length;
            } 

        }

        errorCount += Object.keys(errors).length;
        
        errors.isValid = errorCount === 0;
        console.log(errors);

        form.errors = errors;
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