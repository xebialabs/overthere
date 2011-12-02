package com.xebialabs.overthere.cifs;

import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.DEFAULT_CIFS_PORT;
import static java.lang.String.format;
import static java.util.regex.Pattern.quote;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import com.xebialabs.overthere.RuntimeIOException;

/**
 * Conversions to/from UNC, SMB and Windows file paths
 */
class PathEncoder {
    private final String smbUrlPrefix;

    PathEncoder(String username, String password, String address, int cifsPort) {
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
        smbUrl.append(hostPath.charAt(0));
        smbUrl.append("$/");
        if (hostPath.length() >= 3) {
            if (hostPath.charAt(2) != '\\') {
                throw new IllegalArgumentException(format("Host path '%s' does not have a backslash (\\) as its third character", hostPath));
            }
            smbUrl.append(hostPath.substring(3).replace('\\', '/'));
        }
        return smbUrl.toString();
    }

    /**
     * @param uncPath
     *            the UNC path to convert to a Windows file path
     * @return the Windows file path representing the UNC path
     */
    static final String fromUncPath(String uncPath) {
        int slashPos = uncPath.indexOf('\\', 2);
        String drive = uncPath.substring(slashPos + 1, slashPos + 2);
        String path = uncPath.substring(slashPos + 3);
        return drive + ":" + path;
    }
}
