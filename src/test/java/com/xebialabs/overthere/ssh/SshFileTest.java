package com.xebialabs.overthere.ssh;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.google.common.collect.Lists;

import com.xebialabs.overthere.OperatingSystemFamily;

import static com.google.common.collect.Lists.newArrayList;
import static com.xebialabs.overthere.OperatingSystemFamily.UNIX;
import static com.xebialabs.overthere.OperatingSystemFamily.WINDOWS;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

public class SshFileTest {

    public static String[] parts(String... strings) {
        return strings;
    }

    @Test
    public void shouldSplitEmptyPaths() {
        assertThat(SshFile.splitPath("", UNIX).isEmpty(), equalTo(true));
        assertThat(SshFile.splitPath("/", UNIX).isEmpty(), equalTo(true));
        assertThat(SshFile.splitPath("", WINDOWS).isEmpty(), equalTo(true));
        assertThat(SshFile.splitPath("\\", WINDOWS).isEmpty(), equalTo(true));
        assertThat(SshFile.splitPath("/", WINDOWS).isEmpty(), equalTo(true));
    }

    @Test(dataProvider = "pathsForSplit")
    public void shouldSplitPaths(String actual, OperatingSystemFamily os, String[] expected) {
        assertThat(SshFile.splitPath(actual, os), contains(expected));
    }

    @DataProvider(name = "pathsForSplit")
    public Object[][] createPathsForSplit() {
        return new Object[][] {
            { "/tmp", UNIX, parts("tmp") },
            { "/tmp/", UNIX, parts("tmp") },
            { "/tmp/foo", UNIX, parts("tmp", "foo") },
            { "/tmp/foo/", UNIX, parts("tmp", "foo") },
            { "/tmp//foo", UNIX, parts("tmp", "foo") },
            { "C:", WINDOWS, parts("C:") },
            { "C:\\", WINDOWS, parts("C:") },
            { "C:\\WINDOWS", WINDOWS, parts("C:", "WINDOWS") },
            { "C:\\WINDOWS\\", WINDOWS, parts("C:", "WINDOWS") },
            { "C:\\WINDOWS\\TEMP", WINDOWS, parts("C:", "WINDOWS", "TEMP") },
            { "C:\\WINDOWS\\TEMP\\", WINDOWS, parts("C:", "WINDOWS", "TEMP") },
            { "C:/", WINDOWS, parts("C:") },
            { "C:/WINDOWS", WINDOWS, parts("C:", "WINDOWS") },
            { "C:/WINDOWS/", WINDOWS, parts("C:", "WINDOWS") },
            { "C:/WINDOWS/TEMP", WINDOWS, parts("C:", "WINDOWS", "TEMP") },
            { "C:/WINDOWS/TEMP/", WINDOWS, parts("C:", "WINDOWS", "TEMP") },
        };
    }

    @Test(dataProvider = "pathsForJoin")
    public void shouldJoinPaths(String[] actual, OperatingSystemFamily os, String expected) {
        assertThat(SshFile.joinPath(newArrayList(actual), os), equalTo(expected));
    }

    @DataProvider(name = "pathsForJoin")
    public Object[][] createPathsForJoin() {
        return new Object[][] {
            { parts(), UNIX, "/" },
            { parts("tmp"), UNIX, "/tmp" },
            { parts("tmp", "foo"), UNIX, "/tmp/foo" },
            { parts("C:"), WINDOWS, "C:\\" },
            { parts("C:", "WINDOWS"), WINDOWS, "C:\\WINDOWS" },
            { parts("C:", "WINDOWS", "TEMP"), WINDOWS, "C:\\WINDOWS\\TEMP" },
        };
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void shouldNotAllowEmptyWindowsPath() {
        SshFile.checkWindowsPath(Lists.<String> newArrayList(), WINDOWS);
    }

}
