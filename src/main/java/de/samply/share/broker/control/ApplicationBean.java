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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;

import de.samply.common.mdrclient.MdrClient;
import de.samply.config.util.FileFinderUtil;
import de.samply.share.broker.model.db.tables.pojos.Action;
import de.samply.share.broker.utils.db.ActionUtil;
import de.samply.share.common.utils.ProjectInfo;

/**
 * A JSF Managed Bean that is existent during the whole lifetime of the application.
 */
@ManagedBean(name = "applicationBean")
@ApplicationScoped
public class ApplicationBean implements Serializable {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -2336762309482647737L;

    /** The mdr client. */
    private transient MdrClient mdrClient;

    /** The list of actions (recent and upcoming) */
    private List<Action> allActions;

    /** The list of actions (recent) */
    private List<Action> recentActions;

    /** The list of actions (upcoming) */
    private List<Action> upcomingActions;
    
    /** The list of URNs for dataelements that shall be hidden from the GUI */
    private List<String> mdrKeyBlacklist;

    public List<Action> getAllActions() {
        return allActions;
    }

    public void setAllActions(List<Action> allActions) {
        this.allActions = allActions;
    }

    public List<Action> getRecentActions() {
        return recentActions;
    }

    public void setRecentActions(List<Action> recentActions) {
        this.recentActions = recentActions;
    }

    public List<Action> getUpcomingActions() {
        return upcomingActions;
    }

    public void setUpcomingActions(List<Action> upcomingActions) {
        this.upcomingActions = upcomingActions;
    }

    /**
     * Get a list of mdr keys that shall be excluded from the searchable items
     * @return a list of keys that will be ignored for the search menu
     */
    public List<String> getMdrKeyBlacklist() {
    	if (mdrKeyBlacklist == null) {
    		try {
				loadMdrKeyBlacklist();
			} catch (IOException e) {
				mdrKeyBlacklist = new ArrayList<>();
			}
    	}
		return mdrKeyBlacklist;
	}

	public void setMdrKeyBlacklist(List<String> mdrKeyBlacklist) {
		this.mdrKeyBlacklist = mdrKeyBlacklist;
	}

	/**
     * Instantiate a new MdrClient on application startup.
     */
    @PostConstruct
    public void init() {
        mdrClient = new MdrClient();
        reloadRecentActions();
        reloadUpcomingActions();
        reloadAllActions();
    }

    /**
     * Gets the mdr client.
     *
     * @return the application wide mdr client
     */
    public MdrClient getMdrClient() {
        return mdrClient;
    }
    
    /**
     * Reload the list of all actions from the database
     */
    public void reloadAllActions() {
        allActions = ActionUtil.getAllActions();
    }
    
    /**
     * Reload the list of upcoming actions from the database
     */
    public void reloadUpcomingActions() {
        upcomingActions = ActionUtil.getUpcomingActions();
    }
    
    /**
     * Reload the list of recent actions from the database
     */
    private void reloadRecentActions() {
        recentActions = ActionUtil.getRecentActions();
    }
    
    /**
     * If there is a mdrkey blacklist file in the config folder, read it into a list of mdr keys to hide
     */
    private void loadMdrKeyBlacklist() throws IOException {
    	final String filename = "mdrkeys_blacklist.txt";
    	BufferedReader in = new BufferedReader(new FileReader(FileFinderUtil.findFile(filename, ProjectInfo.INSTANCE.getProjectName())));
        String str;

        List<String> list = new ArrayList<>();
        while((str = in.readLine()) != null) {
            list.add(str);
        }
        in.close();
        setMdrKeyBlacklist(list);
    }

}
