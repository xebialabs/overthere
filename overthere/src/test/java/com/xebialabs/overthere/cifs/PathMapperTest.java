/* License added by: GRADLE-LICENSE-PLUGIN
 *
 * Copyright 2008-2012 XebiaLabs
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import com.google.common.collect.ImmutableMap;
import org.testng.annotations.Test;

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

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void failsIfSharedPathIsNotExplicitlyMappedOrAdminShare() {
        new PathMapper(ImmutableMap.of("c:\\Windows", "windows-share"))
        .toLocalPath("temp-share\\file.txt");
    }
    
    @Test(expectedExceptions = IllegalArgumentException.class)
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
