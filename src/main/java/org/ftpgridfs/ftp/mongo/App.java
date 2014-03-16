package org.ftpgridfs.ftp.mongo;

import org.apache.ftpserver.FtpServer;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) {
       ApplicationContext ctx = new ClassPathXmlApplicationContext("SpringConfig.xml");
       FtpServer server = ctx.getBean("ftpServer", FtpServer.class);
    }
}
