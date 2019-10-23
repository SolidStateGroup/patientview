<div class="modal-header">
    <h3 class="modal-title">New Recording</h3>
</div>
<div class="modal-body">
    <div data-ng-show="!successMessage">
        <h5>Sent {{letter.date | date:'dd-MMM-yyyy HH:mm'}} by {{letter.group.name}}</h5>
        <p>Are you sure you want to delete this letter?</p>
    </div>
    <div class="alert alert-danger" data-ng-show="errorMessage">
        <p>{{errorMessage}}</p>
    </div>
    <div class="alert alert-success" data-ng-show="successMessage">
        <p>{{successMessage}}</p>
    </div>
</div>
<div class="modal-footer">
    <button class="btn btn-danger" data-ng-click="remove()" data-ng-show="!successMessage">Delete Letter</button>
    <button class="btn btn-grey" data-ng-click="cancel()">Close</button>
</div>