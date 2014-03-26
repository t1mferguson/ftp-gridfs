ftp-gridfs
=========

This project uses the Apache FTP server based on MINA, and uses gridfs supplied by mongodb
as the datastore/filestore.  Spring apps is used to configure and start the application.  
Basics to get up and running:<br>
<ol>
	<li>Open src/main/resources/SpringConfig.xml and update for your mongo server information along with the port you want to use for the FTP server.</li>
	<li>Don't forget to update the fptUsersFactory bean to point to your own file or other user information.  An example user.properties file is located in src/main/resources</li>
        <li>mvn package</li>
        <li>java -jar target/ftp-gridfs-0.0.1-SNAPSHOT.one-jar.jar</li>
</ol>    
Since this is based on Apache FTP it is also very helpful to read their documentation for 
configuration information:  http://mina.apache.org/ftpserver-project/index.html<br>
<br>
Good Luck!<br>
Tim<br>
codetoad96@gmail.com<br>
