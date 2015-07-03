package org.patientview.config.utils;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.joda.time.format.DateTimeFormat;
import org.patientview.persistence.model.enums.IdentifierTypes;

import java.util.Date;

/**
 * Created by james@solidstategroup.com
 * Created on 13/06/2014
 */
public final class CommonUtils {

    private CommonUtils() {
    }

    public static byte[] base64ToByteArray(String base64) {
        return Base64.decodeBase64(base64);
    }

    public static String byteArrayToBase64(byte[] byteArray) {
        return Base64.encodeBase64String(byteArray);
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
                return IdentifierTypes.NON_UK_UNIQUE;
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

    public static String cleanSql(String input) {
        if (input == null) {
            return null;
        }

        if (StringUtils.isEmpty(input)) {
            return "";
        }

        return input.replace("'","''");
    }

    public static Date getDateFromString(String text) {

        try {
            return DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss").parseDateTime(text).toDate();
        } catch (IllegalArgumentException iae) {
            // likely too short
        }

        try {
            return DateTimeFormat.forPattern("dd/MM/yyyy' 'HH:mm:ss").parseDateTime(text).toDate();
        } catch (IllegalArgumentException iae) {
            // likely too short
        }

        try {
            return DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm").parseDateTime(text).toDate();
        } catch (IllegalArgumentException iae) {
            // likely too short
        }

        try {
            return DateTimeFormat.forPattern("yyyy-MM-dd").parseDateTime(text).toDate();
        } catch (IllegalArgumentException iae) {
            // likely too short
        }

        throw new IllegalArgumentException();
    }
}
