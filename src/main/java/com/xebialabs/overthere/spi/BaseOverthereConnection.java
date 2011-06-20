package com.xebialabs.overthere.spi;

import static com.xebialabs.overthere.ConnectionOptions.TEMPORARY_DIRECTORY_DELETE_ON_DISCONNECT;
import static com.xebialabs.overthere.ConnectionOptions.TEMPORARY_DIRECTORY_DELETE_ON_DISCONNECT_DEFAULT;
import static com.xebialabs.overthere.ConnectionOptions.TEMPORARY_DIRECTORY_PATH;
import static com.xebialabs.overthere.ConnectionOptions.TEMPORARY_FILE_CREATION_RETRIES;
import static com.xebialabs.overthere.ConnectionOptions.TEMPORARY_FILE_CREATION_RETRIES_DEFAULT;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.OverthereConnection;
import com.xebialabs.overthere.OverthereFile;
import com.xebialabs.overthere.RuntimeIOException;

/**
 * Base class for implementations of {@link OverthereConnection}.
 */
public abstract class BaseOverthereConnection extends OverthereConnection {

	protected String temporaryDirectoryPath;

	protected OverthereFile connectionTemporaryDirectory;

	protected boolean deleteTemporaryDirectoryOnDisconnect;
	
	protected int temporaryFileCreationRetries;

	protected BaseOverthereConnection(String type, ConnectionOptions options) {
	    super(type, options);
		this.temporaryDirectoryPath = options.get(TEMPORARY_DIRECTORY_PATH, os.getDefaultTemporaryDirectoryPath());
		this.deleteTemporaryDirectoryOnDisconnect = options.get(TEMPORARY_DIRECTORY_DELETE_ON_DISCONNECT, TEMPORARY_DIRECTORY_DELETE_ON_DISCONNECT_DEFAULT);
		this.temporaryFileCreationRetries = options.get(TEMPORARY_FILE_CREATION_RETRIES, TEMPORARY_FILE_CREATION_RETRIES_DEFAULT);
    }

	/**
	 * Closes the connection. Depending on the {@link ConnectionOptions#TEMPORARY_DIRECTORY_DELETE_ON_DISCONNECT} connection option, deletes all temporary files
	 * that have been created on the host.
	 */
	@Override
	public final void disconnect() {
		if (deleteTemporaryDirectoryOnDisconnect) {
			deleteConnectionTemporaryDirectory();
		}

		doDisconnect();

		logger.info("Disconnected from {}", this);
	}

	/**
	 * To be overridden by a base class to implement connection specific disconnection logic.
	 */
	protected abstract void doDisconnect();


	protected synchronized OverthereFile getConnectionTemporaryDirectory() throws RuntimeIOException {
		if (connectionTemporaryDirectory == null) {
			connectionTemporaryDirectory = createConnectionTemporaryDirectory();
		}
		return connectionTemporaryDirectory;
	}

	protected OverthereFile createConnectionTemporaryDirectory() {
	    OverthereFile temporaryDirectory = getFile(temporaryDirectoryPath);
	    Random r = new Random();
	    DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmssSSS");
	    String prefix = "deployit-" + dateFormat.format(new Date());
	    String infix = "";
	    String suffix = ".tmp";
	    for (int i = 0; i < temporaryFileCreationRetries; i++) {
	    	OverthereFile tempDir = getFileForTempFile(temporaryDirectory, prefix + infix + suffix);
	    	if(!tempDir.exists()) {
	    		tempDir.mkdir();
	    		logger.info("Created connection temporary directory {}", tempDir);
	    		return tempDir;
	    	}
	    	infix = "-" + Long.toString(Math.abs(r.nextLong()));
	    }
	    throw new RuntimeIOException("Cannot create connection temporary directory on " + this);
    }

	protected void deleteConnectionTemporaryDirectory() {
		if (connectionTemporaryDirectory != null) {
			try {
				logger.info("Deleting connection temporary directory {}", connectionTemporaryDirectory);
				connectionTemporaryDirectory.deleteRecursively();
			} catch (RuntimeException exc) {
				logger.warn("Got exception while deleting connection temporary directory {}. Ignoring it.", connectionTemporaryDirectory, exc);
			}
		}
	}

	/**
	 * Default implementation of {@link OverthereConnection#getTempFile(String)} that creates a file in the connection temporary directory.
	 */
	@Override
    public OverthereFile getTempFile(String prefix, String suffix) throws RuntimeIOException {
        if (prefix == null)
            throw new NullPointerException("prefix is null");

        if (suffix == null) {
            suffix = ".tmp";
        }

        Random r = new Random();
        String infix = "";
        for (int i = 0; i < temporaryFileCreationRetries; i++) {
            OverthereFile f = getFileForTempFile(getConnectionTemporaryDirectory(), prefix + infix + suffix);
            if (!f.exists()) {
            	logger.debug("Created temporary file {}", f);
                return f;
            }
            infix = "-" + Long.toString(Math.abs(r.nextLong()));
        }
        throw new RuntimeIOException("Cannot generate a unique temporary file name on " + this);
    }

	/**
	 * Invoked by {@link #getTempFile(String)} to create an {@link OverthereFile} object for a file or directory in the system or connection temporary directory.
	 * 
	 * @param parent
	 *            parent of the file to create
	 * @param name
	 *            name of the file to create.
	 * @return the created file object
	 */
    protected abstract OverthereFile getFileForTempFile(OverthereFile parent, String name);

    private static Logger logger = LoggerFactory.getLogger(BaseOverthereConnection.class);

}
