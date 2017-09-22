package com.xebialabs.overthere;

public class Utils {

    public static String getPassword(String passwordPropertyName) throws RuntimeException {
        if(System.getProperty(passwordPropertyName) == null) {
            throw new RuntimeException(String.format("Property [%s] missing. Please define it in your gradle.properties or on the command line", passwordPropertyName));
        } else {
            return System.getProperty(passwordPropertyName);
        }
    }

}
