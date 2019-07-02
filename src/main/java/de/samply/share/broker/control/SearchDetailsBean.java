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
package de.samply.share.broker.control;

import de.samply.share.broker.messages.Messages;
import de.samply.share.broker.model.db.enums.InquiryDetailsType;
import de.samply.share.broker.model.db.tables.pojos.*;
import de.samply.share.broker.utils.db.*;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.servlet.http.Part;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * holds all details linked with an inquiry
 */
public class SearchDetailsBean implements Serializable {

    private static final long serialVersionUID = -4671575946697123638L;

    private String inquiryName;
    private String inquiryDescription;
    private List<String> selectedSites;
    private List<String> resultTypes;
    private Inquiry inquiry;
    private Document expose;
    private Document vote;
    private boolean edit = false;

    private String serializedQuery;

    @PostConstruct
    public void init() {
        selectedSites = new ArrayList<>();
        resultTypes = new ArrayList<>();
    }

    String getInquiryName() {
        return inquiryName;
    }

    String getInquiryDescription() {
        return inquiryDescription;
    }

    List<String> getSelectedSites() {
        return selectedSites;
    }

    List<String> getResultTypes() {
        return resultTypes;
    }

    public Inquiry getInquiry() {
        return inquiry;
    }

    public Document getExpose() {
        return expose;
    }

    Document getVote() {
        return vote;
    }

    boolean isEdit() {
        return edit;
    }

    String getSerializedQuery() {
        return serializedQuery;
    }

    void setSerializedQuery(String serializedQuery) {
        this.serializedQuery = serializedQuery;
    }

    public List<Site> getSites() {
        return SiteUtil.fetchSites();
    }

    /**
     * Clear all variables
     */
    void clearStuff() {
        inquiryDescription = "";
        inquiryName = "";
        selectedSites = new ArrayList<>();
        resultTypes = new ArrayList<>();
        inquiry = null;
        serializedQuery = "";
        expose = null;
        vote = null;
        edit = false;
    }
}