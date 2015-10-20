package org.patientview.api.controller;

import org.patientview.api.config.ExcludeFromApiDoc;
import org.patientview.api.service.LookingLocalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;

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
    private LookingLocalService lookingLocalService;

    private static final Logger LOGGER = LoggerFactory.getLogger(LookingLocalController.class);

    /*private int page = 0;
    private static final int ITEMS_PER_PAGE = 6;
    private static final int LINE_LENGTH = 65;
    private static final int LINES_PER_PAGE = 10;
    private String letterSelection = null;
    private String resultSelection = null;*/

    /**
     * Return Looking Local home XML
     */
    @RequestMapping(value = "/lookinglocal/home", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<String> getHomeXml() throws IOException, TransformerException, ParserConfigurationException {
        LOGGER.debug("home start");
        return new ResponseEntity<>(lookingLocalService.getHomeXml(), HttpStatus.OK);
    }

/*    *//**
     * Deal with the URIs "/lookinglocal/auth", check POSTed credentials
     * @param request HTTP request
     * @param response HTTP response
     * @param username User entered username
     * @param password User entered password
     *//*
    @RequestMapping(value = "/lookinglocal/auth", method = RequestMethod.POST)
    @ResponseBody
    public void getAuth(HttpServletRequest request,
                        @RequestParam(value = "username", required = false) String username,
                        @RequestParam(value = "password", required = false) String password,
                        HttpServletResponse response) {
        LOGGER.debug("auth start");

        PatientViewPasswordEncoder encoder = new PatientViewPasswordEncoder();
        User user = securityUserManager.get(username);

        if (user != null) {
            if (user.getPassword().equals(encoder.encode(password))) {

                // Authenticate user manually
                SecurityUser userLogin = (SecurityUser) userDetailsService.loadUserByUsername(username);
                SecurityContext securityContext = SecurityContextHolder.getContext();
                securityContext.setAuthentication(new UsernamePasswordAuthenticationToken(userLogin,
                        userLogin.getPassword(), userLogin.getAuthorities()));

                // manage extra authentication success handlers manually (usually
                // managed by PatientViewAuthenticationSuccessHandler.onAuthenticationSuccess)
                SecurityUser securityUser = (SecurityUser) securityContext.getAuthentication().getPrincipal();
                List<SpecialtyUserRole> specialtyUserRoles = userManager.getSpecialtyUserRoles(user);

                if (CollectionUtils.isNotEmpty(specialtyUserRoles)) {
                    Specialty specialty = specialtyUserRoles.get(0).getSpecialty();
                    securityUser.setSpecialty(specialty);
                    // manually add to session
                    HttpSession session = request.getSession(true);
                    session.setAttribute("SPRING_SECURITY_CONTEXT", securityContext);
                    LOGGER.debug("auth passed");
                    try {
                        LookingLocalUtils.getAuthXml(response);
                    } catch (Exception e) {
                        LOGGER.error("Could not create home screen response output stream{}" + e);
                    }

                } else {
                    LOGGER.debug("auth failed, no specialties");
                    try {
                        LookingLocalUtils.getErrorXml(response, "Auth error");
                    } catch (Exception e) {
                        LOGGER.error("Could not create home screen response output stream{}" + e);
                    }
                }
            } else {
                LOGGER.debug("auth failed, password");
                try {
                    LookingLocalUtils.getAuthErrorXml(response);
                } catch (Exception e) {
                    LOGGER.error("Could not create home screen response output stream{}" + e);
                }
            }
        } else {
            LOGGER.debug("auth failed, user null");
            try {
                LookingLocalUtils.getAuthErrorXml(response);
            } catch (Exception e) {
                LOGGER.error("Could not create home screen response output stream{}" + e);
            }
        }
    }

    *//**
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
    }

    *//**
     * Deal with the URIs "/lookinglocal/secure/main"
     * @param response HTTP response
     *//*
    @RequestMapping(value = Routes.LOOKING_LOCAL_MAIN)
    @ResponseBody
    public void getMainScreenXml(HttpServletResponse response) {
        LOGGER.debug("main start");
        try {
            LookingLocalUtils.getMainXml(response);
        } catch (Exception e) {
            LOGGER.error("Could not create main screen response output stream{}" + e);
        }
    }

    *//**
     * Deal with the URIs "/lookinglocal/secure/details"
     * @param request HTTP request
     * @param response HTTP response
     * @param selection User option selection
     * @param buttonPressed button according to Looking Local, used for "Back", "More" etc buttons
     *//*
    @RequestMapping(value = Routes.LOOKING_LOCAL_DETAILS)
    @ResponseBody
    public void getDetailsScreenXml(HttpServletRequest request, HttpServletResponse response,
                                    @RequestParam(value = "selection", required = false) String selection,
                                    @RequestParam(value = "buttonPressed", required = false) String buttonPressed) {
        LOGGER.debug("details start");
        page = 0;

        try {
            if (buttonPressed != null) {
                if (buttonPressed.equals("left")) {
                    getMainScreenXml(response);
                } else if (selection != null) {
                switch (Integer.parseInt(selection)) {
                    case LookingLocalUtils.OPTION_1 :
                        getMyDetailsScreenXml(request, response, "go");
                    case LookingLocalUtils.OPTION_2 : LookingLocalUtils.getMedicalResultsXml(request, response);
                        break;
                    case LookingLocalUtils.OPTION_3 :
                        getDrugsScreenXml(request, response, "go");
                    case LookingLocalUtils.OPTION_4 :
                        getLettersScreenXml(request, response, null, "go");
                    default : getErrorScreenXml(response, "Incorrect option");
                    }
                } else {
                    getErrorScreenXml(response, "Incorrect button [details] " + buttonPressed);
                }
            } else {
                getErrorScreenXml(response, "Button error");
            }
        } catch (Exception e) {
            getErrorScreenXml(response, e.getMessage());
            LOGGER.error("Could not create details response output stream: " + e.toString());
        }
    }

    *//**
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
                getDetailsScreenXml(request, response, null, "left");
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
                getDetailsScreenXml(request, response, null, "left");
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
    @RequestMapping(value = Routes.LOOKING_LOCAL_RESULTS_DISPLAY)
    @ResponseBody
    public void getMedicalResultsXml(HttpServletRequest request, HttpServletResponse response,
                                     @RequestParam(value = "selection", required = false) String selection,
                                     @RequestParam(value = "buttonPressed", required = false) String buttonPressed) {
        LOGGER.debug("resultsDisplay start");
        try {
            if (buttonPressed != null) {
                if (buttonPressed.equals("left")) {
                    getDetailsScreenXml(request, response, null, "left");
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
                    getDetailsScreenXml(request, response, null, "left");
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
