ftp-gridfs
=========

This project uses the Apache FTP server based on MINA, and uses gridfs supplied by mongodb
as the datastore/filestore.  Sprint apps is used to configure and start the application.  
Basics to get up and running:
	1.  Open src/main/resources/SpringConfig.xml and updat for your mongo server information
	    along with the port you want to use for the FTP server.
	2.  Don't forget to update the fptUsersFactory bean to point to your own file or other
	    user information.  An example user.properties file is located in srd/main/resources
    2.  mvn package
    3.  java -jar uber-ftp-gridfs-0.0.1-SNAPSHOT.jar
    
Since this is based Apache FTP it is also very helpful to read their documentation for 
configuration information:  http://mina.apache.org/ftpserver-project/index.html

Good Luck!
Tim
codetoad96@gmail.com