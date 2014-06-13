package org.patientview.config.utils;

import org.apache.commons.lang.RandomStringUtils;

/**
 * Created by james@solidstategroup.com
 * Created on 13/06/2014
 */
public final class CommonUtils {

    private CommonUtils() {
    }

    public static String getAuthtoken() {
        return RandomStringUtils.randomAlphanumeric(12);
    }

}
