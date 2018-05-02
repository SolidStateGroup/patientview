"use strict";

 angular.module("config", [])

.constant("ENV", {
  "name": "production",
  "apiEndpoint": "http://localhost:8080/api",
  "reCaptchaPublicKey": "6Lcrn0QUAAAAAJzzJaDrHK9_3udkFe3Xe9Cmj08m",
  "buildDateTime": 1525268377993
})

;