# Project "overthere"
Runs something on a remote machine, ie. over there.

# TODO
* make sure all java.io.File methods are properly overridden.
* publish Maven artifacts to public repostory instead of XebiaLabs' Nexus repository.
* determine semantics of delete, mkdir, mkdirs, renameTo with respect to errors.
* rename HostConnection to OverthereConnection?
* add OverthereFile.valueOf().
* port fixes made in HostSession framework since fork.
  * port CmdLine approach to encoding passwords in command lines.
  * port scp -r functionality.
  * port fix for sudo-copying from remote to local (file and directory).
* figure out what to do with the HostFileInputStreamTransformer filter.
* figure out what to do with HostFileUtils.
  * move copy to OverthereFile?
* decide whether to support winsshd and/or copssh and/or other Windows SSH implementations. Leave that up to subclasses?
* implement SSH pub/private key implementation in a nicer manner.
* implement not-removing temporary directories in a nicer manner, or at least with a more up-to-date key name.
* remove HostConnection.copyToTemporaryFile?
* redesign InputResponse map?
* fix functionality removed from Overthere:
 - untar -> separate utility method, maybe not in here?
 - copy resource to temp file -> add helpers to plugin-api
 - copy resource to file -> actually only needed by "copy resource to temp file" method
 - unreachable host support/tunneled host session -> needs to be reimplemented in a nice way.
* need to be able to set default values for synthetic properties (port).
* need to be able to define abstract synthetic types (SshHost). Then temporaryDirectoryPath could also be defined in an abstract base class below Host.
* add sources and javadoc to distribution.
* add method to build paths to OperatingSystemFamily.

