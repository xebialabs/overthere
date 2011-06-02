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

import java.util.List;

import org.junit.Test;

import com.xebialabs.overthere.CmdLine;
import com.xebialabs.overthere.CmdLineArgument;

public abstract class SshSudoOverthereConnectionItestBase extends SshOverthereConnectionItestBase {

	@Test
	public void commandWithPipeShouldHaveTwoSudoSections() {
		List<CmdLineArgument> prepended = ((SshSudoOverthereConnection) connection).prefixWithSudoCommand(CmdLine.build("a", "|", "b")).getArguments();
		assertThat(prepended.size(), equalTo(9));
		assertThat(prepended.get(0).toString(false), equalTo("sudo"));
		assertThat(prepended.get(5).toString(false), equalTo("sudo"));
	}

	@Test
	public void commandWithSemiColonShouldHaveTwoSudoSections() {
		List<CmdLineArgument> prepended = ((SshSudoOverthereConnection) connection).prefixWithSudoCommand(CmdLine.build("a", ";", "b")).getArguments();
		assertThat(prepended.size(), equalTo(9));
		assertThat(prepended.get(0).toString(false), equalTo("sudo"));
		assertThat(prepended.get(5).toString(false), equalTo("sudo"));
	}

}
