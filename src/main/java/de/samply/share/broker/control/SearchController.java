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

import de.samply.share.broker.model.db.tables.pojos.BankSite;
import de.samply.share.broker.model.db.tables.pojos.Reply;
import de.samply.share.broker.model.db.tables.pojos.Site;
import de.samply.share.broker.model.db.tables.pojos.User;
import de.samply.share.broker.rest.InquiryHandler;
import de.samply.share.broker.statistics.NTokenHandler;
import de.samply.share.broker.utils.db.BankSiteUtil;
import de.samply.share.broker.utils.db.ReplyUtil;
import de.samply.share.broker.utils.db.SiteUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jooq.tools.json.JSONArray;
import org.jooq.tools.json.JSONObject;
import org.jooq.tools.json.JSONParser;
import org.jooq.tools.json.ParseException;

import java.util.ArrayList;
import java.util.List;

/**
 * holds methods and information necessary to create and display queries
 */
public class SearchController {


    private static NTokenHandler N_TOKEN_HANDLER = new NTokenHandler();

    /**
     * release query from UI for bridgeheads
     *
     * @param simpleQueryDtoJson the query
     * @param loggedUser         the logged User
     */
    public static void releaseQuery(String simpleQueryDtoJson, String ntoken, User loggedUser) {
        N_TOKEN_HANDLER.deactivateNToken(ntoken);

        InquiryHandler inquiryHandler = new InquiryHandler();
        int inquiryId = inquiryHandler.storeAndRelease(simpleQueryDtoJson, loggedUser.getId(), "", "", -1, -1, new ArrayList<>(), true);
        if (inquiryId > 0 && !StringUtils.isBlank(ntoken)) {
            N_TOKEN_HANDLER.saveNToken(inquiryId, ntoken, simpleQueryDtoJson);
        }

        List<String> siteIds = new ArrayList<>();
        for (Site site : SiteUtil.fetchSites()) {
            siteIds.add(site.getId().toString());
        }
        inquiryHandler.setSitesForInquiry(inquiryId, siteIds);
    }

    /**
     * get replys from the bridgeheads of the query
     *
     * @param id the id of the query
     * @return all results as JSONObject
     */
    @SuppressWarnings("unchecked")
    public static JSONObject getReplysFromQuery(int id, boolean anonymous) {
        JSONObject replyAllSites = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        replyAllSites.put("replySites", jsonArray);

        List<Reply> replyList = ReplyUtil.getReplyforInquriy(id);
        if (CollectionUtils.isEmpty(replyList)) {
            return replyAllSites;
        }

        JSONParser parser = new JSONParser();
        for (Reply reply : replyList) {
            if (isActiveSite(reply)) {
                try {
                    JSONObject json = (JSONObject) parser.parse(reply.getContent());
                    if (anonymous) {
                        json.put("site", "anonymous");
                    }
                    jsonArray.add(json);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }

        return replyAllSites;
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
