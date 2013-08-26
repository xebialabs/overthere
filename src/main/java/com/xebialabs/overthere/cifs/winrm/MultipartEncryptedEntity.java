package com.xebialabs.overthere.cifs.winrm;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Random;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.entity.mime.HttpMultipart;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;

class MultipartEncryptedEntity implements HttpEntity {

    private final static char[] MULTIPART_CHARS = "-_1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();

    private final HttpMultipart multipart;
    private final Header contentType;

    private long length;
    private boolean dirty;

    MultipartEncryptedEntity() {
        String boundary = generateBoundary();
        this.multipart = new HttpMultipart("encrypted", Charset.forName("UTF-8"), boundary, HttpMultipartMode.STRICT);
        this.contentType = new BasicHeader(HTTP.CONTENT_TYPE, "multipart/encrypted; protocol=\"application/HTTP-Kerberos-session-encrypted\"; boundary=" + boundary);
        this.dirty = true;
    }

    private String generateBoundary() {
        StringBuilder buffer = new StringBuilder();
        Random rand = new Random();
        int count = rand.nextInt(11) + 30; // a random size from 30 to 40
        for (int i = 0; i < count; i++) {
            buffer.append(MULTIPART_CHARS[rand.nextInt(MULTIPART_CHARS.length)]);
        }
        return buffer.toString();
    }

    @Override
    public boolean isRepeatable() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isChunked() {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getContentLength() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Header getContentType() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Header getContentEncoding() {
        throw new UnsupportedOperationException();
    }

    @Override
    public InputStream getContent() throws IOException, IllegalStateException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeTo(OutputStream outstream) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isStreaming() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void consumeContent() throws IOException {
        throw new UnsupportedOperationException();
    }

}
