/*
 * This file is part of Overthere.
 * 
 * Overthere is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Overthere is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Overthere.  If not, see <http://www.gnu.org/licenses/>.
 */
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
