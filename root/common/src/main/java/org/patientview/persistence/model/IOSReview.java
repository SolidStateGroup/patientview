package org.patientview.persistence.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Models a research study criteria that is available to a user
 */
@Data
@NoArgsConstructor
public class IOSReview extends BaseModel {

    private Feed feed;

    @Data
    public class Feed{
        Map<String, Object>[]  entry;
    }

    @Data
    public class Author {
        GenericLabel name;
    }

    @Data
    public class GenericLabel {
        String label;
    }
}