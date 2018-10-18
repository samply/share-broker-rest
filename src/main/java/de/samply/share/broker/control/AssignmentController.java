package de.samply.share.broker.control;

import de.samply.share.broker.model.db.tables.pojos.*;
import de.samply.share.broker.utils.Utils;
import de.samply.share.broker.utils.db.*;
import de.samply.share.common.model.dto.SiteInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.PostConstruct;
import javax.faces.component.UIInput;
import javax.faces.event.ValueChangeEvent;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *  used for the admin view and holds methods to assign users and banks to a site.
 */
public class AssignmentController implements Serializable {

    private static final Logger logger = LogManager.getLogger(AssignmentController.class);

    private List<User> users;
    private List<Bank> banks;
    private List<Site> sites;
    
    private List<UserSite> userSites;
    private List<BankSite> bankSites;

    /**
     * Clear all lists and grab the necessary entities from the database
     */
    @PostConstruct
    public void init() {
        initAllListsEmpty();
        users = UserUtil.fetchUsers();
        banks = BankUtil.fetchBanks();
        sites = SiteUtil.fetchSites();
        
        userSites = UserSiteUtil.fetchUserSites();
        bankSites = BankSiteUtil.fetchBankSites();
    }

    /**
     * Clear all lists
     */
    private void initAllListsEmpty() {
        users = new ArrayList<>();
        banks = new ArrayList<>();
        sites = new ArrayList<>();
        
        userSites = new ArrayList<>();
        bankSites = new ArrayList<>();
    }

    /**
     * Change the assignment of a user to a site
     *
     * @param vcEvent the JSF Value Change Event that is associated with this listener
     */
    public void changeUserSiteAssignment(ValueChangeEvent vcEvent) {
        User user = (User) ((UIInput) vcEvent.getSource()).getAttributes().get("user");
        Boolean approved = (Boolean) vcEvent.getNewValue();
        logger.debug("setting assignment for user " + user.getId() + " to " + approved);
        
        UserSite userSite = UserSiteUtil.fetchUserSiteByUser(user);
        if (userSite != null) {
            userSite.setApproved(approved);
            UserSiteUtil.updateUserSite(userSite);
        }
    }

    /**
     * Change the assignment of a bank to a site
     *
     * @param vcEvent the JSF Value Change Event that is associated with this listener
     */
    public void changeBankSiteAssignment(ValueChangeEvent vcEvent) {
        Bank bank = (Bank) ((UIInput) vcEvent.getSource()).getAttributes().get("bank");
        Boolean approved = (Boolean) vcEvent.getNewValue();
        logger.debug("setting assignment for bank " + bank.getId() + " to " + approved);
        
        BankSite bankSite = BankSiteUtil.fetchBankSiteByBank(bank);
        if (bankSite != null) {
            bankSite.setApproved(approved);
            BankSiteUtil.updateBankSite(bankSite);
        }
    }

    /**
     * Set the approved flag for a user to site assignment
     *
     * @param vcEvent the JSF Value Change Event that is associated with this listener
     */
    public void changeUserSites(ValueChangeEvent vcEvent) {
        User user = (User) ((UIInput) vcEvent.getSource()).getAttributes().get("user");
        Integer newSiteId = (Integer)vcEvent.getNewValue();
        if (newSiteId != null && newSiteId > 0) {
            logger.info("New User-Site assignment: userid: " + user.getId() + " (" + user.getAuthid() + ") now assigned to siteid: " + newSiteId + " (was " + vcEvent.getOldValue() + ")");
            UserUtil.setSiteIdForUser(user, newSiteId, Boolean.TRUE);
        } else {
            logger.debug("new site is null or 0. Remove assignment if any is found.");
            UserSiteUtil.deleteSiteFromUser(user);
        }
    }

    /**
     * Set the approved flag for a bank to site assignment
     *
     * @param vcEvent the JSF Value Change Event that is associated with this listener
     */
    public void changeBankSites(ValueChangeEvent vcEvent) {
        Bank bank = (Bank) ((UIInput) vcEvent.getSource()).getAttributes().get("bank");
        Integer newSiteId = (Integer)vcEvent.getNewValue();
        if (newSiteId != null && newSiteId > 0) {
            logger.info("New Bank-Site assignment: bankid: " + bank.getId() + " (" + bank.getEmail() + ") now assigned to siteid: " + newSiteId + " (was " + vcEvent.getOldValue() + ")");
            BankSiteUtil.setSiteIdForBank(bank, newSiteId, Boolean.TRUE);
        } else {
            logger.debug("new site is null or 0. Remove assignment if any is found.");
            BankSiteUtil.deleteSiteFromBank(bank);
        }
    }

    /**
     * Get the site that is associated with a given user
     *
     * @param user the user for which the site is wanted
     * @return the site associated with the user or null if no association present
     */
    public SiteInfo getSiteInfoForUser(User user) {
        try {
            Site site = UserUtil.getSiteForUser(user);
            UserSite userSite = UserSiteUtil.fetchUserSiteByUser(user);
            
            SiteInfo siteInfo = Utils.siteToSiteInfo(site);
            siteInfo.setApproved(userSite != null ? userSite.getApproved() : null);
            return siteInfo;
        } catch (NullPointerException npe) {
            return new SiteInfo();
        }
    }

    /**
     * Get the site that is associated with a given bank
     *
     * @param bank the bank for which the site is wanted
     * @return the site associated with the bank or null if no association present
     */
    public SiteInfo getSiteInfoForBank(Bank bank) {
        try {
            Site site = BankUtil.getSiteForBank(bank);
            BankSite bankSite = BankSiteUtil.fetchBankSiteByBank(bank);
            
            SiteInfo siteInfo = Utils.siteToSiteInfo(site);
            siteInfo.setApproved(bankSite != null ? bankSite.getApproved() : null);
            return siteInfo;
        } catch (NullPointerException npe) {
            return new SiteInfo();
        }
    }

    /**
     * @return the users
     */
    public List<User> getUsers() {
        return users;
    }

    /**
     * @param users the users to set
     */
    public void setUsers(List<User> users) {
        this.users = users;
    }

    /**
     * @return the banks
     */
    public List<Bank> getBanks() {
        return banks;
    }

    /**
     * @param banks the banks to set
     */
    public void setBanks(List<Bank> banks) {
        this.banks = banks;
    }

    /**
     * @return the sites
     */
    public List<Site> getSites() {
        return sites;
    }

    /**
     * @param sites the sites to set
     */
    public void setSites(List<Site> sites) {
        this.sites = sites;
    }

    /**
     * @return the userSites
     */
    public List<UserSite> getUserSites() {
        return userSites;
    }

    /**
     * @param userSites the userSites to set
     */
    public void setUserSites(List<UserSite> userSites) {
        this.userSites = userSites;
    }

    /**
     * @return the bankSites
     */
    public List<BankSite> getBankSites() {
        return bankSites;
    }

    /**
     * @param bankSites the bankSites to set
     */
    public void setBankSites(List<BankSite> bankSites) {
        this.bankSites = bankSites;
    }
}
