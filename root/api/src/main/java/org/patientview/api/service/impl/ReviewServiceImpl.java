package org.patientview.api.service.impl;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.androidpublisher.AndroidPublisher;
import com.google.api.services.androidpublisher.AndroidPublisherScopes;
import com.google.api.services.androidpublisher.model.Comment;
import com.google.api.services.androidpublisher.model.ReviewsListResponse;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.patientview.api.model.FacebookReviews;
import org.patientview.api.service.ReviewService;
import org.patientview.persistence.model.IOSReview;
import org.patientview.persistence.model.Review;
import org.patientview.persistence.model.enums.ReviewSource;
import org.patientview.persistence.repository.ReviewRepository;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;


/**
 * {@inheritDoc}
 */
@Service
public class ReviewServiceImpl extends AbstractServiceImpl<ReviewServiceImpl> implements ReviewService {

    @Inject
    private ReviewRepository reviewRepository;

    /**
     * Global instance of the HTTP transport.
     */
    private static HttpTransport HTTP_TRANSPORT;

    /**
     * Global instance of the JSON factory.
     */
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    //IOS URL
    private static String IOS_REVIEWS_URL = "https://itunes.apple.com/gb/rss/customerreviews/" +
            "id=1263839920/sortBy=mostRecent/json";

    //URL
    private static String FACEBOOK_REVIEWS_URL = "https://graph.facebook.com/v2.11/freemans.endowed/" +
            "ratings?fields=review_text%2Creviewer%2Chas_rating%2Crating%2Ccreated_time&limit=100%2Cid" +
            "&access_token=EAAbRjIsHEDsBAE5CK3279MgLG3UkhBnnfyZCZB189scxqwCe1ZAsC5ve6n5a6VEe6bB0K1TGHX" +
            "hOmclJ4lJPeO2Xn74KZALdsnrtRQiQbsgzKpNrVLk8WIZARqzfPUN7xhPwQLkN50yaMvfjIZCAfZAszKZCWZCrdgS" +
            "ZANWSfd4FGe5OSqKxRvhksN2zyN71FD410ZD";

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Review> getReviewsToDisplay() {
        Map<Long, Review> reviews = new HashMap<>();
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
            if (reviews.get(returnedReviews.get(randomNum).getId()) == null) {
                reviews.put(returnedReviews.get(randomNum).getId(), returnedReviews.get(randomNum));
            }
        }
        return new ArrayList<>(reviews.values());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void pollForNewReviews() throws IOException, ParseException, GeneralSecurityException {
        try {
            pollForAndroidReviews();
        } catch (Exception e) {
            LOG.error("Unable to get Google Play Reviews ", e);
        }

        try {
            pollForiOSReviews();
        } catch (Exception e) {
            LOG.error("Unable to get iOS Reviews ", e);
        }

        try {
            pollForFacebookReviews();
        } catch (Exception e) {
            LOG.error("Unable to get Facebook Reviews ", e);
        }
    }


    /**
     * Poll for facebook reviews
     *
     * @throws IOException    thrown when not able to poll the updates
     * @throws ParseException thrown when dates cannot be parsed
     */
    private void pollForFacebookReviews() throws IOException, ParseException {
        //https://stackoverflow.com/questions/17197970/facebook-permanent-page-access-token
        //Long lived token
        URL url = new URL(FACEBOOK_REVIEWS_URL);
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
        reviewRepository.deleteByType(ReviewSource.FACEBOOK);
        //Save the new ones
        reviewRepository.save(reviewsToSave);
    }


    /**
     * Polls for ios reviews on the public URL
     *
     * @throws IOException when reviews cannot be read
     */
    private void pollForiOSReviews() throws IOException {
        URL url = new URL(IOS_REVIEWS_URL);

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

        IOSReview returnedData = new Gson().fromJson(response.toString(),
                new TypeToken<IOSReview>() {
                }.getType());

        List<Review> reviewsToSave = new ArrayList<>();
        for (Map<String, Object> entry : returnedData.getFeed().getEntry()) {

            Review newReview = new Review();
            newReview.setReviewSource(ReviewSource.IOS);
            for (Map.Entry<String, Object> entryValue : entry.entrySet()) {

                switch (entryValue.getKey()) {
                    case "author":
                        newReview.setReviewerName(((Map) ((Map) entryValue.getValue())
                                .get("name"))
                                .get("label").toString());
                        break;
                    case "im:rating":
                        newReview.setRating(
                                java.lang.Integer.parseInt(((Map) entryValue.getValue()).get("label").toString()));
                        break;
                    case "title":
                        //Accounts for when a title is given, but no content
                        if (newReview.getReviewText() == null) {
                            newReview.setReviewText(((Map) entryValue.getValue()).get("label").toString());
                        }
                        break;
                    case "content":
                        newReview.setReviewText(((Map) entryValue.getValue()).get("label").toString());
                        break;
                }
            }
            if (newReview.getRating() > 3 && newReview.getReviewerName() != null &&
                    !(newReview.getReviewText().contains("bug") ||
                            newReview.getReviewText().contains("issue") ||
                            newReview.getReviewText().contains("crash"))) {
                reviewsToSave.add(newReview);
            }
        }

        //Delete the existing reviews as ios doesnt have ids on the ratings
        reviewRepository.deleteByType(ReviewSource.IOS);
        //Save the new ones
        reviewRepository.save(reviewsToSave);
    }

    /**
     * Polls for new android reviews using the service account
     *
     * @throws IOException
     * @throws GeneralSecurityException
     */
    private void pollForAndroidReviews() throws IOException, GeneralSecurityException, URISyntaxException {
        // Create the API service.
        final AndroidPublisher service = init(
                "PatientView");

        ReviewsListResponse reviews =
                service.reviews().list("org.patientview.mobile").setMaxResults(1000L).execute();
        List<Review> reviewsToSave = new ArrayList<>();

        for (com.google.api.services.androidpublisher.model.Review review : reviews.getReviews()) {
            Review newReview = new Review();
            for (Comment comment : review.getComments()) {
                newReview.setRating(comment.getUserComment().getStarRating());
                newReview.setReviewText(comment.getUserComment().getText().replaceAll("[^\\p{ASCII}]", " "));
            }
            newReview.setExternalId(review.getReviewId());
            newReview.setReviewerName(review.getAuthorName().replaceAll("[^\\p{ASCII}]", " "));

            if (newReview.getRating() > 3 &&
                    !(newReview.getReviewText().contains("bug") ||
                            newReview.getReviewText().contains("issue") ||
                            newReview.getReviewText().contains("crash"))) {
                //Check if we have that id, as android doesnt maintain a history more than 1 week
                reviewRepository.getByExternalId(newReview.getExternalId());
                reviewsToSave.add(newReview);
            }
        }

        //Save the new ones
        reviewRepository.save(reviewsToSave);
    }


    /**
     * Performs all necessary setup steps for running requests against the API.
     *
     * @param applicationName     the name of the application: com.example.app
     *                            installed application)
     * @return the {@Link AndroidPublisher} service
     * @throws GeneralSecurityException
     * @throws IOException
     */
    private static AndroidPublisher init(String applicationName) throws IOException, GeneralSecurityException, URISyntaxException {

        // Authorization.
        newTrustedTransport();
        Credential credential;

        credential = authorizeWithServiceAccount();

        // Set up and return API client.
        return new AndroidPublisher.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, credential).setApplicationName(applicationName)
                .build();
    }

    /**
     * Create a new trusted transport.
     *
     * @throws GeneralSecurityException
     * @throws IOException
     */
    private static void newTrustedTransport() throws GeneralSecurityException,
            IOException {
        if (null == HTTP_TRANSPORT) {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        }
    }

    /**
     * Create a GoogleCredential service account for API requests
     *
     * @return Credential authorised google credential
     * @throws GeneralSecurityException
     * @throws IOException
     */
    private static Credential authorizeWithServiceAccount()
            throws GeneralSecurityException, IOException, URISyntaxException {
        URL filePath = Thread.currentThread().getContextClassLoader().getResource(
                "google-play-key.json");

        File file = new File(filePath.toURI());

        GoogleCredential credential = GoogleCredential.fromStream(
                new FileInputStream(file))
                .createScoped(Collections.singleton(AndroidPublisherScopes.ANDROIDPUBLISHER));

        return credential;
    }
}
