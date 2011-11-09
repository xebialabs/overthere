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
