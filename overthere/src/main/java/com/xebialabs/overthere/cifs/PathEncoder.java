package com.xebialabs.overthere.cifs;

import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.DEFAULT_CIFS_PORT;
import static java.lang.String.format;
import static java.util.regex.Pattern.quote;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Iterables;
import com.xebialabs.overthere.RuntimeIOException;

/**
 * Conversions to/from UNC, SMB and Windows file paths
 */
class PathEncoder {
    // "\\host-name\share" or "\\host-name\share\" or "\\host-name\share\path"
    private static final Pattern UNC_PATH_PATTERN = Pattern.compile("\\\\\\\\[^\\\\]+\\\\([^\\\\]+(?:\\\\.*)?)");
    private static final char WINDOWS_SEPARATOR = '\\';
    private static final char SMB_URL_SEPARATOR = '/';

    private final String smbUrlPrefix;
    private final PathMapper pathMapper;

    PathEncoder(String username, String password, String address, int cifsPort,
            Map<String, String> pathMappings) {
        StringBuilder urlPrefix = new StringBuilder();
        urlPrefix.append("smb://");
        // this is *not* the Windows file separator ;-)
        urlPrefix.append(urlEncode(username.replaceFirst(quote("\\"), ";")));
        urlPrefix.append(":");
        urlPrefix.append(urlEncode(password));
        urlPrefix.append("@");
        urlPrefix.append(urlEncode(address));
        if (cifsPort != DEFAULT_CIFS_PORT) {
            urlPrefix.append(":");
            urlPrefix.append(cifsPort);
        }
        urlPrefix.append(SMB_URL_SEPARATOR);
        this.smbUrlPrefix = urlPrefix.toString();
        this.pathMapper = new PathMapper(pathMappings);
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

        if (hostPath.length() >= 3) {
            if (hostPath.charAt(2) != WINDOWS_SEPARATOR) {
                throw new IllegalArgumentException(format("Host path '%s' does not have a backslash (\\) as its third character", hostPath));
            }
        }

        StringBuilder smbUrl = new StringBuilder(smbUrlPrefix);
        smbUrl.append(pathMapper.toSharedPath(hostPath).replace(WINDOWS_SEPARATOR, SMB_URL_SEPARATOR));
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
        return pathMapper.toLocalPath(matcher.group(1));
    }

    @VisibleForTesting
    static class PathMapper {
        private static final String DRIVE_DESIGNATOR = ":";
        private static final String ADMIN_SHARE_DESIGNATOR = "$";
        private static final Pattern ADMIN_SHARE_PATTERN = 
            Pattern.compile("[a-zA-Z]" + quote(ADMIN_SHARE_DESIGNATOR));

        private final SortedMap<String, String> sharesForPaths;
        private final Map<String, String> pathsForShares;

        @VisibleForTesting
        PathMapper(Map<String, String> mappings) {
            // longest first, so reverse lexicographical order
            ImmutableSortedMap.Builder<String, String> sharesForPath = ImmutableSortedMap.reverseOrder();
            ImmutableMap.Builder<String, String> pathsForShare = ImmutableMap.builder();
            for (Entry<String, String> mapping : mappings.entrySet()) {
                String pathPrefixToMatch = mapping.getKey();
                String shareForPathPrefix = mapping.getValue();
                sharesForPath.put(pathPrefixToMatch.toLowerCase(), shareForPathPrefix);
                pathsForShare.put(shareForPathPrefix.toLowerCase(), pathPrefixToMatch);
            }
            this.sharesForPaths = sharesForPath.build();
            this.pathsForShares = pathsForShare.build();
        }

        /**
         * Attempts to use provided path-to-share mappings to convert the given
         * local path to a remotely accessible path, using the longest matching
         * prefix if available.
         * <p>
         * Falls back to using administrative shares if none of the explicit
         * mappings applies to the path to convert.
         *  
         * @param path the local path to convert
         * @return the remotely accessible path (using shares) at which the local
         *      path can be accessed using SMB
         */
        @VisibleForTesting
        String toSharedPath(String path) {
            final String lowerCasePath = path.toLowerCase();
            // assumes correct format drive: or drive:\path
            String mappedPathPrefix = Iterables.find(sharesForPaths.keySet(), 
                new Predicate<String>() {
                    @Override
                    public boolean apply(String input) {
                        return lowerCasePath.startsWith(input);
                    }
                },
                null);
            // the share + the remainder of the path if found, otherwise the path with ':' replaced by '$'
            return ((mappedPathPrefix != null)
                    ? sharesForPaths.get(mappedPathPrefix) + path.substring(mappedPathPrefix.length())
                    : path.substring(0, 1) + ADMIN_SHARE_DESIGNATOR + path.substring(2));
        }

        /**
         * @param path the remotely accessible path to convert (minus the host name, i.e. beginning with the share)
         * @return the local path (using drive letters) corresponding to the
         *      path that is remotely accessible using SMB
         */
        @VisibleForTesting
        String toLocalPath(String path) {
            final String lowerCasePath = path.toLowerCase();
            // assumes correct format share or share\path
            String mappedShare = Iterables.find(pathsForShares.keySet(), 
                new Predicate<String>() {
                    @Override
                    public boolean apply(String input) {
                        return lowerCasePath.startsWith(input);
                    }
                }, 
                null);
            if (mappedShare != null) {
                return pathsForShares.get(mappedShare) + path.substring(mappedShare.length());
            } else if ((path.length() >= 2) 
                    && ADMIN_SHARE_PATTERN.matcher(path.substring(0, 2)).matches()) {
                return path.substring(0, 1) + DRIVE_DESIGNATOR + path.substring(2);
            } else {
                throw new IllegalArgumentException(format("Remote path name '%s' uses unrecognized (i.e. neither mapped nor administrative) share", path));
            }
        }
    }
}
