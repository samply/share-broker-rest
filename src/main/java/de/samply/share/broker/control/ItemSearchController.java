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

import com.google.common.base.Optional;
import com.google.common.collect.Iterables;
import de.samply.common.mdrclient.MdrConnectionException;
import de.samply.common.mdrclient.MdrInvalidResponseException;
import de.samply.common.mdrclient.domain.*;
import de.samply.config.util.FileFinderUtil;
import de.samply.jsf.MdrUtils;
import de.samply.share.broker.utils.Config;
import de.samply.share.broker.utils.Utils;
import de.samply.share.common.control.uiquerybuilder.AbstractItemSearchController;
import de.samply.share.common.model.uiquerybuilder.MenuItem;
import de.samply.share.common.model.uiquerybuilder.MenuItemTreeManager;
import de.samply.share.common.utils.MdrIdDatatype;
import de.samply.share.common.utils.ProjectInfo;
import de.samply.share.common.utils.SamplyShareUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.omnifaces.util.Ajax;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * The MDR item search panel controller.
 */
@ManagedBean(name = "ItemSearchController")
@ViewScoped
public class ItemSearchController extends AbstractItemSearchController {

    private static final long serialVersionUID = -8930281571767592989L;

    private static final Logger logger
            = LogManager.getLogger(ItemSearchController.class);
    private static final String URN_ADT_DATAELEMENTGROUP = "urn:adt:dataelementgroup:17:4";

    /**
     * The DKTK entity catalogue
     */
    private final String DKTK_ENTITY_CATALOGUE = "urn:dktk:catalog:1:latest";

    /**
     * The namespace of the OSSE common data set.
     */
    private final String OSSE_CDS_NAMESPACE = "osse-cds";

    /**
     * The namespace of the OSSE ror.
     */
    private final String OSSE_ROR_NAMESPACE = "osse-ror";

    /**
     * The prefix of osse registry namespaces
     */
    private final String OSSE_NAMESPACE_PREFIX = "osse-";

    /**
     * The namespace of GBA
     */
    private final String GBA_NAMESPACE = "mdr16";

    /**
     * The namespace of DKTK.
     */
    private final String DKTK_NAMESPACE = "dktk";

    private static boolean SHOW_ADT = false;

    /**
     * A list of urns, identifying the groups that shall be included in the mdr elements. Used for DKTK
     */
    private List<String> includeGroups;
    private static final String CFG_INCLUDE_GROUPS = "mdr_include_groups";

    private static final String CFG_SHOW_ADT = "show_adt";
    private final String LANGUAGE_FILE = "dktk_bridgehead_info.xml";

    /**
     * Only show a select number of elements from the adt
     */
    private List<String> shownAdtElements;
    private static final String CFG_SHOWN_ADT_ELEMENTS = "shown_adt_elements";

    private ResultList adtElements;

    public ItemSearchController() {
        System.out.println(Utils.getLocaleController().getLanguage());
        includeGroups = new ArrayList<>();
        shownAdtElements = new ArrayList<>();
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse((FileFinderUtil.findFile(LANGUAGE_FILE, ProjectInfo.INSTANCE.getProjectName())));
            NodeList nodeList = document.getElementsByTagName("dkt:language");
            Node node = nodeList.item(0);
            node.getTextContent();
        } catch (IOException | ParserConfigurationException | SAXException e) {
            e.printStackTrace();
        }

        if (ProjectInfo.INSTANCE.getProjectName().equalsIgnoreCase("dktk")) {
            try {
                SHOW_ADT = Boolean.parseBoolean(ProjectInfo.INSTANCE.getConfig().getProperty(CFG_SHOW_ADT));
            } catch (Exception e) {
                // Leave as false on any kind of exception
            }
            if (SHOW_ADT) {
                readShownAdtElements();
                adtElements = getMdrElements(shownAdtElements);
                setAdditionalSearchItems(adtElements.getResults());
            } else {
                adtElements = new ResultList();
            }
            addToSearchNamespaces(DKTK_NAMESPACE);

            readIncludeGroups();
        }

    }

    /**
     * Fill the list of groups to be included from the config file
     */
    private void readShownAdtElements() {
        try {
            String adtElementList = ProjectInfo.INSTANCE.getConfig().getProperty(CFG_SHOWN_ADT_ELEMENTS);
            String[] adtElementArray = adtElementList.split(";");
            for (String adtElement : adtElementArray) {
                MdrIdDatatype adtMdrElement = new MdrIdDatatype(adtElement);
                shownAdtElements.add(adtMdrElement.getLatestMdr());
            }
        } catch (Exception e) {
            logger.error("Caught Exception", e);
        }
    }

    /**
     * Fill the list of groups to be included from the config file
     */
    private void readIncludeGroups() {
        try {
            String groupList = ProjectInfo.INSTANCE.getConfig().getProperty(CFG_INCLUDE_GROUPS);
            String[] groupArray = groupList.split(";");
            for (String group : groupArray) {
                includeGroups.add(ProjectInfo.INSTANCE.getConfig().getProperty(group));
            }
            logger.debug("Got " + includeGroups.size() + " groups to include");
        } catch (Exception e) {
            logger.error("Caught Exception", e);
        }
    }

    /**
     * Gets the the private key to login at the MDR with Auth.
     *
     * @return the private key as string
     */
    @Override
    public String getPrivateKey() {
        String path = SamplyShareUtils.addTrailingFileSeparator(ProjectInfo.INSTANCE.getConfig().getConfigPath()) + "key.der.txt";
        byte[] encoded;
        try {
            encoded = Files.readAllBytes(Paths.get(path));
            return new String(encoded, StandardCharsets.US_ASCII);
        } catch (IOException ex) {
            throw new RuntimeException("Private key string could not be found in " + path, ex);
        }
    }

    /**
     * @return Auth Id from config file
     */
    @Override
    public String getMdrAuthKeyId() {
        return ProjectInfo.INSTANCE.getConfig().getProperty("mdr.auth.keyId");
    }

    /**
     * @return the URL of the Auth server that is used by the MDR
     */
    @Override
    public String getMdrAuthUrl() {
        return ProjectInfo.INSTANCE.getConfig().getProperty("mdr.auth.url");
    }


    /**
     * Loads the items from the MDR and populates the menu items.
     * For OSSE, load all root elements of the {@link #OSSE_CDS_NAMESPACE}.
     */
    @Override
    public void resetMenuItems() {
        menuItems.clear();
        String languageCode = "de";

        try {
            languageCode = Utils.getLocaleController().getLanguage();
        } catch (Exception e) {
            logger.warn("Couldn't get locale..." + e);
        }

        List<Result> results = new ArrayList<>();

        if (ProjectInfo.INSTANCE.getProjectName().equalsIgnoreCase("osse")) {
            results = getMdrRootElements(OSSE_CDS_NAMESPACE).getResults();
            // add ALL osse namespaces
            try {
                List<Namespace> namespaces = mdrClient.getNamespaces(languageCode);
                for (Namespace namespace : namespaces) {
                    String namespaceName = namespace.getName();
                    if (namespaceName.startsWith(OSSE_NAMESPACE_PREFIX) && !namespaceName.equalsIgnoreCase(OSSE_CDS_NAMESPACE) && !namespaceName.equalsIgnoreCase(OSSE_ROR_NAMESPACE)) {
                        results.addAll(getMdrRootElements(namespaceName).getResults());
                    }
                }
            } catch (ExecutionException e) {
                logger.warn("Could not get list of namespaces from mdr. Using only default");
            }


        } else if (ProjectInfo.INSTANCE.getProjectName().equalsIgnoreCase("dktk")) {
            results.addAll(getNamespaceMembersAnonymous("dktk", "de").getResults());
            try {
                Catalogue catalogue = mdrClient.getCatalogue(DKTK_ENTITY_CATALOGUE, languageCode);
                MenuItem menuItem = MenuItemTreeManager.buildMenuItem(catalogue.getRoot().getIdentification().getUrn(),
                        EnumElementType.CATALOGUEGROUP,
                        MdrUtils.getDesignation(catalogue.getRoot().getDesignations()),
                        MdrUtils.getDefinition(catalogue.getRoot().getDesignations()),
                        new ArrayList<>(), null);
                menuItems.add(menuItem);
            } catch (ExecutionException | MdrInvalidResponseException | MdrConnectionException ex) {
                throw new RuntimeException("Error loading MDR root elements", ex);
            }
        } else {
            results.addAll(getNamespaceMembersAnonymous("samply", "de").getResults());
            try {
                List<Namespace> namespaces = mdrClient.getNamespaces(languageCode);
                for (Namespace namespace : namespaces) {
                    String namespaceName = namespace.getName();
                    if (namespaceName.startsWith(GBA_NAMESPACE)) {
                        results.addAll(getMdrRootElements(namespaceName).getResults());
                    }
                }
            } catch (ExecutionException e) {
                logger.warn("Could not get list of namespaces from mdr. Using only default");
            }
        }

        for (Result r : results) {
            MenuItem menuItem = MenuItemTreeManager.buildMenuItem(r.getId(),
                    EnumElementType.valueOf(r.getType()),
                    MdrUtils.getDesignation(r.getDesignations()),
                    MdrUtils.getDefinition(r.getDesignations()),
                    new ArrayList<>(), null);
            menuItems.add(menuItem);
        }

        if (SHOW_ADT) {
            // CCP-340 demands to add select ADT elements without any further hierarchy information
            // So it is necessary to create that manually
            MenuItem adtRootItem = createAdtRootItem();
            menuItems.add(adtRootItem);

            for (Result adtElement : adtElements.getResults()) {
                MenuItem menuItem = MenuItemTreeManager.buildMenuItem(adtElement.getId(),
                        EnumElementType.valueOf(adtElement.getType()),
                        MdrUtils.getDesignation(adtElement.getDesignations()),
                        MdrUtils.getDefinition(adtElement.getDesignations()),
                        new ArrayList<>(), null);
                MenuItemTreeManager.addMenuItem(menuItem, adtRootItem);
            }
        }

    }

    /**
     * Create an anchor for the selected ADT elements
     *
     * @return menu item for the adt dataset
     */
    private MenuItem createAdtRootItem() {
        return MenuItemTreeManager.buildMenuItem(URN_ADT_DATAELEMENTGROUP,
                EnumElementType.valueOf(EnumElementType.DATAELEMENTGROUP.name()),
                "ADT (Auswahl)",
                "ADT (Auswahl)",
                new ArrayList<>(), null);
    }

    @Override
    public void onDataElementGroupClick(final String mdrId) {
        logger.debug("Loading menu item children...");

        MenuItem parent = MenuItemTreeManager.getMenuItem(menuItems, mdrId);

        if (MenuItemTreeManager.isItemOpen(parent)) { // just let javascript close the drawer
            MenuItemTreeManager.cleanMenuItemStyleClass(parent);
        } else if (mdrId.equalsIgnoreCase(URN_ADT_DATAELEMENTGROUP)) {
            // The ADT "Group" only contains the selected elements from config file. So do not reload from the mdr
            // Just expand the pre-filled category
            MenuItemTreeManager.cleanMenuItemsStyleClass(menuItems);
            MenuItemTreeManager.setItemAndParentsOpen(parent);
            Ajax.update(getItemNavigationPanel().getClientId());
        } else {
            MenuItemTreeManager.cleanMenuItemsStyleClass(menuItems);
            MenuItemTreeManager.clearMenuItemChildren(parent);

            logger.trace("Menu " + parent.getDesignation() + ", " + parent.getMdrId() + " clicked.");

            MenuItemTreeManager.setItemAndParentsOpen(parent);
            Ajax.update(getItemNavigationPanel().getClientId());
        }
    }
}
