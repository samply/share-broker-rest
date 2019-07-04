/*
 * Copyright (C) 2015 Working Group on Joint Research, University Medical Center Mainz
 * Contact: info@osse-register.de
 * <p>
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses>.
 * <p>
 * Additional permission under GNU GPL version 3 section 7:
 * <p>
 * If you modify this Program, or any covered work, by linking or combining it
 * with Jersey (https://jersey.java.net) (or a modified version of that
 * library), containing parts covered by the terms of the General Public
 * License, version 2.0, the licensors of this Program grant you additional
 * permission to convey the resulting work.
 */
package de.samply.share.broker.control;

import com.google.common.base.Joiner;
import de.samply.share.broker.messages.Messages;
import de.samply.share.broker.model.db.enums.InquiryStatus;
import de.samply.share.broker.model.db.tables.pojos.*;
import de.samply.share.broker.rest.InquiryHandler;
import de.samply.share.broker.utils.MailUtils;
import de.samply.share.broker.utils.SimpleQueryDto2ShareXmlTransformer;
import de.samply.share.broker.utils.Utils;
import de.samply.share.broker.utils.db.*;
import de.samply.share.common.control.uiquerybuilder.AbstractSearchController;
import de.samply.share.common.utils.ProjectInfo;
import de.samply.share.common.utils.QueryTreeUtil;
import de.samply.share.common.utils.SamplyShareUtils;
import de.samply.share.model.common.Query;
import de.samply.share.query.entity.SimpleQueryDto;
import de.samply.share.utils.QueryConverter;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.jooq.tools.json.JSONObject;
import org.jooq.tools.json.JSONParser;
import org.jooq.tools.json.ParseException;

import javax.faces.application.FacesMessage;
import javax.inject.Inject;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * holds methods and information necessary to create and display queries
 */
public class SearchController {

    /**
     * release query from UI for bridgeheads
     *
     * @param simpleQueryDtoXml the query
     * @param loggedUser        the logged User
     * @return the query ID
     * @throws JAXBException
     */

    public static int releaseQuery(String simpleQueryDtoXml, User loggedUser) throws JAXBException {
        InquiryHandler inquiryHandler = new InquiryHandler();
        int inquiryId = inquiryHandler.storeAndRelease(simpleQueryDtoXml, loggedUser.getId(), "", "", -1, -1, new ArrayList<>(), true);
        List<String> siteIds = new ArrayList<>();
        for (Site site : SiteUtil.fetchSites()) {
            siteIds.add(site.getId().toString());
        }
        inquiryHandler.setSitesForInquiry(inquiryId, siteIds);
        return inquiryId;
    }

    /**
     * get replys from the bridgeheads of the query
     *
     * @param id the id of the query
     * @return all results as JSON String
     */
    public static String getReplysFromQuery(int id) {
        List<Reply> replyList = ReplyUtil.getReplyforInquriy(id);
        if (replyList == null) {
            return new JSONArray().toString();
        }

        JSONArray jsonArray = new JSONArray();
        JSONParser parser = new JSONParser();
        for (Reply reply : replyList) {
            if (isActiveSite(reply)) {
                try {
                    JSONObject json = (JSONObject) parser.parse(reply.getContent());
                    jsonArray.put(json);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }
        return jsonArray.toString();
    }

    private static boolean isActiveSite(Reply reply) {
        BankSite bankSite = BankSiteUtil.fetchBankSiteByBankId(reply.getBankId());
        if (bankSite == null) {
            return false;
        }


        Site site = SiteUtil.fetchSiteById(bankSite.getSiteId());
        if (site == null) {
            return false;
        }

        return site.getActive();
    }
}
