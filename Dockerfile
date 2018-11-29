FROM tomcat:8.5.32-jre8-alpine

ADD target/broker /usr/local/tomcat/webapps/broker/
ADD src/docker/samply.share.broker.conf /etc/samply/
ADD src/docker/log4j2_samply.share.broker.xml /etc/samply/
ADD src/docker/context.xml /usr/local/tomcat/conf/Catalina/localhost/broker.xml
ADD src/docker/OAuth2Client.xml /etc/samply/
ADD src/docker/samply_common_config.xml /etc/samply/
ADD src/docker/mailSending.xml /etc/samply/

ADD src/docker/start.sh /samply/
RUN chmod +x /samply/start.sh

CMD ["/samply/start.sh"]
