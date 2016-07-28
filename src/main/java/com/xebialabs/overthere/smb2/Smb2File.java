package com.xebialabs.overthere.smb2;

import com.hierynomus.msdtyp.AccessMask;
import com.hierynomus.smbj.share.Directory;
import com.hierynomus.smbj.share.File;
import com.xebialabs.overthere.OverthereFile;
import com.xebialabs.overthere.spi.BaseOverthereFile;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;

public class Smb2File extends BaseOverthereFile<Smb2Connection> {
    private final String hostPath;
    private File file;
    private Directory directory;

    public Smb2File(Smb2Connection connection, String hostPath) {
        super(connection);
        this.hostPath = hostPath;
    }

    @Override
    public String getPath() {
        return hostPath;
    }

    @Override
    public String getName() {
        int i = hostPath.lastIndexOf('\\');
        return hostPath.substring(i);
    }

    @Override
    public OverthereFile getParentFile() {
        return null;
    }

    @Override
    public boolean exists() {
        return isFile() || isDirectory();
    }

    @Override
    public boolean canRead() {
        return checkAccessMask(AccessMask.GENERIC_READ);
    }

    private boolean checkAccessMask(AccessMask mask) {
        long accessMask = connection.getShare().getFileInformation(hostPath).getAccessMask();
        return AccessMask.EnumUtils.isSet(accessMask, mask);
    }

    @Override
    public boolean canWrite() {
        return checkAccessMask(AccessMask.GENERIC_WRITE);
    }

    @Override
    public boolean canExecute() {
        return checkAccessMask(AccessMask.GENERIC_EXECUTE);
    }

    @Override
    public boolean isFile() {
        return connection.getShare().fileExists(hostPath);
    }

    @Override
    public boolean isDirectory() {
        return connection.getShare().folderExists(hostPath);
    }

    @Override
    public boolean isHidden() {
        return false;
    }

    @Override
    public long lastModified() {
        return 0;
    }

    @Override
    public long length() {
        return 0;
    }

    @Override
    public InputStream getInputStream() {
        return null;
    }

    @Override
    public OutputStream getOutputStream() {
        return null;
    }

    @Override
    public void setExecutable(boolean executable) {

    }

    @Override
    public void delete() {

    }

    @Override
    public List<OverthereFile> listFiles() {
        return Collections.emptyList();
    }

    @Override
    public void mkdir() {

    }

    @Override
    public void mkdirs() {

    }

    @Override
    public void renameTo(OverthereFile dest) {

    }

    @Override
    public String toString() {
        return null;
    }
}
