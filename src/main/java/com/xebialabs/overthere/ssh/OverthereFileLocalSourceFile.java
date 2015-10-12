package com.xebialabs.overthere.ssh;

import com.xebialabs.overthere.OverthereFile;
import net.schmizz.sshj.xfer.LocalFileFilter;
import net.schmizz.sshj.xfer.LocalSourceFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Wrapper for the LocalSourceFile supplied by SSHJ.
 */
class OverthereFileLocalSourceFile implements LocalSourceFile {

    private OverthereFile f;

    public OverthereFileLocalSourceFile(OverthereFile f) {
        this.f = f;
    }

    @Override
    public String getName() {
        return f.getName();
    }

    @Override
    public long getLength() {
        return f.length();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return f.getInputStream();
    }

    @Override
    public int getPermissions() throws IOException {
        return f.isDirectory() ? 0755 : 0644;
    }

    @Override
    public boolean isFile() {
        return f.isFile();
    }

    @Override
    public boolean isDirectory() {
        return f.isDirectory();
    }

    @Override
    public Iterable<? extends LocalSourceFile> getChildren(LocalFileFilter filter) throws IOException {
        List<LocalSourceFile> files = new ArrayList<LocalSourceFile>();
        for (OverthereFile each : f.listFiles()) {
            files.add(new OverthereFileLocalSourceFile(each));
        }
        return files;
    }

    @Override
    public boolean providesAtimeMtime() {
        return false;
    }

    @Override
    public long getLastAccessTime() throws IOException {
        return 0;
    }

    @Override
    public long getLastModifiedTime() throws IOException {
        return 0;
    }

}
