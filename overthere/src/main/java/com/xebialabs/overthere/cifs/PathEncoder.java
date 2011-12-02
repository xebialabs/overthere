package com.xebialabs.overthere.cifs;

/**
 * Conversions to/from UNC, SMB and Windows file paths
 */
class PathEncoder {

    /**
     * @param uncPath   the UNC path to convert to a Windows file path
     * @return  the Windows file path representing the UNC path
     */
    static final String fromUncPath(String uncPath) {
        int slashPos = uncPath.indexOf('\\', 2);
        String drive = uncPath.substring(slashPos + 1, slashPos + 2);
        String path = uncPath.substring(slashPos + 3);
        return drive + ":" + path;
    }
}
