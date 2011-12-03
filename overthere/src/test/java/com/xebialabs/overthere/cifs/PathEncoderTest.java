package com.xebialabs.overthere.cifs;

import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.DEFAULT_CIFS_PORT;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.google.common.collect.ImmutableMap;

/**
 * Unit tests for the {@link PathEncoder}
 */
public class PathEncoderTest {
    PathEncoder encoder;
    
    @Test(expected = IllegalArgumentException.class)
    public void requiresUniqueDriveMappings() {
        encoder = new PathEncoder("user", "pass", "windows-box", DEFAULT_CIFS_PORT, ImmutableMap.<String, String>of("c", "share", "d", "share"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void hostPathOfLessThanTwoCharsFails() {
        encoder = new PathEncoder("user", "pass", "windows-box", DEFAULT_CIFS_PORT, ImmutableMap.<String, String>of());
        encoder.toSmbUrl("c");
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void requiresColonAsSecondChar() {
        encoder = new PathEncoder("user", "pass", "windows-box", DEFAULT_CIFS_PORT, ImmutableMap.<String, String>of());
        encoder.toSmbUrl("c!");
    }

    @Test(expected = IllegalArgumentException.class)
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
    public void usesDriveMappingIfSpecified() {
        encoder = new PathEncoder("user", "pass", "windows-box", DEFAULT_CIFS_PORT, ImmutableMap.of("c", "cshare"));
        // %3B = ;
        assertThat(encoder.toSmbUrl("c:\\"), is("smb://user:pass@windows-box/cshare/"));
    }
    
    @Test
    public void fallsBackToAdminShareForDrive() {
        encoder = new PathEncoder("user", "pass", "windows-box", DEFAULT_CIFS_PORT, ImmutableMap.<String, String>of());
        // %3B = ;
        assertThat(encoder.toSmbUrl("c:\\"), is("smb://user:pass@windows-box/c$/"));
    }

    @Test
    public void addsTrailingSeparatorIfMissing() {
        encoder = new PathEncoder("user", "pass", "windows-box", DEFAULT_CIFS_PORT, ImmutableMap.<String, String>of());
        // %3B = ;
        assertThat(encoder.toSmbUrl("c:"), is("smb://user:pass@windows-box/c$/"));
    }
    
    @Test
    public void usesForwardSlashAsFileSeparator() {
        encoder = new PathEncoder("user", "pass", "windows-box", DEFAULT_CIFS_PORT, ImmutableMap.<String, String>of());
        // %3B = ;
        assertThat(encoder.toSmbUrl("c:\\Temp\\file.txt"), is("smb://user:pass@windows-box/c$/Temp/file.txt"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void failsIfShareIsNotMappedOrAdminShare() {
        encoder = new PathEncoder("user", "pass", "windows-box", DEFAULT_CIFS_PORT, ImmutableMap.<String, String>of());
        encoder.fromUncPath("\\\\windows-box\\cshare\\Temp\\file.txt");
    }
    
    @Test
    public void usesDriveMappingToConvertUncPath() {
        encoder = new PathEncoder("user", "pass", "windows-box", DEFAULT_CIFS_PORT, ImmutableMap.of("c", "cshare"));
        assertThat(encoder.fromUncPath("\\\\windows-box\\cshare\\Temp\\file.txt"), 
                is("c:\\Temp\\file.txt"));
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
        assertThat(encoder.fromUncPath("\\\\windows-box\\c$"), is("c:\\"));
    }
}
