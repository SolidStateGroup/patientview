'use strict';

angular.module('patientviewApp').controller('DiaryRecordingsCtrl', ['$scope', 'UtilService', 'DiaryRecordingService', '$rootScope',
function ($scope, UtilService, DiaryRecordingService, $rootScope) {

    $scope.formFuncs = {};

    $scope.itemsPerPage = 10;
    $scope.currentPage = 0;
    $scope.totalPages = 0;

    // update page when currentPage is changed (and at start)
    $scope.$watch('currentPage', function(value) {
        $scope.currentPage = value;
        $scope.getRecordings();
    });

    $scope.$watch('errorMessage', function(value) {
        if(value) alert(value);
        delete $scope.errorMessage;
    });

    // update page when currentPage is changed (and at start)
    $scope.$watch('newForm.relapse', function(value) {
        if($scope.newForm.initiallyInRelapse === true){
            $scope.newForm.relapseOngoing = value;
        }
        $scope.newForm.relapse = value;
    });

    $scope.buttonClicked = function (){
        $scope.currentPage = ($scope.currentPage+1) % 2;
    }

    var newFormInitialRelapseInitial, newFormInitialRelapse;

    /* format functions */

    function formatMedicationForForm(m){
        return {
            "id": m.id, 
            "name": m.name, 
            "other": m.other, 
            "doseQty": m.doseQuantity, 
            "doseUnits": m.doseUnits, 
            "doseFrequency": m.doseFrequency, 
            "route": m.route, 
            "started": m.started ? getDateDropdownVals(new Date(m.started)) : {day: null, month: null, year: null},
            "stopped": m.stopped ? getDateDropdownVals(new Date(m.stopped)) : {day: null, month: null, year: null},
        };
    }

    function formatRelapseForForm(val){
        var medications = [];

        if(val.relapse && val.relapse.medications){
            val.relapse.medications.forEach(function(med){
                medications.push(formatMedicationForForm(med));
            });
        }
        return {
            protein: val.dipstickType,

            systolic: val.systolicBP,
            systolicNotMeasured: val.systolicBPExclude,

            diastolic: val.diastolicBP,
            diastolicNotMeasured: val.diastolicBPExclude,

            weight: val.weight,
            weightNotMeasured: val.weightExclude,

            oedema: val.oedema,
            newOedema: val.oedema && val.oedema.length > 0 ? val.oedema[0] : null,

            relapse: val.inRelapse,
            initiallyInRelapse: !!val.relapse,

            relapseId: val.relapse ? val.relapse.id : null,
            relapseDate: getDateDropdownVals(val.relapse? new Date(val.relapse.relapseDate) : new Date() ),
            relapseOngoing: val.relapse && !val.relapse.remissionDate ? true  : false,
            remissionDate: getDateDropdownVals(val.relapse && val.relapse.remissionDate ? new Date(val.relapse.remissionDate) : new Date()),

            viralInfection: val.relapse ? val.relapse.viralInfection : null,
            commonCold: val.relapse ? val.relapse.commonCold : null,
            hayFever: val.relapse ? val.relapse.hayFever : null,
            allergicReaction: val.relapse ? val.relapse.allergicReaction : null,
            allergicSkinRash: val.relapse ? val.relapse.allergicSkinRash : null,
            foodIntolerance: val.relapse ? val.relapse.foodIntolerance : null,

            medications: medications,
        };
    }

    function formatForForm(val){

        var x = Object.assign({
            errors: {},

            id: val.id,
            newMedicationCount: 0,

            date: getDateDropdownVals(new Date(val.entryDate)),

            created: moment(val.created).format('DD-MMM-YYYY'),
            createdBy: val.createdBy,

            updated: val.lastUpdate && moment(val.lastUpdate).format('DD-MMM-YYYY'),
            updatedBy: val.lastUpdatedBy,

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
            "started": m.started.day && m.started.month && m.started.year ? getDateFromDropdowns(m.started).toISOString() : null,
            "stopped": m.stopped.day && m.stopped.month && m.stopped.year ? getDateFromDropdowns(m.stopped).toISOString() : null,
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
            "relapse": form.relapse || form.initiallyInRelapse ? {
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

    /* Date dropdown functions */

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
        return date.day && date.month && date.year ? moment(getDateFromDropdowns(date)).format('DD-MMM-YYYY') : '-';
    }

    $scope.formatTimeFromDropdowns = function(date) {
        return moment(getDateTimeFromDropdowns(date)).format('HH:mm')
    }

    /* Local medication maipulation functions */

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

    /* Oedema manipulation functions */

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

    /* General Initialisation */

    $scope.init = function(){

        $scope.showEdit = null;

        $scope.pagedItems = [];

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

        $scope.options.medicationsLookup = {};

        $scope.options.medicationNames.forEach(function(obj){
            $scope.options.medicationsLookup[obj.val] = obj.key;
        });

        $scope.options.doseFrequencies = [{val: 'ONE_DAY', key: 'once a day'}, {val: 'TWO_DAY', key: 'X2 a day'}, {val: 'THREE_DAY', key: 'X3 a day'}, {val: 'FOUR_DAY', key: 'X4 a day'}]; 

        $scope.options.doseFrequenciesLookup = {};

        $scope.options.doseFrequencies.forEach(function(obj){
            $scope.options.doseFrequenciesLookup[obj.val] = obj.key;
        });
        
        $scope.options.routes = [{val: 'ORAL', key: 'Oral'}, {val: 'IV', key: 'IV'}, {val: 'IM', key: 'IM'}]; 

        $scope.options.routesLookup = {};

        $scope.options.routes.forEach(function(obj){
            $scope.options.routesLookup[obj.val] = obj.key;
        });

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

            relapseOngoing: true,
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
    }

    /* Sub-initialisation functions */

    $scope.initNewMedication = function (form){
        form.newMedication = {
            id: form.newMedicationCount,
            name: null,
            other: null,
            doseQty: null,
            doseUnits: 'MG',
            doseFrequency: null,
            route: null,
            started: {day: null, month: null, year: null},
            stopped: {day: null, month: null, year: null},
        };
    }
    
    $scope.initNewForm = function(){

        $scope.newForm = Object.assign({

            errors: {},
            newMedicationCount: 0,

            date: getDateDropdownVals(new Date()),
            protein: null,

            systolic: null,
            systolicNotMeasured: false,

            diastolic: null,
            diastolicNotMeasured: false,

            weight: null,
            weightNotMeasured: false,

            oedema: [],
            newOedema: null,

        }, newFormInitialRelapse);

        $scope.initNewMedication($scope.newForm);
    }

    /* General Validation */

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
        console.log(form);
        var medication = form.newMedication;
        medication.errors = {};

        if (!medication.name) {
            medication.errors.name = 'Required';
        }

        if (medication.name === 'OTHER' && !medication.other) {
            medication.errors.other = 'Required';
        }

        if (medication.started.day && medication.started.month && medication.started.year && !UtilService.validationDate(medication.started.day,
            medication.started.month, medication.started.year)) {
            medication.errors.started = 'Non-existant Date';
        }

        if (medication.stopped.day && medication.stopped.month && medication.stopped.year){
            if(!UtilService.validationDate(medication.stopped.day, medication.stopped.month, medication.stopped.year)) {
                medication.errors.stopped = 'Non-existant Date';
            } else if( !(medication.started.day && medication.started.month && medication.started.year) ||
                getDateFromDropdowns(medication.started) > getDateFromDropdowns(medication.stopped) ){
                    medication.errors.stopped = 'Date must be after started';
            }
        }

        if(medication.errors) console.log(medication.errors);

        medication.errors.isValid = Object.keys(medication.errors).length === 0;

        return medication.errors;
    }

    /* Service handlers */

    $scope.getRecordings = function() {

        $scope.loading = true;

        DiaryRecordingService.getPaged($scope.loggedInUser.id, $scope.currentPage, $scope.itemsPerPage).then(function(data) {
            $scope.totalPages = data.totalPages;
            $scope.total = data.totalElements;
            $scope.pagedItems = [];
            data.content.forEach(function(d, i){
                if(i === 0 && $scope.currentPage === 0){
                    if(d.inRelapse && !d.relapse.remissionDate){
                        newFormInitialRelapse = formatRelapseForForm(d);
                    } else {
                        newFormInitialRelapse = newFormInitialRelapseInitial;
                    }
                    $scope.initNewForm();
                }
                $scope.pagedItems.push(formatForForm(d));
                $scope.pagedItems[$scope.pagedItems.length - 1].editForm = formatForForm(d);
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
                $scope.pagedItems.unshift(formatForForm(data));
                $scope.pagedItems[0].editForm = formatForForm(data);
                alert('Entry Saved!');
                delete $scope.errorMessage;
                $scope.getRecordings();
                $scope.initNewForm();
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
                alert('Medication Saved!');
                $scope.initNewMedication(form);
                delete $scope.errorMessage;
            }, function(error){
                $scope.loading = false;
                $scope.errorMessage = error.data;
                scrollToError();
            });
        }
    }

    $scope.deleteMedication = function(form, id){
        $scope.loading = true;
        DiaryRecordingService.removeMedication($scope.loggedInUser.id, form.relapseId, id).then(function(data){
            $scope.loading = false;
            form.medications = form.medications.filter(function(m){
                return m.id !== id;
            });
            alert('Medication Deleted');
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
                $scope.pagedItems = $scope.pagedItems.filter(function(x){
                    return x.id !== form.id;
                });
                $scope.pagedItems.push(formatForForm(data));
                $scope.pagedItems[$scope.pagedItems.length-1].editForm = formatForForm(data);
                $scope.showEdit = null;
                $scope.getRecordings();
                alert('Entry Updated!');
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
            $scope.pagedItems = $scope.pagedItems.filter(function(val){
                return val.id !== id;
            });
            $scope.getRecordings();
            alert('Entry Deleted');
            delete $scope.errorMessage;
        }, function() {
            $scope.loading = false;
            $scope.errorMessage = error.data;
            scrollToError();
        });
    }

    /* Misc. */

    $scope.editEntry = function(row){
        if(row && row.id !== $scope.showEdit) {
            $scope.showEdit = row ? row.id : null;
        } else {
            $scope.showEdit = null;
        }
        setTimeout(function(){$('.faux-table .faux-table .dull').removeClass('dull');}, 50);
    };


    function scrollToError(){
        setTimeout(function(){
            if($("insError").length === 1){
                $([document.documentElement, document.body]).scrollTop( $("#insError").offset().top - 20);
            }
        }, 50);
    }

    $scope.debugThing = function(){
        console.log($scope)
    };

    $scope.init();
}]);