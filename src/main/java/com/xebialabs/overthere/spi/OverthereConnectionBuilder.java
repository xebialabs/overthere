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
package com.xebialabs.overthere.spi;

import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.OverthereConnection;

/**
 * Implemented by {@link Protocol protocol} implementations.
 */
public interface OverthereConnectionBuilder {

	/**
	 * Creates the connection that corresponds to the arguments that were passed in the two-arg constructor of this class. The first argument is a
	 * {@link String} that specifies the type (and is identical to the name field of the {@link Protocol} annotation). The second argument is the
	 * {@link ConnectionOptions}.
	 * 
	 * @return the created connection.
	 */
	OverthereConnection connect();
}

