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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

import javax.faces.application.ProjectStage;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import javax.servlet.http.Part;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;

import com.google.gson.Gson;
import de.samply.share.broker.utils.connector.IcingaConnector;
import de.samply.share.broker.utils.connector.IcingaConnectorException;
import de.samply.share.broker.utils.connector.SiteReportItem;
import de.samply.share.common.model.dto.monitoring.StatusReportItem;
import de.samply.share.common.utils.AbstractConfig;
import de.samply.share.common.utils.Constants;
import de.samply.share.common.utils.SamplyShareUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.jooq.DSLContext;
import org.jooq.Record;

import de.samply.auth.client.jwt.JWTAccessToken;
import de.samply.auth.rest.AccessTokenDTO;
import de.samply.auth.rest.LocationDTO;
import de.samply.auth.rest.LocationListDTO;
import de.samply.auth.rest.UserDTO;
import de.samply.share.broker.control.LocaleController;
import de.samply.share.broker.control.LoginController;
import de.samply.share.broker.control.SearchDetailsBean;
import de.samply.share.broker.jdbc.ResourceManager;
import de.samply.share.broker.model.db.Tables;
import de.samply.share.broker.model.db.tables.pojos.Site;
import de.samply.share.broker.utils.db.BankUtil;
import de.samply.share.common.model.dto.SiteInfo;
import de.samply.share.common.model.dto.UserAgent;
import de.samply.share.common.utils.ProjectInfo;
import de.samply.share.common.utils.oauth2.OAuthConfig;
import de.samply.share.common.utils.oauth2.OAuthUtils;

import static de.samply.share.common.model.dto.monitoring.StatusReportItem.*;
import static org.omnifaces.util.Faces.getServletContext;

/**
 * A collection of utility methods.
 */
public class Utils {

    private static final Logger logger = LogManager.getLogger(Utils.class);

    public static final String PROXY_REALM = "proxy.realm";

    public static final String PROXY_HTTPS_PASSWORD = "proxy.https.password";

    public static final String PROXY_HTTPS_USERNAME = "proxy.https.username";

    public static final String PROXY_HTTP_PASSWORD = "proxy.http.password";

    public static final String PROXY_HTTP_USERNAME = "proxy.http.username";

    public static final String PROXY_HTTPS_PORT = "proxy.https.port";

    public static final String PROXY_HTTPS_HOST = "proxy.https.host";

    public static final String PROXY_HTTP_PORT = "proxy.http.port";

    public static final String PROXY_HTTP_HOST = "proxy.http.host";

    public static final String USER_AGENT = "http.useragent";

    public static final String XML_NAMESPACE_BASEURL = "http://schema.samply.de/";

    /**
     * Get the real path for a relative path
     *
     * @param relativeWebPath the relative path
     * @return the real path
     */
    public static String getRealPath(String relativeWebPath) {
        ServletContext sc = ProjectInfo.INSTANCE.getServletContext();
        return sc.getRealPath(relativeWebPath);
    }

    /**
     * Save to file.
     *
     * @param uploadedInputStream the uploaded input stream
     * @param uploadedFileLocation the uploaded file location
     */
    public static void saveToFile(InputStream uploadedInputStream, String uploadedFileLocation) {

        try {
            OutputStream out;
            int read;
            byte[] bytes = new byte[1024];

            out = new FileOutputStream(new File(uploadedFileLocation));
            while ((read = uploadedInputStream.read(bytes)) != -1) {
                out.write(bytes, 0, read);
            }
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Gets the bank id from an authorization header
     *
     * @param authKeyHeader the authorization header containing the api key
     * @return the bank id
     */
    public static int getBankId(String authKeyHeader) {
        int bankId = -1;
        String authKey;

        if (authKeyHeader == null || !authKeyHeader.startsWith("Samply ")) {
            return bankId;
        }
        try {
            authKey = authKeyHeader.substring(7);
        } catch (Exception e) {
            return bankId;
        }

        try (Connection connection = ResourceManager.getConnection()) {
            DSLContext create = ResourceManager.getDSLContext(connection);

            Record r = create.select().from(Tables.BANK).join(Tables.AUTHTOKEN).on(Tables.BANK.AUTHTOKEN_ID.equal(Tables.AUTHTOKEN.ID))
                    .where(Tables.AUTHTOKEN.VALUE.equal(authKey)).fetchAny();

            if (r == null) {
                return bankId;
            }

            bankId = r.getValue(Tables.BANK.ID);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return bankId;
    }

    /**
     * Gets the bank id from an authorization header, if it fits the email address
     *
     * @param authKeyHeader the authorization header
     * @param bankEmail the bank email
     * @return the bank id if it matches the mail address, -1 if not
     */
    public static int getBankId(String authKeyHeader, String bankEmail) {
        int bankId = -1;
        String authKey;

        if (authKeyHeader == null || !authKeyHeader.startsWith("Samply ")) {
            return bankId;
        }
        try {
            authKey = authKeyHeader.substring(7);
        } catch (Exception e) {
            return bankId;
        }

        try (Connection connection = ResourceManager.getConnection()) {
            DSLContext create = ResourceManager.getDSLContext(connection);

            Record r = create.select().from(Tables.BANK).join(Tables.AUTHTOKEN).on(Tables.BANK.AUTHTOKEN_ID.equal(Tables.AUTHTOKEN.ID))
                    .where(Tables.AUTHTOKEN.VALUE.equal(authKey).and(Tables.BANK.EMAIL.equalIgnoreCase(bankEmail))).fetchAny();

            if (r == null) {
                return bankId;
            }

            bankId = r.getValue(Tables.BANK.ID);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return bankId;
    }

    /**
     * Gets the login controller
     *
     * @return the LoginController
     */
    public static LoginController getLoginController() {
        return (LoginController) FacesContext.getCurrentInstance().getApplication().getELResolver()
                .getValue(FacesContext.getCurrentInstance().getELContext(), null, "loginController");
    }

    /**
     * Gets the locale controller.
     *
     * @return the locale controller
     */
    public static LocaleController getLocaleController() {
        return (LocaleController) FacesContext.getCurrentInstance().getApplication().getELResolver()
                .getValue(FacesContext.getCurrentInstance().getELContext(), null, "localeController");
    }

    /**
     * Gets the search details bean.
     *
     * @return the search details bean.
     */
    public static SearchDetailsBean getSearchDetailsBean() {
        return (SearchDetailsBean) FacesContext.getCurrentInstance().getApplication().getELResolver()
                .getValue(FacesContext.getCurrentInstance().getELContext(), null, "searchDetailsBean");
    }

    /**
     * Gets the file name of a file part
     *
     * @param filePart the file part
     * @return the file name
     */
    public static String getFileName(Part filePart) {
        String header = filePart.getHeader("content-disposition");
        for (String headerPart : header.split(";")) {
            if (headerPart.trim().startsWith("filename")) {
                String filepath = headerPart.substring(headerPart.indexOf('=') + 1).trim().replace("\"", "");
                // in case of IE, the part name is the full path of the file
                return FilenameUtils.getName(filepath);
            }

        }
        return null;
    }

    /**
     * Gets the pub key from a file in the config directory
     *
     * @return the pub key string
     */
    public static String getPubKeyString() throws IOException {
        String path = SamplyShareUtils.addTrailingFileSeparator(ProjectInfo.INSTANCE.getConfig().getConfigPath()) + "key.pub";
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, StandardCharsets.US_ASCII);
    }

    /**
     * Save a file part to a temporary file
     *
     * @param prefix the prefix of the temp file
     * @param part the file part to save
     * @return the resulting temp file
     */
    public static File savePartToTmpFile(String prefix, Part part) throws IOException {
        File file = Files.createTempFile(prefix, getFileName(part)).toFile();
        try (InputStream input = part.getInputStream()) {
            Files.copy(input, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        return file;
    }

    /**
     * Save an input stream to a temporary file
     *
     * @param prefix the prefix of the temp file
     * @param inputStream the input stream to save
     * @return the resulting temp file
     */
    public static File saveInputstreamToTmpFile(String prefix, InputStream inputStream, FormDataContentDisposition contentDispositionHeader) throws IOException {
        File file = Files.createTempFile(prefix, contentDispositionHeader.getFileName()).toFile();
        Files.copy(inputStream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
        return file;
    }

    /**
     * Save a byte array to a temporary file
     *
     * @param prefix the prefix of the temp file
     * @param buf the byte array to save
     * @return the resulting temp file
     */
    public static File saveByteArrayToTmpFile(String prefix, byte[] buf, FormDataContentDisposition contentDispositionHeader) throws IOException {
        File file = Files.createTempFile(prefix, contentDispositionHeader.getFileName()).toFile();
        FileUtils.writeByteArrayToFile(file, buf);
        return file;
    }

    /**
     * Get a list of site names from a list of sites
     *
     * @param sites the list of sites
     * @return the list of site names
     */
    public static List<String> getSiteNames(List<Site> sites) {
        List<String> siteNames = new ArrayList<>();
        for (Site site : sites) {
            siteNames.add(site.getName());
        }
        return siteNames;
    }

    /**
     * Get the list of locations (sites) from an access token
     *
     * @param accesstoken the access token from the authentication service
     * @return the location list
     */
    public static LocationListDTO getLocationList(AccessTokenDTO accesstoken) {
        String projectName = ProjectInfo.INSTANCE.getProjectName();
        String[] fallbacks = {System.getProperty("catalina.base") + File.separator + "conf", getServletContext().getRealPath("/WEB-INF")};
        String authUrl = OAuthConfig.getOAuth2Client(projectName, fallbacks).getHost() + "/oauth2/locations";

        Client client = ClientBuilder.newClient();
        Invocation.Builder invocationBuilder = client.target(authUrl).request("application/json").
                accept("application/json").header("Authorization", accesstoken.getHeader());

        return invocationBuilder.get(LocationListDTO.class);
    }

    /**
     * Get the location id from the access token
     *
     * @param accessTokenHeader the authorization header containing the access token
     * @return the location id from the first location
     */
    public static String getLocationIdFromAccessToken(String accessTokenHeader) {
        String projectName = ProjectInfo.INSTANCE.getProjectName();
        String[] fallbacks = {System.getProperty("catalina.base") + File.separator + "conf", getServletContext().getRealPath("/WEB-INF")};
        String authUrl = OAuthConfig.getOAuth2Client(projectName, fallbacks).getHost() + "/oauth2/userinfo";

        JWTAccessToken accessToken = OAuthUtils.getJwtAccessToken(accessTokenHeader);

        Client client = ClientBuilder.newClient();
        Invocation.Builder invocationBuilder = client.target(authUrl).request("application/json").
                accept("application/json").header("Authorization", accessToken.getHeader());

        UserDTO userInfo = invocationBuilder.get(UserDTO.class);

        List<LocationDTO> locations = userInfo.getLocations();
        if (locations == null || locations.size() == 0) {
            return null;
        }

        return locations.get(0).getId();
    }

    /**
     * Get a map of settings for the http connector
     *
     * @param config the configuration, containing the parameters
     * @return a map with http connector compliant values
     */
    public static HashMap<String, String> getHttpConfigParams(AbstractConfig config) {
        HashMap<String, String> configParams = new HashMap<>();
        configParams.put(PROXY_HTTP_HOST, config.getProperty(PROXY_HTTPS_HOST));
        configParams.put(PROXY_HTTP_PORT, config.getProperty(PROXY_HTTP_PORT));
        configParams.put(PROXY_HTTP_USERNAME, config.getProperty(PROXY_HTTP_USERNAME));
        configParams.put(PROXY_HTTP_PASSWORD, config.getProperty(PROXY_HTTP_PASSWORD));
        configParams.put(PROXY_HTTPS_HOST, config.getProperty(PROXY_HTTPS_HOST));
        configParams.put(PROXY_HTTPS_PORT, config.getProperty(PROXY_HTTPS_PORT));
        configParams.put(PROXY_HTTPS_USERNAME, config.getProperty(PROXY_HTTPS_USERNAME));
        configParams.put(PROXY_HTTPS_PASSWORD, config.getProperty(PROXY_HTTPS_PASSWORD));
        configParams.put(PROXY_REALM, config.getProperty(PROXY_REALM));

        return configParams;
    }

    /**
     * Convert User Agent String to JSON
     *
     * @param userAgentHeader the user agent header as received via http request.
     * @param bankId the id of the bank that sent this user agent
     * @return JSON String of the user agent information
     */
    public static String userAgentAndBankToJson(String userAgentHeader, int bankId) {
        SiteReportItem siteReportItem = new SiteReportItem();

        // {"id":12,"name":"Teststandort","versions""samply.share.client": "1.1.4-SNAPSHOT"}}
        Site site = BankUtil.getSiteForBankId(bankId);
        if (site != null) {
            siteReportItem.setId(Integer.toString(site.getId()));
            siteReportItem.setName(site.getName());
        } else {
            siteReportItem.setId("unknown");
            siteReportItem.setName("unknown");
        }

        if (userAgentHeader != null) {
            UserAgent userAgent = new UserAgent(userAgentHeader);
            if (userAgent.getShareName() != null) {
                siteReportItem.getVersions().put(userAgent.getShareName(), userAgent.getShareVersion());
            }
            if (userAgent.getLdmName() != null) {
                siteReportItem.getVersions().put(userAgent.getLdmName(), userAgent.getLdmVersion());
            }
            if (userAgent.getIdManagerName() != null) {
                siteReportItem.getVersions().put(userAgent.getIdManagerName(), userAgent.getIdManagerVersion());
            }
        }
        Gson gson = new Gson();
        return gson.toJson(siteReportItem);
    }

    /**
     * Use IcingaConnector to send version information
     *
     * @param userAgentHeader the user agent header as received via http request
     * @param bankId the id of the bank that reported it
     */
    public static void sendVersionReportsToIcinga(String userAgentHeader, int bankId) {
        // {"id":12,"name":"Teststandort","versions""samply.share.client": "1.1.4-SNAPSHOT"}}
        Site site = BankUtil.getSiteForBankId(bankId);
        if (site == null) {
            return;
        }

        List<StatusReportItem> statusReportItems = new ArrayList<>();
        StatusReportItem statusReportItemStatus = new StatusReportItem();
        StatusReportItem statusReportItemVersion = new StatusReportItem();

        if (userAgentHeader != null) {
            UserAgent userAgent = new UserAgent(userAgentHeader);
            try {
                if (userAgent.getShareName() != null) {
                    statusReportItemStatus.setExit_status("0");
                    statusReportItemStatus.setParameter_name(PARAMETER_SHARE_STATUS);
                    statusReportItemStatus.setStatus_text("ok");

                    statusReportItemVersion.setExit_status("0");
                    statusReportItemVersion.setParameter_name(PARAMETER_SHARE_VERSION);
                    statusReportItemVersion.setStatus_text(userAgent.getShareName() + "/" + userAgent.getShareVersion());
                } else {
                    statusReportItemStatus.setExit_status("1");
                    statusReportItemStatus.setParameter_name(PARAMETER_SHARE_STATUS);
                    statusReportItemStatus.setStatus_text("unknown");

                    statusReportItemVersion.setExit_status("1");
                    statusReportItemVersion.setParameter_name(PARAMETER_SHARE_VERSION);
                    statusReportItemVersion.setStatus_text("unknown");
                }
                statusReportItems.add(statusReportItemStatus);
                statusReportItems.add(statusReportItemVersion);

                statusReportItemStatus = new StatusReportItem();
                statusReportItemVersion = new StatusReportItem();
                if (userAgent.getLdmName() != null) {
                    statusReportItemStatus.setExit_status("0");
                    statusReportItemStatus.setParameter_name(PARAMETER_LDM_STATUS);
                    statusReportItemStatus.setStatus_text("ok");

                    statusReportItemVersion.setExit_status("0");
                    statusReportItemVersion.setParameter_name(PARAMETER_LDM_VERSION);
                    statusReportItemVersion.setStatus_text(userAgent.getLdmName() + "/" + userAgent.getLdmVersion());
                } else {
                    statusReportItemStatus.setExit_status("1");
                    statusReportItemStatus.setParameter_name(PARAMETER_LDM_STATUS);
                    statusReportItemStatus.setStatus_text("unknown");

                    statusReportItemVersion.setExit_status("1");
                    statusReportItemVersion.setParameter_name(PARAMETER_LDM_VERSION);
                    statusReportItemVersion.setStatus_text("unknown");
                }

                statusReportItems.add(statusReportItemStatus);
                statusReportItems.add(statusReportItemVersion);

                statusReportItemStatus = new StatusReportItem();
                statusReportItemVersion = new StatusReportItem();
                if (userAgent.getIdManagerName() != null) {
                    statusReportItemStatus.setExit_status("0");
                    statusReportItemStatus.setParameter_name(PARAMETER_IDM_STATUS);
                    statusReportItemStatus.setStatus_text("ok");

                    statusReportItemVersion.setExit_status("0");
                    statusReportItemVersion.setParameter_name(PARAMETER_IDM_VERSION);
                    statusReportItemVersion.setStatus_text(userAgent.getIdManagerName() + "/" + userAgent.getIdManagerVersion());
                } else {
                    statusReportItemStatus.setExit_status("1");
                    statusReportItemStatus.setParameter_name(PARAMETER_IDM_STATUS);
                    statusReportItemStatus.setStatus_text("unknown");

                    statusReportItemVersion.setExit_status("1");
                    statusReportItemVersion.setParameter_name(PARAMETER_IDM_VERSION);
                    statusReportItemVersion.setStatus_text("unknown");
                }

                statusReportItems.add(statusReportItemStatus);
                statusReportItems.add(statusReportItemVersion);

                IcingaConnector.reportStatusItems(site.getName(), statusReportItems);
            } catch (IcingaConnectorException e) {
                logger.warn("There was an error while trying to submit to icinga", e);
            }
        }
    }
    
	/**
	 * Get the current Project stage as set in web.xml.
	 *
	 * @return ProjectStage
	 */
	public static ProjectStage getProjectStage() {
	    return FacesContext.getCurrentInstance().getApplication().getProjectStage();
	}

    /**
     * Check if the list of sites contains only the given site
     *
     * @param sites the list of sites to check
     * @param site the site to check
     * @return true if the site is the only entry in the list
     */
    public static boolean onlySelf(List<String> sites, Integer site) {
        return (sites != null && site != null && sites.size() == 1 && sites.contains(site.toString()));
    }

    /**
     * Convert a site pojo to a site info object
     *
     * @param site the site pojo
     * @return the site info object
     */
    public static SiteInfo siteToSiteInfo(Site site) {
        SiteInfo siteInfo = new SiteInfo();
        siteInfo.setContact(site.getContact());
        siteInfo.setDescription(site.getDescription());
        siteInfo.setId(site.getId());
        siteInfo.setName(site.getName());
        siteInfo.setNameExtended(site.getNameExtended());
        return siteInfo;
    }

    /**
     * Fixes the namespaces of serialized objects for external requests,
     * depending on the project context.
     * Internally, the common schemas are used for serializing XML.
     *
     * @param in serialized object as XML string
     * @param targetNamespace desired output namespace (will be converted to lower case)
     * @return String with namespaces depending on project context
     */
    public static String fixNamespaces(String in, String targetNamespace) {

        if (SamplyShareUtils.isNullOrEmpty(in)) {
            return "";
        } else if (SamplyShareUtils.isNullOrEmpty(targetNamespace) ){
            // For older clients (<2.0.0-RC5) that don't supply a target namespace, use the project name (or "ccp" for project "dktk")
            String fallbackNamespace = ProjectInfo.INSTANCE.getProjectName().toLowerCase();
            if (fallbackNamespace.equalsIgnoreCase("dktk")) {
                return in.replace(XML_NAMESPACE_BASEURL + Constants.VALUE_XML_NAMESPACE_COMMON, XML_NAMESPACE_BASEURL + Constants.VALUE_XML_NAMESPACE_CCP);
            } else {
                return in.replace(XML_NAMESPACE_BASEURL + Constants.VALUE_XML_NAMESPACE_COMMON, XML_NAMESPACE_BASEURL + fallbackNamespace);
            }
        } else if (!targetNamespace.equalsIgnoreCase(Constants.VALUE_XML_NAMESPACE_COMMON)) {
            return in.replace(XML_NAMESPACE_BASEURL + Constants.VALUE_XML_NAMESPACE_COMMON, XML_NAMESPACE_BASEURL + targetNamespace.toLowerCase());
        } else {
            return in;
        }
    }
}
