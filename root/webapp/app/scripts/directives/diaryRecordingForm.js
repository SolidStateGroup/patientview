
angular.module('patientviewApp').directive('diaryform', function() {

  return {
    restrict: 'E',
    scope: {
      form: '=',
      options: '=',
      formFuncs: '=funcs',
      submit: '=',
      addMedication: '=addmed',
      removeMedication: '=remmed',
      editMode: '=edit',
    },
    templateUrl:'scripts/directives/templates/diaryRecordingForm.html',
  };
});