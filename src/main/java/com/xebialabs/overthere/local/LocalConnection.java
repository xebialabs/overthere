/**
 * Copyright (c) 2008-2016, XebiaLabs B.V., All rights reserved.
 *
 *
 * Overthere is licensed under the terms of the GPLv2
 * <http://www.gnu.org/licenses/old-licenses/gpl-2.0.html>, like most XebiaLabs Libraries.
 * There are special exceptions to the terms and conditions of the GPLv2 as it is applied to
 * this software, see the FLOSS License Exception
 * <http://github.com/xebialabs/overthere/blob/master/LICENSE>.
 *
 * This program is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation; version 2
 * of the License.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin St, Fifth
 * Floor, Boston, MA 02110-1301  USA
 */
package com.xebialabs.overthere.local;

import com.xebialabs.overthere.*;
import com.xebialabs.overthere.spi.AddressPortMapper;
import com.xebialabs.overthere.spi.BaseOverthereConnection;
import com.xebialabs.overthere.spi.OverthereConnectionBuilder;
import com.xebialabs.overthere.spi.Protocol;
import com.xebialabs.overthere.util.DefaultAddressPortMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

import static com.xebialabs.overthere.ConnectionOptions.OPERATING_SYSTEM;
import static com.xebialabs.overthere.ConnectionOptions.TEMPORARY_DIRECTORY_PATH;
import static com.xebialabs.overthere.OperatingSystemFamily.WINDOWS;
import static com.xebialabs.overthere.OperatingSystemFamily.getLocalHostOperatingSystemFamily;
import static com.xebialabs.overthere.local.LocalConnection.LOCAL_PROTOCOL;
import static com.xebialabs.overthere.util.OverthereUtils.*;
import static com.xebialabs.overthere.util.WindowsCommandLineArgsSanitizer.containsAnySpecialChars;
import static java.lang.String.format;

/**
 * A connection to the local host.
 */
@Protocol(name = LOCAL_PROTOCOL)
public class LocalConnection extends BaseOverthereConnection implements OverthereConnectionBuilder {

    /**
     * Name of the protocol handled by this connection builder, i.e. "local".
     */
    public static final String LOCAL_PROTOCOL = "local";

    /**
     * Constructs a connection to the local host.
     */
    public LocalConnection(String protocol, ConnectionOptions options, AddressPortMapper mapper) {
        super(protocol, fixOptions(options), mapper, true);
    }

    /**
     * Constructs a connection to the local host.
     */
    public LocalConnection(String protocol, ConnectionOptions options) {
        this(protocol, options, new DefaultAddressPortMapper());
    }

    private static ConnectionOptions fixOptions(ConnectionOptions options) {
        options = new ConnectionOptions(options);
        options.set(OPERATING_SYSTEM, getLocalHostOperatingSystemFamily());
        if (options.getOptional(TEMPORARY_DIRECTORY_PATH) == null) {
            options.set(TEMPORARY_DIRECTORY_PATH, System.getProperty("java.io.tmpdir"));
        }
        return options;
    }

    @Override
    public OverthereConnection connect() {
        connected();
        return this;
    }

    @Override
    public void doClose() {
        // no-op
    }

    @Override
    protected void logDisconnect() {
        logger.debug("Disconnected from: {}", this);
    }

    @Override
    public OverthereFile getFile(String path) throws RuntimeIOException {
        return new LocalFile(this, new File(path));
    }

    @Override
    public OverthereFile getFile(OverthereFile parent, String child) throws RuntimeIOException {
        if (!(parent instanceof LocalFile)) {
            throw new IllegalStateException("parent is not a LocalOverthereFile");
        }

        File childFile = new File(((LocalFile) parent).getFile(), child);
        return new LocalFile(this, childFile);
    }

    @Override
    protected OverthereFile getFileForTempFile(OverthereFile parent, String name) {
        return getFile(parent, name);
    }

    @Override
    public OverthereProcess startProcess(CmdLine cmd) {
        checkNotNull(cmd, "Cannot execute null command line");
        checkArgument(cmd.getArguments().size() > 0, "Cannot execute empty command line");

        final String obfuscatedCmd = cmd.toCommandLine(os, true);
        logger.info("Starting command [{}] on [{}]", obfuscatedCmd, this);

        File wd = null;
        if (workingDirectory != null) {
            wd = ((LocalFile) workingDirectory).getFile();
        }

        try {
            logger.debug("Creating " + os + " process with command line [{}]", obfuscatedCmd);
            CmdLine command = isWindows(os) ? getCmdForWindows(cmd) : cmd;
            final ProcessBuilder pb = new ProcessBuilder(command.toCommandArray(os, false));
            if (wd != null) {
                logger.debug("Setting working directory to [{}]", wd);
                pb.directory(wd);
            } else {
                logger.debug("Not setting working directory");
            }

            logger.debug("Starting process");
            final Process p = pb.start();
            return new LocalProcess(p);
        } catch (IOException exc) {
            throw new RuntimeIOException(format("Cannot start command [%s] on [%s]", obfuscatedCmd, this), exc);
        }
    }

    @Override
    public String toString() {
        return LOCAL_PROTOCOL + ":";
    }

    /**
     * Override this from {@link com.xebialabs.overthere.spi.BaseOverthereConnection#finalize()}, because the LocalConnection needn't be closed, so it should not log messages.
     * @throws Throwable
     */
    @Override
    protected void finalize() throws Throwable {
        closeQuietly(this);
        super.finalize();
    }

    /**
     * Creates a connection to the local host.
     */
    public static OverthereConnection getLocalConnection() {
        return Overthere.getConnection(LOCAL_PROTOCOL, new ConnectionOptions());
    }

    private boolean isWindows(final OperatingSystemFamily os) {
        return os == OperatingSystemFamily.WINDOWS;
    }

    private CmdLine getCmdForWindows(final CmdLine cmd) {
        String command = cmd.toCommandLine(WINDOWS, false);
        CmdLine c = CmdLine.build("cmd", "/s", "/c");
        if (containsAnySpecialChars(command)) {
            return c.addArgument("\"" + command + "\"");
        }
        return c.add(cmd.getArguments());
    }

    private static final Logger logger = LoggerFactory.getLogger(LocalConnection.class);

}
