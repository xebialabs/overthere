# Project "overthere"
Runs something on a remote machine, ie. over there.

# TODO
* make sure all java.io.File methods are properly overridden. (1)
* publish Maven artifacts to public repostory instead of XebiaLabs' Nexus repository.
* determine semantics of delete, mkdir, mkdirs, renameTo with respect to errors. (1)
* rename HostConnection to OverthereConnection? (4)
* add OverthereFile.valueOf(). (4)
* port fixes made in HostSession framework since fork, including but not limited to: (3)
  * port CmdLine approach to encoding passwords in command lines.
  * port scp -r functionality.
  * port fix for sudo-copying from remote to local (file and directory).
* figure out what to do with the HostFileInputStreamTransformer filter. (2)
* figure out what to do with HostFileUtils. (2)
  * move copy to OverthereFile?
* decide whether to support winsshd and/or copssh and/or other Windows SSH implementations. Leave that up to subclasses? (2)
* implement SSH pub/private key implementation in a nicer manner. (2)
* implement not-removing temporary directories in a nicer manner, or at least with a more up-to-date key name. (2)
* remove HostConnection.copyToTemporaryFile? (2)
* redesign InputResponse map? (2)
* fix functionality removed from Overthere: (2)
 - untar -> separate utility method, maybe not in here?
 - copy resource to temp file -> add helpers to plugin-api
 - copy resource to file -> actually only needed by "copy resource to temp file" method
 - unreachable host support/tunneled host session -> needs to be reimplemented in a nice way.
* need to be able to define abstract synthetic types (SshHost). Then temporaryDirectoryPath could also be defined in an abstract base class below Host. (4)
* add sources and javadoc to distribution. (4)
* add method to build paths to OperatingSystemFamily. (4)

