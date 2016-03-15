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

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static com.xebialabs.overthere.OperatingSystemFamily.UNIX;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SshElevatedUserFileTest {

    public static final String REMOTE_PATH = "/tmp/file.txt";
    private SshSudoConnection connection;

    @BeforeClass
    public void setup() {
        connection = mock(SshSudoConnection.class);
        when(connection.getHostOperatingSystem()).thenReturn(UNIX);
    }

    @Test
    public void shouldNotBeEqualWhenOneIsTempAndAnotherIsNot() {
        SshElevatedUserFile f1 = new SshElevatedUserFile(connection, REMOTE_PATH, false);
        SshElevatedUserFile f2 = new SshElevatedUserFile(connection, REMOTE_PATH, true);
        assertThat(f1, is(not(f2)));
        assertThat(f1.hashCode(), is(not(f2.hashCode())));
    }

    @Test
    public void shouldBeEqual() {
        SshElevatedUserFile f1 = new SshElevatedUserFile(connection, REMOTE_PATH, false);
        SshElevatedUserFile f2 = new SshElevatedUserFile(connection, REMOTE_PATH, false);
        assertThat(f1, is(f2));
        assertThat(f1.hashCode(), is(f2.hashCode()));
    }

}
