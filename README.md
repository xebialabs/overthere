# Project "overthere"
Runs something on a remote machine, i.e. over there.

# TODO
* Write Javadoc (incl. package-info.java) for API (com.xebialabs.overthere package)
* Replace dependency on apache commons with dependency on guava.
* Add more itests, also checking whether the correct exceptions are thrown when files cannot be found, cannot be deleted, etc.
* Run Gradle license plugin.
* Write Javadoc (incl. package-info.java) for SPI (com.xebialabs.overthere.spi package)
* Write Javadoc (incl. package-info.jav for protocol packages.
* Port fixes made in HostSession framework since fork. Including, but not limited to:
  - Port scp -r functionality (OverthereFileCopier and OverthereFileDirectoryWalker have already been copied over).
  - Port fix for sudo-copying from remote to local (file and directory).
* Figure out what to do with the HostFileInputStreamTransformer filter.
* Allow constants to be overridden through ConnectionOptions:
  - Disable automatic removal of temporary files.
  - Connection timeout.
  - Number of attempts to create a temporary file.
  - Enable/disable pesudo-tty on SSH connections.
* Decide whether to support winsshd and/or copssh and/or other Windows SSH implementations. Leave that up to subclasses?
* Document or fix functionality removed from Overthere:
 - SSH pub/private support -> through ConnectionOptions?
 - Untar -> separate utility method, maybe not in here?
 - Unreachable host support/tunneled host session -> needs to be reimplemented in a nice way.
 - inputResponse map: can be re-implemented using OverthereProcess if needed.
 - Copy resource to temp file -> add helpers to plugin-api
 - Copy resource to file -> actually only needed by "copy resource to temp file" method
* Publish Maven artifacts to public repostory instead of XebiaLabs' Nexus repository.

