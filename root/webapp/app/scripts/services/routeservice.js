'use strict';

angular.module('patientviewApp').factory('RouteService', [function () {
    return {
        getVerifyRoute: function() {
            return {
                'url': '/verify',
                'templateUrl': 'views/verify.html',
                'controller': 'VerifyCtrl',
                'title': 'Verify',
                'lookup': {
                    'id': 5,
                    'value': 'NOT_DISPLAYED',
                    'lookupType': {
                        'id': 2,
                        'type': 'MENU',
                        'description': 'Type of menu'
                    }
                }
            };
        },
        getMainRoute: function() {
            return {
                'url': '/',
                'templateUrl': 'views/main.html',
                'controller': 'MainCtrl',
                'title': 'Home',
                'lookup': {
                    'id': 4,
                    'value': 'TOP',
                    'lookupType': {
                        'id': 2,
                        'type': 'MENU',
                        'description': 'Type of menu'
                    }
                }
            };
        },
        getLoginRoute: function() {
            return {
                'url': '/login',
                'templateUrl': 'views/login.html',
                'controller': 'LoginCtrl',
                'title': 'Login',
                'lookup': {
                    'id': 100,
                    'value': 'NOT_DISPLAYED',
                    'lookupType': {
                        'id': 2,
                        'type': 'MENU',
                        'description': 'Type of menu'
                    }
                }
            };
        },
        getLogoutRoute: function() {
            return {
                'url': '/logout',
                'templateUrl': 'views/logout.html',
                'controller': 'LogoutCtrl',
                'title': 'Log Out',
                'lookup': {
                    'id': 96,
                    'value': 'NOT_DISPLAYED',
                    'lookupType': {
                        'id': 2,
                        'type': 'MENU',
                        'description': 'Type of menu'
                    }
                }
            };
        },
        getAccountRoute: function() {
            return {
                'url': '/settings',
                'templateUrl': 'views/account.html',
                'controller': 'AccountCtrl',
                'title': 'Settings',
                'lookup': {
                    'id': 97,
                    'value': 'NOT_DISPLAYED',
                    'lookupType': {
                        'id': 2,
                        'type': 'MENU',
                        'description': 'Type of menu'
                    }
                }
            };
        },
        getRequestRoute: function() {
            return {
                'url': '/request',
                'templateUrl': 'views/request.html',
                'controller': 'RequestCtrl',
                'title': 'Request',
                'lookup': {
                    'id': 98,
                    'value': 'NOT_DISPLAYED',
                    'lookupType': {
                        'id': 2,
                        'type': 'MENU',
                        'description': 'Type of menu'
                    }
                }
            };
        },
        getChangePasswordRoute: function() {
            return {
                'url': '/changepassword',
                'templateUrl': 'views/changepassword.html',
                'controller': 'PasswordChangeCtrl',
                'title': 'Change Password',
                'lookup': {
                    'id': 99,
                    'value': 'TOP',
                    'lookupType': {
                        'id': 3,
                        'type': 'MENU',
                        'description': 'Type of menu'
                    }
                }
            };
        },
        getForgottenPasswordRoute: function() {
            return {
                'url': '/forgottenpassword',
                'templateUrl': 'views/forgottenpassword.html',
                'controller': 'ForgottenPasswordCtrl',
                'title': 'Forgotten Password',
                'lookup': {
                    'id': 101,
                    'value': 'NOT_DISPLAYED',
                    'lookupType': {
                        'id': 3,
                        'type': 'MENU',
                        'description': 'Type of menu'
                    }
                }
            };
        },
        getContactUnitRoute: function() {
            return {
                'url': '/contactunit',
                'templateUrl': 'views/contactunit.html',
                'controller': 'ContactUnitCtrl',
                'title': 'Contact Unit',
                'lookup': {
                    'id': 102,
                    'value': 'NOT_DISPLAYED',
                    'lookupType': {
                        'id': 3,
                        'type': 'MENU',
                        'description': 'Type of menu'
                    }
                }
            };
        },
        getTermsRoute: function() {
            return {
                'url': '/terms',
                'templateUrl': 'views/terms.html',
                'controller': 'TermsCtrl',
                'title': 'Terms and Conditions',
                'lookup': {
                    'id': 103,
                    'value': 'NOT_DISPLAYED',
                    'lookupType': {
                        'id': 3,
                        'type': 'MENU',
                        'description': 'Type of menu'
                    }
                }
            };
        },
        getQuestionnaireFAQRoute: function() {
            return {
                'url': '/questionnaire-faq',
                'templateUrl': 'views/questionnaire-faq.html',
                'controller': 'TermsCtrl',
                'title': 'Questionnaire FAQs',
                'lookup': {
                    'id': 103,
                    'value': 'NOT_DISPLAYED',
                    'lookupType': {
                        'id': 3,
                        'type': 'MENU',
                        'description': 'Type of menu'
                    }
                }
            };
        },
        getQuestionnaireScoresRoute: function() {
            return {
                'url': '/questionnaire-scores',
                'templateUrl': 'views/questionnaire-scores.html',
                'controller': 'TermsCtrl',
                'title': 'Questionnaire Scoring',
                'lookup': {
                    'id': 103,
                    'value': 'NOT_DISPLAYED',
                    'lookupType': {
                        'id': 3,
                        'type': 'MENU',
                        'description': 'Type of menu'
                    }
                }
            };
        },
        getPrivacyRoute: function() {
            return {
                'url': '/privacy',
                'templateUrl': 'views/privacy.html',
                'controller': 'PrivacyCtrl',
                'title': 'Privacy Policy',
                'lookup': {
                    'id': 103,
                    'value': 'NOT_DISPLAYED',
                    'lookupType': {
                        'id': 3,
                        'type': 'MENU',
                        'description': 'Type of menu'
                    }
                }
            };
        },
        getWhatCanItDoRoute: function() {
            return {
                'url': '/whatcanitdo',
                'templateUrl': 'views/whatcanitdo.html',
                'controller': 'WhatCanItDoCtrl',
                'title': 'What Can It Do',
                'lookup': {
                    'id': 103,
                    'value': 'NOT_DISPLAYED',
                    'lookupType': {
                        'id': 3,
                        'type': 'MENU',
                        'description': 'Type of menu'
                    }
                }
            };
        },
        getGpLoginRoute: function() {
            return {
                'url': '/gplogin',
                'templateUrl': 'views/gplogin.html',
                'controller': 'GpLoginCtrl',
                'title': 'GP Practices - Claim your PatientView login here',
                'lookup': {
                    'id': 103,
                    'value': 'NOT_DISPLAYED',
                    'lookupType': {
                        'id': 3,
                        'type': 'MENU',
                        'description': 'Type of menu'
                    }
                }
            };
        },
        getSetSecretWordRoute: function() {
            return {
                'url': '/setsecretword',
                'templateUrl': 'views/setsecretword.html',
                'controller': 'SetSecretWordCtrl',
                'title': 'Set Secret Word',
                'lookup': {
                    'id': 99,
                    'value': 'NOT_DISPLAYED',
                    'lookupType': {
                        'id': 3,
                        'type': 'MENU',
                        'description': 'Type of menu'
                    }
                }
            };
        },
        getApiDocsRoute: function() {
            return {
                'url': '/apidocs',
                'templateUrl': 'views/apidocs.html',
                'controller': 'ApiDocsCtrl',
                'title': 'API Documentation',
                'lookup': {
                    'id': 99,
                    'value': 'NOT_DISPLAYED',
                    'lookupType': {
                        'id': 3,
                        'type': 'MENU',
                        'description': 'Type of menu'
                    }
                }
            };
        }
    };
}]);
