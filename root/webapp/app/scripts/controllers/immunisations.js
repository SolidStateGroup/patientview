'use strict';

angular.module('patientviewApp').controller('ImmunisationsCtrl', ['$scope', 'UtilService', 'ImmunisationService', '$rootScope',
function ($scope, UtilService, ImmunisationService, $rootScope) {

    function formatImmunisation(d){
        d.type = $scope.codeConversions[d.codelist];
        if(d.type === 'Other'){
            d.type= 'Other (' + d.other + ')';
        }
        d.date = moment(new Date(d.immunisationDate)).format('ll');
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
        return vals;
    }

    $scope.init = function(){
        $scope.showEdit = null;

        $scope.immunisations = [];

        $scope.codeConversions = {
            MMR: 'MMR',
            PNEUMOCCAL: 'Pneumoccal',
            ROTAVIRUS: 'Rotavirus',
            MEN_B: 'MenB',
            MEN_ASWY: 'MenACWY',
            VERICELLA: 'Varicella',
            HIB_MENC: 'Hib/MenC',
            FLU: 'Flu',
            HPV: 'HPV',
            OTHER: 'Other',
        }
        
        $scope.codeList = Object.keys($scope.codeConversions).map(function(k){return {key: k, value: $scope.codeConversions[k]}});

        $scope.days = UtilService.generateDays().filter(function(x){ return !!x; });
        $scope.months = UtilService.generateMonths().filter(function(x){ return !!x; });
        $scope.years = UtilService.generateYears2000().filter(function(x){ return !!x; });

        $scope.date = {};

        $scope.newForm = {
            code: 'MMR',
            date: getDateDropdownVals(new Date()),
        }

        $scope.editForm = {
            code: 'MMR',
            date: {},
        }


        $scope.getImmunisations();
    }

    
    $scope.getImmunisations = function() {
        $scope.loading = true;

        ImmunisationService.getAll($scope.loggedInUser.id).then(function(data) {
            data.forEach(function(d){
                $scope.immunisations.push(formatImmunisation(d));
            });
            $scope.loading = false;
        }, function() {
            $scope.loading = false;
        });
    }
    
    $scope.postImmunisation = function() {
        $scope.newForm.errors = $scope.validate($scope.newForm);
        if(!$scope.newForm.errors.isValid) return;

        $scope.loading = true;
        var date = new Date();
        date.setFullYear($scope.newForm.date.year, $scope.newForm.date.month, $scope.newForm.date.day);

        ImmunisationService.post($scope.loggedInUser.id, {
            codelist: $scope.newForm.code,
            other: $scope.newForm.code === 'OTHER' ? $scope.newForm.other : undefined,
            immunisationDate: date.toISOString(),
        }, $rootScope.previousLoggedInUser.id).then(function(data) {
            $scope.loading = false;
            $scope.immunisations.push(formatImmunisation(data));
        }, function() {
            $scope.loading = false;
        });
    }

    $scope.updateImmunisation = function(id) {
        $scope.editForm.errors = $scope.validate($scope.editForm);
        if(!$scope.editForm.errors.isValid) return;

        $scope.loading = true;
        var date = new Date();
        date.setFullYear($scope.editForm.date.year, $scope.editForm.date.month, $scope.editForm.date.day);

        ImmunisationService.save($scope.loggedInUser.id, id,{
            codelist: $scope.editForm.code,
            other: $scope.editForm.code === 'OTHER' ? $scope.editForm.other : undefined,
            immunisationDate: date.toISOString(),
        }, $rootScope.previousLoggedInUser.id).then(function(data) {
            $scope.loading = false;
            $scope.immunisations = $scope.immunisations.filter(function(val){
                return val.id !== id;
            });
            $scope.immunisations.push(formatImmunisation(data));
            $scope.openEdit(null);
            
            $('.faux-row').removeClass('highlight');
            $('.highlight').removeClass('highlight');
            $('.item-header').removeClass('open');
            $('.faux-row').removeClass('dull');
            $('.edit-button').removeClass('editing');
        }, function() {
            $scope.loading = false;
        });
    }


    $scope.deleteImmunisation = function(id) {
        if(!confirm('Permanantly Delete Record?')) return; 

        $scope.loading = true;

        ImmunisationService.remove($scope.loggedInUser.id, id, $rootScope.previousLoggedInUser.id).then(function(data) {
            $scope.loading = false;
            $scope.immunisations = $scope.immunisations.filter(function(val){
                return val.id !== id;
            });
        }, function() {
            $scope.loading = false;
        });
    }

    $scope.getImmunisation = function(id) {
        $scope.loading = true;

        ImmunisationService.get($scope.loggedInUser.id, id).then(function(data) {
            $scope.loading = false;
            console.log(data);
        }, function() {
            $scope.loading = false;
        });
    }

    $scope.openEdit = function(row){
        if(row && row.id !== $scope.showEdit) {
            $scope.showEdit = row ? row.id : null;
            $scope.editForm.date = getDateDropdownVals(new Date(row.immunisationDate));
            $scope.editForm.code = row.codelist;
            $scope.editForm.other = row.other;
        } else {
            $scope.showEdit = null;
        }
    };

    $scope.validate = function (form) {
        const errors = {};

        if (!UtilService.validationDate(form.date.day,
            form.date.month, form.date.year)) {
            errors.date = 'Non-existent date';
        }

        if(form.code === 'OTHER' && (!form.other || form.other.length === 0)){
            errors.other = 'Please Specify';
        }

        errors.isValid = Object.keys(errors).length === 0;

        console.log(errors);

        return errors;
    };


    $scope.debugThing = function(){
        console.log($scope)
    };

    $scope.init();
}]);