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
package com.xebialabs.overthere.cifs;

import org.testng.annotations.Test;
import com.google.common.collect.ImmutableMap;

import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.CIFS_PORT_DEFAULT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * Unit tests for the {@link PathEncoder}
 */
public class PathEncoderTest {
    PathEncoder encoder;

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void requiresUniquePathMappings() {
        encoder = new PathEncoder("user", "pass", "windows-box", CIFS_PORT_DEFAULT, ImmutableMap.<String, String>of("c", "share", "d", "share"));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void hostPathOfLessThanTwoCharsFails() {
        encoder = new PathEncoder("user", "pass", "windows-box", CIFS_PORT_DEFAULT, ImmutableMap.<String, String>of());
        encoder.toSmbUrl("c");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void requiresColonAsSecondChar() {
        encoder = new PathEncoder("user", "pass", "windows-box", CIFS_PORT_DEFAULT, ImmutableMap.<String, String>of());
        encoder.toSmbUrl("c!");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void charAfterColonMustBeSlash() {
        encoder = new PathEncoder("user", "pass", "windows-box", CIFS_PORT_DEFAULT, ImmutableMap.<String, String>of());
        encoder.toSmbUrl("c:c");
    }

    @Test
    public void replacesFirstSlashInUsername() {
        encoder = new PathEncoder("domain\\user", "pass", "windows-box", CIFS_PORT_DEFAULT, ImmutableMap.<String, String>of());
        // %3B = ;
        assertThat(encoder.toSmbUrl("c:\\Temp"), is("smb://domain%3Buser:pass@windows-box/c$/Temp"));
    }

    @Test
    public void urlEncodesUsername() {
        encoder = new PathEncoder("us;er", "pass", "windows-box", CIFS_PORT_DEFAULT, ImmutableMap.<String, String>of());
        // %3B = ;
        assertThat(encoder.toSmbUrl("c:\\Temp"), is("smb://us%3Ber:pass@windows-box/c$/Temp"));
    }

    @Test
    public void urlEncodesPassword() {
        encoder = new PathEncoder("user", "pa;ss", "windows-box", CIFS_PORT_DEFAULT, ImmutableMap.<String, String>of());
        // %3B = ;
        assertThat(encoder.toSmbUrl("c:\\Temp"), is("smb://user:pa%3Bss@windows-box/c$/Temp"));
    }

    @Test
    public void urlEncodesAddress() {
        encoder = new PathEncoder("user", "pass", "windows;box", CIFS_PORT_DEFAULT, ImmutableMap.<String, String>of());
        // %3B = ;
        assertThat(encoder.toSmbUrl("c:\\Temp"), is("smb://user:pass@windows%3Bbox/c$/Temp"));
    }

    @Test
    public void ignoresDefaultCifsPort() {
        encoder = new PathEncoder("user", "pass", "windows-box", CIFS_PORT_DEFAULT, ImmutableMap.<String, String>of());
        // %3B = ;
        assertThat(encoder.toSmbUrl("c:\\Temp"), is("smb://user:pass@windows-box/c$/Temp"));
    }

    @Test
    public void addsNonDefaultCifsPortToUrl() {
        encoder = new PathEncoder("user", "pass", "windows-box", 0, ImmutableMap.<String, String>of());
        // %3B = ;
        assertThat(encoder.toSmbUrl("c:\\Temp"), is("smb://user:pass@windows-box:0/c$/Temp"));
    }

    @Test
    public void usesPathMappingIfSpecified() {
        encoder = new PathEncoder("user", "pass", "windows-box", CIFS_PORT_DEFAULT, ImmutableMap.of("c:\\windows", "windows-share"));
        // %3B = ;
        assertThat(encoder.toSmbUrl("c:\\Windows"), is("smb://user:pass@windows-box/windows-share"));
    }

    @Test
    public void fallsBackToAdminShare() {
        encoder = new PathEncoder("user", "pass", "windows-box", CIFS_PORT_DEFAULT, ImmutableMap.<String, String>of());
        // %3B = ;
        assertThat(encoder.toSmbUrl("c:\\Windows"), is("smb://user:pass@windows-box/c$/Windows"));
    }

    @Test
    public void supportsPathlessLocalPath() {
        encoder = new PathEncoder("user", "pass", "windows-box", CIFS_PORT_DEFAULT, ImmutableMap.<String, String>of());
        // %3B = ;
        assertThat(encoder.toSmbUrl("c:"), is("smb://user:pass@windows-box/c$"));
    }

    @Test
    public void usesForwardSlashAsFileSeparator() {
        encoder = new PathEncoder("user", "pass", "windows-box", CIFS_PORT_DEFAULT, ImmutableMap.<String, String>of());
        // %3B = ;
        assertThat(encoder.toSmbUrl("c:\\Temp\\file.txt"), is("smb://user:pass@windows-box/c$/Temp/file.txt"));
    }

    @Test
    public void usesPathMappingToConvertUncPath() {
        encoder = new PathEncoder("user", "pass", "windows-box", CIFS_PORT_DEFAULT, ImmutableMap.of("c:\\Windows", "windows-share"));
        assertThat(encoder.fromUncPath("\\\\windows-box\\windows-share\\Temp\\file.txt"),
                is("c:\\Windows\\Temp\\file.txt"));
    }

    @Test
    public void understandsAdminSharesWhenConvertingUncPath() {
        encoder = new PathEncoder("user", "pass", "windows-box", CIFS_PORT_DEFAULT, ImmutableMap.<String, String>of());
        assertThat(encoder.fromUncPath("\\\\windows-box\\c$\\Temp\\file.txt"),
                is("c:\\Temp\\file.txt"));
    }

    @Test
    public void supportsShareAndSeparatorUncPath() {
        encoder = new PathEncoder("user", "pass", "windows-box", CIFS_PORT_DEFAULT, ImmutableMap.<String, String>of());
        assertThat(encoder.fromUncPath("\\\\windows-box\\c$\\"), is("c:\\"));
    }

    @Test
    public void supportsPathlessUncPath() {
        encoder = new PathEncoder("user", "pass", "windows-box", CIFS_PORT_DEFAULT, ImmutableMap.<String, String>of());
        assertThat(encoder.fromUncPath("\\\\windows-box\\c$"), is("c:"));
    }
}
