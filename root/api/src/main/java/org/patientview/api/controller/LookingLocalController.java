package org.patientview.api.controller;

import org.apache.commons.lang3.StringUtils;
import org.patientview.api.config.ExcludeFromApiDoc;
import org.patientview.api.service.AuthenticationService;
import org.patientview.api.service.LookingLocalProperties;
import org.patientview.api.service.LookingLocalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;

/**
 * RESTful interface for Looking Local, XML based front end for TV.
 *
 * Created by jamesr@solidstategroup.com
 * Created on 20/10/2014.
 */
@RestController
@ExcludeFromApiDoc
public class LookingLocalController extends BaseController {

    @Inject
    private AuthenticationService authenticationService;

    @Inject
    private LookingLocalService lookingLocalService;

    private static final Logger LOGGER = LoggerFactory.getLogger(LookingLocalController.class);

    /**
     * Deal with the URIs "/lookinglocal/auth", check POSTed credentials and return login successful screen
     * @param username User entered username
     * @param password User entered password
     */
    @RequestMapping(value = LookingLocalProperties.LOOKING_LOCAL_AUTH, method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<String> auth(
            @RequestParam(value = "username", required = false) String username,
            @RequestParam(value = "password", required = false) String password) {
        try {
            LOGGER.info("auth start");

            try {
                String token = authenticationService.authenticate(username, password);
                try {
                    return new ResponseEntity<>(lookingLocalService.getLoginSuccessfulXml(token), HttpStatus.OK);
                } catch (Exception e) {
                    return new ResponseEntity<>(
                            lookingLocalService.getErrorXml("Could not login, " + e.getMessage()), HttpStatus.OK);
                }
            } catch (UsernameNotFoundException e) {
                return new ResponseEntity<>(lookingLocalService.getAuthErrorXml(), HttpStatus.OK);
            } catch (AuthenticationServiceException e) {
                return new ResponseEntity<>(lookingLocalService.getAuthErrorXml(), HttpStatus.OK);
            }
        } catch (Exception e) {
            try {
                return new ResponseEntity<>(lookingLocalService.getErrorXml("Could not get auth " + e.getMessage()),
                        HttpStatus.OK);
            } catch (Exception e2) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Deal with the URIs "/lookinglocal/secure/details"
     * @param selection User option selection
     * @param buttonPressed button according to Looking Local, used for "Back", "More" etc buttons
     */
    @RequestMapping(value = LookingLocalProperties.LOOKING_LOCAL_DETAILS, method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<String> details(
            @RequestParam(value = "selection", required = false) String selection,
            @RequestParam(value = "buttonPressed", required = false) String buttonPressed,
            @RequestParam(value = "token", required = false) String token) {
        LOGGER.info("details start");

        try {
            if (buttonPressed != null) {
                if (buttonPressed.equals("left")) {
                    return new ResponseEntity<>(lookingLocalService.getMainXml(token), HttpStatus.OK);
                } else if (selection != null) {
                    switch (Integer.parseInt(selection)) {
                        case LookingLocalProperties.OPTION_1 :
                            return myDetails("go", 0, token);
                        case LookingLocalProperties.OPTION_2 :
                            return results("go", 0, null, token);
                        case LookingLocalProperties.OPTION_3 :
                            return drugs("go", 0, token);
                        case LookingLocalProperties.OPTION_4 :
                            return letters("go", 0, null, token);
                        default :
                            return new ResponseEntity<>(lookingLocalService.getErrorXml("Incorrect option"), HttpStatus.OK);
                    }
                } else {
                    return new ResponseEntity<>(
                            lookingLocalService.getErrorXml("Incorrect button [details]"), HttpStatus.OK);
                }
            } else {
                return new ResponseEntity<>(lookingLocalService.getErrorXml("Button error"), HttpStatus.OK);
            }
        } catch (Exception e) {
            try {
                return new ResponseEntity<>(lookingLocalService.getErrorXml(e.getMessage()), HttpStatus.OK);
            } catch (Exception e2) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Deal with the URIs "/lookinglocal/secure/drugs"
     * @param buttonPressed button according to Looking Local, used for "Back", "More" etc buttons
     * @param page page of details to get
     * @param token authorisation token
     * @return XML for drugs list
     */
    @RequestMapping(value = LookingLocalProperties.LOOKING_LOCAL_DRUGS, method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<String> drugs(
            @RequestParam(value = "buttonPressed", required = false) String buttonPressed,
            @RequestParam(value = "page", required = false) int page,
            @RequestParam(value = "token", required = false) String token) {
        LOGGER.info("drugs start");

        try {
            if (buttonPressed.equals("left") || buttonPressed.equals("right")) {
                if (buttonPressed.equals("right")) {
                    page++;
                } else if (buttonPressed.equals("left")) {
                    page--;
                }
            }

            if (page < 0) {
                return details(null, "left", token);
            }

            return new ResponseEntity<>(lookingLocalService.getDrugsXml(token, page), HttpStatus.OK);

        } catch (Exception e) {
            try {
                return new ResponseEntity<>(lookingLocalService.getErrorXml(e.getMessage()), HttpStatus.OK);
            } catch (Exception e2) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Deal with URI "/lookinglocal/home", shows login screen
     */
    @RequestMapping(value = LookingLocalProperties.LOOKING_LOCAL_HOME, method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<String> home() {
        LOGGER.info("home start");
        try {
            return new ResponseEntity<>(lookingLocalService.getHomeXml(), HttpStatus.OK);
        } catch (Exception e) {
            try {
                return new ResponseEntity<>(lookingLocalService.getErrorXml(e.getMessage()), HttpStatus.OK);
            } catch (Exception e2) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Deal with the URIs "/lookinglocal/secure/letter"
     * @param buttonPressed button according to Looking Local, used for "Back", "More" etc buttons
     * @param page page of details to get
     * @param token authorisation token
     * @return XML for single letter
     */
    @RequestMapping(value = LookingLocalProperties.LOOKING_LOCAL_LETTER, method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<String> letter(
            @RequestParam(value = "buttonPressed", required = false) String buttonPressed,
            @RequestParam(value = "page", required = false) int page,
            @RequestParam(value = "selection", required = false) String selection,
            @RequestParam(value = "token", required = false) String token) {
        LOGGER.info("letter start");

        if (selection == null) {
            try {
                return new ResponseEntity<>(lookingLocalService.getErrorXml("letter not chosen"), HttpStatus.OK);
            } catch (Exception e2) {
                throw new RuntimeException(e2);
            }
        }

        if (selection.isEmpty()) {
            return letters("go", 0, null, token);
        }

        try {
            if (buttonPressed.equals("left") || buttonPressed.equals("right")) {
                if (buttonPressed.equals("right")) {
                    page++;
                } else if (buttonPressed.equals("left")) {
                    page--;
                }
            }

            if (page < 0) {
                return letters("go", 0, null, token);
            }

            return new ResponseEntity<>(lookingLocalService.getLetterXml(token, page, selection), HttpStatus.OK);

        } catch (Exception e) {
            try {
                return new ResponseEntity<>(lookingLocalService.getErrorXml(e.getMessage()), HttpStatus.OK);
            } catch (Exception e2) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Deal with the URIs "/lookinglocal/secure/letters"
     * @param buttonPressed button according to Looking Local, used for "Back", "More" etc buttons
     * @param page page of details to get
     * @param token authorisation token
     * @return XML for letters list
     */
    @RequestMapping(value = LookingLocalProperties.LOOKING_LOCAL_LETTERS, method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<String> letters(
            @RequestParam(value = "buttonPressed", required = false) String buttonPressed,
            @RequestParam(value = "page", required = false) int page,
            @RequestParam(value = "selection", required = false) String selection,
            @RequestParam(value = "token", required = false) String token) {
        LOGGER.info("letters start");

        try {
            if (StringUtils.isNotEmpty(buttonPressed)) {
                // left or right button pressed
                if (buttonPressed.equals("left") || buttonPressed.equals("right")) {
                    if (buttonPressed.equals("right")) {
                        page++;
                    } else if (buttonPressed.equals("left")) {
                        page--;
                    }
                }

                if (page < 0) {
                    return details(null, "left", token);
                }

                return new ResponseEntity<>(lookingLocalService.getLettersXml(token, page), HttpStatus.OK);
            } else if (selection != null) {
                // selection of letter made
                return new ResponseEntity<>(lookingLocalService.getLetterXml(token, page, selection), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(lookingLocalService.getErrorXml("Button error"), HttpStatus.OK);
            }

        } catch (Exception e) {
            try {
                return new ResponseEntity<>(lookingLocalService.getErrorXml(e.getMessage()), HttpStatus.OK);
            } catch (Exception e2) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
    * Deal with the URIs "/lookinglocal/secure/main"
    */
    @RequestMapping(value = LookingLocalProperties.LOOKING_LOCAL_MAIN, method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<String> main(@RequestParam(value = "token", required = false) String token) {
        LOGGER.info("main start");
        try {
            return new ResponseEntity<>(lookingLocalService.getMainXml(token), HttpStatus.OK);
        } catch (Exception e) {
            try {
                return new ResponseEntity<>(lookingLocalService.getErrorXml("Could not get main " + e.getMessage()),
                        HttpStatus.OK);
            } catch (Exception e2) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Deal with the URIs "/lookinglocal/secure/myDetails"
     * @param buttonPressed button according to Looking Local, used for "Back", "More" etc buttons
     * @param page page of details to get
     * @param token authorisation token
     * @return XML for my details
     */
    @RequestMapping(value = LookingLocalProperties.LOOKING_LOCAL_MY_DETAILS, method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<String> myDetails(
            @RequestParam(value = "buttonPressed", required = false) String buttonPressed,
            @RequestParam(value = "page", required = false) int page,
            @RequestParam(value = "token", required = false) String token) {
        LOGGER.info("my details start");

        try {
            if (buttonPressed.equals("left") || buttonPressed.equals("right")) {
                if (buttonPressed.equals("right")) {
                    page++;
                } else if (buttonPressed.equals("left")) {
                    page--;
                }
            }

            if (page < 0) {
                return details(null, "left", token);
            }

            return new ResponseEntity<>(lookingLocalService.getMyDetailsXml(token, page), HttpStatus.OK);

        } catch (Exception e) {
            try {
                return new ResponseEntity<>(lookingLocalService.getErrorXml(e.getMessage()), HttpStatus.OK);
            } catch (Exception e2) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Deal with the URIs "/lookinglocal/secure/result"
     * @param buttonPressed button according to Looking Local, used for "Back", "More" etc buttons
     * @param page page of details to get
     * @param token authorisation token
     * @return XML for results list for one result type
     */
    @RequestMapping(value = LookingLocalProperties.LOOKING_LOCAL_RESULT, method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<String> result(
            @RequestParam(value = "buttonPressed", required = false) String buttonPressed,
            @RequestParam(value = "page", required = false) int page,
            @RequestParam(value = "selection", required = false) String selection,
            @RequestParam(value = "token", required = false) String token) {
        LOGGER.info("result start");

        if (selection == null) {
            try {
                return new ResponseEntity<>(lookingLocalService.getErrorXml("Result type not chosen"), HttpStatus.OK);
            } catch (Exception e2) {
                throw new RuntimeException(e2);
            }
        }

        try {
            if (buttonPressed.equals("left") || buttonPressed.equals("right")) {
                if (buttonPressed.equals("right")) {
                    page++;
                } else if (buttonPressed.equals("left")) {
                    page--;
                }
            }

            if (page < 0) {
                return results("go", 0, null, token);
            }

            return new ResponseEntity<>(lookingLocalService.getResultXml(token, page, selection), HttpStatus.OK);

        } catch (Exception e) {
            try {
                return new ResponseEntity<>(lookingLocalService.getErrorXml(e.getMessage()), HttpStatus.OK);
            } catch (Exception e2) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Deal with the URIs "/lookinglocal/secure/results"
     * @param buttonPressed button according to Looking Local, used for "Back", "More" etc buttons
     * @param page page of details to get
     * @param token authorisation token
     * @return XML for result type list
     */
    @RequestMapping(value = LookingLocalProperties.LOOKING_LOCAL_RESULTS, method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<String> results(
            @RequestParam(value = "buttonPressed", required = false) String buttonPressed,
            @RequestParam(value = "page", required = false) int page,
            @RequestParam(value = "selection", required = false) String selection,
            @RequestParam(value = "token", required = false) String token) {
        LOGGER.info("results start");

        try {
            if (StringUtils.isNotEmpty(buttonPressed)) {
                // left or right button pressed
                if (buttonPressed.equals("left") || buttonPressed.equals("right")) {
                    if (buttonPressed.equals("right")) {
                        page++;
                    } else if (buttonPressed.equals("left")) {
                        page--;
                    }
                }

                if (page < 0) {
                    return details(null, "left", token);
                }

                return new ResponseEntity<>(lookingLocalService.getResultsXml(token, page), HttpStatus.OK);
            } else if (selection != null) {
                // selection of result type made
                return result("go", 0, selection, token);
            } else {
                return new ResponseEntity<>(lookingLocalService.getErrorXml("Button error"), HttpStatus.OK);
            }
        } catch (Exception e) {
            try {
                return new ResponseEntity<>(lookingLocalService.getErrorXml(e.getMessage()), HttpStatus.OK);
            } catch (Exception e2) {
                throw new RuntimeException(e);
            }
        }
    }
}
