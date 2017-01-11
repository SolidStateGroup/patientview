'use strict';

angular.module('patientviewApp').factory('Mixins', [function () {
    return {
        isGroupChecked: function (id) {
            if (_.contains(this.selectedGroup, id)) {
                return 'glyphicon glyphicon-ok pull-right';
            }
            return false;
        },
        isRoleChecked: function (id) {
            if (_.contains(this.selectedRole, id)) {
                return 'glyphicon glyphicon-ok pull-right';
            }
            return false;
        },
        removeAllSelectedGroup: function (groupType) {
            delete this.successMessage;
            var newSelectedGroupList = [];

            for (var i = 0; i < this.selectedGroup.length; i++) {
                if (this.groupMap[this.selectedGroup[i]].groupType.value !== groupType) {
                    newSelectedGroupList.push(this.selectedGroup[i]);
                }
            }

            this.selectedGroup = newSelectedGroupList;
            this.currentPage = 0;
            this.getItems();
        },
        removeAllSelectedRole: function () {
            this.selectedRole = [];
            this.currentPage = 0;
            this.getItems();
        },
        removeSelectedGroup: function (group) {
            delete this.successMessage;
            this.selectedGroup.splice(this.selectedGroup.indexOf(group.id), 1);
            this.currentPage = 0;
            this.getItems();
        },
        removeSelectedRole: function (role) {
            this.selectedRole.splice(this.selectedRole.indexOf(role.id), 1);
            this.currentPage = 0;
            this.getItems();
        },
        removeStatusFilter: function() {
            delete this.statusFilter;
            this.getItems();
        },
        printSuccessMessageCompat: function() {
            // ie8 compatibility
            var printContent = $('#success-message').clone();
            printContent.children('.print-success-message').remove();
            var windowUrl = 'PatientView';
            var uniqueName = new Date();
            var windowName = 'Print' + uniqueName.getTime();
            var printWindow = window.open(windowUrl, windowName, 'left=50000,top=50000,width=0,height=0');
            printWindow.document.write(printContent.html());
            printWindow.document.close();
            printWindow.focus();
            printWindow.print();
            printWindow.close();
        },
        search: function () {
            delete this.successMessage;
            this.currentPage = 0;
            this.getItems();
        },
        setSelectedGroup: function () {
            delete this.successMessage;
            var id = this.group.id;
            if (_.contains(this.selectedGroup, id)) {
                this.selectedGroup = _.without(this.selectedGroup, id);
            } else {
                this.selectedGroup.push(id);
            }
            this.currentPage = 0;
            this.getItems();
        },
        setSelectedRole: function () {
            var id = this.role.id;
            if (_.contains(this.selectedRole, id)) {
                this.selectedRole = _.without(this.selectedRole, id);
            } else {
                this.selectedRole.push(id);
            }
            this.currentPage = 0;
            this.getItems();
        },
        sortBy: function(sortField) {
            delete this.successMessage;
            this.currentPage = 0;
            if (this.sortField !== sortField) {
                this.sortDirection = 'ASC';
                this.sortField = sortField;
            } else {
                if (this.sortDirection === 'ASC') {
                    this.sortDirection = 'DESC';
                } else {
                    this.sortDirection = 'ASC';
                }
            }
        
            this.getItems();
        }
    }
}]);
