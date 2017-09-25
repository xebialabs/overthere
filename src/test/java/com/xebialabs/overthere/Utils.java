package com.xebialabs.overthere;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

public class Utils {

    public static String getProperty(String propertyName) throws RuntimeException {
        if(System.getProperty(propertyName) == null) {
            throw new RuntimeException(String.format("Property [%s] missing. Please define it in your gradle.properties or on the command line", propertyName));
        } else {
            return System.getProperty(propertyName);
        }
    }

    public static String getFilePath(String urlLoc) {
        URL url;
        File file;
        try {
            url = new URL(urlLoc);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        try {
            file = new File(url.toURI());
        } catch(URISyntaxException e) {
            file = new File(url.getPath());
        }
        return file.getPath();
    }

}
