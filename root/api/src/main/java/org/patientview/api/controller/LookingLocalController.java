package org.patientview.api.controller;

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

    /*private int page = 0;
    private static final int ITEMS_PER_PAGE = 6;
    private static final int LINE_LENGTH = 65;
    private static final int LINES_PER_PAGE = 10;
    private String letterSelection = null;
    private String resultSelection = null;*/

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
     * Deal with the URIs "/lookinglocal/error"
     * @param response HTTP response
     *//*
    @RequestMapping(value = Routes.LOOKING_LOCAL_ERROR)
    @ResponseBody
    public void getErrorScreenXml(HttpServletResponse response, String message) {
        LOGGER.debug("error start");
        try {
            LookingLocalUtils.getErrorXml(response, message);
        } catch (Exception e) {
            LOGGER.error("Could not create main screen response output stream{}" + e);
        }
    }*/



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
                        return results("go", 0, token);
                    case LookingLocalProperties.OPTION_3 :
                        return drugs("go", 0, token);
                    case LookingLocalProperties.OPTION_4 :
                        return letters("go", 0, token);
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
            @RequestParam(value = "token", required = false) String token) {
        LOGGER.info("results start");

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

            return new ResponseEntity<>(lookingLocalService.getResultsXml(token, page), HttpStatus.OK);

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
            @RequestParam(value = "token", required = false) String token) {
        LOGGER.info("letters start");

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

            return new ResponseEntity<>(lookingLocalService.getLettersXml(token, page), HttpStatus.OK);

        } catch (Exception e) {
            try {
                return new ResponseEntity<>(lookingLocalService.getErrorXml(e.getMessage()), HttpStatus.OK);
            } catch (Exception e2) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Deal with the URIs "/lookinglocal/secure/myDetails"
     * @param request HTTP request
     * @param response HTTP response
     * @param buttonPressed button according to Looking Local, used for "Back", "More" etc buttons
     *//*
    @RequestMapping(value = Routes.LOOKING_LOCAL_MY_DETAILS)
    @ResponseBody
    public void getMyDetailsScreenXml(HttpServletRequest request, HttpServletResponse response,
                                    @RequestParam(value = "buttonPressed", required = false) String buttonPressed) {
        LOGGER.debug("my details start");

        try {
            if (buttonPressed.equals("left") || buttonPressed.equals("right")) {
                if (buttonPressed.equals("right")) {
                    page++;
                } else if (buttonPressed.equals("left")) {
                    page--;
                }
            }

            if (page == -1) {
                details(request, response, null, "left");
            }

            LookingLocalUtils.getMyDetailsXml(request, response, page);

        } catch (Exception e) {
            getErrorScreenXml(response, e.getMessage());
            LOGGER.error("Could not create details response output stream: " + e.toString());
        }
    }

    *//**
     * Deal with the URIs "/lookinglocal/secure/drugs"
     * @param request HTTP request
     * @param response HTTP response
     * @param buttonPressed button according to Looking Local, used for "Back", "More" etc buttons
     *//*
    @RequestMapping(value = Routes.LOOKING_LOCAL_DRUGS)
    @ResponseBody
    public void getDrugsScreenXml(HttpServletRequest request, HttpServletResponse response,
                                    @RequestParam(value = "buttonPressed", required = false) String buttonPressed) {
        LOGGER.debug("drugs start");

        try {
            if (buttonPressed.equals("left") || buttonPressed.equals("right")) {
                if (buttonPressed.equals("right")) {
                    page++;
                } else if (buttonPressed.equals("left")) {
                    page--;
                }
            } else {
                page = 0;
                LookingLocalUtils.getDrugsXml(request, response, page, ITEMS_PER_PAGE);
            }

            if (page == -1) {
                details(request, response, null, "left");
            } else {
                LookingLocalUtils.getDrugsXml(request, response, page, ITEMS_PER_PAGE);
            }
        } catch (Exception e) {
            getErrorScreenXml(response, e.getMessage());
            LOGGER.error("Could not create details response output stream: " + e.toString());
        }
    }

    *//**
     * Deal with the URIs "/lookinglocal/secure/resultsDisplay"
     * @param request HTTP request
     * @param response HTTP response
     * @param selection User option selection
     * @param buttonPressed button according to Looking Local, used for "Back", "More" etc buttons
     *//*
    @RequestMapping(value = Routes.LOOKING_LOCAL_RESULTS)
    @ResponseBody
    public void getMedicalResultsXml(HttpServletRequest request, HttpServletResponse response,
                                     @RequestParam(value = "selection", required = false) String selection,
                                     @RequestParam(value = "buttonPressed", required = false) String buttonPressed) {
        LOGGER.debug("resultsDisplay start");
        try {
            if (buttonPressed != null) {
                if (buttonPressed.equals("left")) {
                    details(request, response, null, "left");
                } else if (selection != null) {
                    page = 0;
                    resultSelection = selection;
                    getResultXml(request, response, resultSelection, "go");
                } else {
                    getErrorScreenXml(response, "Incorrect button [resultsDisplay] " + buttonPressed);
                }
            } else {
                getErrorScreenXml(response, "Button error");
            }
        } catch (Exception e) {
            LOGGER.error("Could not create medical result details response output stream{}" + e);
        }
    }

    *//**
     * Deal with the URIs "/lookinglocal/secure/resultDisplay"
     * @param request HTTP request
     * @param response HTTP response
     * @param selection User option selection
     * @param buttonPressed button according to Looking Local, used for "Back", "More" etc buttons
     *//*
    @RequestMapping(value = Routes.LOOKING_LOCAL_RESULT_DISPLAY)
    @ResponseBody
    public void getResultXml(HttpServletRequest request, HttpServletResponse response,
                             @RequestParam(value = "selection", required = false) String selection,
                             @RequestParam(value = "buttonPressed", required = false) String buttonPressed) {
        LOGGER.debug("resultDisplay start");
        try {
            if (buttonPressed.equals("left") || buttonPressed.equals("right")) {
                if (buttonPressed.equals("right")) {
                    page++;
                } else if (buttonPressed.equals("left")) {
                    page--;
                }

                if (page == -1) {
                    page = 0;
                    LookingLocalUtils.getMedicalResultsXml(request, response);
                } else {
                    LookingLocalUtils.getResultsDetailsXml(request, response, resultSelection, page, ITEMS_PER_PAGE);
                }
            } else {
                page = 0;

                if (selection == null) {
                    page = 0;
                    LookingLocalUtils.getMedicalResultsXml(request, response);
                } else {
                    resultSelection = selection;
                    LookingLocalUtils.getResultsDetailsXml(request, response, resultSelection, page, ITEMS_PER_PAGE);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Could not create result details response output stream{}" + e);
            getErrorScreenXml(response, e.getMessage());
        }
    }

    *//**
     * Deal with the URIs "/lookinglocal/secure/letters"
     * @param request HTTP request
     * @param response HTTP response
     * @param buttonPressed button according to Looking Local, used for "Back", "More" etc buttons
     *//*
    @RequestMapping(value = Routes.LOOKING_LOCAL_LETTERS)
    @ResponseBody
    public void getLettersScreenXml(HttpServletRequest request, HttpServletResponse response,
                                    @RequestParam(value = "selection", required = false) String selection,
                                    @RequestParam(value = "buttonPressed", required = false) String buttonPressed) {
        LOGGER.debug("letters start");
        letterSelection = null;

        try {
            if (buttonPressed.equals("left") || buttonPressed.equals("right")) {
                if (buttonPressed.equals("right")) {
                    page++;
                } else if (buttonPressed.equals("left")) {
                    page--;
                }

                if (page == -1) {
                    details(request, response, null, "left");
                } else {
                    LookingLocalUtils.getLettersXml(request, response, page, ITEMS_PER_PAGE);
                }
            } else if (selection != null) {
                page = 0;
                letterSelection = selection;
                getLetterXml(request, response, letterSelection, "go");
            } else {
                LookingLocalUtils.getLettersXml(request, response, page, ITEMS_PER_PAGE);
            }
        } catch (Exception e) {
            getErrorScreenXml(response, e.getMessage());
            LOGGER.error("Could not create details response output stream: " + e.toString());
        }
    }

    *//**
     * Deal with the URIs "/lookinglocal/secure/letterDisplay"
     * @param request HTTP request
     * @param response HTTP response
     * @param selection User option selection
     * @param buttonPressed button according to Looking Local, used for "Back", "More" etc buttons
     *//*
    @RequestMapping(value = Routes.LOOKING_LOCAL_LETTER_DISPLAY)
    @ResponseBody
    public void getLetterXml(HttpServletRequest request, HttpServletResponse response,
                             @RequestParam(value = "selection", required = false) String selection,
                             @RequestParam(value = "buttonPressed", required = false) String buttonPressed) {
        LOGGER.debug("letterDisplay start");
        try {
            if (buttonPressed.equals("left") || buttonPressed.equals("right")) {
                if (buttonPressed.equals("right")) {
                    page++;
                } else if (buttonPressed.equals("left")) {
                    page--;
                }

                if (page == -1) {
                    page = 0;
                    LookingLocalUtils.getLettersXml(request, response, page, ITEMS_PER_PAGE);
                } else {
                    LookingLocalUtils.getLetterDetailsXml(request, response, letterSelection, page, LINES_PER_PAGE,
                            LINE_LENGTH);
                }
            } else {
                page = 0;

                if (selection == null) {
                    page = 0;
                    LookingLocalUtils.getLettersXml(request, response, page, ITEMS_PER_PAGE);
                } else {
                    letterSelection = selection;
                    LookingLocalUtils.getLetterDetailsXml(request, response, letterSelection, page, LINES_PER_PAGE,
                            LINE_LENGTH);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Could not create letter details response output stream{}" + e);
            getErrorScreenXml(response, e.getMessage());
        }
    }*/
}
