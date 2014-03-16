/**
 * 
 */
package org.ftpgridfs.ftp.mongo;

import static org.junit.Assert.*;

import org.ftpgridfs.ftp.mongo.MongoFtpFile;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author tferguso
 *
 */
public class TestMongoFtpFile {

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link org.ftpgridfs.ftp.mongo.MongoFtpFile#MongoFtpFile(org.springframework.data.mongodb.gridfs.GridFsOperations, java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testMongoFtpFile() {
//		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.ftpgridfs.ftp.mongo.MongoFtpFile#getAbsolutePath()}.
	 */
	@Test
	public void testGetAbsolutePath() {
//		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.ftpgridfs.ftp.mongo.MongoFtpFile#getName()}.
	 */
	@Test
	public void testGetName() {
//		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.ftpgridfs.ftp.mongo.MongoFtpFile#getPhysicalName(java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testGetPhysicalName() {
		String name = MongoFtpFile.getPhysicalName(null, "test.txt");
		assertEquals("/test.txt", name);
		
		name = MongoFtpFile.getPhysicalName("/test", "test.txt");
		assertEquals("/test/test.txt", name);
		
		name = MongoFtpFile.getPhysicalName("/test/", "test.txt");
		assertEquals("/test/test.txt", name);
		
		name = MongoFtpFile.getPhysicalName(null, "../test.txt");
		assertEquals("/test.txt", name);
		
		name = MongoFtpFile.getPhysicalName("/test", "../test.txt");
		assertEquals("/test.txt", name);
		
		name = MongoFtpFile.getPhysicalName("/test/", "../test.txt");
		assertEquals("/test.txt", name);
		
		name = MongoFtpFile.getPhysicalName(null, "..");
		assertEquals("/", name);
		
		name = MongoFtpFile.getPhysicalName("/test", "..");
		assertEquals("/", name);
		
		name = MongoFtpFile.getPhysicalName("/test/", "..");
		assertEquals("/", name);
		
		name = MongoFtpFile.getPhysicalName(null, "test/../test.txt");
		assertEquals("/test.txt", name);
		
		name = MongoFtpFile.getPhysicalName("/test", "test2/../test.txt");
		assertEquals("/test/test.txt", name);
		
		name = MongoFtpFile.getPhysicalName("/test/", "test2/../test.txt");
		assertEquals("/test/test.txt", name);
		
		
		name = MongoFtpFile.getPhysicalName(null, "~/test.txt");
		assertEquals("/test.txt", name);
		
		name = MongoFtpFile.getPhysicalName("/test", "~/test.txt");
		assertEquals("/test.txt", name);
		
		name = MongoFtpFile.getPhysicalName("/test/", "~/test.txt");
		assertEquals("/test.txt", name);
		
		name = MongoFtpFile.getPhysicalName(null, "./test.txt");
		assertEquals("/test.txt", name);
		
		name = MongoFtpFile.getPhysicalName("/test", "./test.txt");
		assertEquals("/test/test.txt", name);
		
		name = MongoFtpFile.getPhysicalName("/test/", "./test.txt");
		assertEquals("/test/test.txt", name);
		
		name = MongoFtpFile.getPhysicalName(null, "~");
		assertEquals("/", name);
		
		name = MongoFtpFile.getPhysicalName("/test", "~");
		assertEquals("/", name);
		
		name = MongoFtpFile.getPhysicalName("/test/", "~");
		assertEquals("/", name);
		
		name = MongoFtpFile.getPhysicalName(null, "test/~");
		assertEquals("/", name);
		
		name = MongoFtpFile.getPhysicalName("/test", "test/~");
		assertEquals("/", name);
		
		name = MongoFtpFile.getPhysicalName("/test/", "test/~");
		assertEquals("/", name);
		
		name = MongoFtpFile.getPhysicalName(null, "test/~/test.txt");
		assertEquals("/test.txt", name);
		
		name = MongoFtpFile.getPhysicalName("/test", "test/~/test.txt");
		assertEquals("/test.txt", name);
		
		name = MongoFtpFile.getPhysicalName("/test/", "test/~/test.txt");
		assertEquals("/test.txt", name);
		
		name = MongoFtpFile.getPhysicalName(null, "/test/test.txt");
		assertEquals("/test/test.txt", name);
		
		name = MongoFtpFile.getPhysicalName("/test", "/test/test.txt");
		assertEquals("/test/test.txt", name);
		
		name = MongoFtpFile.getPhysicalName("/test/", "/test/test.txt");
		assertEquals("/test/test.txt", name);
		
		name = MongoFtpFile.getPhysicalName(null, null);
		assertEquals("/", name);
		
		name = MongoFtpFile.getPhysicalName("/test", null);
		assertEquals("/test", name);
		
		name = MongoFtpFile.getPhysicalName("/test/", null);
		assertEquals("/test", name);
		
		name = MongoFtpFile.getPhysicalName("/", "/");
		assertEquals("/", name);
		
		
	}

}
