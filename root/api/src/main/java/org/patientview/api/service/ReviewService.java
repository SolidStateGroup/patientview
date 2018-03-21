package org.patientview.api.service;

import org.patientview.persistence.model.Review;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;


/**
 * The review service which persists facebook reviews and
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface ReviewService {

    /**
     * Get 3 reviews to display, randomly selected from the database
     *
     * @return List 3 reviews to display on the FE
     */
    List<Review> getReviewsToDisplay();

    /**
     * Polls facebook for new product reviews and persists if they are a - good and b - contain review text
     *
     * @throws IOException when an IOException happens
     * @throws ParseException when date cannot be parsed
     */
    void pollForNewReviews() throws IOException, ParseException;
}
