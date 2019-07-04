/*
 * Copyright (C) 2015 Working Group on Joint Research,
 * Division of Medical Informatics,
 * Institute of Medical Biometrics, Epidemiology and Informatics,
 * University Medical Center of the Johannes Gutenberg University Mainz
 *
 * Contact: info@osse-register.de
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 * Additional permission under GNU GPL version 3 section 7:
 *
 * If you modify this Program, or any covered work, by linking or combining it
 * with Jersey (https://jersey.java.net) (or a modified version of that
 * library), containing parts covered by the terms of the General Public
 * License, version 2.0, the licensors of this Program grant you additional
 * permission to convey the resulting work.
 */
package de.samply.share.broker.utils;

import de.samply.common.mdrclient.MdrClient;
import de.samply.common.mdrclient.MdrConnectionException;
import de.samply.common.mdrclient.MdrInvalidResponseException;
import de.samply.common.mdrclient.domain.Meaning;
import de.samply.common.mdrclient.domain.PermissibleValue;
import de.samply.common.mdrclient.domain.Validations;
import de.samply.share.common.utils.ProjectInfo;
import de.samply.web.mdrFaces.MdrContext;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Utilities that are made available via the webutils taglib
 */
public final class WebUtils {

    private static final String VALIDATION_DATATYPE_ENUMERATED = "enumerated";

    /**
     * Prevent instantiation
     */
    private WebUtils() {
    }

    /**
     * Gets the designation of a dataelement
     *
     * @param dataElement the data element mdr id
     * @param languageCode the language code
     * @return the designation of the dataelement
     */
    public static String getDesignation(String dataElement, String languageCode) {

        MdrClient mdrClient = MdrContext.getMdrContext().getMdrClient();

        try {
            return mdrClient.getDataElementDefinition(dataElement, languageCode).getDesignations().get(0).getDesignation();
        } catch (MdrConnectionException | MdrInvalidResponseException | ExecutionException e) {
            e.printStackTrace();
            return ("??" + dataElement + "??");
        }

    }

    /**
     * Get the designation of a value of a dataelement
     *
     * @param dataElement the data element mdr id
     * @param value the permitted value for which the designation shall be got
     * @param languageCode the language code
     * @return the designation of the value
     */
    public static String getValueDesignation(String dataElement, String value, String languageCode) {
        MdrClient mdrClient = MdrContext.getMdrContext().getMdrClient();

        try {
            Validations validations = mdrClient.getDataElementValidations(dataElement, languageCode);
            String dataType = validations.getDatatype();
            if (dataType.equalsIgnoreCase(VALIDATION_DATATYPE_ENUMERATED)) {
                List<PermissibleValue> permissibleValues = validations.getPermissibleValues();
                for (PermissibleValue pv : permissibleValues) {
                    List<Meaning> meanings = pv.getMeanings();
                    if (pv.getValue().equals(value)) {
                        for (Meaning m : meanings) {
                            if (m.getLanguage().equalsIgnoreCase(languageCode)) {
                                return m.getDesignation();
                            }
                        }
                    }
                }
            }
        } catch (MdrConnectionException | MdrInvalidResponseException | ExecutionException e) {
            e.printStackTrace();
        }
        return value;
    }

    /**
     * Convert a date to German ISO format
     *
     * @param isoDate the date to format
     * @return dd.MM.yyyy representation of the given date as a string
     */
    static String isoToGermanDateString(Date isoDate) {
        if (isoDate == null)
            return "";
        return new SimpleDateFormat("dd.MM.yyyy").format(isoDate);
    }

    /**
     * Gets the operator.
     *
     * @param operator the operator
     * @return the operator
     */
    public static String getOperator(String operator) {
        String ret = operator;

        if (operator.equalsIgnoreCase("eq")) {
            ret = " == ";
        } else if (operator.equalsIgnoreCase("neq")) {
            ret = " != ";
        } else if (operator.equalsIgnoreCase("leq")) {
            ret = " ≤ ";
        } else if (operator.equalsIgnoreCase("geq")) {
            ret = " ≥ ";
        } else if (operator.equalsIgnoreCase("gt")) {
            ret = " > ";
        } else if (operator.equalsIgnoreCase("lt")) {
            ret = " < ";
        } else if (operator.equalsIgnoreCase("isnull")) {
            ret = " ist null";
        } else if (operator.equalsIgnoreCase("isnotnull")) {
            ret = " ist nicht null";
        } else if (operator.equalsIgnoreCase("like")) {
            ret = " ~ ";
        }

        return ret;
    }

    /**
     * Gets the project name.
     *
     * @return the project name
     */
    public static String getProjectName() {
        return ProjectInfo.INSTANCE.getProjectName();
    }
}
