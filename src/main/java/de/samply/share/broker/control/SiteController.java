package de.samply.share.broker.control;

import com.mchange.util.AlreadyExistsException;
import de.samply.share.broker.feature.ClientFeature;
import de.samply.share.broker.model.db.tables.pojos.Site;
import de.samply.share.broker.utils.db.BankSiteUtil;
import de.samply.share.broker.utils.db.SiteUtil;
import java.util.List;
import javax.ws.rs.NotAllowedException;
import javax.ws.rs.core.Response;

public class SiteController {

  /**
   * Set a site for a bank.
   *
   * @param siteName the sitename for the bank
   * @param bankId   the id of the bank
   */
  public static void setSiteForBank(String siteName, int bankId) throws AlreadyExistsException,
      NotAllowedException {
    if (!ApplicationBean.getFeatureManager().getFeatureState(ClientFeature.CREATE_NEW_SITE)
        .isEnabled()) {
      Site site = SiteUtil.fetchSiteByName(siteName);
      if (site != null) {
        BankSiteUtil.setSiteIdForBankId(bankId, site.getId(), false);
      } else {
        throw new NotAllowedException("Not allowed to add a new site",
            Response.status(405).build());
      }
    } else {
      Site site = SiteUtil.fetchSiteByName(siteName);
      Integer siteId;
      if (site != null) {
        siteId = site.getId();
        if (BankSiteUtil.fetchBankSiteBySiteId(siteId) != null) {
          throw new AlreadyExistsException("A bank with the same site exist");
        }
      } else {
        Site newSite = new Site();
        newSite.setName(siteName);
        newSite.setActive(true);
        SiteUtil.insertNewSite(newSite);
        siteId = SiteUtil.fetchSiteByName(siteName).getId();
      }
      BankSiteUtil.setSiteIdForBankId(bankId, siteId, true);
    }
  }

  public static List<Site> getAllSites() {
    return SiteUtil.fetchSites();
  }

}
