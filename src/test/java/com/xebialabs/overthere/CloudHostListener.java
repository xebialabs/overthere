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

import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ISuite;
import org.testng.ISuiteListener;

import com.xebialabs.overcast.host.CloudHost;
import com.xebialabs.overcast.host.CloudHostFactory;

import static java.lang.String.format;

public abstract class CloudHostListener implements ISuiteListener {

    private String cloudHostLabel;
    private AtomicReference<CloudHost> hostHolder;

    public CloudHostListener(final String cloudHostLabel, final AtomicReference<CloudHost> hostHolder) {
        this.cloudHostLabel = cloudHostLabel;
        this.hostHolder = hostHolder;
    }

    public void onStart(ISuite suite) {
        logger.debug("Setting up cloud host {}", cloudHostLabel);
        CloudHost host = CloudHostFactory.getCloudHost(cloudHostLabel);
        host.setup();
        if (!hostHolder.compareAndSet(null, host)) {
            throw new IllegalStateException(format("Cannot initialize host [%s] twice", cloudHostLabel));
        }
    }

    public void onFinish(ISuite suite) {
        logger.debug("Tearing down cloud host {}", cloudHostLabel);
        hostHolder.get().teardown();
    }

    private Logger logger = LoggerFactory.getLogger(CloudHostListener.class);

}
