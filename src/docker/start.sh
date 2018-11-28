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

sed -i "s%{auth-host}%${AUTH_HOST:-https://auth.dev.germanbiobanknode.de}%" /etc/samply/OAuth2Client.xml
sed -i "s%{auth-public-key}%${AUTH_PUBLIC_KEY:-MIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEA/D51sGPXYNn/2cYYxCB5bP0JMRrWXhn3qmCIwrJCO8VgmsT9DiRJz/EmiP6qOrC3NMYSaELBGgaQeb690mqIMxY/5qkfIbNJYuV7F2gcPQFWxY2neO24XnFXQqsA0PcdlF5lB0HPCYKoqjV1hVtBl9IS8/v8mJ1FMa4oifGD8AqrLEQkItkEK+yg53rbs0sxlEFYp1U4gogmW6MdQ1ZDfCLiL6eWBFWRpHZAzXxfkauoxcccReH6hv7DPkI3ngxxARx8ivcLS+psJOe8RL2LrlS49flbazOWBmG/f3DFdoEcXYcraSnFc9lx7SJK4xsL6mBv6Tc1Qtf0nuAG+3bLICe9M0pE62z9wSVebe4F7htfElSr7MS2EMXX5iW0whe1RrsPojPY12ZEKOL7WGvJTyDOnA2Nzp22p5Ii/wru1uNaD/7xsw4OcMxHaYFi87dJSbsfx1OEXP3Co+zWZ2B1WdV83bvlx7NNHsATYeQuKG7IeBco+oYoXAjOk7IBlc0M6WqOpuXuBNXOGpvPR4aRd0COYXIZd+DqoK3ZLCr7gEYHHeCUx6Y8cKLK4sxbhHjGqusjVEPYdM46txSawNNIhp0LtfDilWWwecYX3N0WIPFElfKL43tIrjVrzsfL7nECsapVByhqBGFZX+mY2gEprBnqDCrVeUELmKiwm+ioQtkCAwEAAQ==}%" /etc/samply/OAuth2Client.xml
sed -i "s%{auth-client-id}%${AUTH_CLIENT_ID:-bniuhugdkvr2b}%" /etc/samply/OAuth2Client.xml
sed -i "s%{auth-client-secret}%${AUTH_CLIENT_SECRET:-}%" /etc/samply/OAuth2Client.xml

sed -i "s/{proxy-host}/${PROXY_HOST:-}/" /etc/samply/samply_common_config.xml
sed -i "s/{proxy-port}/${PROXY_PORT:-}/" /etc/samply/samply_common_config.xml

# Replace start.sh with catalina.sh
exec /usr/local/tomcat/bin/catalina.sh run