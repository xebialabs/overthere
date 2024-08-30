package com.xebialabs.overthere;

import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.cifs.CifsConnectionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.xebialabs.overthere.ConnectionOptions.FILE_COPY_COMMAND_FOR_WINDOWS;
import static com.xebialabs.overthere.cifs.BaseCifsConnectionBuilder.CONNECTION_TYPE;

public class ConnectionOptionsUtil {

    private static final Logger logger = LoggerFactory.getLogger(ConnectionOptionsUtil.class);

    public static ConnectionOptions fixOptions(final ConnectionOptions options) {
        CifsConnectionType type = options.getEnum(CONNECTION_TYPE, CifsConnectionType.class);
        if (type.equals(CifsConnectionType.WINRM_NATIVE) || type.equals(CifsConnectionType.WINRM_INTERNAL)) {
            Process process = null;
            ConnectionOptions fixedOptions = new ConnectionOptions(options);
            try {
                process = Runtime.getRuntime().exec("winrs /c \"(dir 2>&1 *`|echo CMD);&<# rem #>echo PowerShell\"");
                process.waitFor();
                String defaultTerminal = new String(process.getInputStream().readAllBytes()).trim();

                if ("PowerShell".equals(defaultTerminal)) {
                    logger.debug("Default terminal is PowerShell");
                    fixedOptions.set(FILE_COPY_COMMAND_FOR_WINDOWS, "echo F|xcopy {0} {1} /y");
                }
            } catch (Exception e) {
                logger.warn("Command to find windows default terminal failed: " + e.getMessage());
            } finally {
                if (process != null) {
                    process.destroy();
                }
            }
            return fixedOptions;
        }
        return options;
    }
}
