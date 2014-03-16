package org.ftpgridfs.ftp.mongo;


import javax.annotation.Resource;

import org.apache.ftpserver.ftplet.FileSystemFactory;
import org.apache.ftpserver.ftplet.FileSystemView;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;

public class MongoFileSystemFactory implements FileSystemFactory {



	@Resource
	private GridFsTemplate gridfs = null;
	
	@Resource
	private MongoDbFactory mongoFactory = null;

	private long cacheTimeout = 0; // default to 3 minutes


    /**
     * Create the appropriate user file system view.  This creates a MongoFileSystemView object.  Expects the 
     * GridFsTemplate and MongoDbFactory to be supplied as @Resource.
     */
    public FileSystemView createFileSystemView(User user) throws FtpException {
        synchronized (user) {
           
            MongoFileSystemView fsView = new MongoFileSystemView(user, cacheTimeout);
            fsView.setGridFsTemplate(gridfs);
            fsView.setMongoDbFactory(mongoFactory);
            return fsView;
        }
    }


	/**
	 * The cacheTimeout will be used to control how long to cache directory listing results.
	 * 
	 * @return the cacheTimeout in milliseconds
	 */
	public long getCacheTimeout() {
		return cacheTimeout;
	}


	/**
	 * The cacheTimeout will be used to control how long to cache directory listing results.
	 * 
	 * @param cacheTimeout the cacheTimeout in milliseconds
	 */
	public void setCacheTimeout(long cacheTimeout) {
		this.cacheTimeout = cacheTimeout;
	}
    
   

}
