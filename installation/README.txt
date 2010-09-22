                    ICAT API Installation Guide

Introduction
------------
This document is to guide an administrator through the steps needed to install the iCAT API onto
Glassfish v2.1 on Microsoft Windows or Linux

Prerequisites
-------------
The installation needs the following software installed:

1. Java 6+ http://java.sun.com/javase/downloads/index.jsp 
2. Apache Ant v1.7+ http://ant.apache.org
3. Glassfish v2.x or v3.x https://glassfish.dev.java.net/
4. (Optional only to use MyProxy-SSO for authentication in ICAT API) Installed and configured Java COGKit 
	http://wiki.cogkit.org/index.php/Main_Page

5. Oracle DB with ICAT Schema and ICATUser schema installed. See README.txt file in database directory for schema installation on Oracle 10g Express Edition.

6. Download and put the ojdbc14.jar file in lib directory http://download.oracle.com/otn/utilities_drivers/jdbc/10105/ojdbc14.jar

Installation
------------
Note: <JAVA_HOME> -- is Java Home directory path
      <GLASSFISH_HOME> -- is glassfish direcotry path
      <INSTALLATION_HOME> -- is path where icat installation directory is extracted to
1) Extract icatx.x.x.zip to <INSTALLATION_PATH>
2) Check ant is installed with the correct version and its binary is available in path.
	$ ant -version
	  Apache Ant Version 1.x.x compiled on x xx xxx
   Version should be > 1.7
3) Configure Database: edit file <INSTALLATION_HOME>/nbproject/database.properties file
	a)  Configure iCAT Schema
		i)   Add your database details for icat.user,icat.url, and icat.password properties
		ii)  icat.user and icat.password are username and password used at iCAT Schema creation
		iii) icat.url must be a JDBC connection string, usually starts with jdbc:oracle:thin:@
4) Configure Glassfish: edit file <INSTALLATION_HOME>/nbproject/glassfish.properties
	a)  if you are using default Glassfish installation sjsas.admin.port,sjsas.http.port,sjsas.password
	    sjsas.host,sjsas.hot,sjsas.username,sjsas.domain need not be changed otherwise set according to
	    your glassfish configuration.
	b)  Change sjsas.root to the <GLASSFISH_HOME>. NOTE: Add the escape character \ if you are on windows for : and \
	c)  Set the sjsas.version to "2" for glassfish v2.x or "3" for glassfish v3.x (Without the double quotes)
	c)  Change admin.auth.password to the password you wish to add for the Admin ICAT API webservices. the username
	    will be facility.name-admin (facility.name is set in database.properties) ie DLS-admin or ISIS-admin
5) Installation: Open a command window and cd <INSTALLATION_HOME> now execute following commands
	a) ant -f icat.xml init-config
		i) Copies the required files onto the Glassfish server i.e Oracle JDBC drivers
		ii) Copies a jar file to fix a bug in the StAX Parser in Java 6.
		    Note: if you are using Linux, you will need to manually copy <INSTALLATION_HOME>/lib/wstx-asl-3.9.2.jar into
			  <JAVA_HOME>/jre/lib/endorsed creating the endorsed directory if it doesn't exist.
	b) ant -f icat.xml start-domain
		i) Starts the Glassfish server, if it complains of not starting, check http://localhost:4848
		ii) If it still hasn't started the server, use the commands <GLASSFISH_HOME>/bin/asadmin start-domain <sjsas.domain> and
		    <GLASSFISH_HOME>/bin/asadmin stop-domain <sjsas.domain> to start and stop the domain. Note: replace <sjsas.domain> in command
		    with setting of sjsas.domain in glassfish.properties set earlier.
	c) ant -f icat.xml install
		i) Creates all the glassfish data sources and connection pools and deploys icat-api-ws.jar onto Glassfish
		Note: if you are reinstalling icat api then some errors will occur saying it cannot create some tables, do
		not worry about this, it just means the tables have already been created. 
	d) ant -f icat.xml configure-icat-db
		i) Configures the MyProxy Servers and enables SUPER and ADMIN for ICAT API admin logins
	e) ant -f icat.xml restart-domain
		i) allows the changes done to the database to be picked up by glassfish.
	f) Check if the installation is working, check https://localhost:8181/ICATService/ICAT?wsdl to view the wsdl file.
