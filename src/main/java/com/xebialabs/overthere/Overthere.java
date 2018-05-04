/**
 * Copyright (c) 2008-2016, XebiaLabs B.V., All rights reserved.
 *
 *
 * Overthere is licensed under the terms of the GPLv2
 * <http://www.gnu.org/licenses/old-licenses/gpl-2.0.html>, like most XebiaLabs Libraries.
 * There are special exceptions to the terms and conditions of the GPLv2 as it is applied to
 * this software, see the FLOSS License Exception
 * <http://github.com/xebialabs/overthere/blob/master/LICENSE>.
 *
 * This program is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation; version 2
 * of the License.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin St, Fifth
 * Floor, Boston, MA 02110-1301  USA
 */
package com.xebialabs.overthere;

import com.xebialabs.overthere.spi.OverthereConnectionBuilder;
import com.xebialabs.overthere.spi.Protocol;
import nl.javadude.scannit.Configuration;
import nl.javadude.scannit.Scannit;
import nl.javadude.scannit.scanner.TypeAnnotationScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * Factory object to create {@link OverthereConnection connections}.
 */
@SuppressWarnings("unchecked")
public class Overthere {
    // The "logger" field has to be declared and defined at the top so that the static initializer below can access it
    private static final Logger logger = LoggerFactory.getLogger(Overthere.class);

    private static final OverthereConnector connector = new OverthereConnector();
    static {
        if (Scannit.isBooted()) {
            logger.info("Scannit already booted, checking to see whether it has scanned 'com.xebialabs'");
            Set<Class<?>> protocols = Scannit.getInstance().getTypesAnnotatedWith(Protocol.class);
            if (!protocols.isEmpty()) {
                boot(Scannit.getInstance());
            } else {
                boot();
            }
        } else {
            boot();
        }
    }

    private static void boot() {
        boot(new Scannit(Configuration.config().scan("com.xebialabs").with(new TypeAnnotationScanner())));
    }

    private static void boot(Scannit scannit) {
        final Set<Class<?>> protocolClasses = scannit.getTypesAnnotatedWith(Protocol.class);
        for (Class<?> protocol : protocolClasses) {
            if (OverthereConnectionBuilder.class.isAssignableFrom(protocol)) {
                connector.registerProtocol((Class<? extends OverthereConnectionBuilder>) protocol);
            } else {
                logger.warn("Skipping class {} because it is not a HostConnectionBuilder.", protocol);
            }
        }
    }

    private Overthere() {
        // should not instantiate
    }

    /**
     * Creates a connection.
     *
     * @param protocol The protocol to use, e.g. "local".
     * @param options  A set of options to use for the connection.
     * @return the connection.
     */
    public static OverthereConnection getConnection(String protocol, final ConnectionOptions options) {
        return connector.getConnection(protocol, options);
    }

}
