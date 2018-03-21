package org.patientview.api.model;

import lombok.Getter;
import lombok.Setter;
import org.patientview.persistence.model.Review;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tom on 21/03/2018.
 */
@Getter
@Setter
public class FacebookReviews {

    private ArrayList<Review> data;
    private Object paging;


    public List<Review> getData(){
        return data;
    }
}
