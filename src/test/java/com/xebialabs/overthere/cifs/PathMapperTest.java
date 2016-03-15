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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

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
