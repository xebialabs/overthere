/*
 * Copyright (c) 2008-2013, XebiaLabs B.V., All rights reserved.
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

import static com.google.common.collect.Maps.newHashMap;
import static com.xebialabs.overthere.ConnectionOptions.ADDRESS;
import static com.xebialabs.overthere.ConnectionOptions.JUMPSTATION;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITest;

/**
 * Base class for all parametrized Overthere connection itests that use a {@link com.xebialabs.overcast.CloudHost}.
 */
public class OverthereConnectionItest extends OverthereConnectionItestBase implements ITest {

    protected final ConnectionOptions partialOptions;
    private static final Map<String, AtomicInteger> timesHostNeeded = newHashMap();
    private final String testName;

    public OverthereConnectionItest(String testName, String protocol, ConnectionOptions partialOptions, String expectedConnectionClassName, String host) throws Exception {
        this.testName = testName;
        this.protocol = protocol;
        this.partialOptions = partialOptions;
        this.expectedConnectionClassName = expectedConnectionClassName;
        this.hostname = host;
        registerHostNeeded(host);
    }

    private static void registerHostNeeded(String host) {
        if (!timesHostNeeded.containsKey(host)) {
            timesHostNeeded.put(host, new AtomicInteger(0));
        }
        int i = timesHostNeeded.get(host).incrementAndGet();
        logger.info("Host [{}] is now needed [{}] times", host, i);
    }

    @Override
    public String getTestName() {
        return testName;
    }

    @Override
    protected void doInitHost() {
        CloudHostHolder.setupHost(hostname);
    }

    @Override
    protected void doTeardownHost() {
        int i = timesHostNeeded.get(hostname).decrementAndGet();
        logger.info("Tearing down host [{}], now needed [{}] times", hostname, i);
        if (i == 0) {
            logger.info("Cleaning up host [{}]", hostname);
            CloudHostHolder.teardownHost(hostname);
        }
    }

    @Override
    protected void setTypeAndOptions() throws Exception {
        options = new ConnectionOptions(partialOptions);
        options.set(ADDRESS, host.getHostName());

        ConnectionOptions tunnelOptions = options.getOptional(JUMPSTATION);
        if (tunnelOptions != null) {
            tunnelOptions.set(ADDRESS, host.getHostName());
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(OverthereConnectionItest.class);

}
