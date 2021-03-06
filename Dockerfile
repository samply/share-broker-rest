FROM tomcat:8.5.32-jre8-alpine

RUN ["rm", "-fr", "/usr/local/tomcat/webapps"]
ADD target/searchbroker.war                     /usr/local/tomcat/webapps/broker.war

ADD src/docker/context.xml                      /usr/local/tomcat/conf/Catalina/localhost/broker.xml

ADD src/docker/OAuth2Client.xml                 /usr/local/tomcat/conf/
ADD src/docker/samply_common_config.xml         /usr/local/tomcat/conf/
ADD src/docker/mailSending.xml                  /usr/local/tomcat/conf/
ADD src/docker/log4j2.xml                       /usr/local/tomcat/conf/
ADD src/docker/statistic_notification.txt       /usr/local/tomcat/conf/statistic_notification.txt
ADD src/docker/samply.share.broker.conf         /usr/local/tomcat/conf/samply.share.broker.conf
ADD src/docker/features.properties              /usr/local/tomcat/conf/features.properties

# JMX Exporter
ENV JMX_EXPORTER_VERSION 0.3.1
COPY src/docker/jmx-exporter.yml                /samply/jmx-exporter.yml
ADD https://repo1.maven.org/maven2/io/prometheus/jmx/jmx_prometheus_javaagent/$JMX_EXPORTER_VERSION/jmx_prometheus_javaagent-$JMX_EXPORTER_VERSION.jar /samply/

ADD src/docker/start.sh                         /samply/
RUN chmod +x                                    /samply/start.sh

RUN apk add --no-cache ttf-dejavu \
    && rm -rf /var/cache/apk/*

CMD ["/samply/start.sh"]
