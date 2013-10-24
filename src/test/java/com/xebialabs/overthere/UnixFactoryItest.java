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

import static com.google.common.collect.Lists.newArrayList;
import static com.xebialabs.overthere.ConnectionOptions.OPERATING_SYSTEM;
import static com.xebialabs.overthere.ConnectionOptions.PASSWORD;
import static com.xebialabs.overthere.ConnectionOptions.PORT;
import static com.xebialabs.overthere.ConnectionOptions.USERNAME;
import static com.xebialabs.overthere.OperatingSystemFamily.UNIX;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.ALLOCATE_DEFAULT_PTY;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.CONNECTION_TYPE;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.PASSPHRASE;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.PRIVATE_KEY_FILE;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.SSH_PROTOCOL;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.SUDO_OVERRIDE_UMASK;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.SUDO_PASSWORD_PROMPT_REGEX;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.SUDO_USERNAME;
import static com.xebialabs.overthere.ssh.SshConnectionType.INTERACTIVE_SUDO;
import static com.xebialabs.overthere.ssh.SshConnectionType.SCP;
import static com.xebialabs.overthere.ssh.SshConnectionType.SFTP;
import static com.xebialabs.overthere.ssh.SshConnectionType.SUDO;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

import org.testng.annotations.Factory;

import com.google.common.io.CharStreams;
import com.google.common.io.OutputSupplier;

public class UnixFactoryItest {

    @Factory
    public Object[] createItests() throws Exception {
        List<Object> itests = newArrayList();
        itests.add(sshSftp());
        itests.add(sshScp());
        itests.add(sshSudo());
        itests.add(sshInteractiveSudo());
        return itests.toArray(new Object[itests.size()]);
    }

    private OverthereConnectionItest sshSftp() throws Exception {
        ConnectionOptions options = new ConnectionOptions();
        options.set(OPERATING_SYSTEM, UNIX);
        options.set(CONNECTION_TYPE, SFTP);
        options.set(PORT, 22);
        options.set(USERNAME, "overthere");
        options.set(PASSWORD, "overhere");
        return new OverthereConnectionItest(this.getClass().getSimpleName() + "_sshSftp", SSH_PROTOCOL, options,
            "com.xebialabs.overthere.ssh.SshSftpUnixConnection", "overthere-unix");
    }

    private OverthereConnectionItest sshScp() throws Exception, IOException {
        ConnectionOptions options = new ConnectionOptions();
        options.set(OPERATING_SYSTEM, UNIX);
        options.set(CONNECTION_TYPE, SCP);
        options.set(PORT, 22);
        options.set(USERNAME, "overthere");
        options.set(PRIVATE_KEY_FILE, createPrivateKeyFile(
            "-----BEGIN RSA PRIVATE KEY-----\r\n" + "Proc-Type: 4,ENCRYPTED\r\n" + "DEK-Info: AES-128-CBC,EB6F3B5C2F847A0F47AC9C8C757E0AD8\r\n"
                + "IvSSAW5r/sPwtH99/csjEZdtnUci80ak0E+7Puvjo4+H4r+ObFF1gEHOUaNi1rf+\r\n"
                + "5EDhqtPWy6q1zDYBjOdG5jnDhiVSv2P8ZsxA+w1xVR6Lrm5Q5XWcNO+/xXp2/WUc\r\n"
                + "ae1KsyDKCYAsMwD3TaXs01aNrzAD58kNBvFZLkQh8y8ilDiHec0IMtOJHHi2rW0T\r\n"
                + "42crCMyvMvnZNVEQrbXLeThQrAmDWALXrQ14p/D9yEQftv+yDjNnnPB7sjEM1cdi\r\n"
                + "dwM7kHV1YtkYE+mlBPONAqNQURVkVUSyNGgF7qLMhW6UUH8pboXcILFrqEvHPBWn\r\n"
                + "lhnMH1VJUEB0K6r9BvwNKQlNHbAdghT/2oTANzirPXZX7ZTg4Tq6IPAVkDMYARu6\r\n"
                + "PxfLXgt6qCQkT3W++bHPQSFaE6vwPneGKcqtKHThtmJbqoxwK5eD4cccV9OaaSUU\r\n"
                + "lHtzTL0NJL0HCH5UQUumD+8CiRvY+11q3+fJXuMl6Nc/Fi9tLUMfrziOVbl5FIo/\r\n"
                + "tRnQx0h2touaZdig29tOHqJMjqu2vcRGmzkU/DNnELiTAlQE5OeuuIxOmZU4yEcb\r\n"
                + "qAXvRafPTTfXXWatND1Y1RuWaw7ccfh/ZOVL2yhhozHU6978rX3XaD7eS/HiSJIu\r\n"
                + "1qOxk6qkx3PZNrlBjGwNKcblqZTQX9adL+fsQwZaVwaNa9cSAs9wLOmdvpRH4ZsS\r\n"
                + "Q7BmEmEpXhLh/v7ZIyhih30MyOSPh42X/QldBBaXUJOdV3T/vmH20a3avEg1pqGA\r\n"
                + "ZDn1LNnHAMdp8hcWGOkjM8AJuUWPJ9+AM5+7TSZ0qlF4q0yGWWnkCO7agQ4PBAXR\r\n"
                + "3/ghYccVKiel6KBPesbjwGA/Ajmzkkp/iAHuvOVuSQdbJdZS+nAiYr5vkf1MUNWI\r\n"
                + "qst4zXrNOuhjCtPn5dki0uuUpb8tF5QhOByW79aD1wcF5NzgCEZkyO1Y+CCe9PNM\r\n"
                + "V9VFqtlmESGcNpBFVk7NamQOnFzu7nE+IWUXhe2i8D1z0gyrt05ES5MQ1W9yxytb\r\n"
                + "f0j4n1ELNRUTQk54EU9cOMwLHgoU1hNgFJk6zL7dRG3J7S8GGqYJZjjoQbRwloQq\r\n"
                + "FxF2BowFV7TL/hvrS/UydR7guFDlHq/JhnC/HYQJGDGmmuHEzZNLsnWp+kY0q96g\r\n"
                + "wLydnrqwYmAgk4jxdoxXIj5lolAWFPf51+TnIr3DWJiveE7EP7JW0cNMKqW5x63F\r\n"
                + "g2qJ0LjFVutyuxm4j4joF7DFGxAHbl8BG3aAOzmKbiNnPxv5O1nmZcvgjtIoC0+G\r\n"
                + "yvE0S/S4eY57NL8zJ2/cvTZfe0QLqJF2lYN4RmNhjXY69MC/quy/Ida2p+noxVqP\r\n"
                + "R2a/uGVMCAT38bX66YXVI4Muc2dsxH96pMh7OiOb6/KoWx6+IFoKxFL0AvC5qFeo\r\n"
                + "TzIHrzs33O6ejXFXamXqRfnobGdvHijaNdAPFJhIfrA2OWoGaAZpiQKdTaCQXBbx\r\n"
                + "8q65UtXR7Cv2+vJ7B1gkHMTY0qxwJD0b55KGgjhvIcGR16TrIe24puJbpMvAdP1J\r\n"
                + "w42kSNNrOIp8/Bq/t+6sUEqd7Cz7dzl/xi5s6CxJuq3U3ypfCdL9Ij0K8EiOa/a2\r\n" + "-----END RSA PRIVATE KEY-----\r\n").getPath());
        // Corresponding public key:
        // ssh-rsa
        // AAAAB3NzaC1yc2EAAAADAQABAAABAQDSuXwO9Pvcde3onMWQ+ek3zYq38XOU/vcHgy0sr1yjeGqt8H2WQZOjW4wfpKs5TVhfjMoL4Znw6uSV7UHqsDw3K5lnI/3jV+SwiPry4DlMQ+wNoNCTZUBxhRWWK6AwFrkOGfH7JwTgzUvwxgoxi67jP+G5sCiAux2NHnkQCX4wq8O5bygHS5FgTVAPqKrkGBPZVDBGhi2VmEsFeUZQEFhe8Vb3ywk2O6hLWANmevpfTSdS/7tgcoxV13HJuC/KvdWnsCw+/CCV6QIY0+u23zcHL3uQi9Ytl4s5jQBfuO4L5L/TQm1U9X8a4Tx8WAtnUh7o2MwYFGgarw0mGN11M7sn
        // overthere@overthere
        options.set(PASSPHRASE, "letmein");
        return new OverthereConnectionItest(this.getClass().getSimpleName() + "_sshScp", SSH_PROTOCOL, options, "com.xebialabs.overthere.ssh.SshScpConnection",
            "overthere-unix");
    }

    private OverthereConnectionItest sshSudo() throws Exception, IOException {
        ConnectionOptions options = new ConnectionOptions();
        options.set(OPERATING_SYSTEM, UNIX);
        options.set(CONNECTION_TYPE, SUDO);
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
        return new OverthereConnectionItest(this.getClass().getSimpleName() + "_sshSudo", SSH_PROTOCOL, options,
            "com.xebialabs.overthere.ssh.SshSudoConnection",
            "overthere-unix");
    }

    private OverthereConnectionItest sshInteractiveSudo() throws Exception {
        ConnectionOptions options = new ConnectionOptions();
        options.set(OPERATING_SYSTEM, UNIX);
        options.set(CONNECTION_TYPE, INTERACTIVE_SUDO);
        options.set(PORT, 22);
        options.set(USERNAME, "untrusted");
        options.set(PASSWORD, "donttrustme");
        options.set(SUDO_USERNAME, "overthere");
        options.set(SUDO_PASSWORD_PROMPT_REGEX, ".*[P|p]assword.*:");
        options.set(ALLOCATE_DEFAULT_PTY, true);
        options.set(SUDO_OVERRIDE_UMASK, true);
        return new OverthereConnectionItest(this.getClass().getSimpleName() + "_sshInteractiveSudo", SSH_PROTOCOL, options,
            "com.xebialabs.overthere.ssh.SshInteractiveSudoConnection", "overthere-unix");
    }

    private static File createPrivateKeyFile(String privateKey) throws IOException {
        final File privateKeyFile = File.createTempFile("private", ".key");
        privateKeyFile.deleteOnExit();
        CharStreams.write(privateKey, new OutputSupplier<Writer>() {
            @Override
            public Writer getOutput() throws IOException {
                return new FileWriter(privateKeyFile);
            }
        });
        return privateKeyFile;
    }

}
