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
package com.xebialabs.overthere.ssh;

import static com.xebialabs.overthere.OperatingSystemFamily.UNIX;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.xebialabs.overthere.ConnectionOptions;

public class SshScpConnectionItest extends SshConnectionItestBase {

	@Override
	protected void setTypeAndOptions() {
		type = "ssh_scp";
		options = new ConnectionOptions();
		options.set("address", "overthere");
		options.set("username", "overthere");
		options.set("privateKeyFile", createPrivateKeyFile("-----BEGIN RSA PRIVATE KEY-----\r\n" + "Proc-Type: 4,ENCRYPTED\r\n" + "DEK-Info: AES-128-CBC,EB6F3B5C2F847A0F47AC9C8C757E0AD8\r\n"
		        + "IvSSAW5r/sPwtH99/csjEZdtnUci80ak0E+7Puvjo4+H4r+ObFF1gEHOUaNi1rf+\r\n" + "5EDhqtPWy6q1zDYBjOdG5jnDhiVSv2P8ZsxA+w1xVR6Lrm5Q5XWcNO+/xXp2/WUc\r\n"
		        + "ae1KsyDKCYAsMwD3TaXs01aNrzAD58kNBvFZLkQh8y8ilDiHec0IMtOJHHi2rW0T\r\n" + "42crCMyvMvnZNVEQrbXLeThQrAmDWALXrQ14p/D9yEQftv+yDjNnnPB7sjEM1cdi\r\n"
		        + "dwM7kHV1YtkYE+mlBPONAqNQURVkVUSyNGgF7qLMhW6UUH8pboXcILFrqEvHPBWn\r\n" + "lhnMH1VJUEB0K6r9BvwNKQlNHbAdghT/2oTANzirPXZX7ZTg4Tq6IPAVkDMYARu6\r\n"
		        + "PxfLXgt6qCQkT3W++bHPQSFaE6vwPneGKcqtKHThtmJbqoxwK5eD4cccV9OaaSUU\r\n" + "lHtzTL0NJL0HCH5UQUumD+8CiRvY+11q3+fJXuMl6Nc/Fi9tLUMfrziOVbl5FIo/\r\n"
		        + "tRnQx0h2touaZdig29tOHqJMjqu2vcRGmzkU/DNnELiTAlQE5OeuuIxOmZU4yEcb\r\n" + "qAXvRafPTTfXXWatND1Y1RuWaw7ccfh/ZOVL2yhhozHU6978rX3XaD7eS/HiSJIu\r\n"
		        + "1qOxk6qkx3PZNrlBjGwNKcblqZTQX9adL+fsQwZaVwaNa9cSAs9wLOmdvpRH4ZsS\r\n" + "Q7BmEmEpXhLh/v7ZIyhih30MyOSPh42X/QldBBaXUJOdV3T/vmH20a3avEg1pqGA\r\n"
		        + "ZDn1LNnHAMdp8hcWGOkjM8AJuUWPJ9+AM5+7TSZ0qlF4q0yGWWnkCO7agQ4PBAXR\r\n" + "3/ghYccVKiel6KBPesbjwGA/Ajmzkkp/iAHuvOVuSQdbJdZS+nAiYr5vkf1MUNWI\r\n"
		        + "qst4zXrNOuhjCtPn5dki0uuUpb8tF5QhOByW79aD1wcF5NzgCEZkyO1Y+CCe9PNM\r\n" + "V9VFqtlmESGcNpBFVk7NamQOnFzu7nE+IWUXhe2i8D1z0gyrt05ES5MQ1W9yxytb\r\n"
		        + "f0j4n1ELNRUTQk54EU9cOMwLHgoU1hNgFJk6zL7dRG3J7S8GGqYJZjjoQbRwloQq\r\n" + "FxF2BowFV7TL/hvrS/UydR7guFDlHq/JhnC/HYQJGDGmmuHEzZNLsnWp+kY0q96g\r\n"
		        + "wLydnrqwYmAgk4jxdoxXIj5lolAWFPf51+TnIr3DWJiveE7EP7JW0cNMKqW5x63F\r\n" + "g2qJ0LjFVutyuxm4j4joF7DFGxAHbl8BG3aAOzmKbiNnPxv5O1nmZcvgjtIoC0+G\r\n"
		        + "yvE0S/S4eY57NL8zJ2/cvTZfe0QLqJF2lYN4RmNhjXY69MC/quy/Ida2p+noxVqP\r\n" + "R2a/uGVMCAT38bX66YXVI4Muc2dsxH96pMh7OiOb6/KoWx6+IFoKxFL0AvC5qFeo\r\n"
		        + "TzIHrzs33O6ejXFXamXqRfnobGdvHijaNdAPFJhIfrA2OWoGaAZpiQKdTaCQXBbx\r\n" + "8q65UtXR7Cv2+vJ7B1gkHMTY0qxwJD0b55KGgjhvIcGR16TrIe24puJbpMvAdP1J\r\n"
		        + "w42kSNNrOIp8/Bq/t+6sUEqd7Cz7dzl/xi5s6CxJuq3U3ypfCdL9Ij0K8EiOa/a2\r\n" + "-----END RSA PRIVATE KEY-----\r\n").getPath());
		options.set("passphrase", "letmein");
		options.set("os", UNIX);
	}

	@Test
	public void hostSessionIsAnSshScpHostSession() {
		assertThat(connection, instanceOf(SshScpConnection.class));
	}

}
