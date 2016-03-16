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
        if ($scope.patientManagement.IBD_PATIENT_MANAGEMENT_EGIMCOMPLICATION === undefined) {
            $scope.patientManagement.IBD_PATIENT_MANAGEMENT_EGIMCOMPLICATION = {};
            $scope.patientManagement.IBD_PATIENT_MANAGEMENT_EGIMCOMPLICATION.values = [];
        }

        var found = false;
        for (var i = 0; i < $scope.patientManagement.IBD_PATIENT_MANAGEMENT_EGIMCOMPLICATION.values.length; i++) {
            if ($scope.patientManagement.IBD_PATIENT_MANAGEMENT_EGIMCOMPLICATION.values[i].id === complication.id) {
                found = true;
            }
        }

        if (!found) {
            $scope.patientManagement.IBD_PATIENT_MANAGEMENT_EGIMCOMPLICATION.values.push(complication);
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

        $scope.years = UtilService.generateYears();
        $scope.yearsPlusSix = UtilService.generateYears(new Date().getFullYear() + 6);
        $scope.patientManagement.IBD_PATIENT_MANAGEMENT_COLONOSCOPYSURVEILLANCE = $scope.yearsPlusSix[0];

        delete $scope.successMessage;

        // check if viewing as patient
        $scope.isStaff = $rootScope.previousLoggedInUser ? true : false;

        // survey based
        var i, j;

        // prepare for survey response
        SurveyService.getByType('IBD_PATIENT_MANAGEMENT').then(function(survey) {
            $scope.survey = survey;
            $scope.questionMap = [];

            // create map of question id to question type, used when creating object to send to backend
            for (i = 0; i < survey.questionGroups.length; i++) {
                for (j = 0; j < survey.questionGroups[i].questions.length; j++) {
                    var question = survey.questionGroups[i].questions[j];
                    $scope.questionMap[question.type] = question;
                }
            }

        }, function () {
            alert('error getting patient management programme details')
        });
    };

    $scope.removeEgimComplication = function (complication) {
        var complications = [];
        for (var i = 0; i < $scope.patientManagement.IBD_PATIENT_MANAGEMENT_EGIMCOMPLICATION.values.length; i++) {
            if ($scope.patientManagement.IBD_PATIENT_MANAGEMENT_EGIMCOMPLICATION.values[i].id !== complication.id) {
                complications.push($scope.patientManagement.IBD_PATIENT_MANAGEMENT_EGIMCOMPLICATION.values[i])
            }
        }
        $scope.patientManagement.IBD_PATIENT_MANAGEMENT_EGIMCOMPLICATION.values = complications;
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

        // testing
        // build object to send to back end
        var surveyResponse = {}, questionAnswer = {};
        surveyResponse.survey = {};
        surveyResponse.survey.id = $scope.survey.id;
        surveyResponse.questionAnswers = [];
        surveyResponse.date = new Date();

        for (var type in $scope.questionMap) {
            if ($scope.questionMap.hasOwnProperty(type)
                && $scope.patientManagement[type] !== null
                && $scope.patientManagement[type] !== undefined) {
                if ($scope.questionMap[type].elementType === 'MULTI_SELECT') {
                    for (var i = 0; i < $scope.patientManagement[type].values.length; i++) {
                        questionAnswer = {};
                        questionAnswer.questionOption = $scope.patientManagement[type].values[i];
                        questionAnswer.question = {};
                        questionAnswer.question.id = $scope.questionMap[type].id;
                        surveyResponse.questionAnswers.push(questionAnswer);
                    }
                } else if ($scope.questionMap[type].elementType === 'TEXT') {
                    questionAnswer = {};
                    questionAnswer.value = $scope.patientManagement[type];
                    questionAnswer.question = {};
                    questionAnswer.question.id = $scope.questionMap[type].id;
                    surveyResponse.questionAnswers.push(questionAnswer);
                } else if ($scope.questionMap[type].elementType === 'SINGLE_SELECT') {
                    questionAnswer = {};
                    questionAnswer.questionOption = $scope.patientManagement[type];
                    questionAnswer.question = {};
                    questionAnswer.question.id = $scope.questionMap[type].id;
                    surveyResponse.questionAnswers.push(questionAnswer);
                } else if ($scope.questionMap[type].elementType === 'DATE') {
                    var value = $scope.patientManagement[type];
                    questionAnswer = {};
                    //questionAnswer.value = new Date(parseInt(value.selectedYear), parseInt(value.selectedMonth) - 1, parseInt(value.selectedDay));
                    questionAnswer.value = value.selectedYear + '-' + value.selectedMonth + '-' + value.selectedDay;
                    questionAnswer.question = {};
                    questionAnswer.question.id = $scope.questionMap[type].id;
                    surveyResponse.questionAnswers.push(questionAnswer);
                }
            }
        }

        console.log(surveyResponse);

        // handle modal close (via button click)
        modalInstance.result.then(function (surgery) {
            $scope.patientManagement.surgeries.push(surgery);
        }, function () {

        });
    };

    $scope.init();
}]);
