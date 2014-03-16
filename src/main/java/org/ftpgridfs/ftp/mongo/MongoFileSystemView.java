package org.ftpgridfs.ftp.mongo;


import org.apache.ftpserver.ftplet.FileSystemView;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.FtpFile;
import org.apache.ftpserver.ftplet.User;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;




public class MongoFileSystemView implements FileSystemView {


	
	
	String currDir;
	private GridFsTemplate gridFsTemplate;
	private MongoDbFactory mongoFactory;
	private User user;
	private long cacheTimeout;
	
	public MongoFileSystemView(User user, long cacheTimeout) {
		
		this.user = user;
		this.cacheTimeout = cacheTimeout;
		currDir = "/";
	}
	
	/* (non-Javadoc)
	 * @see org.apache.ftpserver.ftplet.FtpFileSystemView#getHomeDirectory()
	 */
    @Override
	public FtpFile getHomeDirectory() throws FtpException {
		return new MongoFtpFile("/", user.getHomeDirectory(), false, mongoFactory, gridFsTemplate, cacheTimeout, user);
	}

    /* (non-Javadoc)
	 * @see org.apache.ftpserver.ftplet.FtpFileSystemView#getWorkingDirectory()
	 */
    @Override
	public FtpFile getWorkingDirectory() throws FtpException {
		return new MongoFtpFile(currDir, null,false, mongoFactory, gridFsTemplate, cacheTimeout, user);
	}

    /* (non-Javadoc)
	 * @see org.apache.ftpserver.ftplet.FtpFileSystemView#getFile()
	 */
    @Override
    public FtpFile getFile(String file) {

        
        return new MongoFtpFile( currDir, file,false, mongoFactory, gridFsTemplate, cacheTimeout, user);
    }

    /* (non-Javadoc)
	 * @see org.apache.ftpserver.ftplet.FtpFileSystemView#changeWorkingDirectory()
	 */
    @Override
    public boolean changeWorkingDirectory(String dir) {

        // not a directory - return false
        dir = MongoFtpFile.getPhysicalName(currDir, dir);
               
        if (dir.charAt(dir.length() - 1) != '/') {
            dir = dir + '/';
        }

        currDir = dir;
        return true;
    }

    /* (non-Javadoc)
	 * @see org.apache.ftpserver.ftplet.FtpFileSystemView#isRandomAccessible()
	 */
    @Override
	public boolean isRandomAccessible() throws FtpException {
		
		return false;
	}

    /* (non-Javadoc)
	 * @see org.apache.ftpserver.ftplet.FtpFileSystemView#dispose()
	 */
    @Override
	public void dispose() {

	}


	public void setGridFsTemplate(GridFsTemplate gridfs) {
		this.gridFsTemplate = gridfs;		
	}


	public void setMongoDbFactory(MongoDbFactory mongoFactory) {
		this.mongoFactory = mongoFactory;
		
	}

}
