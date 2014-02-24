/*
 * Copyright (c) 2008-2014, XebiaLabs B.V., All rights reserved.
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
package com.xebialabs.overthere.ssh;

import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.itest.OverthereConnectionItestBase;
import com.xebialabs.overthere.UnixCloudHostListener;

import static com.xebialabs.overthere.ConnectionOptions.ADDRESS;
import static com.xebialabs.overthere.ConnectionOptions.OPERATING_SYSTEM;
import static com.xebialabs.overthere.ConnectionOptions.PORT;
import static com.xebialabs.overthere.ConnectionOptions.USERNAME;
import static com.xebialabs.overthere.OperatingSystemFamily.UNIX;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.ALLOCATE_DEFAULT_PTY;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.CONNECTION_TYPE;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.PRIVATE_KEY_FILE;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.SSH_PROTOCOL;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.SUDO_USERNAME;
import static com.xebialabs.overthere.ssh.SshConnectionType.SUDO;
import static com.xebialabs.overthere.ssh.SshTestUtils.createPrivateKeyFile;

@Test
@Listeners({UnixCloudHostListener.class})
public class SshSudoConnectionItest extends OverthereConnectionItestBase {

    @Override
    protected String getProtocol() {
        return SSH_PROTOCOL;
    }

    @Override
    protected ConnectionOptions getOptions() {
        ConnectionOptions options = new ConnectionOptions();
        options.set(OPERATING_SYSTEM, UNIX);
        options.set(CONNECTION_TYPE, SUDO);
        options.set(ADDRESS, UnixCloudHostListener.getHost().getHostName());
        options.set(PORT, 22);
        options.set(USERNAME, "trusted");
        options.set(PRIVATE_KEY_FILE, createPrivateKeyFile(
                "-----BEGIN RSA PRIVATE KEY-----\r\n" + "MIIEpgIBAAKCAQEA65Jf19SCv8rZ/kLyfOw+OjHt5fQnxHVQR2B6UW0B0q6RhSSg\r\n"
                        + "YA2gOUEPJph7+O5605jjrMlblScsXO7VnFJtBTMRFoQjBlOP8VFuEg0MoaN5RPQU\r\n"
                        + "ZVYoFKP9V6ycEKpmfeSmVhpEipUdgL+fUjb1nsw+Cpx8WNLXf3Cvqrk4d1xZ0f0i\r\n"
                        + "u3D4/B+LmyZuVqvlmRfVDOe0Orvo/hfkiM+hMqKnFfKnUldvUb6feq4HrxpeYoEm\r\n"
                        + "sO+oqkNfVp/RfXiuSzBHQl6sYYwWDSHgsyIo/7o2ATcDw3FzErxR3pMjH0rqh39A\r\n"
                        + "TJtFTzvV90VNTRd+1727hzS6IQLf+M/Y6OvzawIDAQABAoIBAQDashc8XdPMjlv2\r\n"
                        + "ytwn0YKrsDK1qwdIQcj3mr+z3Ek2+E2sl6YzxjKbNKUGJcXiAjQRQP0NKhpVy/pJ\r\n"
                        + "hIjXCUag7xnMF3wUoXseg4R2SZsSbJtmwlo1AdlP4DaQMHTqm+dutNkfUl+TcH/l\r\n"
                        + "SQB16QP6Go72dvSR2Zuqekj7a9zaISAopv+B68ShkX5NIf/96JCLpa0J7O00dN6k\r\n"
                        + "bO9UiH3g6jtb9CgmG836rXabdTpSma1fsOLJCq/sEkCL1ImF1rCKTIjvK0djDdjK\r\n"
                        + "NEL7e0v4s/RYqCIiCoji617Xesu9OYgYHWBkH3mSRKzIw6LUSxAQKiiCwTDaYNz5\r\n"
                        + "X4IFy8ihAoGBAP1i4ztQpBwq4teEHuJ6klpFyT03tcMqFxK1zrcr0qQSqhc8ww55\r\n"
                        + "AWUKACS7QAA0hFH286+dzYApo0jn2MRzIu9KP9xUEX4K0qw3h/zsBxkIGsUbUSp7\r\n"
                        + "7f5mK8vfTJstUZbyh/XlJDWNI5Q/QvybgAhZmXYoQHt6r4NwcOj47A9JAoGBAO4A\r\n"
                        + "cb//EB+Nj62kKQJN/UI60VZ5Koo3cBXwLhkVsfCfUXwOEb9uhYs3bePGRadgpn3z\r\n"
                        + "6XvYs0IU4mFabvAMXLQz59zVFqotEEsCVy1acETbaCYCHLRRolxsdMeF528HEMqB\r\n"
                        + "7tTJVbrmZaWxtnHzd3Mlj9tiszrwvidQUJ33s0kTAoGBAPpGHmOL90zLH1v35/mT\r\n"
                        + "T9NScr7AtAudG0UjxpYt9tSQiuiA37j/1FzUT+f3+/M37Cp5XaDsoPoiJmHwfq8r\r\n"
                        + "eioYkJMzhkOUtRndj7hF+YzD8I0XukfYOO66RDAO0z/Ct3/89kXumqE6UxYulh+k\r\n"
                        + "CAY3WdjXUTmlqI6PFTdIBwHhAoGBAKKWtR6nfXlAuO2znrxPUPtEuSus3K3Nj4m9\r\n"
                        + "KZDDbGroO79W0TMIqrxfYnffREhC05pp3ZBYiqVTJQ/CutTMbSxB5VzMSY55+I51\r\n"
                        + "i96U0OuJQ83rVXat6g/fm6uOQ3tqxULCnsjIvgNPUBNwoyWXYHvOJkeGVtCmFBFB\r\n"
                        + "YcF4rQb3AoGBAJ7Ekj7G0qDD+gJQ3JnWfWJNHHDU1LclntuZlzrLhKEAku9y7Q5I\r\n"
                        + "tygMnR1DYrWKTZPDtrCb1NJqPntPp8A4VMnkZ89AY11CexWrtDx9VDy4MJFr8uCx\r\n"
                        + "c05G9exoAKHonnFcWxOg+xIgofdfic3/vTg/aGPp1I2urqLzv00vPyeX\r\n"
                        + "-----END RSA PRIVATE KEY-----\r\n").getPath());
        // Corresponding public key:
        // ssh-rsa
        // AAAAB3NzaC1yc2EAAAADAQABAAABAQDrkl/X1IK/ytn+QvJ87D46Me3l9CfEdVBHYHpRbQHSrpGFJKBgDaA5QQ8mmHv47nrTmOOsyVuVJyxc7tWcUm0FMxEWhCMGU4/xUW4SDQyho3lE9BRlVigUo/1XrJwQqmZ95KZWGkSKlR2Av59SNvWezD4KnHxY0td/cK+quTh3XFnR/SK7cPj8H4ubJm5Wq+WZF9UM57Q6u+j+F+SIz6EyoqcV8qdSV29Rvp96rgevGl5igSaw76iqQ19Wn9F9eK5LMEdCXqxhjBYNIeCzIij/ujYBNwPDcXMSvFHekyMfSuqHf0BMm0VPO9X3RU1NF37XvbuHNLohAt/4z9jo6/Nr
        // overthere@overthere
        options.set(SUDO_USERNAME, "overthere");
        options.set(ALLOCATE_DEFAULT_PTY, true);
        return options;
    }

    @Override
    protected String getExpectedConnectionClassName() {
        return SshSudoConnection.class.getName();
    }

}
