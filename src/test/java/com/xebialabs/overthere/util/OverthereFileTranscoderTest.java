package com.xebialabs.overthere.util;

import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.Overthere;
import com.xebialabs.overthere.OverthereConnection;
import com.xebialabs.overthere.OverthereFile;

import com.xebialabs.overthere.local.LocalFile;
import org.testng.annotations.Test;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static org.testng.AssertJUnit.assertEquals;

public class OverthereFileTranscoderTest {

    @Test
    public void testTransmitFileUtf8ToCp1047() throws IOException {
        // Special characters to test
        String specialChars = "This is a Sample Encoding";

        // Create source file with UTF-8 encoding
        File srcFile = File.createTempFile("source", ".txt");
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(srcFile), Charset.forName("UTF-8"))) {
            writer.write(specialChars);
        }

        // Create destination file
        File dstFile = File.createTempFile("destination", ".txt");

        // Create OverthereFile instances
        OverthereFile srcOverthereFile = LocalFile.from(srcFile);
        OverthereFile dstOverthereFile = LocalFile.from(dstFile);

        // Transcode file from UTF-8 to CP1047
        OverthereFileTranscoder transcoder = new OverthereFileTranscoder(Charset.forName("UTF-8"), Charset.forName("CP1047"));
        transcoder.transmitFile(srcOverthereFile, dstOverthereFile);

        // Read and assert the content of the destination file
        try (Reader reader = new InputStreamReader(new FileInputStream(dstFile), Charset.forName("CP1047"))) {
            char[] buffer = new char[specialChars.length()];
            int read = reader.read(buffer);
            assertEquals(specialChars.length(), read);
            assertEquals(specialChars, new String(buffer));
        }
    }

    @Test
    public void testfileTransmitFromLinuxToLinuxSftp() {
        OverthereFileTranscoder oft = new OverthereFileTranscoder(StandardCharsets.UTF_8, Charset.forName("CP1047"));
        ConnectionOptions fromConnection = new ConnectionOptions();
        /*fromConnection.set(ADDRESS, "qe-ubuntu-2.xebialabs.com");
        fromConnection.set(USERNAME, "root");
        fromConnection.set(PASSWORD, "devopsqe@123");
        fromConnection.set(OPERATING_SYSTEM, UNIX);
        fromConnection.set(CONNECTION_TYPE, SshConnectionType.SFTP);*/

        ConnectionOptions toConnection = new ConnectionOptions();
        /*toConnection.set("address", "qe-ubuntu-2.xebialabs.com");
        toConnection.set("username", "root");
        toConnection.set("password", "devopsqe@123");
        toConnection.set("os", "UNIX");
        toConnection.set("connectionType", "SFTP");*/
        OverthereConnection fromConn = Overthere.getConnection("local", fromConnection);
        //OverthereConnection toConn = Overthere.getConnection("local", toConnection);
        try {
            OverthereFile from = fromConn.getFile("src/test/resources/test.txt");
            OverthereFile to = fromConn.getFile("src/test/resources/test-transcoded.txt");
            OverthereFileTranscoder.transcode(from, StandardCharsets.UTF_8, to, Charset.forName("CP1047"));

            String srcFilePath = "src/test/resources/test.txt";
            String transcodedFilePath = "src/test/resources/test-transcoded.txt";

            // Read the content of the source file in UTF-8 encoding
            StringBuilder srcContent = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(srcFilePath), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    srcContent.append(line).append(System.lineSeparator());
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            // Read the content of the transcoded file in CP1047 encoding
            StringBuilder transcodedContent = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(transcodedFilePath), Charset.forName("CP1047")))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    transcodedContent.append(line).append(System.lineSeparator());
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            // Remove the last line separator for accurate comparison
            if (srcContent.length() > 0) {
                srcContent.setLength(srcContent.length() - System.lineSeparator().length());
            }
            if (transcodedContent.length() > 0) {
                transcodedContent.setLength(transcodedContent.length() - System.lineSeparator().length());
            }

            // Assert that the content of both files is equal
            assertEquals(srcContent.toString(), transcodedContent.toString());
        } finally {
            fromConn.close();
            //toConn.close();
        }
    }
}
