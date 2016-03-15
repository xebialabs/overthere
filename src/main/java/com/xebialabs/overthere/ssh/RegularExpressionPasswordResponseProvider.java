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

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.schmizz.sshj.userauth.method.ChallengeResponseProvider;
import net.schmizz.sshj.userauth.password.PasswordFinder;
import net.schmizz.sshj.userauth.password.Resource;

/**
 * Sends passwords in response to an keyboard-interactive challenge.
 */
class RegularExpressionPasswordResponseProvider implements ChallengeResponseProvider {
    private static final char[] EMPTY_RESPONSE = new char[0];
    private final Pattern passwordPromptRegex;
    private Resource<?> resource;
    private PasswordFinder passwordFinder;
    private boolean gaveAlready;

    public RegularExpressionPasswordResponseProvider(PasswordFinder pwdf, String passwordPromptRegex) {
        this.passwordFinder = pwdf;
        this.passwordPromptRegex = Pattern.compile(passwordPromptRegex);
    }

    @Override
    public List<String> getSubmethods() {
        return Collections.emptyList();
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void init(Resource resource, String name, String instruction) {
        this.resource = resource;
        logger.debug("Initializing - name=[{}], instruction=[{}]", name, instruction);
    }

    @Override
    public char[] getResponse(String prompt, boolean echo) {
        if (!gaveAlready && !echo && passwordPromptRegex.matcher(prompt).matches()) {
            gaveAlready = true;
            return passwordFinder.reqPassword(resource);
        }
        return EMPTY_RESPONSE;
    }

    @Override
    public boolean shouldRetry() {
        return passwordFinder.shouldRetry(resource);
    }

    private static final Logger logger = LoggerFactory.getLogger(RegularExpressionPasswordResponseProvider.class);
}
