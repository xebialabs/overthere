package com.xebialabs.overthere.cifs;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

/**
 * Unit tests for the {@link PathEncoder}
 */
public class PathEncoderTest {

    @Test
    public void extractsDriveAndPath() {
        assertThat(PathEncoder.fromUncPath("\\\\windows-box\\c$\\Temp\\file.txt"), 
                is("c:\\Temp\\file.txt"));
    }
}
