package org.patientview.api.service;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.patientview.api.service.impl.ReviewServiceImpl;
import org.patientview.persistence.model.Review;
import org.patientview.persistence.model.User;
import org.patientview.persistence.repository.ReviewRepository;
import org.patientview.test.util.TestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.ParseException;

public class ReviewServiceTest {

    User creator;

    @Mock
    ReviewRepository reviewRepository;

    @InjectMocks
    ReviewService reviewService = new ReviewServiceImpl();

    private static final Logger LOG = LoggerFactory.getLogger(ReviewServiceTest.class);


    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        creator = TestUtils.createUser("creator");
    }

    @After
    public void tearDown() {
        TestUtils.removeAuthentication();
    }

    @Test
    public void pollForData() throws IOException, ParseException, GeneralSecurityException {
        reviewService.pollForNewReviews();
    }

    @Test
    public void testForNameCorrection() throws ParseException {
        Review review = new Review();
        review.setReview_text("This is á test");
        review.createAndSetReviewer("Siobhán Willy-Furth ");

        Review newReview = new Review(review);

        LOG.info(newReview.getReviewerName());
        LOG.info(newReview.getReviewText());

    }
}
