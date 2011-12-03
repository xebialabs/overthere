package com.xebialabs.overthere.cifs;

import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.DEFAULT_CIFS_PORT;
import static java.lang.String.format;
import static java.util.regex.Pattern.quote;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.xebialabs.overthere.RuntimeIOException;

/**
 * Conversions to/from UNC, SMB and Windows file paths
 */
class PathEncoder {
    // "\\host-name\share" or "\\host-name\share\" or "\\host-name\share\path"
    private static final Pattern UNC_PATH_PATTERN = Pattern.compile("\\\\\\\\[^\\\\]+\\\\([^\\\\]+)(\\\\.*)?");
    private static final char WINDOWS_SEPARATOR = '\\';

    private final String smbUrlPrefix;
    private final DriveMappings driveMappings;

    PathEncoder(String username, String password, String address, int cifsPort,
            Map<String, String> driveMappings) {
        StringBuilder urlPrefix = new StringBuilder();
        urlPrefix.append("smb://");
        urlPrefix.append(urlEncode(username.replaceFirst(quote("\\"), ";")));
        urlPrefix.append(":");
        urlPrefix.append(urlEncode(password));
        urlPrefix.append("@");
        urlPrefix.append(urlEncode(address));
        if (cifsPort != DEFAULT_CIFS_PORT) {
            urlPrefix.append(":");
            urlPrefix.append(cifsPort);
        }
        urlPrefix.append("/");
        this.smbUrlPrefix = urlPrefix.toString();
        this.driveMappings = new DriveMappings(driveMappings);
    }

    private static String urlEncode(String value) {
        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeIOException("Unable to construct SMB URL", e);
        }
    }

    String toSmbUrl(String hostPath) {
        if (hostPath.length() < 2) {
            throw new IllegalArgumentException(format("Host path '%s' is too short", hostPath));
        }

        if (hostPath.charAt(1) != ':') {
            throw new IllegalArgumentException(format("Host path '%s' does not have a colon (:) as its second character", hostPath));
        }

        StringBuilder smbUrl = new StringBuilder(smbUrlPrefix);
        smbUrl.append(driveMappings.toShare(hostPath.substring(0, 1)));
        smbUrl.append("/");
        if (hostPath.length() >= 3) {
            if (hostPath.charAt(2) != WINDOWS_SEPARATOR) {
                throw new IllegalArgumentException(format("Host path '%s' does not have a backslash (\\) as its third character", hostPath));
            }
            smbUrl.append(hostPath.substring(3).replace(WINDOWS_SEPARATOR, '/'));
        }
        return smbUrl.toString();
    }

    /**
     * @param uncPath
     *            the UNC path to convert to a Windows file path
     * @return the Windows file path representing the UNC path
     */
    final String fromUncPath(String uncPath) {
        Matcher matcher = UNC_PATH_PATTERN.matcher(uncPath);
        if (!matcher.matches()) {
            throw new IllegalArgumentException(format("UNC path '%s' did not match expected expression '%s'", uncPath, UNC_PATH_PATTERN));
        }
        String path = matcher.group(2);
        return format("%s:%s", driveMappings.toDrive(matcher.group(1)), 
                ((path == null) ? WINDOWS_SEPARATOR : path));
    }

    private static class DriveMappings {
        private static final Pattern ADMINISTRATIVE_SHARE_PATTERN = Pattern.compile("[a-zA-Z]\\$");

        private final BiMap<String, String> mappings;

        private DriveMappings(Map<String, String> mappings) {
            this.mappings = ImmutableBiMap.copyOf(mappings);
        }

        /**
         * @return the mapping (share name) for the given drive letter, 
         *      or the administrative share if no share name has been specified
         */
        private String toShare(String drive) {
            return (mappings.containsKey(drive) ? mappings.get(drive) : format("%s$", drive));
        }

        /**
         * @return for a drive mapped to a share, the drive letter given share name;
         *      for an administrative share, the drive letter 
         */
        private String toDrive(String share) {
            if (mappings.containsValue(share)) {
                return mappings.inverse().get(share);
            } else if (ADMINISTRATIVE_SHARE_PATTERN.matcher(share).matches()) {
                // will be two characters in length
                return share.substring(0, 1);
            } else {
                throw new IllegalArgumentException(format("Share name '%s' was neither mapped to a drive nor an administrative share", share));
            }
        }
    }
}
