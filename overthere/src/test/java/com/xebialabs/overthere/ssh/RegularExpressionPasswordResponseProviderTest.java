/* License added by: GRADLE-LICENSE-PLUGIN
 *
 * Copyright 2008-2012 XebiaLabs
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.xebialabs.overthere.ssh;

import static com.xebialabs.overthere.ssh.SshConnectionBuilder.INTERACTIVE_KEYBOARD_AUTH_PROMPT_REGEX_DEFAULT;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import net.schmizz.sshj.userauth.password.PasswordFinder;
import net.schmizz.sshj.userauth.password.Resource;

import org.junit.Before;
import org.junit.Test;

public class RegularExpressionPasswordResponseProviderTest {

	private static final char[] SECRET_PASSWORD = "secret".toCharArray();
	private RegularExpressionPasswordResponseProvider provider;

	@Before
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

