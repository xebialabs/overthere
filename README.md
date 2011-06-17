# Project "overthere"
Runs something on a remote machine, i.e. over there.

# TODO
* Write Javadoc (incl. package-info.java) for API (com.xebialabs.overthere package)
* Write Javadoc (incl. package-info.java) for SPI (com.xebialabs.overthere.spi package)
* Write Javadoc (incl. package-info.jav for protocol packages.
* For small uploads in SCP use in-memory storage instead of a temporary file.
* Port fixes made in HostSession framework since fork. Including, but not limited to:
  - Port fix for sudo-copying from remote to local (file and directory).
* Figure out what to do with the HostFileInputStreamTransformer filter.
* Allow constants to be overridden through ConnectionOptions:
  - Connection timeout.
  - Number of attempts to create a temporary file.
  - Enable/disable pesudo-tty on SSH connections.
* Document or fix functionality removed from Overthere:
 - Untar -> separate utility method, maybe not in here?
 - Unreachable host support/tunneled host session -> needs to be reimplemented in a nice way.
* Publish Maven artifacts to public repostory instead of XebiaLabs' Nexus repository.

