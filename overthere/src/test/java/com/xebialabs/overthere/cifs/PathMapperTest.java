package com.xebialabs.overthere.cifs;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.google.common.collect.ImmutableMap;
import com.xebialabs.overthere.cifs.PathEncoder.PathMapper;

/**
 * Unit tests for {@link PathMapper}.
 */
public class PathMapperTest {
    private PathMapper mapper;
    
    @Test
    public void usesExplicitMappingForSharedPath() {
        mapper = new PathMapper(ImmutableMap.of("c:\\Windows", "windows-share"));
        assertThat(mapper.toSharedPath("c:\\Windows\\Temp"), is("windows-share\\Temp"));
    }
    
    @Test
    public void ignoresCaseInLocalPath() {
        mapper = new PathMapper(ImmutableMap.of("C:\\Windows", "windows-share"));
        assertThat(mapper.toSharedPath("c:\\windows\\Temp"), is("windows-share\\Temp"));
    }
    
    @Test
    public void usesLongestExplicitMappingForSharedPath() {
        mapper = new PathMapper(ImmutableMap.of("c:\\Windows", "windows-share",
                "c:\\Windows\\Temp", "temp-share", "c:", "c-share"));
        assertThat(mapper.toSharedPath("c:\\Windows\\Temp\\file.txt"), is("temp-share\\file.txt"));
    }
    
    @Test
    public void fallsBackToAdminSharesForSharedPath() {
        mapper = new PathMapper(ImmutableMap.of("c:\\Windows", "windows-share"));
        assertThat(mapper.toSharedPath("c:\\Temp"), is("c$\\Temp"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void failsIfSharedPathIsNotExplicitlyMappedOrAdminShare() {
        new PathMapper(ImmutableMap.of("c:\\Windows", "windows-share"))
        .toLocalPath("temp-share\\file.txt");
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void failsIfSharedPathIsNotExplicitlyMappedAndTooShort() {
        new PathMapper(ImmutableMap.of("c:\\Windows", "windows-share"))
        .toLocalPath("s");
    }
    
    @Test
    public void handlesExplicitMappingInSharedPath() {
        mapper = new PathMapper(ImmutableMap.of("c:\\Windows", "windows-share"));
        assertThat(mapper.toLocalPath("windows-share\\Temp"), is("c:\\Windows\\Temp"));
    }
    
    @Test
    public void handlesShortExplicitMappingInSharedPath() {
        mapper = new PathMapper(ImmutableMap.of("c:\\Windows", "w"));
        assertThat(mapper.toLocalPath("w\\Temp"), is("c:\\Windows\\Temp"));
    }
    
    @Test
    public void ignoresCaseInSharedPath() {
        mapper = new PathMapper(ImmutableMap.of("c:\\Windows", "windows-share"));
        assertThat(mapper.toLocalPath("Windows-Share\\Temp"), is("c:\\Windows\\Temp"));
    }
    
    @Test
    public void handlesAdminSharesInSharedPath() {
        mapper = new PathMapper(ImmutableMap.of("c:\\Windows\\Temp", "temp-share"));
        assertThat(mapper.toLocalPath("c$\\Windows"), is("c:\\Windows"));
    }
}
