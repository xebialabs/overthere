/* License added by: GRADLE-LICENSE-PLUGIN
 *
 * Copyright 2008-2011 XebiaLabs
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.xebialabs.overthere.cifs;

import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.DEFAULT_CIFS_PORT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import com.google.common.collect.ImmutableMap;
import org.testng.annotations.Test;

/**
 * Unit tests for the {@link PathEncoder}
 */
public class PathEncoderTest {
    PathEncoder encoder;
    
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void requiresUniquePathMappings() {
        encoder = new PathEncoder("user", "pass", "windows-box", DEFAULT_CIFS_PORT, ImmutableMap.<String, String>of("c", "share", "d", "share"));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void hostPathOfLessThanTwoCharsFails() {
        encoder = new PathEncoder("user", "pass", "windows-box", DEFAULT_CIFS_PORT, ImmutableMap.<String, String>of());
        encoder.toSmbUrl("c");
    }
    
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void requiresColonAsSecondChar() {
        encoder = new PathEncoder("user", "pass", "windows-box", DEFAULT_CIFS_PORT, ImmutableMap.<String, String>of());
        encoder.toSmbUrl("c!");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void charAfterColonMustBeSlash() {
        encoder = new PathEncoder("user", "pass", "windows-box", DEFAULT_CIFS_PORT, ImmutableMap.<String, String>of());
        encoder.toSmbUrl("c:c");
    }

    @Test
    public void replacesFirstSlashInUsername() {
        encoder = new PathEncoder("domain\\user", "pass", "windows-box", DEFAULT_CIFS_PORT, ImmutableMap.<String, String>of());
        // %3B = ;
        assertThat(encoder.toSmbUrl("c:\\Temp"), is("smb://domain%3Buser:pass@windows-box/c$/Temp"));
    }
    
    @Test
    public void urlEncodesUsername() {
        encoder = new PathEncoder("us;er", "pass", "windows-box", DEFAULT_CIFS_PORT, ImmutableMap.<String, String>of());
        // %3B = ;
        assertThat(encoder.toSmbUrl("c:\\Temp"), is("smb://us%3Ber:pass@windows-box/c$/Temp"));
    }
    
    @Test
    public void urlEncodesPassword() {
        encoder = new PathEncoder("user", "pa;ss", "windows-box", DEFAULT_CIFS_PORT, ImmutableMap.<String, String>of());
        // %3B = ;
        assertThat(encoder.toSmbUrl("c:\\Temp"), is("smb://user:pa%3Bss@windows-box/c$/Temp"));
    }
    
    @Test
    public void urlEncodesAddress() {
        encoder = new PathEncoder("user", "pass", "windows;box", DEFAULT_CIFS_PORT, ImmutableMap.<String, String>of());
        // %3B = ;
        assertThat(encoder.toSmbUrl("c:\\Temp"), is("smb://user:pass@windows%3Bbox/c$/Temp"));
    }
    
    @Test
    public void ignoresDefaultCifsPort() {
        encoder = new PathEncoder("user", "pass", "windows-box", DEFAULT_CIFS_PORT, ImmutableMap.<String, String>of());
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
        encoder = new PathEncoder("user", "pass", "windows-box", DEFAULT_CIFS_PORT, ImmutableMap.of("c:\\windows", "windows-share"));
        // %3B = ;
        assertThat(encoder.toSmbUrl("c:\\Windows"), is("smb://user:pass@windows-box/windows-share"));
    }
    
    @Test
    public void fallsBackToAdminShare() {
        encoder = new PathEncoder("user", "pass", "windows-box", DEFAULT_CIFS_PORT, ImmutableMap.<String, String>of());
        // %3B = ;
        assertThat(encoder.toSmbUrl("c:\\Windows"), is("smb://user:pass@windows-box/c$/Windows"));
    }

    @Test
    public void supportsPathlessLocalPath() {
        encoder = new PathEncoder("user", "pass", "windows-box", DEFAULT_CIFS_PORT, ImmutableMap.<String, String>of());
        // %3B = ;
        assertThat(encoder.toSmbUrl("c:"), is("smb://user:pass@windows-box/c$"));
    }
    
    @Test
    public void usesForwardSlashAsFileSeparator() {
        encoder = new PathEncoder("user", "pass", "windows-box", DEFAULT_CIFS_PORT, ImmutableMap.<String, String>of());
        // %3B = ;
        assertThat(encoder.toSmbUrl("c:\\Temp\\file.txt"), is("smb://user:pass@windows-box/c$/Temp/file.txt"));
    }

    @Test
    public void usesPathMappingToConvertUncPath() {
        encoder = new PathEncoder("user", "pass", "windows-box", DEFAULT_CIFS_PORT, ImmutableMap.of("c:\\Windows", "windows-share"));
        assertThat(encoder.fromUncPath("\\\\windows-box\\windows-share\\Temp\\file.txt"), 
                is("c:\\Windows\\Temp\\file.txt"));
    }

    @Test
    public void understandsAdminSharesWhenConvertingUncPath() {
        encoder = new PathEncoder("user", "pass", "windows-box", DEFAULT_CIFS_PORT, ImmutableMap.<String, String>of());
        assertThat(encoder.fromUncPath("\\\\windows-box\\c$\\Temp\\file.txt"), 
                is("c:\\Temp\\file.txt"));
    }

    @Test
    public void supportsShareAndSeparatorUncPath() {
        encoder = new PathEncoder("user", "pass", "windows-box", DEFAULT_CIFS_PORT, ImmutableMap.<String, String>of());
        assertThat(encoder.fromUncPath("\\\\windows-box\\c$\\"), is("c:\\"));
    }

    @Test
    public void supportsPathlessUncPath() {
        encoder = new PathEncoder("user", "pass", "windows-box", DEFAULT_CIFS_PORT, ImmutableMap.<String, String>of());
        assertThat(encoder.fromUncPath("\\\\windows-box\\c$"), is("c:"));
    }
}
