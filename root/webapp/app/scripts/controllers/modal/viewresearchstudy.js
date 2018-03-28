'use strict';
var ViewResearchStudyModalInstanceCtrl = ['$scope', '$modalInstance', 'researchStudy',
    function ($scope, $modalInstance, researchStudy) {
        $scope.researchStudy = researchStudy;

        $scope.cancel = function () {
            $modalInstance.dismiss('cancel');
        };

        $scope.print = function() {
            var header = document.getElementsByClassName("modal-header")[0];
            var body = document.getElementsByClassName("modal-body")[0];


            var domHeaderClone = header.cloneNode(true);
            var domBodyClone = body.cloneNode(true);

            var $printSection = document.getElementById("printSection");

            if (!$printSection) {
                var $printSection = document.createElement("div");
                $printSection.id = "printSection";
                document.body.appendChild($printSection);
            }

            $printSection.innerHTML = "";
            $printSection.appendChild(domHeaderClone);
            $printSection.appendChild(domBodyClone);
            window.print();
        }
    }];
