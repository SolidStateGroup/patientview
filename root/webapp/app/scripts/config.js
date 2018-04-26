"use strict";

 angular.module("config", [])

.constant("ENV", {
  "name": "production",
  "apiEndpoint": "https://test.patientview.org/api",
  "reCaptchaPublicKey": "",
  "buildDateTime": 1524731547569
})

;