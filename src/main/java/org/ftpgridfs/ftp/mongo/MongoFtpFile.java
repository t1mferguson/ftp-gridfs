package org.ftpgridfs.ftp.mongo;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.ftpserver.ftplet.FtpFile;
import org.apache.ftpserver.ftplet.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;

@Configurable
public class MongoFtpFile implements FtpFile {

    private final Logger LOG = LoggerFactory
            .getLogger(MongoFtpFile.class);
	
	private static final String USER = "user";


	private static final String FILENAME = "filename";


	private static final String UPLOADDATE = "uploadDate";
	
	
	private String fileName = null;
	private String currDir = null;
	

	private GridFsTemplate gridFsTemplate = null;
	

	private MongoDbFactory mongoFactory = null;

	
	private GridFsResource resource = null;
	private boolean searched = false;
	
	
	private long dirListingCacheTimeout = 0;
	private List<FtpFile> dirListingCache = null;
	
	
	private boolean listingPerformed = false;
	
	private boolean knownExists = false;
	private long lastListingTime;
	private User user;
	private GridFS gridFs;
	private GridFSDBFile gridFsDbFile;

	/**
	 * Public constructor
	 * 
	 * @param dir
	 * @param fileName
	 * @param knownExists
	 * @param mongoFactory
	 * @param gridFsTemplate
	 * @param cacheTimeout milliseconds to keep directory listing before performing it again.
	 * @param user
	 */
	public MongoFtpFile( String dir, String fileName, boolean knownExists, MongoDbFactory mongoFactory, GridFsTemplate gridFsTemplate,
						long cacheTimeout, User user) {
		if (dir.endsWith("/")) {
			this.currDir = dir;
		}
		else {
			this.currDir = dir + "/";
		}
		
		this.fileName = fileName;
		this.mongoFactory = mongoFactory;
		this.gridFsTemplate = gridFsTemplate;
		this.knownExists = knownExists;
		this.dirListingCacheTimeout = cacheTimeout;
		this.user = user;
	}


	/**
	 * A constructor used internally for creating from a search that has already happened.  The main key here is that the FridFsResource
	 * object does not need to be searched for from the data store, it is being passed in.  Several controlling booleans are then set
	 * in the constructor to control lookup behavior.  This constructor also takes in an object  being passed in that is not a direct child
	 * of the currDir passed in, and then extracts the currDir childs directory this is an ancestor of and creates the instance around that
	 * immedidate child of the currDir.  For instance currDir is '/', the objects name is '/foo/bar.txt'.  This instance will be for 'foo', not
	 * 'bar.txt'.
	 * 
	 * @param currDir
	 * @param gridFsResource
	 * @param mongoFactory
	 * @param gridFsTemplate
	 * @param cacheTimeout milliseconds to keep directory listing before performing it again.
	 * @param user
	 */
    private MongoFtpFile(String currDir, GridFsResource gridFsResource,
			MongoDbFactory mongoFactory, GridFsTemplate gridFsTemplate,
			long cacheTimeout,
			User user) {
		this.resource = gridFsResource;
		this.mongoFactory = mongoFactory;
		this.gridFsTemplate = gridFsTemplate;
		this.knownExists = true;
		this.searched = true;
		this.currDir = currDir;
		this.fileName = gridFsResource.getFilename();
		fileName = fileName.replaceFirst(currDir + "/", "");
		int slash = fileName.indexOf('/');
		int filelen = fileName.length();
		if ((slash != -1) && (slash != filelen - 1)) {
			fileName = fileName.substring(0, slash +1);
			
	    }
		this.dirListingCacheTimeout  = cacheTimeout;
		this.user = user;
	}



	/**
     * Get full name.
     */
    @Override
    public String getAbsolutePath() {

    	if ("/".equals(fileName)) {
    		return "/";
    	}
        // strip the last '/' if necessary
        String fullName = getPhysicalName(currDir, fileName);
        int filelen = fullName.length();
        if ((filelen != 1) && (fullName.charAt(filelen - 1) == '/')) {
            fullName = fullName.substring(0, filelen - 1);
        }

        return fullName;
    }

	/* (non-Javadoc)
	 * @see org.apache.ftpserver.ftplet.FtpFile#getName()
	 */
    @Override
    public String getName() {

        // root - the short name will be '/'
        if (fileName.equals("/")) {
            return "/";
        }

        // strip the last '/' and before the 
        String shortName = fileName;
        int filelen = fileName.length();
        
        if (shortName.charAt(filelen - 1) == '/') {
            shortName = shortName.substring(0, filelen - 1);
        }
        // return from the last '/'
        int slashIndex = shortName.lastIndexOf('/');
        if (slashIndex != -1) {
            shortName = shortName.substring(slashIndex + 1);
        }
        return shortName;
    }
	
	/* (non-Javadoc)
	 * @see org.apache.ftpserver.ftplet.FtpFile#isHidden()
	 */
    @Override
	public boolean isHidden() {
		
		
		return false;
	}

	/* (non-Javadoc)
	 * @see org.apache.ftpserver.ftplet.FtpFile#isDirectory()
	 */
    @Override
	public boolean isDirectory() {

		if ("/".equals(this.fileName)) {
			return true;
		}
		int filelen = fileName.length();
		if ((filelen != 1) && (fileName.charAt(filelen - 1) == '/')) {
	            return true;
	    }
	    this.findFileResource();

		if (resource != null) {
			return false;
		}
		else {			
			this.listFiles();
			// Look for files that start with this as a directory structure to say that it does exist
			if (dirListingCache.size() > 0) {
				return true;
			}
		}

		return false;
	}

	/* (non-Javadoc)
	 * @see org.apache.ftpserver.ftplet.FtpFile#isFile()
	 */
    @Override
	public boolean isFile() {
		if ("/".equals(fileName)) {
			return false;
		}
		int filelen = fileName.length();
        if ((filelen != 1) && (fileName.charAt(filelen - 1) == '/')) {
            return false;
        }
	    this.findFileResource();

		if (resource != null) {
			return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.apache.ftpserver.ftplet.FtpFile#doesExist()
	 */
    @Override
	public boolean doesExist() {
		if ("/".equals(this.fileName)) {
			return true;
		}
		if (knownExists) {
			return true;
		}
		else {
			this.findFileResource();
			if (knownExists) {
				return true;
			}
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.apache.ftpserver.ftplet.FtpFile#isReadable()
	 */
    @Override
	public boolean isReadable() {
		return this.gridFsTemplate != null;		
	}

	/* (non-Javadoc)
	 * @see org.apache.ftpserver.ftplet.FtpFile#isWritable()
	 */
    @Override
	public boolean isWritable() {
    	return this.gridFsTemplate != null;	
	}

	/* (non-Javadoc)
	 * @see org.apache.ftpserver.ftplet.FtpFile#isRemovable()
	 */
    @Override
	public boolean isRemovable() {
    	return this.gridFsTemplate != null;	
	}

	/* (non-Javadoc)
	 * @see org.apache.ftpserver.ftplet.FtpFile#getOwnerName()
	 */
    @Override
	public String getOwnerName() {
    	
    	getGridFsDbFile();
    	DBObject md = gridFsDbFile.getMetaData();
    	if(md != null) {
    		if (md.containsField(USER)) {
    			return md.get(USER).toString();
    		}
    	}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.apache.ftpserver.ftplet.FtpFile#getGroupName()
	 */
    @Override
	public String getGroupName() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.apache.ftpserver.ftplet.FtpFile#getLinkCount()
	 */
    @Override
	public int getLinkCount() {
		// TODO Auto-generated method stub
		return 1;
	}

	/* (non-Javadoc)
	 * @see org.apache.ftpserver.ftplet.FtpFile#getLastModified()
	 */
    @Override
	public long getLastModified() {
		this.findFileResource();
		if (resource != null) {
			try {
				return resource.lastModified();
			} catch (IOException e) {
				LOG.error("Unable to get the last modified date", e);
			}
		}
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.apache.ftpserver.ftplet.FtpFile#setLastModified()
	 */
    @Override
	public boolean setLastModified(long time) {
    	getGridFsDbFile();
    	gridFsDbFile.put(UPLOADDATE, new Date(time));

    	gridFsDbFile.save();
		return true;
	}

	/* (non-Javadoc)
	 * @see org.apache.ftpserver.ftplet.FtpFile#getSize()
	 */
    @Override
	public long getSize() {
		this.findFileResource();
		if (resource != null) { 
			try {
				return resource.contentLength();
			} catch (IOException e) {
				LOG.error("Unable to get the size", e);
			}
		}
		return 1;
	}

	/* (non-Javadoc)
	 * @see org.apache.ftpserver.ftplet.FtpFile#mkdir()
	 */
    @Override
	public boolean mkdir() {
		// TODO Not sure whether it makes sense to do anything as there is no real concept of directories
    	// in the mongo data store.
		return true;
	}

	/* (non-Javadoc)
	 * @see org.apache.ftpserver.ftplet.FtpFile#delete()
	 */
    @Override
	public boolean delete() {
    	Query q = createQuery();
    	gridFsTemplate.delete(q);
    	searched = false;
    	this.findFileResource();
		return resource == null;
	}


	
	/* (non-Javadoc)
	 * @see org.apache.ftpserver.ftplet.FtpFile#move()
	 */
    @Override
	public boolean move(FtpFile destination) {

    	getGridFsDbFile();
    	gridFsDbFile.put(FILENAME, destination.getAbsolutePath());
    	gridFsDbFile.save();

		return true;
	}


	private void getGridFsDbFile() {
		if (gridFsDbFile == null) {
	    	this.getGridFs();
			gridFsDbFile = this.gridFs.findOne(getPhysicalName(currDir, fileName));
		}
	}

	/* (non-Javadoc)
	 * @see org.apache.ftpserver.ftplet.FtpFile#listFiles()
	 */
    @Override
	public List<FtpFile> listFiles() {
    	
		long now = new Date().getTime();
		boolean cacheTimedOut = lastListingTime - now < dirListingCacheTimeout; 
		if (!listingPerformed || cacheTimedOut) {
			
			lastListingTime = new Date().getTime();
			GridFsResource[] listing = this.listFilesByDir();
			setFilesList(listing);
			listingPerformed = true;
		}
		return this.dirListingCache;
    	
//		Map<String, FtpFile> filesMap = new HashMap<>();
//		
//		GridFsResource[] resources = this.listFilesByDir();
//		for (GridFsResource res : resources) {
//			MongoFtpFile mf = new MongoFtpFile(currDir, res, mongoFactory, gridFsTemplate, dirListingCacheTimeout, user);
//			
//			filesMap.put(mf.getName(), mf);
//		}
//		List<FtpFile> files = new ArrayList<>();
//		files.addAll(filesMap.values());
//		return files;
	}

    private void setFilesList(GridFsResource[] listing) {
    	
		Map<String, FtpFile> filesMap = new HashMap<>();
    	
		if (listing != null && listing.length > 0) {
			String currDir = getPhysicalName(this.currDir, fileName);
			Set<FtpFile> set = new HashSet<>();
			for (GridFsResource resource : listing) {
				String resName = resource.getFilename();
				String dir = currDir;
				if (dir.endsWith("/") != true) {
					dir += "/";
				}
				resName = resName.replaceFirst(dir, "");
				int slash = resName.indexOf('/');
				int filelen = resName.length();

				if ((slash != -1) && (slash != filelen - 1)) {
					resName = resName.substring(0, slash +1);

					if (filesMap.containsKey(resName) != true) {
						MongoFtpFile mf = new MongoFtpFile(currDir, resName, true, mongoFactory, gridFsTemplate, dirListingCacheTimeout, user);					
						filesMap.put(mf.getName(), mf);
					}
				}
				else {
					MongoFtpFile mf = new MongoFtpFile(currDir, resource, mongoFactory, gridFsTemplate, dirListingCacheTimeout, user);					
					filesMap.put(mf.getName(), mf);
				}
			}
			
		}
		dirListingCache = new ArrayList<>();
		dirListingCache.addAll(filesMap.values());
	}
	
	/* (non-Javadoc)
	 * @see org.apache.ftpserver.ftplet.FtpFile#createOutputStream()
	 */
    @Override
	public OutputStream createOutputStream(long offset) throws IOException {
		
    	this.getGridFs();
    	
		GridFSInputFile inputFile = gridFs.createFile(getPhysicalName(currDir, fileName));
		
		if (user != null) {
			DBObject md = new BasicDBObject();
			md.put(USER, user.getName());
			inputFile.setMetaData(md);
			inputFile.save();
		}
		
		searched = false;
		return inputFile.getOutputStream();

	}

    private void getGridFs() {
    	if(this.gridFs == null) {
    		this.gridFs = new GridFS(mongoFactory.getDb());
    	}
		
	}

	/* (non-Javadoc)
	 * @see org.apache.ftpserver.ftplet.FtpFile#createInputStream()
	 */
	@Override
	public InputStream createInputStream(long offset) throws IOException {
		GridFsResource result = findFileResource();
		if (result == null) {
			throw new IOException("Unable to locate " + this.getAbsolutePath());
		}
		
		return result.getInputStream();
	}

    
	private GridFsResource findFileResource() {
		if (!searched) {
		   	getGridFsDbFile();
			resource = gridFsTemplate.getResource(getPhysicalName(currDir, fileName));
			searched = true;
			if (resource != null) {
				knownExists = true;
			}
		}
		
		return resource;
	}

	/**
	 * Actually performs the search in mongo for all files starting with name physical name
	 * of this object.  All ancestors will be listed in the tree under this directory, this search needs
	 * to be updated to return just direct ancestors
	 * 
	 * @return
	 */
	private GridFsResource[] listFilesByDir() {

		String resourceName = getPhysicalName(currDir, fileName);
		if ("/".equals(resourceName)) {
			resourceName = "/*";
		}
		else {
			resourceName += "/*";
		}
		
		return gridFsTemplate.getResources(resourceName);			

	}
	

	private Query createQuery() {
		Query q = new Query().addCriteria(Criteria.where(FILENAME).is(getPhysicalName(currDir, fileName)));
		return q;
	}


	/**
     * Get the physical canonical file name. It is modeled after the NativeFtpFile.getPhysicalName, but not
     * exactly the same.
     * 
     * @param currDir
     *            The current directory. It will always be with respect to the
     *            root directory.
     * @param fileName
     *            The input file name.
     * @return The return string will always begin with the root directory. It
     *         will never be null.
     */
    public final static String getPhysicalName(String currDir, String fileName) {

        String resArg;

        if (fileName == null) {
        	fileName = "";
        }
        if (fileName.length() == 0 || fileName.charAt(0) != '/') {
            if (currDir == null) {
                currDir = "/";
            }
            if (currDir.length() == 0) {
                currDir = "/";
            }


            if (currDir.charAt(0) != '/') {
            	currDir = '/' + currDir;
            }
            if (currDir.charAt(currDir.length() - 1) != '/') {
            	currDir += '/';
            }

            resArg = currDir;
        } else {
            resArg = "/";
        }

        // strip last '/'
        if (resArg.charAt(resArg.length() - 1) == '/') {
            resArg = resArg.substring(0, resArg.length() - 1);
        }

        	// replace ., ~ and ..
        	// in this loop resArg will never end with '/'
	        StringTokenizer st = new StringTokenizer(fileName, "/");
	        while (st.hasMoreTokens()) {
	            String tok = st.nextToken();
	
	            // . => current directory
	            if (tok.equals(".")) {
	                continue;
	            }
	
	            // .. => parent directory (if not root)
	            if (tok.equals("..")) {
	                if (resArg.startsWith("/")) {
	                    int slashIndex = resArg.lastIndexOf('/');
	                    if (slashIndex != -1) {
	                        resArg = resArg.substring(0, slashIndex);
	                    }
	                }
	                continue;
	            }
	
	            // ~ => home directory (in this case the root directory)
	            if (tok.equals("~")) {
	                resArg = "";
	                continue;
	            }
	
	            resArg = resArg + '/' + tok;
	        }
   
        // add last slash if necessary
        if ((resArg.length()) + 1 == "/".length()) {
            resArg += '/';
        }

        // final check
        if (!resArg.regionMatches(0, "/", 0, "/"
                .length())) {
            resArg = "/";
        }

        return resArg;
    }
	
}
