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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public abstract class SshSudoHostConnectionItestBase extends SshHostConnectionItestBase {

	@Test
	public void commandWithPipeShouldHaveTwoSudoSections() {
		String[] prepended = ((SshSudoHostConnection) connection).prependSudoCommand("a", "|", "b");
		assertThat(prepended.length, equalTo(9));
		assertThat(prepended[0], equalTo("sudo"));
		assertThat(prepended[5], equalTo("sudo"));
	}

	@Test
	public void commandWithSemiColonShouldHaveTwoSudoSections() {
		String[] prepended = ((SshSudoHostConnection) connection).prependSudoCommand("a", ";", "b");
		assertThat(prepended.length, equalTo(9));
		assertThat(prepended[0], equalTo("sudo"));
		assertThat(prepended[5], equalTo("sudo"));
	}

}

