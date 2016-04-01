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

import java.io.*;

import org.testng.annotations.Test;

import com.xebialabs.overthere.CmdLine;
import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.itest.OverthereConnectionItestBase;
import com.xebialabs.overthere.OverthereFile;
import com.xebialabs.overthere.util.CapturingOverthereExecutionOutputHandler;
import com.xebialabs.overthere.util.OverthereUtils;

import static com.xebialabs.overthere.ConnectionOptions.TEMPORARY_DIRECTORY_PATH;
import static com.xebialabs.overthere.OperatingSystemFamily.UNIX;
import static com.xebialabs.overthere.local.LocalConnection.LOCAL_PROTOCOL;
import static com.xebialabs.overthere.util.CapturingOverthereExecutionOutputHandler.capturingHandler;
import static com.xebialabs.overthere.util.ConsoleOverthereExecutionOutputHandler.syserrHandler;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * This is not an itest, because this can always run.
 */
public class LocalConnectionTest extends OverthereConnectionItestBase {

    @Override
    protected String getProtocol() {
        return LOCAL_PROTOCOL;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected ConnectionOptions getOptions() {
        ConnectionOptions options = new ConnectionOptions();
        options.set(TEMPORARY_DIRECTORY_PATH, temp.getRoot().getPath());
        return options;
    }

    @Override
    protected String getExpectedConnectionClassName() {
        return LocalConnection.class.getName();
    }

    @Test
    public void isDirectoryWorks() {
        OverthereFile tempFile = connection.getTempFile("tmpDir");
        tempFile.mkdir();
        assertThat("expected temp is a dir", tempFile.isDirectory(), equalTo(true));
    }

    @Test
    public void canExecuteCommand() {
        OverthereFile tempFile = connection.getTempFile("afile");
        OverthereUtils.write("Some text", "UTF-8", tempFile);
        String lsCommand = connection.getHostOperatingSystem() == UNIX ? "ls" : "dir/b";
        CmdLine commandLine = CmdLine.build(lsCommand, tempFile.getParentFile().getPath());
        CapturingOverthereExecutionOutputHandler handler = capturingHandler();

        int res = connection.execute(handler, syserrHandler(), commandLine);
        assertThat(res, equalTo(0));
        assertThat(handler.getOutputLines().contains(tempFile.getName()), equalTo(true));
    }

    @Test
    public void localFileShouldBeSerializable() throws IOException, ClassNotFoundException {
        OverthereFile tempFile = connection.getTempFile("afile");

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ObjectOutputStream objectsOut = new ObjectOutputStream(bytes);
        objectsOut.writeObject(tempFile);

        ObjectInputStream objectsIn = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()));
        Object read = objectsIn.readObject();
        assertThat(read, instanceOf(LocalFile.class));
        assertThat(((LocalFile) read).getPath(), equalTo(tempFile.getPath()));
    }

    @Test
    public void deleteTemporaryFilesWhenClosed() throws IOException {
        OverthereFile tempFile = connection.getTempFile("someFile");
        tempFile.mkdirs();
        assertThat(new File(tempFile.getPath()).exists(), equalTo(true));
        connection.close();
        assertThat(new File(tempFile.getPath()).exists(), equalTo(false));
    }

}
