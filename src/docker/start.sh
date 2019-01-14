#!/usr/bin/env bash

set -e

sed -i "s%{mail-host}%${MAIL_HOST:-relay2int1.klinik.uni-wuerzburg.de}%" /etc/samply/mailSending.xml
sed -i "s%{mail-port}%${MAIL_PORT:-25}%" /etc/samply/mailSending.xml
sed -i "s%{mail-protocol}%${MAIL_PROTOCOL:-smtp}%" /etc/samply/mailSending.xml
sed -i "s%{mail-from-address}%${MAIL_FROM_ADDRESS:-breu_m@ukw.de}%" /etc/samply/mailSending.xml
sed -i "s%{mail-from-name}%${MAIL_FROM_NAME:-Lokal Samply Searchbroker}%" /etc/samply/mailSending.xml

sed -i "s/{postgres-host}/${POSTGRES_HOST:-postgres}/" /usr/local/tomcat/conf/Catalina/localhost/broker.xml
sed -i "s/{postgres-port}/${POSTGRES_PORT:-5432}/" /usr/local/tomcat/conf/Catalina/localhost/broker.xml
sed -i "s/{postgres-db}/${POSTGRES_DB:-samply.broker}/" /usr/local/tomcat/conf/Catalina/localhost/broker.xml
sed -i "s/{postgres-user}/${POSTGRES_USER:-samply}/" /usr/local/tomcat/conf/Catalina/localhost/broker.xml
sed -i "s/{postgres-pass}/${POSTGRES_PASS:-samply}/" /usr/local/tomcat/conf/Catalina/localhost/broker.xml

sed -i "s/{proxy-host}/${PROXY_HOST:-}/" /etc/samply/samply_common_config.xml
sed -i "s/{proxy-port}/${PROXY_PORT:-}/" /etc/samply/samply_common_config.xml

# Replace start.sh with catalina.sh
exec /usr/local/tomcat/bin/catalina.sh run