'use strict';

// add Surgery modal instance controller
var AddSurgeryModalInstanceCtrl = ['$scope', '$rootScope', '$modalInstance', 'UtilService',
    function ($scope, $rootScope, $modalInstance, UtilService) {
        var init = function () {
            $scope.months = UtilService.generateMonths();
            $scope.years = UtilService.generateYears();
            $scope.days = UtilService.generateDays();

            $scope.surgery = {};

            $scope.surgery.selectedDay = $scope.days[0];
            $scope.surgery.selectedMonth = $scope.months[0];
            $scope.surgery.selectedYear = $scope.years[0];

            delete $scope.errorMessage;
            $scope.procedures = [];
            $scope.procedures.push({'code': '01.1', 'description': 'Apendicectomy'});
            $scope.procedures.push({'code': '04.1', 'description': 'Total proctocolectomy'});
            $scope.procedures.push({'code': '04.2', 'description': 'Ileonal pouch'});
            $scope.procedures.push({'code': '05.1', 'description': 'Partial colectomy & colostomy with retained rectal stump'});
            $scope.procedures.push({'code': '05.2', 'description': 'Colectomy ileostomy with retained rectal stump'});
            $scope.procedures.push({'code': '05.3', 'description': 'Pancolectomy'});
            $scope.procedures.push({'code': '06.1', 'description': 'Partial (segmental) colectomy'});
            $scope.procedures.push({'code': '07.1', 'description': 'Right hemicolectomy'});
            $scope.procedures.push({'code': '09.1', 'description': 'Left hemicolectomy'});
            $scope.procedures.push({'code': '55.4', 'description': 'Insertion of seton'});
            $scope.procedures.push({'code': '55.5', 'description': 'Fistulectomy'});
            $scope.procedures.push({'code': '58.2', 'description': 'Drainage of perianal sepsis'});
            $scope.procedures.push({'code': '27.2', 'description': 'Gastric surgery'});
            $scope.procedures.push({'code': '58.1', 'description': 'Small bowel resection'});
            $scope.procedures.push({'code': '73.3', 'description': 'Permanent ileostomy'});
            $scope.procedures.push({'code': '78.2', 'description': 'Stricturoplasty'});
            $scope.procedures.push({'code': 'J18.1', 'description': 'Cholecystectomy'});
            $scope.procedures.push({'code': 'Y53.1', 'description': 'Radiological drainage of abscess'});
            $scope.procedures.push({'code': '99', 'description': 'Other surgery (not specified)'});
            
            $scope.surgery.selectedProcedures = [];
        };

        $scope.addProcedure = function (procedure) {
            var found = false;
            for (var i = 0; i < $scope.surgery.selectedProcedures.length; i++) {
                if ($scope.surgery.selectedProcedures[i].code === procedure.code) {
                    found = true;
                }
            }

            if (!found) {
                $scope.surgery.selectedProcedures.push(procedure);
            }
        };

        $scope.removeProcedure = function (procedure) {
            var procedures = [];
            for (var i = 0; i < $scope.surgery.selectedProcedures.length; i++) {
                if ($scope.surgery.selectedProcedures[i].code !== procedure.code) {
                    procedures.push($scope.surgery.selectedProcedures[i])
                }
            }
            $scope.surgery.selectedProcedures = procedures;
        };

        $scope.ok = function () {
            delete $scope.errorMessage;

            if (!UtilService.validationDate($scope.surgery.selectedDay,
                    $scope.surgery.selectedMonth, $scope.surgery.selectedYear)) {
                $scope.errorMessage = "Please select a valid date";
            } else {
                // set date
                $scope.surgery.date = new Date(parseInt($scope.surgery.selectedYear),
                    parseInt($scope.surgery.selectedMonth) - 1, parseInt($scope.surgery.selectedDay));

                // set id (for removing)
                $scope.surgery.id = new Date().getTime();
                $modalInstance.close($scope.surgery);
            }
        };

        $scope.cancel = function () {
            delete $scope.errorMessage;
            $modalInstance.dismiss('cancel');
        };

        init();
    }];


angular.module('patientviewApp').controller('IbdPatientManagementCtrl', ['$scope', '$rootScope', 'SurveyService',
    'SurveyResponseService', '$modal', 'UtilService',
function ($scope, $rootScope, SurveyService, SurveyResponseService, $modal, UtilService) {

    $scope.addEgimComplication = function(complication) {
        var found = false;
        for (var i = 0; i < $scope.patientManagement.ibd_egimcomplications.length; i++) {
            if ($scope.patientManagement.ibd_egimcomplications[i].code === complication.code) {
                found = true;
            }
        }

        if (!found) {
            $scope.patientManagement.ibd_egimcomplications.push(complication);
        }
    };

    $scope.calculateBMI = function() {
        if ($scope.patientManagement.height !== undefined
            && $scope.patientManagement.height.length
            && $scope.patientManagement.weight !== undefined
            && $scope.patientManagement.weight.length) {
            var height = parseFloat($scope.patientManagement.height);
            var weight = parseFloat($scope.patientManagement.weight);

            return (weight / (height * height)).toFixed(2);
        }
    };

    $scope.init = function() {
        $scope.patientManagement = {};
        $scope.patientManagement.surgeries = [];
        $scope.patientManagement.selectedProcedures = [];
        $scope.patientManagement.ibd_egimcomplications = [];

        // example options
        $scope.sexes = [];
        $scope.sexes.push({'code': 0, 'description': 'Not known'});
        $scope.sexes.push({'code': 1, 'description': 'Male'});
        $scope.sexes.push({'code': 2, 'description': 'Female'});
        $scope.diagnoses = [];
        $scope.diagnoses.push({'code': 'CD', 'description': 'Crohn\'s Disease'});
        $scope.diagnoses.push({'code': 'UC', 'description': 'Ulcerative Colitis'});
        $scope.diagnoses.push({'code': 'IBDU', 'description': 'IBD Indeterminate'});
        $scope.ibd_crohnslocations = [];
        $scope.ibd_crohnslocations.push({'code': 'L1', 'description': 'Terminal Ileum +/- limited caecal disease'});
        $scope.ibd_crohnslocations.push({'code': 'L2', 'description': 'Colonic'});
        $scope.ibd_crohnslocations.push({'code': 'L3', 'description': 'Ileocolonic'});
        $scope.ibd_crohnslocations.push({'code': 'None', 'description': 'None of the above'});
        $scope.ibd_crohnsproximalterminalileums = [];
        $scope.ibd_crohnsproximalterminalileums.push({'code': 'YES', 'description': 'Yes'});
        $scope.ibd_crohnsproximalterminalileums.push({'code': 'NO', 'description': 'No'});
        $scope.ibd_crohnsperianals = [];
        $scope.ibd_crohnsperianals.push({'code': 'YES', 'description': 'Yes'});
        $scope.ibd_crohnsperianals.push({'code': 'NO', 'description': 'No'});
        $scope.ibd_crohnsbehaviours = [];
        $scope.ibd_crohnsbehaviours.push({'code': 'B1', 'description': 'Inflammation only'});
        $scope.ibd_crohnsbehaviours.push({'code': 'B2', 'description': 'Stricturing disease'});
        $scope.ibd_crohnsbehaviours.push({'code': 'B3', 'description': 'Fistulating disease'});
        $scope.ibd_ucextents = [];
        $scope.ibd_ucextents.push({'code': 'E1', 'description': 'Ulcerative proctitis'});
        $scope.ibd_ucextents.push({'code': 'E2', 'description': 'Left sided UC (distal UC)'});
        $scope.ibd_ucextents.push({'code': 'E3', 'description': 'Extensive UC (pancolitis)'});
        $scope.ibd_egimcomplications = [];
        $scope.ibd_egimcomplications.push({'code': '1', 'description': 'Head'});
        $scope.ibd_egimcomplications.push({'code': '2', 'description': 'Foot'});
        $scope.ibd_smokingstatuses = [];
        $scope.ibd_smokingstatuses.push({'code': '1', 'description': 'Current Smoker'});
        $scope.ibd_smokingstatuses.push({'code': '2', 'description': 'Ex-Smoker'});
        $scope.ibd_smokingstatuses.push({'code': '3', 'description': 'Non-Smoker - history unknown'});
        $scope.ibd_smokingstatuses.push({'code': '4', 'description': 'Never Smoked'});
        $scope.ibd_familyhistorys = [];
        $scope.ibd_familyhistorys.push({'code': 'YES', 'description': 'Yes'});
        $scope.ibd_familyhistorys.push({'code': 'NO', 'description': 'No'});

        $scope.years = UtilService.generateYears();
        $scope.patientManagement.colonoscopysurveillance = $scope.years[0];

        delete $scope.successMessage;

        // check if viewing as patient
        $scope.isStaff = $rootScope.previousLoggedInUser ? true : false;
    };

    $scope.removeEgimComplication = function (complication) {
        var complications = [];
        for (var i = 0; i < $scope.patientManagement.ibd_egimcomplications.length; i++) {
            if ($scope.patientManagement.ibd_egimcomplications[i].code !== complication.code) {
                complications.push($scope.patientManagement.ibd_egimcomplications[i])
            }
        }
        $scope.patientManagement.ibd_egimcomplications = complications;
    };

    $scope.removeSurgery = function(surgery) {
        var surgeries = [];
        for (var i = 0; i < $scope.patientManagement.surgeries.length; i++) {
            if ($scope.patientManagement.surgeries[i].id !== surgery.id) {
                surgeries.push(surgery);
            }
        }
        $scope.patientManagement.surgeries = surgeries;
    };

    $scope.showSurgeryModal = function() {
        // open modal and pass in required objects for use in modal scope
        var modalInstance = $modal.open({
            templateUrl: 'addSurgeryModal.html',
            controller: AddSurgeryModalInstanceCtrl,
            size: 'lg',
            backdrop: 'static',
            resolve: {
                UtilService: function() {
                    return UtilService;
                }
            }
        });

        // handle modal close (via button click)
        modalInstance.result.then(function (surgery) {
            $scope.patientManagement.surgeries.push(surgery);
        }, function () {

        });
    };

    $scope.init();
}]);
