package com.xebialabs.overthere.local;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xebialabs.overthere.CmdLine;
import com.xebialabs.overthere.OperatingSystemFamily;
import com.xebialabs.overthere.OverthereProcess;
import com.xebialabs.overthere.RuntimeIOException;

import static java.lang.String.format;

public class LocalProcess implements OverthereProcess {

    private Process p;

    public static LocalProcess fromCommandLine(CmdLine commandLine, OperatingSystemFamily os) {
        return fromCommandLine(commandLine, null, os);
    }

    public static LocalProcess fromCommandLine(CmdLine commandLine, File workingDirectory, OperatingSystemFamily os) {
        try {
            logger.debug("Creating " + os + " process with command line [{}]", commandLine.toCommandLine(os, true));
            final ProcessBuilder pb = new ProcessBuilder(commandLine.toCommandArray(os, false));
            if(workingDirectory != null) {
                logger.debug("Setting working directory to [{}]", workingDirectory);
                pb.directory(workingDirectory);
            } else {
                logger.debug("Not setting working directory");
            }
            logger.debug("Starting process");
            final Process p = pb.start();
            return new LocalProcess(p);
        } catch (IOException exc) {
            throw new RuntimeIOException(format("Cannot start process for [%s]", commandLine), exc);
        }
    }

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

    private static Logger logger = LoggerFactory.getLogger(LocalProcess.class);

}
