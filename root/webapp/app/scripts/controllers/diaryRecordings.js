'use strict';

angular.module('patientviewApp').controller('DiaryRecordingsCtrl', ['$scope', 'UtilService', 'DiaryRecordingService', '$rootScope',
function ($scope, UtilService, DiaryRecordingService, $rootScope) {

    $scope.formFuncs = {};

    var pageSize = 10;
    $scope.page = 0;

    var newFormInitialRelapseInitial, newFormInitialRelapse;


    function formatMedicationForForm(m){
        return {
            "id": m.id, 
            "name": m.name, 
            "other": m.other, 
            "doseQty": m.doseQuantity, 
            "doseUnits": m.doseUnits, 
            "doseFrequency": m.doseFrequency, 
            "route": m.route, 
            "started": getDateDropdownVals(new Date(m.started)),
            "stopped": getDateDropdownVals(new Date(m.stopped)),
        };
    }

    function formatRelapseForForm(val){
        var medications = [];

        if(val.inRelapse && val.relapse.medications){
            val.relapse.medications.forEach(function(med){
                medications.push(formatMedicationForForm(med));
            });
        }
        return {
            relapse: val.inRelapse,
            relapseId: val.inRelapse ? val.relapse.id : null,
            relapseDate: getDateDropdownVals(val.inRelapse? new Date(val.relapse.relapseDate) : new Date() ),
            relapseOngoing: val.inRelapse && !val.relapse.remissionDate ? true  : false,
            remissionDate: getDateDropdownVals(val.inRelapse && val.relapse.remissionDate ? new Date(val.relapse.remissionDate) : new Date()),

            viralInfection: val.inRelapse ? val.relapse.viralInfection : null,
            commonCold: val.inRelapse ? val.relapse.commonCold : null,
            hayFever: val.inRelapse ? val.relapse.hayFever : null,
            allergicReaction: val.inRelapse ? val.relapse.allergicReaction : null,
            allergicSkinRash: val.inRelapse ? val.relapse.allergicSkinRash : null,
            foodIntolerance: val.inRelapse ? val.relapse.foodIntolerance : null,

            medications: medications,
        };
    }

    function formatForForm(val){

        var x = Object.assign({
            errors: {},

            id: val.id,
            newMedicationCount: 0,

            date: console.log(getDateDropdownVals(new Date(val.entryDate))) || getDateDropdownVals(new Date(val.entryDate)),
            protein: val.dipstickType,

            systolic: val.systolicBP,
            systolicNotMeasured: val.systolicBPExclude,

            diastolic: val.diastolicBP,
            diastolicNotMeasured: val.diastolicBPExclude,

            weight: val.weight,
            weightNotMeasured: val.weightExclude,

            oedema: val.oedema,
            newOedema: val.oedema && val.oedema.length > 0 ? val.oedema[0] : null,

        }, formatRelapseForForm(val));

        $scope.initNewMedication(x);
        return x;
    }

    function formatMedicationForPost(m){
        return {
            "name": m.name, 
            "other": m.name === 'OTHER' ? m.other : null, 
            "doseQuantity": m.doseQty, 
            "doseUnits": m.doseUnits, 
            "doseFrequency": m.doseFrequency, 
            "route": m.route, 
            "started": getDateFromDropdowns(m.started).toISOString(),
            "stopped": getDateFromDropdowns(m.stopped).toISOString(),
        };
    }

    function formatForPost(form){
        var medications = [];

        form.medications.forEach(function(med){
            medications.push(formatMedicationForPost(med));
        });
        return {
            "entryDate": getDateTimeFromDropdowns(form.date).toISOString(),
            "dipstickType": form.protein,
            "oedema": form.newOedema === 'NONE' ? ['NONE'] : form.oedema,
            "systolicBP": form.systolic,
            "systolicBPExclude": form.systolicNotMeasured,
            "diastolicBP": form.diastolic,
            "diastolicBPExclude": form.diastolicNotMeasured,
            "weight": form.weight,
            "weightExclude": form.weightNotMeasured,
            "inRelapse": form.relapse,
            "relapse": form.relapse ? {
                "id": form.relapseId || undefined,
                "relapseDate": getDateFromDropdowns(form.relapseDate).toISOString(), 
                "remissionDate": !form.relapseOngoing ? getDateFromDropdowns(form.remissionDate).toISOString() : null, 
                "viralInfection": form.viralInfection, 
                "commonCold": form.commonCold, 
                "hayFever": form.hayFever, 
                "allergicReaction": form.allergicReaction, 
                "allergicSkinRash": form.allergicSkinRash,
                "foodIntolerance": form.foodIntolerance,
                "medications": !form.id ? medications : undefined,
            } : null,
        };
    }

    function is1dp(val){
        return /^(\d+)?([.]?\d?)?$/g.test('' + val);
    }

    function getDateDropdownVals(date){
        var vals = {};
        for (var i=0;i<$scope.options.days.length;i++) {
            if (parseInt($scope.options.days[i]) === date.getDate()) {
                vals.day = $scope.options.days[i];
            }
        }
        for (var i=0;i<$scope.options.months.length;i++) {
            if (parseInt($scope.options.months[i]) === date.getMonth() + 1) {
                vals.month = $scope.options.months[i];
            }
        }
        for (var i=0;i<$scope.options.years.length;i++) {
            if (parseInt($scope.options.years[i]) === date.getFullYear()) {
                vals.year = $scope.options.years[i];
            }
        }
        for (var i=0;i<$scope.options.mins.length;i++) {
            if (parseInt($scope.options.mins[i]) === date.getMinutes()) {
                vals.mins = $scope.options.mins[i];
            }
        }
        for (var i=0;i<$scope.options.hrs.length;i++) {
            if (parseInt($scope.options.hrs[i]) === date.getHours()) {
                vals.hrs = $scope.options.hrs[i];
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

    $scope.formatDateFromDropdowns = function(date) {
        return moment(getDateFromDropdowns(date)).format('DD-MMM-YYYY');
    }

    $scope.formatTimeFromDropdowns = function(date) {
        return moment(getDateTimeFromDropdowns(date)).format('HH:mm')
    }

    $scope.addMedicationLocal = function(form){
        var errors = $scope.validateMedication(form);
        if(!errors.isValid) return;
        form.newMedicationCount ++;
        form.medications.push(Object.assign({}, form.newMedication));
        $scope.initNewMedication(form);
    }

    $scope.removeMedicationLocal = function(form, id){
        form.medications = form.medications.filter(function(m){
            return m.id !== id;
        });
    }

    $scope.formFuncs.canAddNewOedema = function(form) {
        if(form.oedema.filter(function(o){return o === form.newOedema}).length === 0){
            return true;
        }
        return false;
    };

    $scope.formFuncs.addOedema = function(form) {
        if($scope.formFuncs.canAddNewOedema(form)){
            form.oedema.push(form.newOedema);
            delete form.errors.newOedema;
        } else {
            form.errors.newOedema = "Already Exists!";
        }
    };

     $scope.formFuncs.removeOedema = function(form, oedem){
        form.oedema = form.oedema.filter(function(o){return o !== oedem});
        if(form.oedema.length === 0){
            form.newOedema = 'NONE';
        }
        delete form.errors.newOedema;
    };

    $scope.formFuncs.newOedemaChanged = function(form) {
        if(form.oedema.length === 0 && form.newOedema !== 'NONE'){
            $scope.formFuncs.addOedema(form);
        }
    };

    $scope.init = function(){

        $scope.showEdit = null;

        $scope.recordings = [];

        $scope.options = {};

        /*  ---- SELECT OPTIONS  ---- */

        $scope.options.days = UtilService.generateDays().filter(function(x){return !!x});
        $scope.options.months = UtilService.generateMonths().filter(function(x){return !!x});
        $scope.options.years = UtilService.generateYears2000().filter(function(x){return !!x});

        $scope.options.hrs = UtilService.generateHours().filter(function(x){return !!x});
        $scope.options.mins = UtilService.generateMinutes().filter(function(x){return !!x});

        $scope.options.yesNo = [{key: 'Yes', val: true}, {key: 'No', val: false}];


        /* - Diary Options - */

        $scope.options.proteinDipsticks = [{val: 'NEGATIVE', key:'Negative'}, {val: 'TRACE', key:'Trace'}, {val: 'ONE', key:'One+'},
            {val: 'TWO', key:'Two+'}, {val: 'THREE', key:'Three+'}, {val: 'FOUR', key:'Four+'}];

        $scope.options.proteinLookup = {};

        $scope.options.proteinDipsticks.forEach(function(obj){
            $scope.options.proteinLookup[obj.val] = obj.key;
        });

        $scope.options.oedemas = [{val: 'NONE', key:'None'}, {val: 'ANKLES', key:'Ankles'}, {val: 'LEGS', key:'Legs'}, {val: 'HANDS', key:'Hands'}, 
            {val: 'ABDOMEN', key:'Abdomen'}, {val: 'NECK', key:'Neck'}, {val: 'EYES', key:'Eyes'}];
        

        $scope.options.oedemasLookup = {};

        $scope.options.oedemas.forEach(function(obj){
            $scope.options.oedemasLookup[obj.val] = obj.key;
        });

        /*  - Medication Options - */

        $scope.options.doseUnits = [{key: 'Mg', val: 'MG'}, {key: 'g', val: 'G'}, {key:'iU', val:'IU'}];

        $scope.options.medicationNames = [{val: 'ORAL_PREDNISOLONE', key: 'Oral Prednisolone'}, {val: 'METHYL_ORAL_PREDNISOLONE', key: 'Methyl Prednisolone'}, {val: 'OTHER', key: 'Other'}]; 

        $scope.options.doseFrequencies = [{val: 'ONE_DAY', key: 'once a day'}, {val: 'TWO_DAY', key: 'X2 a day'}, {val: 'THREE_DAY', key: 'X2 a day'}, {val: 'FOUR_DAY', key: 'X4 a day'}]; 

        $scope.options.routes = [{val: 'ORAL', key: 'Oral'}, {val: 'IV', key: 'IV'}, {val: 'IM', key: 'IM'}]; 

        $scope.weightChanged = function(){
            // tests for 1 d.p.
            if(!is1dp($scope.newForm.weight)){
                $scope.newForm.errors.weight = 'Must be 1dp'
            } else {
                delete $scope.newForm.errors.weight;
            }
        };

        newFormInitialRelapseInitial = {
            relapse: null,
            relapseDate: getDateDropdownVals(new Date()),
            remissionDate: getDateDropdownVals(new Date()),
    
            viralInfection: null,
            commonCold: false,
            hayFever: false,
            allergicReaction: false,
            allergicSkinRash: false,
            foodIntolerance: false,
    
            medications: [],
        };

        if(!newFormInitialRelapse){
            newFormInitialRelapse = newFormInitialRelapseInitial;
        }

        $scope.initNewForm();


        $scope.editForm = {
            reason: '',
            dateAdmitted: {},
            dateDischarged: {},
            ongoing: false,
        }

        $scope.initNewMedication($scope.newForm);
        $scope.getRecordings();
    }

    $scope.initNewMedication = function (form){
        form.newMedication = {
            id: form.newMedicationCount,
            name: null,
            other: null,
            doseQty: null,
            doseUnits: 'MG',
            doseFrequency: null,
            route: null,
            started: getDateDropdownVals(new Date()),
            stopped: getDateDropdownVals(new Date()),
        };
    }
    
    $scope.initNewForm = function(){

        $scope.newForm = Object.assign({

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

        }, newFormInitialRelapse);
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
            //TODO Overlap
        }

        errorCount += Object.keys(errors).length;
        
        errors.isValid = errorCount === 0;
        console.log(errors);

        form.errors = errors;

        return errors;
    };

    /* Medication Validation */

    $scope.validateMedication = function(form){
        var medication = form.newMedication;
        medication.errors = {};

        if (!medication.name) {
            medication.errors.name = 'Required';
        }

        if (medication.name === 'OTHER' && !medication.other) {
            medication.errors.other = 'Required';
        }

        if (!UtilService.validationDate(medication.started.day,
            medication.started.month, medication.started.year)) {
            medication.errors.started = 'Non-existant Date';
        }

        if (!UtilService.validationDate(medication.stopped.day,
            medication.stopped.month, medication.stopped.year)) {
            medication.errors.stopped = 'Non-existant Date';
        }
        if(medication.errors) console.log(medication.errors);

        medication.errors.isValid = Object.keys(medication.errors).length === 0;

        return medication.errors;
    }


    $scope.getRecordings = function() {

        $scope.loading = true;

        DiaryRecordingService.getPaged($scope.loggedInUser.id, $scope.page, pageSize).then(function(data) {
            data.content.forEach(function(d, i){
                if(i === 0 && $scope.page === 0){
                    console.log('jhgjh', d.inRelapse, d.relapse.remissionDate)
                    if(d.inRelapse && !d.relapse.remissionDate){
                        newFormInitialRelapse = formatRelapseForForm(d);
                    } else {
                        newFormInitialRelapse = newFormInitialRelapseInitial;
                    }
                    $scope.initNewForm();
                }
                $scope.recordings.push(formatForForm(d));
                $scope.recordings[$scope.recordings.length - 1].editForm = formatForForm(d);
            });
            $scope.loading = false;

            delete $scope.errorMessage;
        }, function() {
            $scope.loading = false;
            $scope.errorMessage = error.data;
            scrollToError();
        });
    }

    $scope.postEntry = function(form){
        var errors = $scope.validate(form);
        if(errors.isValid){
            var entry = formatForPost(form);
            $scope.loading = true;
            DiaryRecordingService.post($scope.loggedInUser.id, entry, $rootScope.previousLoggedInUser.id).then(function(data){
                $scope.loading = false;
                $scope.recordings.push(formatForForm(data));
                $scope.recordings[$scope.recordings.length-1].editForm = formatForForm(data);
                delete $scope.errorMessage;
                $scope.init();
            }, function(error){
                $scope.loading = false;
                $scope.errorMessage = error.data;
                scrollToError();
            });
        }
    }
    
    $scope.postMedication = function(form){
        var errors = $scope.validateMedication(form);
        if(errors.isValid){
            $scope.loading = true;
            DiaryRecordingService.postMedication($scope.loggedInUser.id, form.relapseId, formatMedicationForPost(form.newMedication)).then(function(data){
                $scope.loading = false;
                form.medications.push(formatMedicationForForm(data));
                delete $scope.errorMessage;
            }, function(error){
                $scope.loading = false;
                $scope.errorMessage = error.data;
                scrollToError();
            });
        }
    }

    function scrollToError(){
        setTimeout(function(){
            if($("insError").length === 1){
                $([document.documentElement, document.body]).scrollTop( $("#insError").offset().top - 20);
            }
        }, 50);
    }

    $scope.deleteMedication = function(form, id){
        $scope.loading = true;
        console.log('id:', id);
        DiaryRecordingService.removeMedication($scope.loggedInUser.id, form.relapseId, id).then(function(data){
            $scope.loading = false;
            form.medications = form.medications.filter(function(m){
                return m.id !== id;
            });
            delete $scope.errorMessage;
        }, function(error){
            $scope.loading = false;
            $scope.errorMessage = error.data;
            scrollToError();
        });
    }
    
    $scope.updateEntry = function(form){
        var errors = $scope.validate(form);
        if(errors.isValid){
            var entry = formatForPost(form);
            entry.id = form.id;
            $scope.loading = true;
            DiaryRecordingService.save($scope.loggedInUser.id, entry, $rootScope.previousLoggedInUser.id).then(function(data){
                $scope.loading = false;
                $scope.recordings = $scope.recordings.filter(function(x){
                    return x.id !== form.id;
                });
                $scope.recordings.push(formatForForm(data));
                $scope.recordings[$scope.recordings.length-1].editForm = formatForForm(data);
                delete $scope.errorMessage;
            }, function(error){
                $scope.loading = false;
                $scope.errorMessage = error.data;
                scrollToError();
            });
        }
    }

    $scope.deleteEntry = function(id) {
        if(!confirm('Permanantly Delete Record?')) return; 

        $scope.loading = true;

        DiaryRecordingService.remove($scope.loggedInUser.id, id, $rootScope.previousLoggedInUser.id).then(function(data) {
            $scope.loading = false;
            $scope.recordings = $scope.recordings.filter(function(val){
                return val.id !== id;
            });
            delete $scope.errorMessage;
        }, function() {
            $scope.loading = false;
            $scope.errorMessage = error.data;
            scrollToError();
        });
    }

    $scope.editEntry = function(row){
        if(row && row.id !== $scope.showEdit) {
            $scope.showEdit = row ? row.id : null;
        } else {
            $scope.showEdit = null;
        }
        setTimeout(function(){$('.faux-table .faux-table .dull').removeClass('dull');}, 50);
    };

    $scope.debugThing = function(){
        console.log($scope)
    };

    $scope.init();
}]);