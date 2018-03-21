package org.patientview.api.service.impl;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.patientview.api.model.FacebookReviews;
import org.patientview.api.service.ReviewService;
import org.patientview.persistence.model.Review;
import org.patientview.persistence.model.enums.ReviewSource;
import org.patientview.persistence.repository.ReviewRepository;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * {@inheritDoc}
 */
@Service
public class ReviewServiceImpl extends AbstractServiceImpl<ReviewServiceImpl> implements ReviewService {

    @Inject
    private ReviewRepository reviewRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Review> getReviewsToDisplay() {
        List<Review> reviews = new ArrayList<>();
        List<Review> returnedReviews = reviewRepository.findAll();

        if (returnedReviews.size() <= 3) {
            return returnedReviews;
        }

        while (reviews.size() < 3) {
            //Get a random selection of reviews
            int randomNum = ThreadLocalRandom.current().nextInt(0, returnedReviews.size() + 1);

            if (randomNum > reviews.size()) {
                randomNum = 0;
            }
            reviews.add(returnedReviews.get(randomNum));
        }

        return reviews;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void pollForNewReviews() throws IOException, ParseException {
        //https://stackoverflow.com/questions/17197970/facebook-permanent-page-access-token
        //Long lived token
        URL url = new URL("https://graph.facebook.com/v2.11/freemans.endowed/" +
                "ratings?fields=review_text%2Creviewer%2Chas_rating%2Crating%2Ccreated_time&limit=100%2Cid" +
                "&access_token=EAAbRjIsHEDsBAE5CK3279MgLG3UkhBnnfyZCZB189scxqwCe1ZAsC5ve6n5a6VEe6bB0K1TGHX" +
                "hOmclJ4lJPeO2Xn74KZALdsnrtRQiQbsgzKpNrVLk8WIZARqzfPUN7xhPwQLkN50yaMvfjIZCAfZAszKZCWZCrdgS" +
                "ZANWSfd4FGe5OSqKxRvhksN2zyN71FD410ZD");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        FacebookReviews returnedData = new Gson().fromJson(response.toString(),
                new TypeToken<FacebookReviews>() {
                }.getType());

        List<Review> reviewsToSave = new ArrayList<>();
        for (Review review : returnedData.getData()) {
            if (review.getRating() > 3 && review.getReview_text() != null) {
                review.setReviewSource(ReviewSource.FACEBOOK);
                reviewsToSave.add(new Review(review));
            }
        }

        //Delete the existing reviews as facebook doesnt have ids on the ratings
        reviewRepository.deleteAll();
        //Save the new ones
        reviewRepository.save(reviewsToSave);
    }
}
