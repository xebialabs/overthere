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
package com.xebialabs.overthere.ssh;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static com.xebialabs.overthere.ssh.SshConnectionBuilder.SUDO_PASSWORD_PROMPT_REGEX_DEFAULT;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

public class SshElevatedPasswordHandlingStreamTest {

    @Mock
    private OutputStream os;

    @BeforeMethod
    public void init() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void shouldSendPasswordOnMatchingOutput() throws IOException {
        InputStream is = new ByteArrayInputStream("[sudo] password for user bar:".getBytes());
        SshElevatedPasswordHandlingStream foo = new SshElevatedPasswordHandlingStream(is, os, "foo", SUDO_PASSWORD_PROMPT_REGEX_DEFAULT);
        readStream(foo);
        verify(os).write("foo\r\n".getBytes());
        verify(os).flush();
    }

    @Test
    public void shouldSendPasswordOnMatchingOutputNotOnFirstLine() throws IOException {
        InputStream is = new ByteArrayInputStream(("We trust you have received the usual lecture from the local System\r\n" +
                "Administrator. It usually boils down to these three thin\r\n" +
                "#1) Respect the privacy of others.\r\n" +
                "#2) Think before you type.\r\n" +
                "#3) With great power comes great responsibility.\r\n" +
                "\r\n" +
                "[sudo] password for user bar:").getBytes());
        SshElevatedPasswordHandlingStream foo = new SshElevatedPasswordHandlingStream(is, os, "foo", SUDO_PASSWORD_PROMPT_REGEX_DEFAULT);
        readStream(foo);
        verify(os).write("foo\r\n".getBytes());
        verify(os).flush();
    }

    @Test
    public void shouldNotSendPasswordWhenRegexDoesntMatch() throws IOException {
        InputStream is = new ByteArrayInputStream("Password:".getBytes());
        SshElevatedPasswordHandlingStream foo = new SshElevatedPasswordHandlingStream(is, os, "foo", ".*[Pp]assword.*>");
        readStream(foo);
        verifyZeroInteractions(os);
    }

    private static void readStream(SshElevatedPasswordHandlingStream foo) throws IOException {
        while (foo.available() > 0) {
            foo.read();
        }
    }

}
