package com.xebialabs.overthere.gcp;

import java.nio.file.Paths;
import com.google.common.io.Resources;

public final class Utils {

    private Utils() {
    }

    public static String getClasspathFile(String name) throws Exception {
        return Paths.get(Resources.getResource(name).toURI()).toFile().getAbsolutePath();
    }

}
