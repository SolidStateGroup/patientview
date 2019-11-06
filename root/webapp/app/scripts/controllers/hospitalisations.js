'use strict';

angular.module('patientviewApp').controller('HospitalisationsCtrl', ['$scope', 'UtilService', 'HostpitalisationService', '$rootScope',
function ($scope, UtilService, HostpitalisationService, $rootScope) {

    function formatHostpitalisation(d){
        d.originalAdmitted = d.dateAdmitted;
        d.originalDischarged = d.dateDischarged;

        d.dateAdmitted = moment(new Date(d.dateAdmitted)).format('ll');
        d.dateDischarged = d.dateDischarged ? moment(new Date(d.dateDischarged)).format('ll') : '(Ongoing)';

        return d;
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
        return vals;
    }


    function scrollToError(){
        setTimeout(function(){
            if($("#hospError").length === 1){
                $([document.documentElement, document.body]).scrollTop( $("#hospError").offset().top - 20);
            }
        }, 50);
    }

    $scope.init = function(){
        $scope.showEdit = null;

        if(!$scope.hospitalisations) $scope.hospitalisations = [];

        $scope.days = UtilService.generateDays().filter(function(x){return !!x});
        $scope.months = UtilService.generateMonths().filter(function(x){return !!x});
        $scope.years = UtilService.generateYears2000().filter(function(x){return !!x});

        $scope.date = {};

        $scope.newForm = {
            reason: '',
            dateAdmitted: getDateDropdownVals(new Date()),
            dateDischarged: getDateDropdownVals(new Date()),
            ongoing: true,
        }

        $scope.editForm = {
            reason: '',
            dateAdmitted: {},
            dateDischarged: {},
            ongoing: false,
        }

    }

    
    $scope.getHospitalisations = function() {
        $scope.loading = true;

        HostpitalisationService.getAll($scope.loggedInUser.id).then(function(data) {
            data.forEach(function(d){
                $scope.hospitalisations.push(formatHostpitalisation(d));
            });
            $scope.loading = false;

            delete $scope.errorMessage;
        }, function() {
            $scope.loading = false;
            $scope.errorMessage = error.data;
            scrollToError();
        });
    }
    
    $scope.postHostpitalisation = function() {
        $scope.newForm.errors = $scope.validate($scope.newForm);
        if($scope.hospitalisations.filter(function(h){
                return !h.originalDischarged && (!$scope.newForm.dateDischarged || new Date(h.originalAdmitted) <= new Date($scope.newForm.dateAdmitted));
            }).length > 0){
                $scope.newForm.errors.existingOngoing = 'Please complete currently active hospitalisation first';
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
            $scope.hospitalisations.push(formatHostpitalisation(data));

            $scope.init();

            delete $scope.errorMessage;
        }, function(error) {
            $scope.loading = false;
            $scope.errorMessage = error.data;
            scrollToError();
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
            $scope.hospitalisations = $scope.hospitalisations.filter(function(val){
                return val.id !== id;
            });
            $scope.hospitalisations.push(formatHostpitalisation(data));
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
            scrollToError();
        });
    }

    $scope.validate = function (form) {
        var errors = {};

        if (!UtilService.validationDate(form.dateAdmitted.day,
            form.dateAdmitted.month, form.dateAdmitted.year)) {
            errors.dateAdmitted = 'Non-existent date';
        }
        else if (!UtilService.validationDateNoFuture(form.dateAdmitted.day,
            form.dateAdmitted.month, form.dateAdmitted.year)) {
            errors.dateAdmitted = 'Date cannot be in future';
        }

        if (!form.ongoing && !UtilService.validationDate(form.dateDischarged.day,
            form.dateDischarged.month, form.dateDischarged.year)) {
                errors.dateDischarged =  'Non-existant date';
        }
        else if (!form.ongoing && !UtilService.validationDateNoFuture(form.dateDischarged.day,
            form.dateDischarged.month, form.dateDischarged.year)) {
                errors.dateDischarged =  'Date cannot be in future';
        }
        
        if ( !form.ongoing && 
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
            $scope.hospitalisations = $scope.hospitalisations.filter(function(val){
                return val.id !== id;
            });
            delete $scope.errorMessage;
        }, function() {
            $scope.loading = false;
            $scope.errorMessage = error.data;
            scrollToError();
        });
    }

    $scope.getHostpitalisation = function(id) {
        $scope.loading = true;

        HostpitalisationService.get($scope.loggedInUser.id, id).then(function(data) {
            $scope.loading = false;
            $scope.init();
            delete $scope.errorMessage;
        }, function() {
            $scope.loading = false;
            $scope.errorMessage = error.data;
            scrollToError();
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
    $scope.getHospitalisations();
}]);