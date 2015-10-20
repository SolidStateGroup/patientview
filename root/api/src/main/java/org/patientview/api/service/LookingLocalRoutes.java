/*
 * PatientView
 *
 * Copyright (c) Worth Solutions Limited 2004-2013
 *
 * This file is part of PatientView.
 *
 * PatientView is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 * PatientView is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with PatientView in a file
 * titled COPYING. If not, see <http://www.gnu.org/licenses/>.
 *
 * @package PatientView
 * @link http://www.patientview.org
 * @author PatientView <info@patientview.org>
 * @copyright Copyright (c) 2004-2013, Worth Solutions Limited
 * @license http://www.gnu.org/licenses/gpl-3.0.html The GNU General Public License V3.0
 */

package org.patientview.api.service;

public final class LookingLocalRoutes {

    public static final String LOOKING_LOCAL_HOME = "/lookinglocal/home";
    public static final String LOOKING_LOCAL_AUTH = "/lookinglocal/auth";
    public static final String LOOKING_LOCAL_ERROR = "/lookinglocal/error";
    public static final String LOOKING_LOCAL_ERROR_REDIRECT = "error";
    public static final String LOOKING_LOCAL_MAIN = "/lookinglocal/secure/main";
    public static final String LOOKING_LOCAL_MAIN_REDIRECT = "secure/main";
    public static final String LOOKING_LOCAL_DETAILS = "/lookinglocal/secure/details";
    public static final String LOOKING_LOCAL_MY_DETAILS = "/lookinglocal/secure/myDetails";
    public static final String LOOKING_LOCAL_DRUGS = "/lookinglocal/secure/drugs";
    public static final String LOOKING_LOCAL_LETTERS = "/lookinglocal/secure/letters";
    public static final String LOOKING_LOCAL_RESULTS_DISPLAY = "/lookinglocal/secure/resultsDisplay";
    public static final String LOOKING_LOCAL_RESULT_DISPLAY = "/lookinglocal/secure/resultDisplay";
    public static final String LOOKING_LOCAL_LETTER_DISPLAY = "/lookinglocal/secure/letterDisplay";

    private LookingLocalRoutes() { }
}
