package org.patientview.api.controller;

import org.patientview.api.config.ExcludeFromApiDoc;
import org.patientview.api.service.ReviewService;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.Review;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.ParseException;
import java.util.List;

/**
 * RESTful interface for the management and retrieval of Platform reviews
 * These include reviews from Facebook, Google Play and iOS
 */
@RestController
@ExcludeFromApiDoc
public class ReviewsController extends BaseController<ReviewsController> {

    @Inject
    private ReviewService reviewService;


    /**
     * Get the public reviews
     *
     * @return 3 random public reviews
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @RequestMapping(value = "/public/reviews", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<List<Review>> getAll()
            throws ResourceNotFoundException, ResourceForbiddenException {
        return new ResponseEntity<>(reviewService.getReviewsToDisplay(), HttpStatus.OK);
    }
}
