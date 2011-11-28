/* License added by: GRADLE-LICENSE-PLUGIN
 *
 * Copyright 2008-2011 XebiaLabs
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


