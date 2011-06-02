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
package com.xebialabs.overthere;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Ignore;
import org.junit.Test;

public class OverthereConnectionTest {

	@Test
	@Ignore("Test worked before but not anymore. Need to read up on Mockito.")
	public void getTempFileWithSingleArgumentInvokesTemplateMethod() throws SecurityException, NoSuchMethodException {
		String prefix1 = "prefix";
		String suffix1 = ".suffix";
		String prefix2 = "thereisnosuffix";
		String suffix2 = "";

		OverthereConnection session = mock(OverthereConnection.class);
		when(session.getTempFile(anyString())).thenCallRealMethod();

		session.getTempFile(prefix1 + suffix1);
		session.getTempFile(prefix2 + suffix2);
		session.getTempFile(null);
		session.getTempFile("");
		session.getTempFile(" ");
		verify(session).getTempFile(prefix1, suffix1);
		verify(session).getTempFile(prefix2, ".");
		verify(session, times(3)).getTempFile("hostsession", ".tmp");
	}

}

