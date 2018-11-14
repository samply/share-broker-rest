# Samply Share Broker

## General information

Samply Share Broker is a vital part of the decentral search infrastructure.
It allows to create and distribute decentral search inquiries based on dataelements in the metadata repository.

#### Conventions

Create a database with:
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

Your context.xml should resemble (for Tomcat 7)

```
<?xml version="1.0" encoding="UTF-8"?>
<Context path="/">
    <Resource name="jdbc/postgres/samply.share.broker" auth="Container"
              type="javax.sql.DataSource" driverClassName="org.postgresql.Driver"
              url="jdbc:postgresql://<database_url>/<database_name>"
              username="<username>" password="<password>" maxActive="50" maxIdle="20"
              maxWait="30000" removeAbandoned="true" 
              removeAbandonedTimeout="120" logAbandoned="true" />
</Context>
```
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
 
Also, adapt _pom.xml_ accordingly. The respective parameters there are:

```
<properties>
    (...)
    <database.username>your database username</database.username>
    <database.password>your database password</database.password>
    <database.url>jdbc:postgresql://your_database_url:your_database_port/your_database_name</database.url>
    (...)
</properties>
```

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

## Build

Simply execute the maven goal

```
mvn clean package
```




