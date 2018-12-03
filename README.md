# Samply-Search-Broker

## General information

The Samply Searchbroker is a vital part of the decentral search infrastructure.
It allows to create and distribute decentral search inquiries based on dataelements in the metadata repository.

## Usage

To start the Backend, Database and the UI in once, go to samply.share.broker.ui which has a Docker compose file.

To run only the backend, 
Clone this Git repository, if you haven't already.

Make sure you have port 8083 and 5438 free or edit the two run -p contexts below, before bringing the system up with:

### Docker
Install Docker and open a console:

#### Start a postgres container by:
```
docker network create --driver bridge broker
docker stop postgres
docker rm postgres
docker run -p 5438:5432 --name=postgres --net=broker -d -e POSTGRES_USER=samply -e POSTGRES_PASSWORD=samply -e POSTGRES_DB=samply.broker postgres:9.6
```

#### Start Searchbroker by:

go to repository directory, e.g.: cd D:\Repositories\itc\samply.share.broker.rest

call: mvn clean package

```
docker stop searchbroker
docker rm searchbroker
docker build .
docker run  --name=searchbroker --net=broker -p 8083:8080 $ID_FROM_BUILD
```
get "Hello world" from http://localhost:8083/broker

Also it's very likely to change mail settings, so you can receive emails to register a Connector.

To verify a Bridgehead after adding the Searchbroker in Connector, verify in database (port=5436, db=samply.broker, user/password=samply):

Go to db samply.broker and make a new row in table bank_site with bank_id={bank_id in table bank}, site_id={site_id in table site}, approved=true

Without defining volumes, the database will be lost after deleting the container.

#### Environment

The Docker container needs certain environment variables to be able to run:

Use like this: docker run  --name=searchbroker --net=broker -p 8083:8080 e6a8653b5744 -e PROXY_HOST=197.149.128.60 -e PROXY_PORT=4593

* `MAIL_HOST` - eg. relay2int1.klinik.uni.de
* `MAIL_PORT` - eg. 25
* `MAIL_PROTOCO`L - eg. smtp
* `MAIL_FROM_ADDRESS` - eg. Searchbroker@samply.de
* `MAIL_FROM_NAME` - eg. Lokal Samply Searchbroker

* `POSTGRES_HOST` - the host name of the Postgres DB
* `POSTGRES_PORT` - the port of the Postgres DB, defaults to 5432
* `POSTGRES_DB` - the database name, defaults to samply.broker
* `POSTGRES_USER` - the database username, defaults to samply
* `POSTGRES_PASS` - the database password

* `AUTH_HOST` - eg. https://auth.dev.germanbiobanknode.de
* `AUTH_PUBLIC_KEY` - eg. base64DerFormat of https://auth.dev.germanbiobanknode.de/oauth2/certs
* `AUTH_CLIENT_ID` - eg. aun53c97n41iu
* `AUTH_CLIENT_SECRET` - eg. 65r9umrs4koi5c6325nflbb63hduko5t6nbu02o54mf841cdmfcfc41ob7fqjhgkm1ut0qconbd3dgo1ihu21n73k1vvid0pi8geqt

* `PROXY_HOST` - the URL of the HTTP proxy to use for outgoing connections; enables proxy usage if set
* `PROXY_PORT` - the port of the HTTP proxy to use for outgoing connections; enables proxy usage if set


## Manual Install

#### Conventions

Create a Postgresql 9.6 database with:
username = samply
password = samply
port = 5432
name = samply.broker

Run in Tomcat 8.5 under http://localhost:8083 so you can use standard configuration files.
Except you are behind a PROXY: edit samply_common_config.xml

for details see https://wiki.mitro.dkfz.de/x/RIAHAw

#### Configuration files

```
- samply.share.broker.conf
- samply_common_config.xml
- log4j2_samply.share.broker.xml
- OAuth2Client.xml (Deprecated)
- context.xml (goes to tomcat dir)
```

You can save these files local to 
WINDOWS: "C:\/Users/\%username%/\.config/\samply/\"
LINUX: "/etc/samply/"
This will of course fully override the WEB-INF fallback directory, so your configurations are stable.

#### Database Connection

The database connection uses a connection pool, for which the datasource is defined in
 _src/main/webapp/META-INF/context.xml_ or the context.xml from your tomcat installation. Usually, the definition
 in tomcat's own context.xml has a higher priority.
  Samply Share Broker uses a database schema named _samply_.

Since Tomcat 8, a newer version of Apache Commons DBCP is used, which needs some parameters renamed. 
See [here](https://tomcat.apache.org/migration-8.html#Database_Connection_Pooling). That should lead to the following 
file:

```
<?xml version="1.0" encoding="UTF-8"?>
<Context path="/">
    <Resource name="jdbc/postgres/samply.share.broker" auth="Container"
              type="javax.sql.DataSource" driverClassName="org.postgresql.Driver"
              url="jdbc:postgresql://<database_url>/<database_name>"
              username="<username>" password="<password>" maxTotal="50" maxIdle="20"
              maxWaitMillis="30000" removeAbandonedOnBorrow="true" removeAbandonedOnMaintenance="true"
              removeAbandonedTimeout="120" logAbandoned="true" />
</Context>
```

You have to set the parameters "database_url", "database_name", "username" and "password" according to
your setup. The resource name can not be changed without changing it in
 _de.samply.share.broker.jdbc.ResourceManager.java_ as well, but feel free to change the database name,
 the user name and the user password. Especially changing the password is highly recommended.
 
The values set for the db pool are just example values. Feel free to change them to whatever suits your needs. 
 
Also, adapt _pom.xml_ accordingly for Flyway to prepare the database.

#### Logging

Copy the example _log4j2.xml_ file from _/src/main/resources_ to the place defined in the corresponding
parameter in your _web.xml_. By default, the parameter in _web.xml_ is set to.

```
<context-param>
    <param-name>log4jConfiguration</param-name>
    <param-value>file:///etc/dktk/log4j2_samply.share.broker.xml</param-value>
</context-param>
```

Feel free to change this to whatever you like, and make sure that tomcat has the right to read this file. Also ensure
that tomcat may write to the specified directory in the log4j config file. The parameter to change is:

```
<Property name="logDir">/var/log/samply/</Property>
```

### Test

http://localhost:8083/rest/test/inquiries/1



