# Project "overthere"
Runs something on a remote machine, i.e. over there.

# TODO
* Clean up tempFile code scattered throughout codebase.
* Clean up directory walker code and transformations.
* Allow constants to be overridden through ConnectionOptions:
  - Connection timeout.
  - Number of attempts to create a temporary file.
  - Enable/disable pesudo-tty on SSH connections.
* Document or fix functionality removed from Overthere:
 - Untar -> separate utility method, maybe not in here?
 - Unreachable host support/tunneled host session -> needs to be reimplemented in a nice way.
* Publish Maven artifacts to public repostory instead of XebiaLabs' Nexus repository.

