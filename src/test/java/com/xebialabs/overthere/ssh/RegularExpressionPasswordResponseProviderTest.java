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
package com.xebialabs.overthere.ssh;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import net.schmizz.sshj.userauth.password.PasswordFinder;
import net.schmizz.sshj.userauth.password.Resource;

import static com.xebialabs.overthere.ssh.SshConnectionBuilder.INTERACTIVE_KEYBOARD_AUTH_PROMPT_REGEX_DEFAULT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Mockito.mock;

public class RegularExpressionPasswordResponseProviderTest {

    private static final char[] SECRET_PASSWORD = "secret".toCharArray();
    private RegularExpressionPasswordResponseProvider provider;

    @BeforeMethod
    public void setup() {
        provider = new RegularExpressionPasswordResponseProvider(new PasswordFinder() {
            @Override
            public char[] reqPassword(Resource<?> resource) {
                return SECRET_PASSWORD;
            }

            @Override
            public boolean shouldRetry(Resource<?> resource) {
                return false;
            }
        }, INTERACTIVE_KEYBOARD_AUTH_PROMPT_REGEX_DEFAULT);
        provider.init(mock(Resource.class), null, null);
    }

    @Test
    public void shouldRespondWithPasswordToDefaultPrompt() {
        assertThat(provider.getResponse("Password: ", false), equalTo(SECRET_PASSWORD));
    }

    @Test
    public void shouldResponseWithPasswordToNonDefaultPrompt() {
        assertThat(provider.getResponse("user's Password: ", false), equalTo(SECRET_PASSWORD));
    }

}
