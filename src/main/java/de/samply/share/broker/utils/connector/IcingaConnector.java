package de.samply.share.broker.utils.connector;

import com.google.gson.Gson;
import de.samply.common.http.HttpConnector;
import de.samply.share.broker.listener.Proxy;
import de.samply.share.broker.utils.Config;
import de.samply.share.broker.utils.Utils;
import de.samply.share.common.model.dto.monitoring.StatusReportItem;
import de.samply.share.common.utils.ProjectInfo;
import de.samply.share.common.utils.SamplyShareUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.Consts;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.NoHttpResponseException;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Relay version information from attached clients to Icinga
 */
public class IcingaConnector {

    public static final String CFG_ICINGA_HOST = "icinga.host";
    public static final String CFG_ICINGA_PATH = "icinga.path";
    public static final String CFG_ICINGA_USERNAME = "icinga.username";
    public static final String CFG_ICINGA_PASSWORD = "icinga.password";
    public static final String CFG_ICINGA_SITE_SUFFIX = "icinga.site_suffix";

    public static final String ICINGA_PREFIX = "BK ";

    private static CloseableHttpClient httpClient;
    private static HttpHost httpHost;
    private static Gson gson;
    private static String targetPath;
    private static String siteSuffix;
    private static CredentialsProvider credentialsProvider;

    static {
        try {
            String username = ProjectInfo.INSTANCE.getConfig().getProperty(CFG_ICINGA_USERNAME);
            String password = ProjectInfo.INSTANCE.getConfig().getProperty(CFG_ICINGA_PASSWORD);
            siteSuffix = ProjectInfo.INSTANCE.getConfig().getProperty(CFG_ICINGA_SITE_SUFFIX);
            String targetHost = ProjectInfo.INSTANCE.getConfig().getProperty(CFG_ICINGA_HOST);
            targetPath = ProjectInfo.INSTANCE.getConfig().getProperty(CFG_ICINGA_PATH);
            HttpConnector httpConnector = new HttpConnector(Utils.getHttpConfigParams(ProjectInfo.INSTANCE.getConfig()));
            httpHost = SamplyShareUtils.getAsHttpHost(targetHost);
            credentialsProvider = prepareCredentialsProvider(httpHost, username, password);
            httpConnector.setCp(credentialsProvider);
            httpClient = httpConnector.getHttpClient(targetHost);
            gson = new Gson();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    // prevent instantiation
    private IcingaConnector() {
    }

    /**
     * Send one Report item (corresponding to one service in icinga) to icinga
     *
     * @param sitename the name of the site that sent the report
     * @param statusReportItem the status report item
     */
    private static void sendReport(String sitename, StatusReportItem statusReportItem) throws IcingaConnectorException {
        boolean performanceReport = (StringUtils.indexOfAny(statusReportItem.getParameter_name(), new String[]{
                StatusReportItem.PARAMETER_PATIENTS_DKTKFLAGGED_COUNT,
                StatusReportItem.PARAMETER_PATIENTS_TOTAL_COUNT,
                StatusReportItem.PARAMETER_REFERENCE_QUERY_RESULTCOUNT,
                StatusReportItem.PARAMETER_REFERENCE_QUERY_RUNTIME
        }) != -1);

        if (performanceReport) {
            sendPerformanceReport(sitename, statusReportItem);
        } else {
            sendSimpleReport(sitename, statusReportItem);
        }
    }

    /**
     * Send a simple report item (one parameter and one value) to icinga
     *
     * @param sitename the name of the site that sent the report
     * @param statusReportItem the status report item
     */
    private static void sendSimpleReport(String sitename, StatusReportItem statusReportItem) throws IcingaConnectorException {
        sendSimpleReport(sitename, statusReportItem, false);
    }

    /**
     * Send a simple report item (one parameter and one value) to icinga
     *
     * @param sitename the name of the site that sent the report
     * @param statusReportItem the status report item
     * @param isRetry if an error occurs, one attempt to re-transmit is made after 5 seconds. if this is set to true,
     *                this is the retry and no further attempt will be made
     */
    private static void sendSimpleReport(String sitename, StatusReportItem statusReportItem, boolean isRetry) throws IcingaConnectorException {
        try {
            HttpPost httpPost;
            if (statusReportItem.getParameter_name().equals("host")) {
                httpPost = createPostHost(sitename);
            } else {
                if (ProjectInfo.INSTANCE.getProjectName().toLowerCase().equals("samply")) {
                    httpPost = createPost(sitename, statusReportItem.getParameter_name() + "-gba");
                } else {
                    httpPost = createPost(sitename, statusReportItem.getParameter_name());
                }
            }
            IcingaReportItem icingaReportItem = new IcingaReportItem();
            icingaReportItem.setExit_status(statusReportItem.getExit_status());
            icingaReportItem.setPlugin_output(statusReportItem.getStatus_text());
            httpPost.setEntity(new StringEntity(gson.toJson(icingaReportItem), Consts.UTF_8));


            // only for local test with proxy
//            HttpHost httpHost = new HttpHost("193.174.53.221", 3128);
//            SSLContextBuilder builder = new SSLContextBuilder();
//            builder.loadTrustMaterial(null, new TrustStrategy() {
//                public boolean isTrusted(final X509Certificate[] chain, String authType) {
//                    return true;
//                }
//            });
//            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(builder.build());
//            CloseableHttpClient httpClient = HttpClients
//                    .custom()
//                    .setSSLSocketFactory(sslsf).setProxy(httpHost).setDefaultCredentialsProvider(credentialsProvider)
//                    .build();


            CloseableHttpResponse response = httpClient.execute(httpPost);
            EntityUtils.consume(response.getEntity());
        } catch (NoHttpResponseException nhre) {
            if (!isRetry) {
                try {
                    TimeUnit.SECONDS.sleep(5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                sendSimpleReport(sitename, statusReportItem, true);
            }
        } catch (URISyntaxException | IOException e) {
            throw new IcingaConnectorException(e);
//        } catch (KeyManagementException e) {
//            e.printStackTrace();
//        } catch (NoSuchAlgorithmException e) {
//            e.printStackTrace();
//        } catch (KeyStoreException e) {
//            e.printStackTrace();
        }
    }

    /**
     * Send a performance report item (one parameter and one value) to icinga
     *
     * @param sitename the name of the site that sent the report
     * @param statusReportItem the status report item
     */
    private static void sendPerformanceReport(String sitename, StatusReportItem statusReportItem) throws IcingaConnectorException {
        sendPerformanceReport(sitename, statusReportItem, false);
    }

    /**
     * Send a performance report item (one parameter and one value) to icinga
     *
     * @param sitename the name of the site that sent the report
     * @param statusReportItem the status report item
     * @param isRetry if an error occurs, one attempt to re-transmit is made after 5 seconds. if this is set to true,
     *                this is the retry and no further attempt will be made
     */
    private static void sendPerformanceReport(String sitename, StatusReportItem statusReportItem, boolean isRetry) throws IcingaConnectorException {
        try {
            HttpPost httpPost;
            if (ProjectInfo.INSTANCE.getProjectName().toLowerCase().equals("samply")) {
                httpPost = createPost(sitename, statusReportItem.getParameter_name() + "-gba");
            } else {
                httpPost = createPost(sitename, statusReportItem.getParameter_name());
            }
            IcingaReportItem icingaReportItem = new IcingaReportItem();
            icingaReportItem.setExit_status(statusReportItem.getExit_status());
            icingaReportItem.setPlugin_output(statusReportItem.getStatus_text());
            IcingaPerformanceData icingaPerformanceData = new IcingaPerformanceData(
                    statusReportItem.getParameter_name(),
                    statusReportItem.getStatus_text(),
                    statusReportItem.getParameter_name().equals(StatusReportItem.PARAMETER_REFERENCE_QUERY_RUNTIME) ? IcingaPerformanceData.UnitOfMeasure.MILISECONDS : IcingaPerformanceData.UnitOfMeasure.NONE);
            icingaReportItem.getPerformance_data().add(icingaPerformanceData);

            httpPost.setEntity(new StringEntity(gson.toJson(icingaReportItem), Consts.UTF_8));

            // only for local test with proxy
//            HttpHost httpHost = new HttpHost("193.174.53.221", 3128);
//            SSLContextBuilder builder = new SSLContextBuilder();
//            builder.loadTrustMaterial(null, new TrustStrategy() {
//                public boolean isTrusted(final X509Certificate[] chain, String authType) {
//                    return true;
//                }
//            });
//            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(builder.build());
//            CloseableHttpClient httpClient = HttpClients
//                    .custom()
//                    .setSSLSocketFactory(sslsf).setProxy(httpHost).setDefaultCredentialsProvider(credentialsProvider)
//                    .build();


            CloseableHttpResponse response = httpClient.execute(httpPost);
            EntityUtils.consume(response.getEntity());
        } catch (NoHttpResponseException nhre) {
            if (!isRetry) {
                try {
                    TimeUnit.SECONDS.sleep(5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                sendPerformanceReport(sitename, statusReportItem, true);
            }
        } catch (URISyntaxException | IOException e) {
            throw new IcingaConnectorException(e);
//        } catch (KeyManagementException e) {
//            e.printStackTrace();
//        } catch (NoSuchAlgorithmException e) {
//            e.printStackTrace();
//        } catch (KeyStoreException e) {
//            e.printStackTrace();
        }
    }

    /**
     * Report a list of status report items to icinga
     *
     * @param sitename the name of the site that sent the report
     * @param statusReportItems the list of status report items
     */
    public static void reportStatusItems(String sitename, List<StatusReportItem> statusReportItems) throws IcingaConnectorException {
        for (StatusReportItem statusReportItem : statusReportItems) {
            sendReport(sitename, statusReportItem);
        }
    }

    /**
     * Create an apache http post object for the given site and parameter
     *
     * If we use "addParameter" instead of "setCustomQuery" in URIBuilder, all spaces will be replaced with "+" instead
     * of "%20". This will cause icinga to not recognize the call.
     *
     * @param sitename the site (or "host" in icinga)
     * @param servicename the service name
     * @return HttpPost object
     */
    private static HttpPost createPost(String sitename, String servicename) throws URISyntaxException {
        String service = ICINGA_PREFIX + sitename + " " + siteSuffix + "!" + servicename;
        URIBuilder uriBuilder = new URIBuilder();
        uriBuilder.setScheme(httpHost.getSchemeName())
                .setHost(httpHost.getHostName())
                .setPort(httpHost.getPort())
                .setPath(targetPath)
                .setCustomQuery("service=" + service);
        HttpPost httpPost = new HttpPost(uriBuilder.build().toString());
        httpPost.setHeader(HttpHeaders.ACCEPT, javax.ws.rs.core.MediaType.APPLICATION_JSON);
        return httpPost;
    }

    private static HttpPost createPostHost(String sitename) throws URISyntaxException {
        String service = "BK " + sitename + " " + siteSuffix;
        URIBuilder uriBuilder = new URIBuilder();
        uriBuilder.setScheme(httpHost.getSchemeName())
                .setHost(httpHost.getHostName())
                .setPort(httpHost.getPort())
                .setPath(targetPath)
                .setCustomQuery("host=" + service);
        HttpPost httpPost = new HttpPost(uriBuilder.build().toString());
        httpPost.setHeader(HttpHeaders.ACCEPT, javax.ws.rs.core.MediaType.APPLICATION_JSON);
        return httpPost;
    }

    /**
     * Prepare a credentials provider with the credentials for icinga
     *
     * @param targetHost address of the icinga service
     * @param username username for basic auth with icinga
     * @param password password for basic auth with icinga
     * @return credentials provider with one entry for icinga
     */
    private static CredentialsProvider prepareCredentialsProvider(HttpHost targetHost, String username, String password) {
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        AuthScope authScope = new AuthScope(targetHost.getHostName(), AuthScope.ANY_PORT, AuthScope.ANY_REALM, AuthSchemes.BASIC);
        Credentials credentials = new UsernamePasswordCredentials(username, password);
        credentialsProvider.setCredentials(authScope, credentials);
        return credentialsProvider;
    }


}
