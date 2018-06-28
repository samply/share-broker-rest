package de.samply.share.broker.rest.listener;

import de.samply.common.config.Configuration;
import de.samply.common.config.ObjectFactory;
import de.samply.common.http.HttpConnector;
import de.samply.config.util.JAXBUtil;
import de.samply.share.common.model.dto.UserAgent;
import de.samply.share.common.utils.ProjectInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.UnmarshalException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.FileNotFoundException;
import java.util.HashMap;

import static de.samply.common.http.HttpConnector.*;

public class Proxy {
    private static final Logger logger = LogManager.getLogger(de.samply.share.broker.rest.listener.Proxy.class);
    private static final String COMMON_CONFIG_FILENAME_SUFFIX = "_common_config.xml";
    public static HttpConnector getHttpConnector() {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(ObjectFactory.class);
            Configuration configuration = JAXBUtil
                    .findUnmarshall(ProjectInfo.INSTANCE.getProjectName().toLowerCase() + COMMON_CONFIG_FILENAME_SUFFIX,
                            jaxbContext, Configuration.class, ProjectInfo.INSTANCE.getProjectName().toLowerCase());

            return getHttpConfigParams(configuration);
        } catch (FileNotFoundException e) {
            logger.error("No config file found by using samply.common.config for project " + ProjectInfo.INSTANCE.getProjectName());
        } catch (UnmarshalException ue) {
            throw new RuntimeException("Unable to unmarshal config file");
        } catch (SAXException | JAXBException | ParserConfigurationException e) {
            e.printStackTrace();
        }
        return null;
    }
    private static HttpConnector getHttpConfigParams(de.samply.common.config.Configuration configuration) {
        HashMap<String, String> configParams = new HashMap<>();

        try {
            try {
                configParams.put(PROXY_HTTP_HOST, configuration.getProxy().getHTTP().getUrl().getHost());
                configParams.put(PROXY_HTTP_PORT, Integer.toString(configuration.getProxy().getHTTP().getUrl().getPort()));
                configParams.put(PROXY_HTTP_USERNAME,configuration.getProxy().getHTTP().getUsername());
                configParams.put(PROXY_HTTP_PASSWORD,configuration.getProxy().getHTTP().getPassword());
            } catch (NullPointerException npe) {
                logger.debug("Could not get HTTP Proxy Settings...should be empty or null");
            }
            try {
                configParams.put(PROXY_HTTPS_HOST, configuration.getProxy().getHTTPS().getUrl().getHost());
                configParams.put(PROXY_HTTPS_PORT, Integer.toString(configuration.getProxy().getHTTPS().getUrl().getPort()));
                configParams.put(PROXY_HTTPS_USERNAME,configuration.getProxy().getHTTPS().getUsername());
                configParams.put(PROXY_HTTPS_PASSWORD,configuration.getProxy().getHTTPS().getPassword());
            } catch (NullPointerException npe) {
                logger.debug("Could not get HTTPS Proxy Settings...should be empty or null");
            }
            configParams.put(PROXY_BYPASS_PRIVATE_NETWORKS, Boolean.TRUE.toString());

            UserAgent userAgent = new UserAgent(ProjectInfo.INSTANCE.getProjectName(), "Samply.Share", ProjectInfo.INSTANCE.getVersionString());
            configParams.put(USER_AGENT, userAgent.toString());

        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        HttpConnector httpConnector = new HttpConnector(configParams);
        return  httpConnector;
    }

}
