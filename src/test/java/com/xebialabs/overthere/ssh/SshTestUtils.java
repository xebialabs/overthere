package com.xebialabs.overthere.ssh;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import com.google.common.io.CharStreams;
import com.google.common.io.OutputSupplier;

import com.xebialabs.overthere.RuntimeIOException;

class SshTestUtils {

    static File createPrivateKeyFile(String privateKey) {
        try {
            final File privateKeyFile = File.createTempFile("private", ".key");
            privateKeyFile.deleteOnExit();
            CharStreams.write(privateKey, new OutputSupplier<Writer>() {
                @Override
                public Writer getOutput() throws IOException {
                    return new FileWriter(privateKeyFile);
                }
            });
            return privateKeyFile;
        } catch (IOException exc) {
            throw new RuntimeIOException("Cannot create private key file", exc);
        }
    }


}
