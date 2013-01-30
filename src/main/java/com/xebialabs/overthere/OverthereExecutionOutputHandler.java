package com.xebialabs.overthere;

public interface OverthereExecutionOutputHandler {
    /**
     * Invoked when an executed command generates a single character of output.
     *
     * @param c
     *            the character of output generated.
     */
    void handleChar(char c);

    /**
     * Invoked when an executed command generated a line of output.
     *
     * @param line
     *            the line of output generated.
     */
    void handleLine(String line);
}
