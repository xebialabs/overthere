package net.schmizz.sshj;

import java.io.IOException;

/**
 * Workaround for <a href="https://code.google.com/p/mockito/issues/detail?id=212" />
 */
public class MockitoFriendlySSHClient extends SSHClient {

    @Override
    public void connect(String hostname, int port) throws IOException {
        super.connect(hostname, port);
    }
}
