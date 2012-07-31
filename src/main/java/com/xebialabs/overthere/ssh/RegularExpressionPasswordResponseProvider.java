package com.xebialabs.overthere.ssh;

import net.schmizz.sshj.userauth.method.ChallengeResponseProvider;
import net.schmizz.sshj.userauth.password.PasswordFinder;
import net.schmizz.sshj.userauth.password.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

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
        logger.debug("Challenge - name=`{}`; instruction=`{}`", name, instruction);
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
