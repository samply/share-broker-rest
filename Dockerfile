FROM tomcat:8.5.32-jre8-alpine
RUN ["rm", "-fr", "/usr/local/tomcat/webapps/ROOT"]
ADD target/searchbroker                         /usr/local/tomcat/webapps/ROOT/

ADD src/docker/context.xml                      /usr/local/tomcat/conf/Catalina/localhost/ROOT.xml
ADD src/docker/samply.share.broker.conf         /etc/samply/
ADD src/docker/log4j2.xml                       /etc/samply/
ADD src/docker/OAuth2Client.xml                 /etc/samply/
ADD src/docker/samply_common_config.xml         /etc/samply/
ADD src/docker/mailSending.xml                  /etc/samply/

# JMX Exporter
ENV JMX_EXPORTER_VERSION 0.3.1
COPY src/docker/jmx-exporter.yml                /samply/jmx-exporter.yml
ADD https://repo1.maven.org/maven2/io/prometheus/jmx/jmx_prometheus_javaagent/$JMX_EXPORTER_VERSION/jmx_prometheus_javaagent-$JMX_EXPORTER_VERSION.jar /samply/

ADD src/docker/start.sh                         /samply/
RUN chmod +x                                    /samply/start.sh
CMD ["/samply/start.sh"]