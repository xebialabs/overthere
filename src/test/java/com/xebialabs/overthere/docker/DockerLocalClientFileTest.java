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
package com.xebialabs.overthere.docker;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.xebialabs.overthere.OperatingSystemFamily;

import static com.google.common.collect.Lists.newArrayList;
import static com.xebialabs.overthere.OperatingSystemFamily.UNIX;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.mockito.Mockito.mock;

public class DockerLocalClientFileTest {

    public static String[] parts(String... strings) {
        return strings;
    }

    private DockerLocalClientConnection connection;
    private DockerLocalClientFile dockerLocalClientFile;

    @BeforeClass
    public void setup() {
        connection = mock(DockerLocalClientConnection.class);
        dockerLocalClientFile = new DockerLocalClientFile(connection, "/foo/bar");
    }

    @Test
    public void shouldParseDirectoryWithAclOnLs() {
        DockerLocalClientFile.LsResults results = new DockerLocalClientFile.LsResults();
        dockerLocalClientFile.parseLsOutputLine(results, "drwxr-xr-x+ 10 ajvanerp  staff    340 Dec 17 15:28 build");
        assertThat("Should be a directory", results.isDirectory);
        assertThat("Should be executable", results.canExecute);
    }

    @Test
    public void shouldParseDirectoryWithMacOSExtendedAttrsOnLs() {
        DockerLocalClientFile.LsResults results = new DockerLocalClientFile.LsResults();
        dockerLocalClientFile.parseLsOutputLine(results, "drwxr-xr-x@ 10 ajvanerp  staff    340 Dec 17 15:28 build");
        assertThat("Should be a directory", results.isDirectory);
        assertThat("Should be executable", results.canExecute);
    }

    @Test
    public void shouldParseDirectoryWithEndingDotOnLs() {
        DockerLocalClientFile.LsResults results = new DockerLocalClientFile.LsResults();
        dockerLocalClientFile.parseLsOutputLine(results, "drwxr-xr-x. 10 ajvanerp  staff    340 Dec 17 15:28 build");
        assertThat("Should be a directory", results.isDirectory);
        assertThat("Should be executable", results.canExecute);
    }

    @Test
    public void shouldParseSymLinkOnLs() {
        DockerLocalClientFile.LsResults results = new DockerLocalClientFile.LsResults();
        dockerLocalClientFile.parseLsOutputLine(results, "lrwxr-xr-x 10 ajvanerp  staff    340 Dec 17 15:28 build");
        assertThat("Should be a directory", !results.isDirectory);
        assertThat("Should be executable", results.canExecute);
    }

    @Test
    public void shouldSplitEmptyPaths() {
        assertThat(DockerFile.splitPath("").isEmpty(), equalTo(true));
        assertThat(DockerFile.splitPath("/").isEmpty(), equalTo(true));
    }

    @Test(dataProvider = "pathsForSplit")
    public void shouldSplitPaths(String actual, OperatingSystemFamily os, String[] expected) {
        assertThat(DockerFile.splitPath(actual), contains(expected));
    }

    @DataProvider(name = "pathsForSplit")
    public Object[][] createPathsForSplit() {
        return new Object[][]{
                {"/tmp", UNIX, parts("tmp")},
                {"/tmp/", UNIX, parts("tmp")},
                {"/tmp/foo", UNIX, parts("tmp", "foo")},
                {"/tmp/foo/", UNIX, parts("tmp", "foo")},
                {"/tmp//foo", UNIX, parts("tmp", "foo")}
        };
    }

    @Test(dataProvider = "pathsForJoin")
    public void shouldJoinPaths(String[] actual, OperatingSystemFamily os, String expected) {
        assertThat(DockerFile.joinPath(newArrayList(actual)), equalTo(expected));
    }

    @DataProvider(name = "pathsForJoin")
    public Object[][] createPathsForJoin() {
        return new Object[][]{
                {parts(), UNIX, "/"},
                {parts("tmp"), UNIX, "/tmp"},
                {parts("tmp", "foo"), UNIX, "/tmp/foo"}
        };
    }



}
