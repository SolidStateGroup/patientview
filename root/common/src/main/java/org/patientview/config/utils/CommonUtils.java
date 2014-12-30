package org.patientview.config.utils;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.patientview.persistence.model.enums.IdentifierTypes;

/**
 * Created by james@solidstategroup.com
 * Created on 13/06/2014
 */
public final class CommonUtils {

    private CommonUtils() {
    }

    public static String getAuthToken() {
        return RandomStringUtils.randomAlphanumeric(48);
    }

    public static String generatePassword() {
        return RandomStringUtils.randomAlphanumeric(12);
    }

    // to get type of identifier based on numeric range
    public static IdentifierTypes getIdentifierType(String identifier) {
        Long CHI_NUMBER_START = 10000010L;
        Long CHI_NUMBER_END = 3199999999L;
        Long HSC_NUMBER_START = 3200000010L;
        Long HSC_NUMBER_END = 3999999999L;
        Long NHS_NUMBER_START = 4000000000L;
        Long NHS_NUMBER_END = 9000000000L;

        try {
            // if non numeric then assume is dummy and return type as NHS number
            if (!NumberUtils.isNumber(identifier)) {
                return IdentifierTypes.NHS_NUMBER;
            } else {
                Long identifierNumber = Long.parseLong(identifier);

                if (identifierNumber != null) {
                    if (CHI_NUMBER_START <= identifierNumber && identifierNumber <= CHI_NUMBER_END) {
                        return IdentifierTypes.CHI_NUMBER;
                    }

                    if (HSC_NUMBER_START <= identifierNumber && identifierNumber <= HSC_NUMBER_END) {
                        return IdentifierTypes.HSC_NUMBER;
                    }

                    if (NHS_NUMBER_START <= identifierNumber && identifierNumber <= NHS_NUMBER_END) {
                        return IdentifierTypes.NHS_NUMBER;
                    }
                }

                // others outside range return type as NON UK
                return IdentifierTypes.NON_UK_UNIQUE;
            }
        } catch (Exception e) {
            return IdentifierTypes.NON_UK_UNIQUE;
        }
    }
}
