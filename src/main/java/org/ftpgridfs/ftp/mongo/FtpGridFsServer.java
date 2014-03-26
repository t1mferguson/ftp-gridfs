package org.ftpgridfs.ftp.mongo;

import org.apache.ftpserver.FtpServer;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * This class simply exists to load the spring application context and start the FTP server.  It looks for "SpringConfig.xml"
 * on the classpath.
 *
 */
public class FtpGridFsServer 
{
    public static void main( String[] args ) {
       ApplicationContext ctx = new ClassPathXmlApplicationContext("SpringConfig.xml");
       FtpServer server = ctx.getBean("ftpServer", FtpServer.class);
    }
}
