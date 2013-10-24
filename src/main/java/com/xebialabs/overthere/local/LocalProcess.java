package com.xebialabs.overthere.local;

import java.io.InputStream;
import java.io.OutputStream;

import com.xebialabs.overthere.OverthereProcess;

public class LocalProcess implements OverthereProcess {

    private Process p;

    public LocalProcess(Process p) {
        this.p = p;
    }

    @Override
    public OutputStream getStdin() {
        return p.getOutputStream();
    }

    @Override
    public InputStream getStdout() {
        return p.getInputStream();
    }

    @Override
    public InputStream getStderr() {
        return p.getErrorStream();
    }

    @Override
    public int waitFor() throws InterruptedException {
        return p.waitFor();
    }

    @Override
    public void destroy() {
        p.destroy();
    }

    @Override
    public int exitValue() {
        return p.exitValue();
    }

}
