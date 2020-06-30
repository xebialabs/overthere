<a name="toc"></a>
# Table of Contents

* [Introduction](#introduction)
* [Getting Overthere](#getting_overthere)
	* [Depending on Overthere](#depending_on_overthere)
	* [Building Overthere](#building_overthere)
	* [Running the Examples](#running_the_examples)
* [Programming Overthere](#programming_overthere)
* [Configuring Overthere](#configuring_overthere)
	* [Protocols](#protocols)
	* [Common connection options](#common_connection_options)
	* [Local](#local)
	* [SSH](#ssh)
	    * [Host setup](#ssh_host_setup)
	    * [Troubleshooting](#ssh_troubleshooting)
	    * [Connection options](#ssh_connection_options)
	* [SMB 2.x/CIFS, WinRM and Telnet](#smb_cifs)
	    * [SMB 2.x](#smb)
	    * [CIFS](#cifs)
	    * [Host setup](#smb_cifs_host_setup)
	    * [Troubleshooting](#smb_cifs_troubleshooting)
	    * [Connection options](#smb_cifs_connection_options)
	* [Jumpstations: SSH tunnels and HTTP proxies](#jumpstations)
* [Release History](#release_history)


<a name="introduction"></a>
# Introduction

Overthere is a Java library to manipulate files and execute processes on remote hosts, i.e. do stuff "over there". Overthere was originally developed for and is used in the [XebiaLabs](http://xebialabs.com/) deployment automation product Deployit as a way to perform tasks on remote hosts, e.g. copy configuration files, install EAR files or restart web servers. Another way of looking at it is to say that Overthere gives you `java.io.File` and `java.lang.Process` as they should've been: as interfaces, created by a factory and extensible through an SPI mechanism.

Overthere is available under the [GPLv2 with XebiaLabs FLOSS License Exception](https://raw.github.com/xebialabs/overthere/master/LICENSE).

__P.S.:__ Check the [Overthere Ohloh page](http://www.ohloh.net/p/overthere) for some interesting code analysis statistics. If you use Overthere, don't forget to tell Ohloh! And while you're at it, you might want to vote for Overthere on the [Overthere Freecode page](http://freecode.com/projects/overthere) too! ;-)

<a name="getting_overthere"></a>
# Getting Overthere

To get Overthere, you have two options:

1. Add a dependency to Overthere to your project.
1. Build Overthere yourself.

And, if you want, you can also run the Overthere examples used in the Overthere presentation mentioned above.

Binary releases of Overthere are not provided here, but you can download it [straight from the Maven Central repository](http://search.maven.org/#artifactdetails%7Ccom.xebialabs.overthere%7Coverthere%7C5.0.8%7Cjar) if you want to.

<a name="depending_on_overthere"></a>
## Depending on Overthere

1. If your project is built with Maven, add the following dependency to the pom.xml:

		<dependency>
			<groupId>com.xebialabs.overthere</groupId>
			<artifactId>overthere</artifactId>
			<version>5.0.8</version>
		</dependency>

1. If your project is built using another build tool that uses the Maven Central repository, translate these dependencies into the format used by your build tool.

<a name="building_overthere"></a>
## Building Overthere

1. Clone the Overthere repository.
1. On unix run the command `./gradlew clean build`, on windows run `gradlew clean build`

<a name="running_the_examples"></a>
## Running the examples

1. Install [Maven 2.2.1](http://maven.apache.org/) or up.
1. Clone the Overthere repository.
1. Go into the `examples` directory and run the command `mvn eclipse:eclipse`.
1. Import the `examples` project into Eclipse.
1. Change the login details in the example classes (address, username and password) and run them!

<a name="programming_overthere"></a>
# Programming Overthere

To program Overthere, browse the source code, check the examples and browse the Overthere <a href="http://xebialabs.github.io/overthere/javadoc">Javadoc</a>.

For a more thorough introduction to Overthere, check the [presentation on Overthere](http://www.slideshare.net/vpartington/presentation-about-overthere-for-jfall-2011) that I gave for J-Fall 2011, a Java conference in the Netherlands (in English).

<a name="configuring_overthere"></a>
# Configuring Overthere

The protocols that Overthere uses to connect to remote hosts, such as SSH, CIFS, Telnet and WinRM, are existing protocols for which support is built into many platforms. As such you will not need to install any custom software on the remote hosts. Nevertheless in some cases the remote hosts have to be configured to correctly work with Overthere. Also, Overthere has a number of configuration features that allow you tweak the way it interfaces with the remote hosts.

<a name="protocols"></a>
## Protocols

Overthere supports a number of protocols to connect to remote hosts:

* [__local__](#local) - a connection to the local host. This is a wrapper around <a href="http://download.oracle.com/javase/6/docs/api/java/io/File.html"></code>java.io.File</code></a> and <a href="http://docs.oracle.com/javase/6/docs/api/java/lang/Process.html"></code>java.lang.Process</code></a>.
* [__ssh__](#ssh) - a connection using the [SSH protocol](http://en.wikipedia.org/wiki/Secure_Shell), to a Unix host, to a z/OS host, or to a Windows host running either OpenSSH on Cygwin (i.e. COPSSH) or WinSSHD.
* [__smb__](#smb) -  a connection using the prevalent [SMB protocol](http://en.wikipedia.org/wiki/Server_Message_Block) for file manipulation and, depending on the settings, using either [WinRM](http://en.wikipedia.org/wiki/WS-Management) or [Telnet](http://en.wikipedia.org/wiki/Telnet) for process execution. This protocol is only supported for Windows hosts.
* [__cifs__](#cifs) - a connection using public variant of the original Server Message Block (SMB) protocol developed by Microsoft known as [CIFS protocol](http://en.wikipedia.org/wiki/Server_Message_Block), for file manipulation and, depending on the settings, using either [WinRM](http://en.wikipedia.org/wiki/WS-Management) or [Telnet](http://en.wikipedia.org/wiki/Telnet) for process execution. This protocol is only supported for Windows hosts, CIFS is widely regarded as an obsolete protocol and users are encouraged to prefer a SMB protocol over a CIFS. Support for CIFS is deprecated and will be removed from subsequent releases. 
* [__ssh-jumpstation__](#jumpstations) - a special protocol type that can only be used as a jumpstation protocol, which allows a connection to be created over an [SSH jumpstation](https://en.wikipedia.org/wiki/Port_forwarding#Local_port_forwarding).
* [__proxy__](#jumpstations) - a special protocol type that can only be used as a jumpstation protocol, which allows a connection to be created over an [HTTP proxy](https://en.wikipedia.org/wiki/HTTP_tunnel).

<a name="common_connection_options"></a>
## Common connection options

Apart from selecting a protocol to use, you will also need to supply a number of connection options when creating a connection. Common connection options are:

<table>
<tr>
    <th align="left" valign="top"><a name="protocol"></a>protocol</th>
    <td>This option is only used when it is present in the connection options of a tunnel (jumpstation) connection. It indicates which protocol is used to tunnel the other connection over. This property can be set to <code>ssh</code> or <code>proxy</code>. If this option is omitted in the jumpstation options, <code>ssh</code> is assumed.</td>
</tr>
<tr>
	<th align="left" valign="top"><a name="os"></a>os</th>
	<td>The operating system of the remote host. This property can be set to <code>UNIX</code>, <code>WINDOWS</code>, and <code>ZOS</code> and is used to
	    determine how to encode paths and commands and to determine the default temporary directory path. This property is required for all protocols, except
	    for the <strong>local</strong> protocol where it is automatically determined.</td>
</tr>
<tr>
	<th align="left" valign="top"><a name="address"></a>address</th>
	<td>The address of the remote host, either an IP address or a DNS name. This property is required for all protocols, except for the <strong>local</strong>
	    protocol.</td>
</tr>
<tr>
	<th align="left" valign="top"><a name="port"></a>port</th>
	<td>The port to use when connecting to the remote host. The interpretation and the default value for this connection option depend on the protocol that is
	    used.</td>
</tr>
<tr>
	<th align="left" valign="top"><a name="username"></a>username</th>
	<td>The username to use when connecting to the remote host. This property is required for all protocols, except for the <strong>local</strong>
	protocol.</td>
</tr>
<tr>
	<th align="left" valign="top"><a name="password"></a>password</th>
	<td>The password to use. This property is required for all protocols, except for the <strong>local</strong> protocol.</td>
</tr>
<tr>
	<th align="left" valign="top"><a name="tmp"></a>tmp</th>
	<td>The temporary directory. For each connection, a <em>connection temporary directory</em> with a name like
	    <code>overthere-20111128T132600-7234435.tmp</code> is created within this temporary directory, e.g.
	    <code>/tmp/overthere-20111128T132600-7234435.tmp</code>, to store temporary files for the duration of the connection.<br/> The default value is
	    <code>tmp</code> for UNIX and z/OS hosts and <code>C:\windows\temp</code> for Windows hosts, except for the <strong>local</strong> protocol where
	    the default is the value of the <code>java.io.tmpdir</code> system property.</td>
</tr>
<tr>
	<th align="left" valign="top"><a name="tmpFileCreationRetries"></a>tmpFileCreationRetries</th>
	<td>The number of times Overthere attempts to create a temporary file with a unique name. The default value is <code>100</code>.</td>
</tr>
<tr>
	<th align="left" valign="top"><a name="tmpDeleteOnDisconnect"></a>tmpDeleteOnDisconnect</th>
	<td>If set to <code>false</code>, the connection temporary directory is not removed when the connection. The default value is <code>true</code>.</td>
</tr>
<tr>
	<th align="left" valign="top"><a name="connectionTimeoutMillis"></a>connectionTimeoutMillis</th>
	<td>The number of milliseconds Overthere waits for a connection to a remote host to be established. The default value is <code>120000</code>, i.e.
	    2 minutes.</td>
</tr>
<tr>
	<th align="left" valign="top"><a name="socketTimeoutMillis"></a>socketTimeoutMillis</th>
	<td>The number of milliseconds Overthere will waits when no data is received on an open connection before raising exception. The default value is <code>0</code>, i.e.
	    infinite timeout.</td>
</tr>
<tr>
	<th align="left" valign="top"><a name="jumpstation"></a>jumpstation</th>
	<td>If set to a non-null value, this property contains the connection options used to connect to an SSH jumpstation (See
	    <a href="#tunnelling">Tunnelling</a>). Recursive configuration is possible, i.e. this property is also available for the connection options of a
	    jumpstation.</td>
</tr>
<tr>
    <th align="left" valign="top"><a name="fileCopyCommandForUnix"></a>fileCopyCommandForUnix</th>
    <td>The command to use when copying a file on a Unix host. The string <code>{0}</code> is replaced with the path of the source file, the string
        <code>{1}</code> is replaced with the path of the destination file. The default value is <code>cp -p {0} {1}</code>.</td>
</tr>
<tr>
    <th align="left" valign="top"><a name="directoryCopyCommandForUnix"></a>directoryCopyCommandForUnix</th>
    <td>The command to use when copying a directory on a Unix host. The string <code>{0}</code> is replaced with the path of the source directory, the string
        <code>{1}</code> is replaced with the path of the destination directory. The default value is <code>cd {1} ; tar -cf - -C {0} . | tar xpf -</code>. If the
        <code>tar</code> command is not available but the <code>find</code> command recognizes the <code>-depth</code> parameter with a value, the alternative
        command <code>find {0} -depth 1 -exec cp -pr {} {1} ;</code> may be configured.</td>
</tr>
<tr>
    <th align="left" valign="top"><a name="fileCopyCommandForWindows"></a>fileCopyCommandForWindows</th>
    <td>The command to use when copying a file on a Windows host. The string <code>{0}</code> is replaced with the path of the source file, the string
        <code>{1}</code> is replaced with the path of the destination file. The default value is <code>copy {0} {1} /y</code>.</td>
</tr>
<tr>
    <th align="left" valign="top"><a name="directoryCopyCommandForWindows"></a>directoryCopyCommandForWindows</th>
    <td>The command to use when copying a directory on a Windows host. The string <code>{0}</code> is replaced with the path of the source directory, the string
        <code>{1}</code> is replaced with the path of the destination directory. The default value is <code>xcopy {0} {1} /i /y /s /e /h /q</code>.</td>
</tr>
<tr>
    <th align="left" valign="top"><a name="fileCopyCommandForZos"></a>fileCopyCommandForZos</th>
    <td>The command to use when copying a file on a z/OS host. The string <code>{0}</code> is replaced with the path of the source file, the string
        <code>{1}</code> is replaced with the path of the destination file. The default value is <code>cp -p {0} {1}</code>.</td>
</tr>
<tr>
    <th align="left" valign="top"><a name="directoryCopyCommandForZos"></a>directoryCopyCommandForZos</th>
    <td>The command to use when copying a directory on a z/OS host. The string <code>{0}</code> is replaced with the path of the source directory, the string
        <code>{1}</code> is replaced with the path of the destination directory. The default value is <code>tar cC {0} . | tar xmC {1} .</code>. If the
        <code>tar</code> command is not available but the <code>find</code> command recognizes the <code>-depth</code> parameter with a value, the alternative
        command <code>find {0} -depth 1 -exec cp -pr {} {1} ;</code> may be configured.</td>
</tr>
<tr>
    <th align="left" valign="top"><a name="remoteCopyBufferSize"></a>remoteCopyBufferSize</th>
    <td>The buffer size to use when copying files from one connection to the other. The buffer size is taken from the _source_ file's connection. The default value is <code>64 KB (64*1024 bytes)</code>. Larger values potentially break copy operations.</td>
</tr>
<tr>
    <th align="left" valign="top"><a name="remoteCharacterEncoding"></a>remoteCharacterEncoding</th>
    <td>The character encoding used to transcode files from one connection to the other. The default value is Operating System dependent and is set to <code>'UTF-8'</code> for Windows and Unix, and to <code>'Cp1047'</code> (EBCDIC) for Z/OS.</td>
</tr>

</table>

Apart from these common connection options, some protocols define additional protocol-specific connection options. These are documented below, with the corresponding protocol.

<a name="local"></a>
## LOCAL

The local protocol implementation uses the local file manipulation and local process execution capabilities built-in to Java. The [__os__](#os) connection option is hardcoded to the operating system of the local host and the [__tmp__](#tmp) connection option defaults to the system temporary directory as specified by the `java.io.tmpdir` <a href="http://docs.oracle.com/javase/6/docs/api/java/lang/System.html#getProperties()">system property</a>. There are no protocol-specific connection options.

<a name="ssh"></a>
## SSH

The SSH protocol implementation of Overthere uses the [SSH](http://en.wikipedia.org/wiki/Secure_Shell) protocol to connect to remote hosts to manipulate files and execute commands. Most Unix systems already have an SSH server installed and configured and a number of different SSH implementations are available for Windows although not all of them are supported by Overthere.

### Compatibility

Overthere uses the [sshj](https://github.com/hierynomus/sshj) library for SSH and supports all algorithms and formats supported by that library:

* Ciphers: ``aes{128,192,256}-{cbc,ctr}``, ``blowfish-{cbc,ctr}``, ``3des-{cbc,ctr}``, ``twofish{128,192,256}-{cbc,ctr}``, ``twofish-cbc``, ``serpent{128,192,256}-{cbc,ctr}``, ``idea-{cbc,ctr}``, ``cast128-{cbc,ctr}``, ``arcfour``, ``arcfour{128,256}``
* Key Exchange methods: ``diffie-hellman-group{1,14}-sha1``, ``diffie-hellman-group-exchange-sha{1,256}``, ``ecdh-sha2-nistp{256,384,521}``, ``curve25519-sha256@libssh.org``
* Signature formats: ``ssh-rsa``, ``ssh-dss``, ``ecdsa-sha2-nistp256``, ``ssh-ed25519``
* MAC algorithms: ``hmac-md5``, ``hmac-md5-96``, ``hmac-sha1``, ``hmac-sha1-96``, ``hmac-sha2-256``, ``hmac-sha2-512``
* Compression algorithms: ``zlib`` and ``zlib@openssh.com`` (delayed zlib)
* Private Key file formats: ``pkcs8`` encoded (the format used by [OpenSSH](http://www.openssh.com/))

<a name="ssh_host_setup"></a>
### SSH host setup

<a name="ssh_host_setup_ssh"></a>
#### SSH
To connect to a remote host using the SSH protocol, you will need to install an SSH server on that remote host. For Unix platforms, we recommend [OpenSSH](http://www.openssh.com/). It is included in all Linux distributions and most other Unix flavours. For Windows platforms two SSH servers are supported:

* OpenSSH on [Cygwin](http://www.cygwin.com/). We recommend [COPSSH](http://www.itefix.no/i2/copssh) as a convenient packaging of OpenSSH and Cygwin. It is a free source download but since 22/11/2011 the binary installers are a paid solution.
* [WinSSHD](http://www.bitvise.com/winsshd) is a commercial SSH server that has a lot of configuration options.

__N.B.:__ The __SFTP__, __SCP__, __SU__, __SUDO__ and __INTERACTIVE_SUDO__ connection types are only available for Unix hosts. To use SSH with z/OS hosts, use the __SFTP__ connection type. To use SSH with Windows hosts, choose either the __SFTP_CYGWIN__ or the __SFTP_WINSSHD__ connection type.

<a name="ssh_host_setup_sftp"></a>
#### SFTP

To use the __SFTP__ connection type, make sure SFTP is enabled in the SSH server. This is enabled by default in most SSH servers.

<a name="ssh_host_setup_sftp_cygwin"></a>
#### SFTP_CYGWIN

To use the __SFTP_CYGWIN__ connection type, install [COPSSH](http://www.itefix.no/i2/copssh) on your Windows host. In the COPSSH control panel, add the users as which you want to connect and select _Linux shell and Sftp_ in the _shell_ dropdown box. Check _Password authentication_ and/or _Public key authentication_ depending on the authentication method you want to use.<br/>__N.B.:__ Overthere will take care of the translation from Windows style paths, e.g. `C:\Program Files\IBM\WebSphere\AppServer`, to Cygwin-style paths, e.g. `/cygdrive/C/Program Files/IBM/WebSphere/AppServer`, so that your code can use Windows style paths.

<a name="ssh_host_setup_sftp_winsshd"></a>
#### SFTP_WINSSHD

To use the __SFTP_WINSSHD__ connection type, install [WinSSHD](http://www.bitvise.com/winsshd) on your Windows host. In the Easy WinSSHD Settings control panel, add the users as which you want to connect, check the _Login allowed_ checkbox and select _Allow full access_ in the _Virtual filesystem layout_ dropdown box. Alternatively you can check the _Allow login to any Windows account_ to allow access to all Windows accounts.<br/>__N.B.:__ Overthere will take care of the translation from Windows style paths, e.g. `C:\Program Files\IBM\WebSphere\AppServer`, to WinSSHD-style paths, e.g. `/C/Program Files/IBM/WebSphere/AppServer`, so that your code can use Windows style paths.

<a name="ssh_host_setup_sudo"></a>
<a name="ssh_host_setup_interactive_sudo"></a>
#### SUDO and INTERACTIVE_SUDO

To use the __SUDO__ connection type, the `/etc/sudoers` configuration will have to be set up in such a way that the user configured with the connection option __username__ can execute the commands below as the user configured with the connection option __sudoUsername__. The arguments passed to these commands depend on the exact usage of the Overthere connection. Check the `INFO` messages on the `com.xebialabs.overthere.ssh.SshConnection` category to see what commands get executed.

* `chmod`
* `cp`
* `ls`
* `mkdir`
* `mv`
* `rm`
* `rmdir`
* `tar`
* Any other command that you want to execute.

The commands mentioned above must be configured with the __NOPASSWD__ setting in the `/etc/sudoers` file. Otherwise you will have to use the __INTERACTIVE_SUDO__ connection type. When the __INTERACTIVE_SUDO__ connection type is used, every line of the output will be matched against the regular expression configured with the __sudoPasswordPromptRegex__ connection option. If a match is found, the value of the __password__ connection option is sent.

<a name="ssh_troubleshooting"></a>
### Troubleshooting SSH connections

This section lists a number of common configuration errors that can occur when using Overthere with SSH. If you run into other connectivity issues when using Overthere, pease let us know by [creating a ticket](https://github.com/xebialabs/overthere/issues) or by [sending us a pull request](https://github.com/xebialabs/overthere/pulls).

#### Cannot start a process on an SSH server because the server disconnects immediately.

If the terminal type requested using the [__allocatePty__](#ssh_allocatePty) connection option or the [__allocateDefaultPty__](#ssh_allocateDefaultPty) connection option is not recognized by the SSH server, the connection will be dropped. Specifically, the `dummy` terminal type configured by [__allocateDefaultPty__] connection option, will cause OpenSSH on AIX and WinSSHD to drop the connection. Try a safe terminal type such as `vt220` instead.

To verify the behaviour of your SSH server with respect to pty allocation, you can manually execute the <code>ssh</code> command with the `-T` (disable pty allocation) or `-t` (force pty allocation) flags.

#### Command executed using SUDO or INTERACTIVE_SUDO fails with the message `sudo: sorry, you must have a tty to run sudo`

The `sudo` command requires a tty to run. Set the [__allocatePty__](#ssh_allocatePty) connection option or the [__allocateDefaultPty__](#ssh_allocateDefaultPty) connection option to ask the SSH server allocate a pty.

#### Command executed using SUDO or INTERACTIVE_SUDO appears to hang.

This may be caused by the sudo command waiting for the user to enter his password to confirm his identity. There are two ways to solve this:

1. Use the [`NOPASSWD`](http://www.gratisoft.us/sudo/sudoers.man.html#nopasswd_and_passwd) tag in your `/etc/sudoers` file.
2. Use the [__INTERACTIVE_SUDO__](#ssh_host_setup_interactive_sudo) connection type instead of the [__SUDO__](ssh_host_setup_sudo) connection type.
3. If you are already using the __INTERACTIVE_SUDO__ connection type and you still get this error, please verify that you have correctly configured the [__sudoPasswordPromptRegex__](#ssh_sudoPasswordPromptRegex) option. If you have trouble determining the proper value for the __sudoPasswordPromptRegex__ connection option, set the log level for the `com.xebialabs.overthere.ssh.SshElevatedPasswordHandlingStream` category to `TRACE` and examine the output.

<a name="ssh_connection_options"></a>
### SSH connection options

The SSH protocol implementation of Overthere defines a number of additional connection options, in addition to the [common connection options](#common_connection_options).

<table>
<tr>
	<th align="left" valign="top"><a name="ssh_connectionType"></a>connectionType</th>
	<td>Specifies how the SSH protocol is used. One of the following values must be set:<ul>
            <li><strong><a href="#ssh_host_setup_sftp">SFTP</a></strong> - uses SFTP to transfer files, to a Unix host or a z/OS host, and SSH to execute
                commands. Requires the SFTP subsystem of the SSH server on the target host to be enabled.</li>
            <li><strong>SCP</strong> - uses SCP to transfer files, to a Unix host, and SSH to execute commands. Can be faster than SFTP, especially over high
                latency networks.</li>
            <li><strong>SU</strong> - uses SCP to transfer files, to a Unix host. Uses the
                <a href="http://en.wikipedia.org/wiki/Su"><code>su</code></a> command to execute commands. Select this connection type if the
                <strong>username</strong> you are connecting with does not have the right permissions to manipulate the files that need to be manipulated
                and/or to execute the commands that need to be executed. <br/>If this connection type is selected, the <strong>suUsername</strong> and
                <strong>suPassword</strong> connection option are required and specify the username/password combination that <em>do</em> have the necessary
                permissions.</li>
            <li><strong><a href="#ssh_host_setup_sudo">SUDO</a></strong> - uses SCP to transfer files, to a Unix host. Uses the
                <a href="http://en.wikipedia.org/wiki/Sudo"><code>sudo</code></a> command, configured with <strong>NOPASSWD</strong> for all commands, to
                execute commands. Select this connection type if the <strong>username</strong> you are connecting with does not have the right permissions to
                manipulate the files that need to be manipulated and/or to execute the commands that need to be executed. <br/>If this connection type is
                selected, the <strong>sudoUsername</strong> connection option is required and specifies the user that <em>does</em> have the necessary
                permissions. See below for a more detailed description.</li>
            <li><strong><a href="#ssh_host_setup_interactive_sudo">INTERACTIVE_SUDO</a></strong> - uses SCP to transfer files, to a Unix host. Uses the
                <code>sudo</code> command, <em>not</em> been configured with <strong>NOPASSWD</strong> for all commands, to execute commands. This is similar
                to the <strong>SUDO</strong> connection type but also detects the password prompt that is shown by the <code>sudo</code> command when the login
                user (<strong>username</strong>) tries to execute a commands as the privileged user (<strong>sudoUsername</strong>) when that command has not
                been configured in <code>/etc/sudoers</code> with <strong>NOPASSWD</strong>. <br/><strong>N.B.:</strong> Because the password of the login user
                is needed to answer this prompt, this connection type is incompatible with the <strong>privateKeyFile</strong> option that can be used to
                authenticate with a private key file.</li>
            <li><strong><a href="#ssh_host_setup_sftp_cygwin">SFTP_CYGWIN</a></strong> - uses SFTP to transfer files, to a Windows host running OpenSSH on
                Cygwin.</li>
            <li><strong><a href="#ssh_host_setup_sftp_winsshd">SFTP_WINSSHD</a></strong> - uses SFTP to transfer files, to a Windows host running WinSSHD.</li>
	    </ul>
	    The connection property <code>port</code> specifies the port on which the SSH server listens. The default value for is <code>22</code> for all
	    connection types.</td>
</tr>
<tr>
	<th align="left" valign="top"><a name="ssh_suUsername"></a>suUsername</th>
	<td>The username of the user that can manipulate the files that need to be manipulated and that can execute the commands that need to be executed.
	<br/>
	<strong>N.B.:</strong> This connection option is only applicable for the <strong>SU</strong> connection type.</td>
</tr>
<tr>
	<th align="left" valign="top"><a name="ssh_suPassword"></a>suPassword</th>
	<td>The password of the user that can manipulate the files that need to be manipulated and that can execute the commands that need to be executed.
	<br/>
	<strong>N.B.:</strong> This connection option is only applicable for the <strong>SU</strong> connection type.</td>
</tr>
<tr>
	<th align="left" valign="top"><a name="ssh_sudoUsername"></a>sudoUsername</th>
	<td>The username of the user that can manipulate the files that need to be manipulated and that can execute the commands that need to be executed.
	<br/>
	<strong>N.B.:</strong> This connection option is only applicable for the <strong>SUDO</strong> and <strong>INTERACTIVE_SUDO</strong> connection types.</td>
</tr>
<tr>
	<th align="left" valign="top"><a name="ssh_privateKey"></a>privateKey</th>
	<td>The RSA private key as String to use when connecting to the remote host. When this connection option is specified, the <strong>password</strong> and <strong>privateKeyFile</strong> connection options are ignored.</td>
</tr>
<tr>
	<th align="left" valign="top"><a name="ssh_privateKeyFile"></a>privateKeyFile</th>
	<td>The RSA private key file to use when connecting to the remote host. When this connection option is specified, the <strong>password</strong> connection option is ignored.</td>
</tr>
<tr>
	<th align="left" valign="top"><a name="ssh_passphrase"></a>passphrase</th>
	<td>The passphrase to unlock the RSA private key file specified with the <strong>privateKeyFile</strong> connection option. If this connection option is not specified, the RSA private key file must have an empty passphrase.</td>
</tr>
<tr>
	<th align="left" valign="top"><a name="ssh_allocateDefaultPty"></a>allocateDefaultPty</th>
	<td>If set to <code>true</code>, the SSH server is requested to allocate a default pty for the process, as if the <a href="#ssh_allocatePty"><strong>allocatePty</strong></a> option were set to the value <code>dummy:80:24:0:0</code>. The default value is <code>false</code>.
	<br/>
	<strong>N.B.:</strong> This connection option has been deprecated in favour of the <a href="#ssh_allocatePty"><strong>allocatePty</strong></a> connection option because it allows the user to specify _what_ pty is allocated.
</tr>
<tr>
	<th align="left" valign="top"><a name="ssh_allocatePty"></a>allocatePty</th>
	<td>If set, the SSH server is requested to allocate a pty (<a href="http://en.wikipedia.org/wiki/Pseudo_terminal">pseudo terminal</a>) for the process with
	    the setting specified by this option. The format is <code>TERM:COLS:ROWS:WIDTH:HEIGHT</code>, e.g. <code>xterm:80:24:0:0</code>. If set, this option
	    overrides the <a href="#ssh_allocateDefaultPty"><strong>allocateDefaultPty</strong></a> option.<br/>
	    If the <a href="#ssh_host_setup_interactive_sudo"><strong>INTERACTIVE_SUDO</strong></a> connection type is used, the default value is
	    <code>vt220:80:24:0:0</code>. Otherwise the default is to not allocate a pty.<br/>
	    All su and sudo implementations require a pty to be allocated for when displaying a password prompt, some sudo implementations even require it when
	    not displaying a password prompt.Some SSH server implementations (notably OpenSSH on AIX 5.3) close the connection when an unknown one is allocated.
	</td>
</tr>
<tr>
	<th align="left" valign="top"><a name="heartbeatInterval"></a>heartbeatInterval</th>
	<td>Specify an interval to send keep-alives packets. Default is 0 (no keep-alive).</td>
</tr>
<tr>
	<th align="left" valign="top"><a name="ssh_interactiveKeyboardAuthRegex"></a>interactiveKeyboardAuthRegex</th>
	<td>The regular expression to look for in keyboard-interactive prompts before sending the password. The default value is <code>.*Password:[ ]?</code>. When the SSH server is configured to not allow <a href="http://www.ietf.org/rfc/rfc4252.txt">password authentication</a> but is configured to allow <a href="http://www.ietf.org/rfc/rfc4256.txt">keyboard-interactive authentication</a> using passwords, Overthere will compare the interactive-keyboard prompt against this regular expression and send the value of the <strong>password</strong> option when they match. The default value is <code>.*Password:[ ]?</code></td>
</tr>
<tr>
	<th align="left" valign="top"><a name="ssh_openShellBeforeExecute"></a>openShellBeforeExecute</th>
	<td>If set to <code>true</code>, Overthere will open and close a shell immediately before executing a command on an ssh host. This is useful when the connecting user does not yet have a homedir, but this is created for him on the fly on the host. A setup commonly seen when user management is done through LDAP.</td>
</tr>
<tr>
	<th align="left" valign="top"><a name="ssh_suCommandPrefix"></a>suCommandPrefix</th>
	<td>The command to prefix to the command to be executed to execute it as <strong>suUsername</strong>. The string <code>{0}</code> is replaced with the
	    value of <strong>suUsername</strong>. The default value is <code>su - {0} -c</code>.
	<br/>
	<strong>N.B.:</strong> This connection option is only applicable for the <strong>SU</strong> connection type.</td>
</tr>
<tr>
	<th align="left" valign="top"><a name="ssh_suOverrideUmask"></a>suOverrideUmask</th>
	<td>If set to <code>true</code>, Overthere will explicitly change the permissions with <code>chmod -R go+rX</code> after uploading a file or directory with
	    scp. The default value is <code>true</code>.
	<br/>
	<strong>N.B.:</strong> This connection option is only applicable for the <strong>SU</strong> connection type.</td>
</tr>
<tr>
	<th align="left" valign="top"><a name="ssh_suPasswordPromptRegex"></a>suPasswordPromptRegex</th>
	<td>The regular expression to be used when looking for su password prompts. When the connection type is set to <strong>INTERACTIVE_SUDO</strong>, Overthere will look for strings that match this regular expression in the first line of the output of a command, and send the password if a match occurs. The default value is <code>.*[Pp]assword.*:</code>
	<br/>
	<strong>N.B.:</strong> This connection option is only applicable for the <strong>SU</strong> connection type.</td>
</tr>
<tr>
	<th align="left" valign="top"><a name="ssh_suPreserveAttributesOnCopyFromTempFile"></a>suPreserveAttributesOnCopyFromTempFile</th>
	<td>If set to <code>true</code>, files are copied <strong>from</strong> the connection temporary directory using the <code>-p</code> flag to the <code>cp</code> command. The default value is <code>false</code>.
	<br/>
	<strong>N.B.:</strong> This connection option is only applicable for the <strong>SU</strong> connection type.</td>
</tr>
<tr>
	<th align="left" valign="top"><a name="ssh_suPreserveAttributesOnCopyToTempFile"></a>suPreserveAttributesOnCopyToTempFile</th>
	<td>If set to <code>true</code>, files are copied <strong>to</strong> the connection temporary directory using the <code>-p</code> flag to the <code>cp</code> command. The default value is <code>false</code>.
	<br/>
	<strong>N.B.:</strong> This connection option is only applicable for the <strong>SU</strong> connection type.</td>
</tr>
<tr>
	<th align="left" valign="top"><a name="ssh_suQuoteCommand"></a>suQuoteCommand</th>
	<td>If set to <code>true</code>, the original command is added as one argument to the prefix configured with the <code>suCommandPrefix</code> connection option. This has the result of quoting the original command, which is needed for commands like <code>su</code>. Compare <code>su -u privilegeduser start server1</code> to <code>su privilegeduser -c 'start server1'</code>. The default value is <code>true</code>.
	<br/>
	<strong>N.B.:</strong> This connection option is only applicable for the <strong>SU</strong> connection type.</td>
</tr>
<tr>
	<th align="left" valign="top"><a name="ssh_sudoCommandPrefix"></a>sudoCommandPrefix</th>
	<td>The command to prefix to the command to be executed to execute it as <strong>sudoUsername</strong>. The string <code>{0}</code> is replaced with the
	    value of <strong>sudoUsername</strong>. The default value is <code>sudo -u {0}</code>.
	<br/>
	<strong>N.B.:</strong> This connection option is only applicable for the <strong>SUDO</strong> and <strong>INTERACTIVE_SUDO</strong> connection types.</td>
</tr>
<tr>
	<th align="left" valign="top"><a name="ssh_sudoOverrideUmask"></a>sudoOverrideUmask</th>
	<td>If set to <code>true</code>, Overthere will explicitly change the permissions with <code>chmod -R go+rX</code> after uploading a file or directory with
	    scp. The default value is <code>true</code>.
	<br/>
	<strong>N.B.:</strong> This connection option is only applicable for the <strong>SUDO</strong> and <strong>INTERACTIVE_SUDO</strong> connection types.</td>
</tr>
<tr>
	<th align="left" valign="top"><a name="ssh_sudoPasswordPromptRegex"></a>sudoPasswordPromptRegex</th>
	<td>The regular expression to be used when looking for sudo password prompts. When the connection type is set to <strong>INTERACTIVE_SUDO</strong>, Overthere will look for strings that match this regular expression in the first line of the output of a command, and send the password if a match occurs. The default value is <code>.*[Pp]assword.*:</code>
	<br/>
	<strong>N.B.:</strong> This connection option is only applicable for the <strong>INTERACTIVE_SUDO</strong> connection type.</td>
</tr>
<tr>
	<th align="left" valign="top"><a name="ssh_sudoPreserveAttributesOnCopyFromTempFile"></a>sudoPreserveAttributesOnCopyFromTempFile</th>
	<td>If set to <code>true</code>, files are copied <strong>from</strong> the connection temporary directory using the <code>-p</code> flag to the <code>cp</code> command. The default value is <code>false</code>.
	<br/>
	<strong>N.B.:</strong> This connection option is only applicable for the <strong>SUDO</strong> and <strong>INTERACTIVE_SUDO</strong> connection types.</td>
</tr>
<tr>
	<th align="left" valign="top"><a name="ssh_sudoPreserveAttributesOnCopyToTempFile"></a>sudoPreserveAttributesOnCopyToTempFile</th>
	<td>If set to <code>true</code>, files are copied <strong>to</strong> the connection temporary directory using the <code>-p</code> flag to the <code>cp</code> command. The default value is <code>false</code>.
	<br/>
	<strong>N.B.:</strong> This connection option is only applicable for the <strong>SUDO</strong> and <strong>INTERACTIVE_SUDO</strong> connection types.</td>
</tr>
<tr>
	<th align="left" valign="top"><a name="ssh_sudoQuoteCommand"></a>sudoQuoteCommand</th>
	<td>If set to <code>true</code>, the original command is added as one argument to the prefix configured with the <code>sudoCommandPrefix</code> connection option. This has the result of quoting the original command, which is needed for commands like <code>su</code>. Compare <code>sudo -u privilegeduser start server1</code> to <code>su privilegeduser -c 'start server1'</code>. The default value is <code>false</code>.
	<br/>
	<strong>N.B.:</strong> This connection option is only applicable for the <strong>SUDO</strong> and <strong>INTERACTIVE_SUDO</strong> connection types.</td>
</tr>
<tr>
	<th align="left" valign="top"><a name="ssh_deleteDirectoryCommand"></a>deleteDirectoryCommand</th>
	<td>The command to be used when deleting a directory. The string <code>{0}</code> is replaced with the value of the path of the directory to be deleted. The default value is <code>rmdir {0}</code>.
	<br/>
	<strong>N.B.:</strong> This connection option is only applicable for the <strong>SCP</strong>, <strong>SUDO</strong> and <strong>INTERACTIVE_SUDO</strong> connection types.</td>
</tr>
<tr>
	<th align="left" valign="top"><a name="ssh_deleteFileCommand"></a>deleteFileCommand</th>
	<td>The command to be used when deleting a file. The string <code>{0}</code> is replaced with the value of the path of the file to be deleted. The default value is <code>rm -f {0}</code>.
	<br/>
	<strong>N.B.:</strong> This connection option is only applicable for the <strong>SCP</strong>, <strong>SUDO</strong> and <strong>INTERACTIVE_SUDO</strong> connection types.</td>
</tr>
<tr>
	<th align="left" valign="top"><a name="ssh_deleteRecursivelyCommand"></a>deleteRecursivelyCommand</th>
	<td>The command to be used when deleting a directory recursively. The string <code>{0}</code> is replaced with the value of the path of the directory to be deleted. The default value is <code>rm -rf {0}</code>.
	<br/>
	<strong>N.B.:</strong> This connection option is only applicable for the <strong>SCP</strong>, <strong>SUDO</strong> and <strong>INTERACTIVE_SUDO</strong> connection types.</td>
</tr>
<tr>
	<th align="left" valign="top"><a name="ssh_getFileInfoCommand"></a>getFileInfoCommand</th>
	<td>The command to be used when getting the metadata of a file/directory. The string <code>{0}</code> is replaced with the value of the path of the file/directory. The default value is <code>ls -ld {0}</code>.
	<br/>
	<strong>N.B.:</strong> This connection option is only applicable for the <strong>SCP</strong>, <strong>SUDO</strong> and <strong>INTERACTIVE_SUDO</strong> connection types.</td>
</tr>
<tr>
	<th align="left" valign="top"><a name="ssh_listFilesCommand"></a>listFilesCommand</th>
	<td>The command to be used when listing the contents of a directory. The string <code>{0}</code> is replaced with the value of the path of the directory to be listed. The default value is <code>ls -a1 {0}</code>.
	<br/>
	<strong>N.B.:</strong> This connection option is only applicable for the <strong>SCP</strong>, <strong>SUDO</strong> and <strong>INTERACTIVE_SUDO</strong> connection types.</td>
</tr>
<tr>
	<th align="left" valign="top"><a name="ssh_mkdirCommand"></a>mkdirCommand</th>
	<td>The command to be used when creating a directory. The string <code>{0}</code> is replaced with the value of the path of the directory to be created. The default value is <code>mkdir {0}</code>.
	<br/>
	<strong>N.B.:</strong> This connection option is only applicable for the <strong>SCP</strong>, <strong>SUDO</strong> and <strong>INTERACTIVE_SUDO</strong> connection types.</td>
</tr>
<tr>
	<th align="left" valign="top"><a name="ssh_mkdirsCommand"></a>mkdirsCommand</th>
	<td>The command to be used when creating a directory tree. The string <code>{0}</code> is replaced with the value of the path of the directory tree to be created. The default value is <code>mkdir -p {0}</code>.
	<br/>
	<strong>N.B.:</strong> This connection option is only applicable for the <strong>SCP</strong>, <strong>SUDO</strong> and <strong>INTERACTIVE_SUDO</strong> connection types.</td>
</tr>
<tr>
	<th align="left" valign="top"><a name="ssh_renameToCommand"></a>renameToCommand</th>
	<td>The command to be used when renaming a file/directory. The string <code>{0}</code> is replaced with the value of the path of the file/directory to be renamed. The string <code>{1}</code> is replaced with the value of the new name. The default value is <code>mv {0} {1}</code>.
	<br/>
	<strong>N.B.:</strong> This connection option is only applicable for the <strong>SCP</strong>, <strong>SUDO</strong> and <strong>INTERACTIVE_SUDO</strong> connection types.</td>
</tr>
<tr>
	<th align="left" valign="top"><a name="ssh_setExecutableCommand"></a>setExecutableCommand</th>
	<td>The command to be used when making a file executable. The string <code>{0}</code> is replaced with the value of the path of the file/directory affected. The default value is <code>chmod a+x {0}</code>.
	<br/>
	<strong>N.B.:</strong> This connection option is only applicable for the <strong>SCP</strong>, <strong>SUDO</strong> and <strong>INTERACTIVE_SUDO</strong> connection types.</td>
</tr>
<tr>
	<th align="left" valign="top"><a name="ssh_setNotExecutableCommand"></a>setNotExecutableCommand</th>
	<td>The command to be used when making a file non-executable. The string <code>{0}</code> is replaced with the value of the path of the file/directory affected. The default value is <code>chmod a-x {0}</code>.
	<br/>
	<strong>N.B.:</strong> This connection option is only applicable for the <strong>SCP</strong>, <strong>SUDO</strong> and <strong>INTERACTIVE_SUDO</strong> connection types.</td>
</tr>
<tr>
	<th align="left" valign="top"><a name="ssh_sudoTempMkdirCommand"></a>sudoTempMkdirCommand</th>
	<td>The command to be used when creating a temporary directory as a sudo user. The directory needs to be read/writeable for both the connecting and the sudo user. The string <code>{0}</code> is replaced with the value of the path of the directory to be created. The default value is <code>mkdir -m 1777 {0}</code>.
	<br/>
	<strong>N.B.:</strong> This connection option is only applicable for the <strong>SUDO</strong> and <strong>INTERACTIVE_SUDO</strong> connection types.</td>
</tr>
<tr>
	<th align="left" valign="top"><a name="ssh_sudoTempMkdirsCommand"></a>sudoTempMkdirsCommand</th>
	<td>The command to be used when creating a temporary directory tree as a sudo user. The directory tree needs to be read/writeable for both the connecting and the sudo user. The string <code>{0}</code> is replaced with the value of the path of the directory to be created. The default value is <code>mkdir -p -m 1777 {0}</code>.
	<br/>
	<strong>N.B.:</strong> This connection option is only applicable for the <strong>SUDO</strong> and <strong>INTERACTIVE_SUDO</strong> connection types.</td>
</tr>
<tr>
	<th align="left" valign="top"><a name="ssh_sudoCopyFromTempFileCommand"></a>sudoCopyFromTempFileCommand</th>
	<td>The command to be used when copying files/directories from the connection temporary directory as the sudo user. The string <code>{0}</code> is replaced with the value of the path of the file/directory being copied. The string <code>{1}</code> is replaced with the value of the target path. The default value is <code>cp -pr {0} {1}</code> if <a href="#ssh_sudoPreserveAttributesOnCopyFromTempFile"><strong>sudoPreserveAttributesOnCopyFromTempFile</strong></a> is set to true, otherwise the default value is <code>cp -r {0} {1}</code>.
	<br/>
	<strong>N.B.:</strong> This connection option is only applicable for the <strong>SUDO</strong> and <strong>INTERACTIVE_SUDO</strong> connection types.</td>
</tr>
<tr>
	<th align="left" valign="top"><a name="ssh_sudoOverrideUmaskCommand"></a>sudoOverrideUmaskCommand</th>
	<td>The command to be used when setting the umask before copying a file/directory from, or after copying it to the connection temporary directory. This command ensures that the sudo user has read (and/or execute) rights for the copied file/directory. The string <code>{0}</code> is replaced with the value of the file/directory being copied. The default value is <code>chmod -R go+rX {0}</code>.
	<br/>
	<strong>N.B.:</strong> This connection option is only applicable for the <strong>SUDO</strong> and <strong>INTERACTIVE_SUDO</strong> connection types.</td>
</tr>
<tr>
	<th align="left" valign="top"><a name="ssh_sudoCopyToTempFileCommand"></a>sudoCopyToTempFileCommand</th>
	<td>The command to be used when copying files/directories to the connection temporary directory as the sudo user. The string <code>{0}</code> is replaced with the value of the path of the file/directory being copied. The string <code>{1}</code> is replaced with the value of the target path. The default value is <code>cp -pr {0} {1}</code> if <a href="#ssh_sudoPreserveAttributesOnCopyToTempFile"><strong>sudoPreserveAttributesOnCopyToTempFile</strong></a> is set to true, otherwise the default value is <code>cp -r {0} {1}</code>.
	<br/>
	<strong>N.B.:</strong> This connection option is only applicable for the <strong>SUDO</strong> and <strong>INTERACTIVE_SUDO</strong> connection types.</td>
</tr>
<tr>
	<th align="left" valign="top"><a name="ssh_localAddress"></a>localAddress</th>
	<td>The address to use on the local machine as the source address of the connection. This property is optional and mainly useful on systems with more than one address. The default behaviour is to let the OS decide.</td>
</tr>
<tr>
	<th align="left" valign="top"><a name="ssh_localPort"></a>localPort</th>
	<td>The port to use on the local machine as the source port of the connection. This property is optional and to be used in combination with the <a href="#ssh_localAddress"><strong>localAddress</strong></a> property. The default behaviour is to let the OS pick a free port.</td>
</tr>
</table>

<a name="smb_cifs"></a>
## SMB 2.x/CIFS, WinRM and Telnet

<a name="smb"></a>
### SMB 2.x

The SMB 2.x protocol implementation of Overthere uses the prevalent [SMB protocol](http://en.wikipedia.org/wiki/Server_Message_Block) for file manipulation and, depending on the settings, using either [WinRM](http://en.wikipedia.org/wiki/WS-Management) or [Telnet](http://en.wikipedia.org/wiki/Telnet) for process execution. 

<a name="cifs"></a>
### CIFS

The CIFS protocol implementation of Overthere uses public variant of the original Server Message Block (SMB) protocol developed by Microsoft known as [CIFS protocol](http://en.wikipedia.org/wiki/Server_Message_Block), for file manipulation and, depending on the settings, uses either [WinRM](http://en.wikipedia.org/wiki/WS-Management) or [Telnet](http://en.wikipedia.org/wiki/Telnet) for process execution. CIFS is widely regarded as an obsolete protocol and users are encouraged to prefer SMB protocol over CIFS. Support for CIFS is deprecated and will be removed from subsequent releases.

### SMB 2.x and CIFS
These protocols are only supported for Windows hosts, you will most likely not need to install new software although you might need to enable and configure some services:

* The built-in file sharing capabilities of Windows are based on CIFS/SMB and are therefore available and enabled by default.
* WinRM is available on Windows Server 2008 and up. Overthere supports basic authentication for local accounts and Kerberos authentication for domain accounts. Overthere has a built-in WinRM library that can be used from all operating systems by setting the [**connectionType**](#smb_cifs_connectionType) connection option to __WINRM_INTERNAL__. When connecting from a host that runs Windows, or when using a "winrs proxy host" that runs Windows, the native WinRM capabilities of Windows, i.e. the `winrs` command, can be used by setting the [**connectionType**](#smb_cifs_connectionType) connection option to __WINRM_NATIVE__.
* A Telnet Server is available on all Windows Server versions although it might not be enabled.

### Password limitations

Due to a limitation of the `winrs` command, passwords containing a single quote (`'`) or a double quote (`"`) cannot be used when using the __WINRM_NATIVE__ connection type.

### Domain accounts
Windows domain accounts are supported by the __WINRM_INTERNAL__, __WINRM_NATIVE__ and __TELNET__ connection types, but the syntax of the username is different:

* For the __WINRM_INTERNAL__ connection type, domain accounts must be specified using the new-style domain syntax, e.g. `USER@FULL.DOMAIN`.
* For the __TELNET__ connection type, domain accounts must be specified using the old-style domain syntax, e.g `DOMAIN\USER`.
* For the __WINRM_NATIVE__ connection type, domain accounts may be specified using either the new-style (`USER@FULL.DOMAIN`) or old-style (`DOMAIN\USER`) domain syntax.
* For all three connection types, local accounts must be specified without an at-sign (`@`) or a backslash (`\`).

__N.B.:__ When using domain accounts with the __WINRM_INTERNAL__ connection type, the Kerberos subsystem of the Java Virtual Machine must be configured correctly. Please read the section on how to set up Kerberos [for the source host](#smb_cifs_host_setup_krb5) and [the remote hosts](#smb_cifs_host_setup_spn).

### Administrative shares
By default Overthere will access the [administrative shares](http://en.wikipedia.org/wiki/Administrative_share) on the remote host. These shares are only accessible for users that are part of the __Administrators__ on the remote host. If you want to access the remote host using a regular account, use the [__pathShareMapping__](#smb_cifs_pathShareMappings) connection option to configure the shares to use for the paths Overthere will be connecting to. Of course, the user configured with the __username__ connection option should have access to those shares and the underlying directories and files.

__N.B.:__ Overthere will take care of the translation from Windows paths, e.g. `C:\Program Files\IBM\WebSphere\AppServer`, to SMB URLs that use the administrative shares, e.g. `smb://username:password@hostname/C$/Program%20Files/IBM/WebSphere/AppServer` (which corresponds to the UNC path `\\hostname\C$\Program Files\IBM\WebSphere\AppServer`), so that your code can use Windows style paths.

<a name="smb_cifs_host_setup"></a>
### Host setup

<a name="smb_cifs_host_setup_smb"></a>
#### SMB 2.x and CIFS
To connect to a remote host using the __SMB__ or __CIFS__ protocol, ensure the host is reachable on port 445.

If you will be connecting as an administrative user, ensure the administrative shares are configured. Otherwise, ensure that the user you will be using to connect has access to shares that correspond to the directory you want to access and that the [__pathShareMappings__](#smb_cifs_pathShareMappings) connection option is configured accordingly.

<a name="smb_cifs_host_setup_telnet"></a>
#### TELNET

To use the __TELNET__ connection type, you'll need to enable and configure the Telnet Server according to these instructions:


1. (Optional) If the Telnet Server is not already installed on the remote host, add it using the __Add Features Wizard__ in the __Server Manager__ console.

1. (Optional) If the remote host is running Windows Server 2003 SP1 or an x64-based version of Windows Server 2003, install the Telnet server according to [these instructions from the Microsoft Support site](http://support.microsoft.com/kb/899260).

1. Enable the Telnet Server Service on the remote host according to <a href="http://technet.microsoft.com/en-us/library/cc732046(WS.10).aspx">these instructions on the Microsoft Technet site</a>.

1. After you have started the Telnet Server, open a command prompt as the __Administrator__ user on the remote host and enter the command `tlntadmn config mode=stream` to enable stream mode.

When the Telnet server is enabled any user that is in the __Administrators__ group or that is in the __TelnetClients__ group and that has the __Allow logon locally__ privilege can log in using Telnet. See the Microsoft Technet to learn <a href="http://technet.microsoft.com/en-us/library/ee957044(WS.10).aspx">how to grant a user or group the right to logon locally</a> on Windows Server 2008 R2.

<a name="smb_cifs_host_setup_winrm"></a>
<a name="smb_cifs_host_setup_winrm_internal"></a>
<a name="smb_cifs_host_setup_winrm_native"></a>
#### WINRM (WINRM_INTERNAL and WINRM_NATIVE)

_For a PowerShell script to do what is described below in one go, check [Richard Downer's blog](http://www.frontiertown.co.uk/2011/12/overthere-control-windows-from-java/)_

To use the __WINRM_INTERNAL__ or the __WINRM_NATIVE__ connection type, you'll need to setup WinRM on the remote host by following these instructions:

1. If the remote host is running Windows Server 2003 SP1 or SP2, or Windows XP SP2, install the [WS-Management v.1.1 package](http://support.microsoft.com/default.aspx?scid=kb;EN-US;936059&wa=wsignin1.0).

1. If the remote host is running Windows Server 2003 R2, go to the __Add/Remove System Components__ feature in the __Control Panel__ and add WinRM under the section __Management and Monitoring Tools__. Afterwards install the [WS-Management v.1.1 package](http://support.microsoft.com/default.aspx?scid=kb;EN-US;936059&wa=wsignin1.0) to upgrade the WinRM installation.

1. If the remote host is running Windows Vista or Windows 7, the __Windows Remote Management (WS-Management)__ service is not started by default. Start the service and change its Startup type to __Automatic (Delayed Start)__ before proceeding with the next steps.

1. On the remote host, open a Command Prompt (not a PowerShell prompt!) using the __Run as Administrator__ option and paste in the following lines when using the __WINRM_INTERNAL__ connection type:

		winrm quickconfig
		y
		winrm set winrm/config/service/Auth @{Basic="true"}
		winrm set winrm/config/service @{AllowUnencrypted="true"}
		winrm set winrm/config/winrs @{MaxMemoryPerShellMB="1024"}

	Or the following lines when using the __WINRM_NATIVE__ connection type:

		winrm quickconfig
		y
		winrm set winrm/config/service/Auth @{Basic="true"}
		winrm set winrm/config/winrs @{MaxMemoryPerShellMB="1024"}

	Or keep reading for more detailed instructions. :-)

1. Run the quick config of WinRM to start the Windows Remote Management service, configure an HTTP listener and create exceptions in the Windows Firewall for the Windows Remote Mangement service:

		winrm quickconfig

	__N.B.:__ The Windows Firewall needs to be running to run this command. See [Microsoft Knowledge Base article #2004640](http://support.microsoft.com/kb/2004640).

1. (Optional) By default basic authentication is disabled in WinRM. Enable it if you are going to use local accounts to access the remote host:

		winrm set winrm/config/service/Auth @{Basic="true"}

1. (Optional) By default Kerberos authentication is enabled in WinRM. Disable it if you are __not__ going to use domain accounts to access the remote host:

		winrm set winrm/config/service/Auth @{Kerberos="false"}

	__N.B.:__ Do not disable Negotiate authentication as the `winrm` command itself uses that to configure the WinRM subsystem!

1. (Only required for __WINRM_INTERNAL__ or when the connection option [**winrsUnencrypted**](#smb_cifs_winrsUnencrypted) is set to `true`) Configure WinRM to allow unencrypted SOAP messages:

		winrm set winrm/config/service @{AllowUnencrypted="true"}

1. Configure WinRM to provide enough memory to the commands that you are going to run, e.g. 1024 MB:

		winrm set winrm/config/winrs @{MaxMemoryPerShellMB="1024"}

	__N.B.:__ This is not supported by WinRM 3.0, included with the Windows Management Framework 3.0. This update [has been temporarily removed from Windows Update](http://blogs.msdn.com/b/powershell/archive/2012/12/20/windows-management-framework-3-0-compatibility-update.aspx) because of numerous incompatiblity issues with other Microsoft products. However, if you have already installed WMF 3.0 and cannot downgrade, [Microsoft Knowledge Base article #2842230](http://support.microsoft.com/kb/2842230) describes a hotfix that can be installed to re-enable the `MaxMemoryPerShellMB` setting.

1. To use the __WINRM_INTERNAL__ or __WINRM_NATIVE__ connection type with HTTPS, i.e. [__winrmEnableHttps__](#smb_cifs_winrmEnableHttps) set to `true`, follow the steps below:

	1. (Optional) Create a self signed certificate for the remote host by installing `selfssl.exe` from [the IIS 6 resource kit](http://www.microsoft.com/download/en/details.aspx?displaylang=en&id=17275) and running the command below or by following the instructions [in this blog by Hans Olav](http://www.hansolav.net/blog/SelfsignedSSLCertificatesOnIIS7AndCommonNames.aspx):

        	C:\Program Files\IIS Resources\SelfSSL>selfssl.exe /T /N:cn=HOSTNAME /V:3650
        	Microsoft (R) SelfSSL Version 1.0
        	Copyright (C) 2003 Microsoft Corporation. All rights reserved.

        	Do you want to replace the SSL settings for site 1 (Y/N)?Y
        	The self signed certificate was successfully assigned to site 1.

	1. Open a PowerShell window and enter the command below to find the thumbprint for the certificate for the remote host:

			PS C:\Windows\system32> Get-childItem cert:\LocalMachine\Root\ | Select-String -pattern HOSTNAME

			[Subject]
			  CN=HOSTNAME

			[Issuer]
			  CN=HOSTNAME

			[Serial Number]
			  527E7AF9142D96AD49A10469A264E766

			[Not Before]
			  5/23/2011 10:23:33 AM

			[Not After]
			  5/20/2021 10:23:33 AM

			[Thumbprint]
			  5C36B638BC31F505EF7F693D9A60C01551DD486F

	1. Create an HTTPS WinRM listener for the remote host with the thumbprint you've just found:

			winrm create winrm/config/Listener?Address=*+Transport=HTTPS @{Hostname="HOSTNAME"; CertificateThumbprint="THUMBPRINT"}


For more information on WinRM, please refer to <a href="http://msdn.microsoft.com/en-us/library/windows/desktop/aa384426(v=vs.85).aspx">the online documentation at Microsoft's DevCenter</a>. As a quick reference, have a look at the list of useful commands below:

* Do a quickconfig for WinRM with HTTPS: `winrm quickconfig -transport:https`
* View the complete WinRM configuration: `winrm get winrm/config`
* View the listeners that have been configured: `winrm enumerate winrm/config/listener`
* Create an HTTP listener: `winrm create winrm/config/listener?Address=*+Transport=HTTP` (also done by `winrm quickconfig`)
* Allow all hosts to connect to the WinRM listener: `winrm set winrm/config/client @{TrustedHosts="*"}`
* Allow a fixed set of hosts to connect to the WinRM listener: `winrm set winrm/config/client @{TrustedHosts="host1,host2..."}`

<a name="smb_cifs_host_setup_krb5"></a>
#### Kerberos - source host

__N.B.:__ You will only need to configure Kerberos if you are going to use Windows domain accounts to access the remote host with the __WINRM_INTERNAL__ connection type.

In addition to the setup described in [the WINRM section](#smb_cifs_host_setup_winrm), using Kerberos authentication requires that you follow the [Kerberos Requirements for Java](http://docs.oracle.com/javase/6/docs/technotes/guides/security/jgss/tutorials/KerberosReq.html) on the host from which the Overthere connections are initiated, i.e. the source host.

Create a file called `krb5.conf` (Unix) or `krb5.ini` (Windows) with at least the following content:

    [realms]
    EXAMPLE.COM = {
        kdc = KDC.EXAMPLE.COM
    }

Replace the values with the name of your domain/realm and the hostname of your domain controller (multiple entries can be added to allow the Overthere source host to connect to multiple domains) and place the file in the default location for your operating system:

* Linux: `/etc/krb5.conf`
* Solaris: `/etc/krb5/krb5.conf`
* Windows: `C:\Windows\krb5.ini`

Alternatively, place the file somewhere else and add the following Java system property to the command line: `-Djava.security.krb5.conf=/path/to/krb5.conf`. Replace the path with the location of the file you just created.

See [the Kerberos V5 System Administrator's Guide at MIT](http://web.mit.edu/kerberos/krb5-1.10/krb5-1.10.6/doc/krb5-admin.html#krb5_002econf) for more information on the `krb5.conf` format.

<a name="smb_cifs_host_setup_spn"></a>
#### Kerberos - remote host

__N.B.:__ You will only need to configure Kerberos if you are going to use Windows domain accounts to access the remote host with the __WINRM_INTERNAL__ connection type.

By default, Overthere 2.1.0 and up will request access to a Kerberos <a href="http://msdn.microsoft.com/en-us/library/windows/desktop/ms677949(v=vs.85).aspx">service principal name</a> of the form <code>WSMAN/<em>HOST</em></code>, for which an SPN should be configured automatically when you [configure WinRM for a remote host](#smb_cifs_host_setup_winrm).

If that was not configured correctly, if you have overridden the default SPN for which a ticket is requested through the [__winrmKerberosAddPortToSpn__](#smb_cifs_winrmKerberosAddPortToSpn) or the [__winrmKerberosUseHttpSpn__](#smb_cifs_winrmKerberosUseHttpSpn) connection properties, or if you are running an older version of Overthere, you will have configure the service principal names manually.
The parameter [__winrmUseCanonicalHostname__](#smb_cifs_winrmUseCanonicalHostname) can be used to use the remote host FQDN provided by the DNS.  

This can be achieved by invoking the <a href="http://technet.microsoft.com/en-us/library/cc731241(v=ws.10).aspx">setspn</a> command, as an Administrator, on any host in the domain, as follows:
<pre>
setspn -A <em>PROTOCOL</em>/<em>ADDRESS</em>:<em>PORT</em> <em>WINDOWS-HOST</em>
</pre>
where:

* `PROTOCOL` is either `WSMAN` (default) or `HTTP` (if [__winrmKerberosUseHttpSpn__](#smb_cifs_winrmKerberosUseHttpSpn) has been set to `true`).
* `ADDRESS` is the [__address__](#address) used to connect to the remote host,
* `PORT` (optional) is the [__port__](#port) used to connect to the remote host (usually 5985 or 5986, only necessary if [__winrmKerberosAddPortToSpn__](#smb_cifs_winrmKerberosAddPortToSpn) has been set to `true`), and
* `WINDOWS-HOST` is the short Windows hostname of the remote host.

Some other useful commands:

* List all service principal names configured for the domain: `setspn -Q */*`
* List all service principal names configured for a specific host in the domain: `setspn -L _WINDOWS-HOST_`

<a name="smb_cifs_troubleshooting"></a>
### Troubleshooting SMB 2.x/CIFS, WinrRM and Telnet

This section lists a number of common configuration errors that can occur when using Overthere with SMB/CIFS, WinRM and/or Telnet. If you run into other connectivity issues when using Overthere, please let us know by [creating a ticket](https://github.com/xebialabs/overthere/issues) or by [sending us a pull request](https://github.com/xebialabs/overthere/pulls).

For more troubleshooting tips for Kerberos, please refer to the [Kerberos troubleshooting guide in the Java SE documentation](http://docs.oracle.com/javase/6/docs/technotes/guides/security/jgss/tutorials/Troubleshooting.html).

#### Kerberos authentication fails with the message `Unable to load realm info from SCDynamicStore`

The Kerberos subsystem of Java cannot start up. Did you configure it as described in [the section on Kerberos setup for the source host](#smb_cifs_host_setup_krb5)?

#### Kerberos authentication fails with the message `Cannot get kdc for realm …`

The Kerberos subsystem of Java cannot find the information for the realm in the `krb5.conf` file. The realm name specified in [the Kerberos configuration on the source host](#smb_cifs_host_setup_krb5) is case sensitive and must be entered in upper case in the `krb5.conf` file.

Alternatively, you can use the `dns_lookup_kdc` and `dns_lookup_realm` options in the `libdefaults` section to automatically find the right realm and KDC from the DNS server if it has been configured to include the necessary `SRV` and `TXT` records:

    [libdefaults]
        dns_lookup_kdc = true
        dns_lookup_realm = true

#### Kerberos authentication fails with the message `Server not found in Kerberos database (7)`

The service principal name for the remote host has not been added to Active Directory. Did you add the SPN as described in [the section on Kerberos setup for remote hosts](#smb_cifs_host_setup_spn)?

#### Kerberos authentication fails with the message `Pre-authentication information was invalid (24)` or `Identifier doesn't match expected value (906)`

The username or the password supplied was invalid. Did you supply the correct credentials?

#### Kerberos authentication fails with the message `Integrity check on decrypted field failed (31)`

Is the target machine part of a Windows 2000 domain? In that case, you'll have to add `rc4-hmac` to the supported encryption types:

    [libdefaults]
        default_tgs_enctypes = aes256-cts-hmac-sha1-96 des3-cbc-sha1 arcfour-hmac-md5 des-cbc-crc des-cbc-md5 des-cbc-md4 rc4-hmac
        default_tkt_enctypes = aes256-cts-hmac-sha1-96 des3-cbc-sha1 arcfour-hmac-md5 des-cbc-crc des-cbc-md5 des-cbc-md4 rc4-hmac

#### Kerberos authentication fails with the message `Message stream modified (41)`

The realm name specified in [the Kerberos configuration on the source host](#smb_cifs_host_setup_krb5) does not match the case of the Windows domain name. The realm name is case sensitive and must be entered in upper case in the `krb5.conf` file.

#### I am not using Kerberos authentication and I still see messages saying `Unable to load realm info from SCDynamicStore`

The Kerberos subsystem of Java cannot start up and the remote WinRM server is sending a Kerberos authentication challenge. If you are using local accounts, the authentication will proceed succesfully despite this message. To remove these messages either configure Kerberos as described in [the section on Kerberos setup for the source host](#smb_cifs_host_setup_krb5) or disallow Kerberos on the WinRM server as described in step 4 of [the section on WinRM setup](#smb_cifs_host_setup_winrm).

#### Telnet connection fails with the message `VT100/ANSI escape sequence found in output stream. Please configure the Windows Telnet server to use stream mode (tlntadmn config mode=stream).`

The Telnet service has been configured to be in "Console" mode. Did you configure it as described in [the section on Telnet setup](#smb_cifs_host_setup_telnet)?

#### The `winrm` configuration command fails with the message `There are no more endpoints available from the endpoint mapper`

The Windows Firewall has not been started. See [Microsoft Knowledge Base article #2004640](http://support.microsoft.com/kb/2004640) for more information.

#### The `winrm` configuration command fails with the message `The WinRM client cannot process the request`

This can occur if you have disabled the `Negotiate` authentication method in the WinRM configuration. To fix this situation, edit the configuration in the Windows registry under the key `HKLM\SOFTWARE\Microsoft\Windows\CurrentVersion\WSMAN\` and restart the Windows Remote Management service.

_Courtesy of [this blog by Chris Knight](http://blog.chrisara.com.au/2012/06/recovering-from-winrm-authentication.html)_

#### WinRM command fails with the message `java.net.ConnectException: Connection refused`

The Windows Remote Management service is not running or is not running on the port that has been configured. Start the service or configure Overthere to use <a href="#port">a different port number</a>.

#### WinRM command fails with a 401 response code

Multiple causes can lead to this error message:

1. The Kerberos ticket is not accepted by the remote host:

    1. Did you set up the correct service principal names (SPNs) as described in [the section on Kerberos setup for remote hosts](#smb_cifs_host_setup_spn)? The hostname is case insenstive, but it has to be the same as the one used in the **address** connection options, i.e. a simple hostname or a fully qualified domain name. Domain policies may prevent the Windows Management Service from creating the required SPNs. See [this blog by LazyJeff](http://fix.lazyjeff.com/2011/02/how-to-fix-winrm-service-failed-to.html) for more information.

    1. Has the reverse DNS of the remote host been set up correctly? See [Principal names and DNS](http://web.mit.edu/Kerberos/krb5-devel/doc/admin/princ_dns.html) for more information. Please note that the `rdns` option is not available in Java's Kerberos implementation.

1. The WinRM service is not set up to accept unencrypted traffic. Did you execute step #8 of the [host setup for WinRM](#smb_cifs_host_setup_winrm)?

1. The user is not allowed to log in. Did you uncheck the "User must change password at next logon" checkbox when you created the user in Windows?

1. The user is not allowed to perform a WinRM command. Did you grant the user (local) administrative privileges?

#### WinRM command fails with a 500 response code

Multiple causes can lead to this error message:

1. If the command was executing for a long time, this might have been caused by a timeout. You can increase the WinRM timeout specified by the [**winrmTimeout**](#smb_cifs_winrmTimeout) connection option to increase the request timeout. Don't forget to increase the `MaxTimeoutms` setting on the remote host as well. For example, to set the maximum timeout on the server to five minutes, enter the following command:

    winrm set winrm/config @{MaxTimeoutms="300000"}

1. If a lot of commands are being executed concurrently, increase the `MaxConcurrentOperationsPerUser` setting on the server. For example, to set the maximum number of concurrent operations per user to 100, enter the following command:

    winrm set winrm/config/service @{MaxConcurrentOperationsPerUser="100"}

Other configuration options that may be of use are `Service/MaxConcurrentOperations` and `MaxProviderRequests` (WinRM 1.0 only).

#### WinRM command fails with an unknown error code

If you see an unknown WinRM error code in the logging, you can use the `winrm helpmsg` command to get more information, e.g.

    winrm helpmsg 0x80338104
    The WS-Management service cannot process the request. The WMI service returned an 'access denied' error.

_Courtesy of [this PowerShell Magazine blog by Shay Levy](http://www.powershellmagazine.com/2013/03/06/pstip-decoding-winrm-error-messages/)_

### Troubleshooting CIFS

#### CIFS connections are very slow to set up.

The [JCIFS library](http://jcifs.samba.org), which Overthere uses to connect to CIFS shares, will try and query the Windows domain controller to resolve the hostname in SMB URLs. JCIFS will send packets over port 139 (one of the [NetBIOS over TCP/IP] ports) to query the <a href="http://en.wikipedia.org/wiki/Distributed_File_System_(Microsoft)">DFS</a>. If that port is blocked by a firewall, JCIFS will only fall back to using regular hostname resolution after a timeout has occurred.

Set the following Java system property to prevent JCIFS from sending DFS query packets:
`-Djcifs.smb.client.dfs.disabled=true`.

See [this article on the JCIFS mailing list](http://lists.samba.org/archive/jcifs/2009-December/009029.html) for a more detailed explanation.

#### CIFS connections time out

If the problem cannot be solved by changing the network topology, try increasing the JCIFS timeout values documented in the [JCIFS documentation](http://jcifs.samba.org/src/docs/api/overview-summary.html#scp). Another system property not mentioned there but only on the [JCIFS homepage](http://jcifs.samba.org/) is `jcifs.smb.client.connTimeout`.

To get more debug information from JCIFS, set the system property `jcifs.util.loglevel` to 3.

<a name="smb_cifs_common_connection_options"></a>
### Common SMB 2.x and CIFS connection options

The SMB 2.x and CIFS protocol implementation of Overthere defines a number of additional connection options, in addition to the [common connection options](#common_connection_options).

<table>
<tr>
	<th align="left" valign="top"><a name="smb_cifs_connectionType"></a>connectionType</th>
	<td>Specifies what protocol is used to execute commands on the remote hsots. One of the following values must be set:<ul>
		<li><strong><a href="#cifs_host_setup_winrm">WINRM_INTERNAL</a></strong> - uses WinRM over HTTP(S) to execute remote commands. The
		    <strong>port</strong> connection option specifies the Telnet port to connect to. The default value is <code>5985</code> for HTTP and
		    <code>5986</code> for HTTPS. A Java implementation of WinRM used.</li>
		<li><strong><a href="#cifs_host_setup_winrm">WINRM_NATIVE</a></strong> - uses WinRM over HTTP(S) to execute remote commands.
		    The <strong>port</strong> connection option specifies the Telnet port to connect to. The default value is <code>5985</code> for HTTP
		    and <code>5986</code> for HTTPS. The native Windows implementation of WinRM is used, i.e. the <code>winrs</code> command.</li>
		<li><strong><a href="#cifs_host_setup_telnet">TELNET</a></strong> - uses Telnet to execute remote commands. The <strong>port</strong>
		    connection option specifies the Telnet port to connect to. The default value is <code>23</code>.</li>
	</ul></td>
</tr>
<tr>
	<th align="left" valign="top"><a name="smb_cifs_pathShareMappings"></a>pathShareMappings</a></th>
	<td>The path to share mappings to use for CIFS specified as a <code>Map&lt;String, String&gt;</code>, e.g. <code>C:\IBM\WebSphere</code> -&gt;
	<code>WebSphere</code>. If a path is not explicitly mapped to a share, an administrative share will be used. The default value is to use no
	path/share mappings, i.e. to use only administrative shares.</td>
</tr>
<tr>
	<th align="left" valign="top"><a name="smb_cifs_winrmEnableHttps"></a>winrmEnableHttps</th>
	<td>If set to <code>true</code>, HTTPS is used to connect to the WinRM server. Otherwise HTTP is used. The default value is <code>false</code>.
	<br/>
	<strong>N.B.:</strong> This connection option is only applicable for the <strong>WINRM_INTERNAL</strong> and <strong>WINRM_NATIVE</strong> connection types.</td>
<tr>
	<th align="left" valign="top"><a name="smb_cifs_winrmContext"></a>winrmContext</th>
	<td>The context used by the WinRM server. The default value is <code>/wsman</code>.
	<br/>
	<strong>N.B.:</strong> This connection option is only applicable for the <strong>WINRM_INTERNAL</strong> connection type.</td>
</tr>
<tr>
	<th align="left" valign="top"><a name="smb_cifs_winrmEnvelopSize"></a>winrmEnvelopSize</th>
	<td>The WinRM envelop size in bytes to use. The default value is <code>153600</code>.
	<br/>
	<strong>N.B.:</strong> This connection option is only applicable for the <strong>WINRM_INTERNAL</strong> connection type.</td>
</tr>
<tr>
	<th align="left" valign="top"><a name="smb_cifs_winrmHttpsCertificateTrustStrategy"></a>winrmHttpsCertificateTrustStrategy</th>
	<td>The certificate trust strategy for WinRM HTTPS connections. One of the following values can be set:<ul>
		<li><strong>STRICT</strong> (default) - use Java's trusted certificate chains.</li>
		<li><strong>SELF_SIGNED</strong> - self-signed certificates are allowed (see <a href="http://hc.apache.org/httpcomponents-client-ga/httpclient/apidocs/org/apache/http/conn/ssl/TrustSelfSignedStrategy.html">TrustSelfSignedStrategy</a>)</li>
		<li><strong>ALLOW_ALL</strong> - trust all certificates.</li>
	</ul>
	<strong>N.B.:</strong> This connection option is only applicable for the <strong>WINRM_INTERNAL</strong> connection type, when <a href="#cifs_winrmEnableHttps"><strong>winrmEnableHttps</strong></a> is set to <code>true</code>.</td>
</tr>
<tr>
	<th align="left" valign="top"><a name="smb_cifs_winrmHttpsHostnameVerificationStrategy"></a>winrmHttpsHostnameVerificationStrategy</th>
	<td>The hostname verification strategy for WinRM HTTPS connections. One of the following values can be set:<ul>
		<li><strong>STRICT</strong> - strict verification (see <a href="http://hc.apache.org/httpcomponents-client-ga/httpclient/apidocs/org/apache/http/conn/ssl/StrictHostnameVerifier.html">StrictHostnameVerifier</a>)</li>
		<li><strong>BROWSER_COMPATIBLE</strong> (default) - wilcards in certifactes are matched (see <a href="http://hc.apache.org/httpcomponents-client-ga/httpclient/apidocs/org/apache/http/conn/ssl/BrowserCompatHostnameVerifier.html">BrowserCompatHostnameVerifier.html</a>)</li>
		<li><strong>ALLOW_ALL</strong> - trust all hostnames (see <a href="http://hc.apache.org/httpcomponents-client-ga/httpclient/apidocs/org/apache/http/conn/ssl/AllowAllHostnameVerifier.html">AllowAllHostnameVerifier</a>)</li>
	</ul>
	See the <a href="http://hc.apache.org/httpcomponents-client-ga/tutorial/html/connmgmt.html#d5e535">Apache HttpComponent HttpClient documentation</a> for more information about the hostname verifications strategies.
	<br/>
	<strong>N.B.:</strong> This connection option is only applicable for the <strong>WINRM_INTERNAL</strong> connection type, when <a href="#cifs_winrmEnableHttps"><strong>winrmEnableHttps</strong></a> is set to <code>true</code>.</td>
</tr>
<tr>
	<th align="left" valign="top"><a name="smb_cifs_winrmKerberosAddPortToSpn"></a>winrmKerberosAddPortToSpn</th>
	<td>If set to <code>true</code>, the port number (e.g. 5985) will be added to the service principal name (SPN) for which a Kerberos ticket is requested. The default value is <code>false</code>.
	<br/>
	<strong>N.B.:</strong> This connection option is only applicable for the <strong>WINRM_INTERNAL</strong> connection type, when a Windows domain acount is used.</td></td>
</tr>
<tr>
	<th align="left" valign="top"><a name="smb_cifs_winrmKerberosDebug"></a>winrmKerberosDebug</th>
	<td>If set to <code>true</code>, enables debug output for the <a href="http://en.wikipedia.org/wiki/Java_Authentication_and_Authorization_Service">JAAS</a>-based Kerberos authentication within the OverThere connector. The default value is <code>false</code>.
	<br/>
	<strong>N.B.:</strong> This connection option is only applicable for the <strong>WINRM_INTERNAL</strong> connection type, when a Windows domain acount is used.</td>
</tr>
<tr>
<th align="left" valign="top"><a name="smb_cifs_winrmKerberosTicketCache"></a>winrmKerberosTicketCache</th>
<td>If set to <code>true</code>, enables the use of the Kerberos ticket cache for use in authentication.  When enabled, if a password is not specfified the system ticket cache will be used as a  The default value is <code>false</code>.
<br/>
<strong>N.B.:</strong> This connection option is only applicable for the <strong>WINRM_INTERNAL</strong> connection type, when a Windows domain acount is used.</td>
</tr>
<tr>
	<th align="left" valign="top"><a name="smb_cifs_winrmKerberosUseHttpSpn"></a>winrmKerberosUseHttpSpn</th>
	<td>If set to <code>true</code>, the protocol <code>HTTP</code> will be used in the service principal name (SPN) for which a Kerberos ticket is requested. Otherwise the protocol <code>WSMAN</code> is used. The default value is <code>false</code>.
	<br/>
	<strong>N.B.:</strong> This connection option is only applicable for the <strong>WINRM_INTERNAL</strong> connection type, when a Windows domain acount is used.</td></td>
</tr>
<tr>
	<th align="left" valign="top"><a name="smb_cifs_winrmLocale"></a>winrmLocale</th>
	<td>The WinRM locale to use. The default value is <code>en-US</code>.
	<br/>
	<strong>N.B.:</strong> This connection option is only applicable for the <strong>WINRM_INTERNAL</strong> connection type.</td>
</tr>
<tr>
	<th align="left" valign="top"><a name="smb_cifs_winrmTimeout"></a>winrmTimeout</th>
	<td>The WinRM timeout to use in <a href="http://www.w3.org/TR/xmlschema-2/#isoformats">XML schema duration format</a>. The default value is <code>PT60.000S</code>.
	<br/>
	<strong>N.B.:</strong> This connection option is only applicable for the <strong>WINRM_INTERNAL</strong> connection type.</td>
</tr>
<tr>
	<th align="left" valign="top"><a name="smb_cifs_winrsAllowDelegate"></a>winrsAllowDelegate</th>
	<td>If set to <code>false</code>, the user's credentials may be passed to the remote host. This option corresponds to the <code>winrs</code> command option <code>-allowdelegate</code>. The default value is <code>false</code>.
	<br/>
	<strong>N.B.:</strong> This connection option is only applicable for the <strong>WINRM_NATIVE</strong> connection type.</td>
</tr>
<tr>
	<th align="left" valign="top"><a name="smb_cifs_winrsCompression"></a>winrsCompression</th>
	<td>If set to <code>true</code>, compression is enabled. This option corresponds to the <code>winrs</code> command option <code>-compression</code>. The default value is <code>false</code>.
	<br/>
	<strong>N.B.:</strong> This connection option is only applicable for the <strong>WINRM_NATIVE</strong> connection type.</td>
</tr>
<tr>
	<th align="left" valign="top"><a name="smb_cifs_winrsNoecho"></a>winrsNoecho</th>
	<td>If set to <code>true</code>, echo is disabled. This option corresponds to the <code>winrs</code> command option <code>-noecho</code>. The default value is <code>false</code>.
	<br/>
	<strong>N.B.:</strong> This connection option is only applicable for the <strong>WINRM_NATIVE</strong> connection type.</td>
</tr>
<tr>
	<th align="left" valign="top"><a name="smb_cifs_winrsNoprofile"></a>winrsNoprofile</th>
	<td>If set to <code>true</code>, loading the user profile before executing the command is disabled. This option corresponds to the <code>winrs</code> command option <code>-noprofile</code>. The default value is <code>false</code>.
	<br/>
	<strong>N.B.:</strong> This connection option is only applicable for the <strong>WINRM_NATIVE</strong> connection type.</td>
</tr>
<tr>
	<th align="left" valign="top"><a name="smb_cifs_winrsUnencrypted"></a>winrsUnencrypted</th>
	<td>If set to <code>true</code>, encryption is disabled. This option corresponds to the <code>winrs</code> command option <code>-unencrypted</code>. The default value is <code>false</code>.
	<br/>
	<strong>N.B.:</strong> This connection option is only applicable for the <strong>WINRM_NATIVE</strong> connection type.</td>
</tr>
<tr>
	<th align="left" valign="top"><a name="smb_cifs_winrsProxyProtocol"></a>winrsProxyProtocol</th>
	<td>The protocol to use when connecting to the "winrs proxy host", i.e. the host that is used to run the <code>winrs</code> command. The "winrs proxy host" must run Windows. The default value is <code>local</code>, which means the commands will be executed on the local host, which means the local host must run Windows.
	<br/>
	<strong>N.B.:</strong> This connection option is only applicable for the <strong>WINRM_NATIVE</strong> connection type.</td>
</tr>
<tr>
	<th align="left" valign="top"><a name="smb_cifs_winrsProxyConnectionOptions"></a>winrsProxyConnectionOptions</th>
	<td>The connection options to use when connecting to the "winrs proxy host".
	<br/>
	<strong>N.B.:</strong> This connection option is only applicable for the <strong>WINRM_NATIVE</strong> connection type.</td>
</tr>
</table>

<a name="smb_connection_options"></a>
### SMB 2.x connection options

<table>
<tr>
	<th align="left" valign="top"><a name="smb_smbPort"></a>smbPort</th>
	<td>The SMB port to connect to. The default value is <code>445</code>.</td>
</tr>
<tr>
	<th align="left" valign="top"><a name="smb_smbRequireSigning"></a>smbRequireSigning</th>
	<td>Whether to require the server to sign the responses. The default value is <code>false</code>.</td>
</tr>
</table>

<a name="cifs_connection_options"></a>
### CIFS connection options

<table>
<tr>
	<th align="left" valign="top"><a name="cifs_cifsPort"></a>cifsPort</th>
	<td>The CIFS port to connect to. The default value is <code>445</code>.</td>
</tr>
</table>


<a name="jumpstations"></a>
## Jumpstations: SSH tunnels and HTTP proxies
In some networks, certain hosts cannot be reached directly. Instead, a connection should be made through an HTTP proxy or an SSH jumpstation. Overthere supports these network topologies by allowing you to configure "jumpstations" by adding a property called `jumpstation` to the connection options that point to another set of connection options for the jumpstation.

The end result looks something like this:

	protocol: ssh
	options: ConnectionOptions[
		connectionType: SCP
		address: dmzhost1.example.com
		os: UNIX
		username: dmzuser
		password: s3cr3t
		jumpstation: ConnectionOptions[
			protocol: proxy
			address: dmzproxy.exmaple.com
			port: 8888
		]
	]

or:

	protocol: cifs
	options: ConnectionOptions[
		connectionType: WINRM_INTERNAL
		address: dmzhost2.example.com
		os: WINDOWS
		username: Administrator
		password: adm1n=me
		jumpstation: ConnectionOptions[
			protocol: ssh-jumpstation
			address: dmzssh.example.com
			username: jumpuser
			password: j0mp!
		]
	]



The `jumpstation` connection options support the same values (for as much as it makes sense) as regular connection options, with the following additions:

<table>
<tr>
	<th align="left" valign="top"><a name="jumpstations_protocol"></a>protocol</th>
	<td>The kind of jumpstation to use. The following values can be used:<ul>
		<li><strong>proxy</strong> - use an HTTP proxy</li>		<li><strong>ssh-jumpstation</strong> (or <strong>ssh</strong>)- set up an SSH tunnel with a local port forwarding</li>
		</ul>
		The default vaue is <code>ssh-jumpstation</code>.
		</td>
</tr>
<tr>
	<th align="left" valign="top"><a name="jumpstations_portAllocationRangeStart"></a>portAllocationRangeStart</th>
	<td>The port number Overthere starts with to find an available local port for setting up an SSH local port forwarder. This option only applies when using the <code>ssh-jumpstation</code> protocol. The default value is <code>1024</code>.</td>
</tr>
</table>

<a name="release_history"></a>
# Release History
* Overthere 5.0.8 (30-Jun-2020)
    * Upgrade to dom4j 2.1.3
* Overthere 5.0.7 (16-Jan-2020)
    * Improved error handling on file delete failure
    * Improved logging on closing of streams
* Overthere 5.0.6 (5-Aug-2019)
    * Upgrade to dom4j 2.1.1
* Overthere 5.0.5 (23-Jul-2019)
    * Improved Windows command line sanitization
* Overthere 5.0.4 (1-Mar-2019)
    * Fixes for #231, #232
    * Upgrade to SSHJ 0.27.0, SMBJ 0.9.1
* Overthere 5.0.3 (22-Aug-2018)
    * Fixes for #207, #211, #212, #213, #218
    * Upgrade to SSHJ 0.26.0, SMBJ 0.8.0
    * Support programmatic protocol registration
* Overthere 5.0.2 (30-Aug-2017)
    * Upgraded to SMBJ 0.3.0
    * Added support for transcode OvertherFile to another encoding
    * Added support for transparent tunneling for SMB
* Overthere 5.0.1 (08-Jun-2017)
    * Mapped diagnostic context of the parent thread to its child.
* Overthere 5.0.0 (21-Mar-2017)
    * Upgraded to SSHJ 0.21.1
        * SSHJ 0.21.1 (2017-04-25)
        * Fix regression from 40f956b (invalid length parameter on outputstream)
        * SSHJ 0.21.0 (2017-04-14)
        * Added support for ssh-rsa-cert-v01@openssh.com and ssh-dsa-cert-v01@openssh.com certificate key files
        * Upgraded Gradle to 3.4.1
        * Added support for custom string encoding
        * Fixed #312: Upgraded BouncyCastle to 1.56       
* Overthere 4.4.6 (21-Mar-2017)
    * Upgraded to SMBJ 0.0.9
* Overthere 4.4.5 (17-Mar-2017)
    * Upgraded to SMBJ 0.0.8
* Overthere 4.4.4 (01-Mar-2017)
    * Upgraded to SSH/J 0.20.0.
    * Added `suPassword` to hidden fields of `ConnectionOptions` to prevent printing it.
* Overthere 4.4.3 (18-Jan-2017)
    * Added missing PKCS5 KeyFileProvider.
    * Fixed hanging Tunnel connection issue.
    * Upgraded to SSH/J 0.19.1.
    * Upgraded to SMB/J 0.0.7.
* Overthere 4.4.2 (12-Dec-2016)
    * Avoided creation of SecureRandom for every connection, reducing the amount of entropy needed.
    * Upgraded to SSH/J 0.19.0.
    * Upgraded to Scannit v1.4.1 (includes fix for [#179](https://github.com/xebialabs/overthere/pull/179) and [#183](https://github.com/xebialabs/overthere/pull/183)).
    * Fixed [#192](https://github.com/xebialabs/overthere/pull/192).
* Overthere 4.4.1 (07-Oct-2016)
    * Fixed bug causing incorrect parent path resolution for SmbFile.
* Overthere 4.4.0 (22-Sep-2016)
    * Added SMB protocol support [#186](https://github.com/xebialabs/overthere/pull/186).
* Overthere 4.2.2 (12-Jul-2016)
    * Upgraded to SSH/J 0.17.2 (includes fix for [SSH/J issue #252](https://github.com/hierynomus/sshj/issues/252)).
* Overthere 4.3.3 (8-Jul-2016)
    * Fixed [#172](https://github.com/xebialabs/overthere/pull/172), [#176](https://github.com/xebialabs/overthere/issues/176) and [#182](https://github.com/xebialabs/overthere/issues/182).
    * Fixed bug where execution of commands on WINRM_NATIVE connections would fail if an argument contained special characters.
    * Upgraded to SSH/J 0.17.2 (includes fix for [SSH/J issue #252](https://github.com/hierynomus/sshj/issues/252)).
* Overthere 4.3.2 (12-Apr-2016)
    * Fixed execution of some commands on localhost when the OS is Windows.
    * Upgraded to SSH/J 0.16.0.
* Overthere 4.3.1 (01-Apr-2016)
    * Fixed ClassCastException that occurred when passing an SshConnectionType as a string.
* Overthere 4.3.0 (15-Mar-2016)
    * Added support for creating [SSH connections over HTTP proxies](http://www.linuxhowtos.org/Security/sshproxy.htm).
    * Removed `TUNNEL` SSH connection type in favour of the new `ssh-jumpstation` protocol.
    * Upgraded to SSH/J 0.15.0.
    * Fixed [#168](https://github.com/xebialabs/overthere/pull/168) and [#169](https://github.com/xebialabs/overthere/pull/169).
* Overthere 4.1.2 (23-Oct-2015)
    * *NOTE:* This release contains the same code as 4.2.1, except for all the library upgrades of 4.2.0
    * Fixed [#165](https://github.com/xebialabs/overthere/issues/165): Fixed slowness of SFTP copy operation(s).
* Overthere 4.2.1 (21-Oct-2015)
    * Fixed [#165](https://github.com/xebialabs/overthere/issues/165): Fixed slowness of SFTP copy operation(s).
* Overthere 4.2.0 (06-Oct-2015)
    * Upgraded to Java 7.
    * Added more debug logging when closing streams.
    * Fix typo in logging of ``WsmanSPNegoSchemeFactory``
    * Upgraded to scannit 1.4.0, fixes Java8 compatibility.
    * Upgraded to bouncy castle 1.52.
    * Upgraded to commons-net 3.3.
    * Upgraded to commons-codec 1.10.
    * Upgraded to slf4j 1.7.12.
* Overthere 4.1.1 (26-Aug-2015)
    * Fixed bug where WINRM_INTERNAL connection with Windows domain account failed with error message "Unexpected HTTP response on http://hostname/wsman: (401)".
* Overthere 4.1.0 (18-Aug-2015)
    * Fixed bug where temporary files were not deleted for a CIFS/WINRM_INTERNAL or a CIFS/TELNET connection when connection was closed.
    * Fixed bug where short-circuit copy would fail with error message "tar: can't set time on .: Not owner". This could occur when copying from/to a temporary file/directory for an SSH/SU, SSH/SUDO or SSH/INTERACTIVE_SUDO connection.
    * Fixed bug where statting a file would fail with error message "ls -ld /path/to/dir returned 0 but its output is unparseable: ESC" for an SSH/SCP, SSH/SU, SSH/SUDO or SSH/INTERACTIVE_SUDO connection.
    * Added logging (at DEBUG level) for local file operations.
    * `Disconnected from local:` messages are now logged at DEBUG level instead of INFO level.
    * Upgraded to SSH/J 0.13.0.
* Overthere 4.0.1 (02-Jun-2015)
    * Implemented correct fix for [#153](https://github.com/xebialabs/overthere/issues/153).
* Overthere 4.0.0 (17-Apr-2015)
    * The binary incompatibility introduced in Overthere 2.4.7 and present in 2.4.8 and 3.0.0 was breaking too many libraries and too much code using Overthere. We've reverted the breaking change so that `LocalFile.valueOf` once again returns an `OverthereFile` and added a new `LocalFile.from` method which returns a `LocalFile`. Because this version is binary incompatible with 3.0.0 we had to bump the major version _again_ but this is one _is_ binary compatible with Overthere 2.4.6 and below.
* Overthere 3.0.0 (14-Apr-2015)
    * *Please do not use this version of Overthere as it is not binary compatible with Overthere 2.4.6 and lower. Please use Overthere 4.0.0 and up.*
    * Because of the binary incompatiblity introduced in Overthere 2.4.7, we've decided to bump the major version and release this version of Overthere 3.0.0.
    * Upgraded SSHJ to 0.12.0.
    * Can now configure socket timeout, fixes [#156](https://github.com/xebialabs/overthere/issues/156) and [#158](https://github.com/xebialabs/overthere/issues/158)
    * LocalConnection does not log 'Connection [LOCAL:] was not closed, closing automatically.'
* Overthere 2.4.8 (05-Feb-2015)
    * *Please do not use this version of Overthere as it is not binary compatible with Overthere 2.4.6 and lower. Please use Overthere 4.0.0 and up.*
    * Upgraded Scannit to 1.3.1.
    * Checking whether the CifsFile.getParentFile() is valid, fixes [#153](https://github.com/xebialabs/overthere/issues/153)
    * Fixed [#145](https://github.com/xebialabs/overthere/issues/145).
* Overthere 2.4.7 (02-Feb-2015)
    * *Please do not use this version of Overthere as it is not binary compatible with Overthere 2.4.6 and lower. Please use Overthere 4.0.0 and up.*
    * Upgraded SSHJ to 0.11.0.
    * Changed return type of `LocalFile.valueOf(java.io.File)` from `OverthereFile` to `LocalFile`. This breaks binary compatibility with Overthere 2.4.6.
    * Fixed [#139](https://github.com/xebialabs/overthere/issues/139) and [#146](https://github.com/xebialabs/overthere/issues/146).
* Overthere 2.4.6 (09-Jan-2015)
    * Upgraded Scannit to 1.3.0 to remove transitive Guava dependency.
    * Upgraded Gradle to 2.2.1.
    * Fixed [#133](https://github.com/xebialabs/overthere/issues/133), [#134](https://github.com/xebialabs/overthere/issues/134) and [#138](https://github.com/xebialabs/overthere/issues/138).
* Overthere 2.4.5 (27-Oct-2014)
    * Fixed [#130](https://github.com/xebialabs/overthere/issues/130), [#131](https://github.com/xebialabs/overthere/issues/131) and [#132](https://github.com/xebialabs/overthere/issues/132) .
* Overthere 2.4.4 (27-Aug-2014)
    * Fixed [#123](https://github.com/xebialabs/overthere/issues/123) and [#126](https://github.com/xebialabs/overthere/issues/126).
* Overthere 2.4.3 (17-May-2014)
    * Fixed [#101](https://github.com/xebialabs/overthere/issues/101), [#106](https://github.com/xebialabs/overthere/issues/106), [#111](https://github.com/xebialabs/overthere/issues/111), [#112](https://github.com/xebialabs/overthere/issues/112), [#114](https://github.com/xebialabs/overthere/issues/114), [#115](https://github.com/xebialabs/overthere/issues/115) and [#116](https://github.com/xebialabs/overthere/issues/116).
    * Some minor code and documentation fixes.
* Overthere 2.4.2 (26-Mar-2014)
    * Bumped Guava to version 16.0.1. This ensures that overthere should work on JDK 1.7.0_51 and up.
* Overthere 2.4.1 (24-Mar-2014)
    * Fixed race condition in creation of temporary directories.
* Overthere 2.4.0 (12-Mar-2014)
    * Added support for the the SU connection type, which fixes [#102](https://github.com/xebialabs/overthere/issues/102), and reverted the fix for [#89](https://github.com/xebialabs/overthere/issues/89).
    * Improved efficiency of copy operations on remote hosts by using a copy command on that remote host instead of downloading and then uploading the file or directory, which fixes [#91](https://github.com/xebialabs/overthere/issues/91). Note that this behaviour is only invoked when copying files or directories _on_ a remote host, not when copying them _between_ remote hosts.
    * Fixed [#87](https://github.com/xebialabs/overthere/issues/87), [#88](https://github.com/xebialabs/overthere/issues/88), [#96](https://github.com/xebialabs/overthere/issues/96), [#99](https://github.com/xebialabs/overthere/issues/99), [#103](https://github.com/xebialabs/overthere/issues/103), [#104](https://github.com/xebialabs/overthere/issues/104).
* Overthere 2.3.1 (16-Jan-2014)
    * Fixed [#89](https://github.com/xebialabs/overthere/issues/89)
    * Fixed race condition in creation of local temporary directories.
* Overthere 2.3.0 (25-Oct-2013)
    * Implemented support for `winrs`, the native WinRM implementation available on Windows hosts, which fixes [#12|https://github.com/xebialabs/overthere/issues/12]. *N.B.:* To distinguish this connection type from the existing Java one, the connection type `WINRM` has been replaced by `WINRM_INTERNAL` (the Java implementation) and `WINRM_NATIVE` (the Windows implementation).
    * Added `refreshKrb5Config=true` option to the Kerberos JAAS configuration to make sure the configuration is re-read for every request.
    * Upgraded the SSH/J 0.9.0.
    * Fixed bug that occurred when reading or writing many files over a single SFTP connection to a WinSSHD server.
* Overthere 2.2.2 (28-Aug-2013)
    * Fixed [#81](https://github.com/xebialabs/overthere/issues/81).
* Overthere 2.2.1 (24-Jul-2013)
    * Fully implemented `OverthereConnection.startProcess()` for CIFS/WinRM connections, which fixes [#54](https://github.com/xebialabs/overthere/issues/54) properly. The previous implementation did not handle `stdin`.
    * Fixed [#57](https://github.com/xebialabs/overthere/issues/57), [#72](https://github.com/xebialabs/overthere/issues/72), [#73](https://github.com/xebialabs/overthere/issues/73), [#76](https://github.com/xebialabs/overthere/issues/76), [#77](https://github.com/xebialabs/overthere/pull/77) and [#79](https://github.com/xebialabs/overthere/issues/79).
    * Updated documentation and troubleshooting guides for SSH, CIFS, WinRM and Kerberos.
    * Some minor code and documentation fixes.
* Overthere 2.2.0 (07-Feb-2013)
    * Introduced `OverthereExecutionOutputHandler` interface to allow `stderr` to also be captured character by character, which fixes [#67](https://github.com/xebialabs/overthere/issues/67).
    * Made the commands used for SSH/SCP, SSH/SUDO and SSH/INTERACTIVE_SUDO connections configurable, which fixes [#52](https://github.com/xebialabs/overthere/issues/52).
    * Made `canStartProcess()` return `false` for CIFS/WinRM connections because its `startProcess` implementation does not correctly handle `stdin`, disables fix for issue [#54](https://github.com/xebialabs/overthere/issues/54).
    * Fixed [#65](https://github.com/xebialabs/overthere/issues/65), [#68](https://github.com/xebialabs/overthere/issues/68) and [#70](https://github.com/xebialabs/overthere/issues/70).
    * Some minor code fixes.
    * Some not-so-minor documentation improvements: more setup and troubleshooting info for WinRM and Kerberos.
* Overthere 2.1.1 (17-Dec-2012)
    * Fixed [#61](https://github.com/xebialabs/overthere/issues/61) and [#62](https://github.com/xebialabs/overthere/issues/62).
    * Added connection option [__openShellBeforeExecute__](#ssh_openShellBeforeExecute), which fixes [#63](https://github.com/xebialabs/overthere/issues/63).
* Overthere 2.1.0 (26-Oct-2012)
    * Re-enabled support for Windows domain accounts in CIFS/Telnet connections, which fixes [#60](https://github.com/xebialabs/overthere/issues/60).
    * Fixed Kerberos code to use WSMAN SPN by default, which fixes [#58](https://github.com/xebialabs/overthere/issues/58).
    * Added option to add the port to the SPN, which fixes [#49](https://github.com/xebialabs/overthere/issues/49).
    * Added support for Negotiate authentication (Kerberos only), which fixes [#59](https://github.com/xebialabs/overthere/issues/59).
    * Some minor code and documentation fixes.
* Overthere 2.1.0-beta-1 (21-Sep-2012)
    * Implemented `OverthereProcess.startProcess()` for CIFS/WinRM connections, which fixes [#54](https://github.com/xebialabs/overthere/issues/54).
    * Fixed [#53](https://github.com/xebialabs/overthere/issues/53) and [#55](https://github.com/xebialabs/overthere/issues/55).
    * Some minor code and documentation fixes.
* Overthere 2.0.0 (22-Aug-2012)
    * Stable release of Overthere 2.0.0.
    * Some minor code and documentation fixes.
* Overthere 2.0.0-rc-1 (17-Aug-2012)
    * Added [__sudoPreserveAttributesOnCopyFromTempFile__](#ssh_sudoPreserveAttributesOnCopyFromTempFile) and [__sudoPreserveAttributesOnCopyToTempFile__](#ssh_sudoPreserveAttributesOnCopyToTempFile) to specify whether `-p` should be used to copy files from and to the connection temporary directory when the connection type is [__SUDO__](#ssh_host_setup_sudo) or [__INTERACTIVE_SUDO__](#ssh_host_setup_interactive_sudo).
    * Changed default value of [__sudoOverrideUmask__](#ssh_sudoOverrideUmask) to `true`.
    * Fixed Kerberos authentication for WinRM connections which was broken by overzealouos code cleanup.
    * Upgraded to SSH/J 0.8.1.
    * Added description of WinRM options and how to set up Kerberos authentication to the documentation.
    * Added troubleshooting section to the documentation.
* Overthere 2.0.0-beta-7 (02-Aug-2012)
    * Fixed bug in WinRM implementation: It was not sending individual stdout chars to `OverthereProcessOutputHandler.handleOutput()`.
* Overthere 2.0.0-beta-6 (02-Aug-2012)
    * Renamed `CIFS_PATH_SHARE_MAPPING` back to `PATH_SHARE_MAPPINGS`.
* Overthere 2.0.0-beta-5 (02-Aug-2012)
    * Added support for Windows domain accounts to CIFS and WinRM connection methods.
    * Renamed a few options.
    * Fixed bug in SSH tunnel port allocation code that caused the same local port to be allocated multiple times on Windows.
    * Changed license to GPLv2 with XebiaLabs FLOSS License Exception.
* Overthere 2.0.0-beta-4 (19-Jun-2012)
    * Fixed [#42](https://github.com/xebialabs/overthere/issues/42).
    * Moved the `itest-support` sub project out to new Github repository [Overcast](https://github.com/xebialabs/overcast)
    * Updated documentation.
* Overthere 2.0.0-beta-3 (27-Mar-2012)
    * Updated documentation.
* Overthere 2.0.0-beta-2 (23-Mar-2012)
	* Fixed [#39](https://github.com/xebialabs/overthere/issues/39) and [#40](https://github.com/xebialabs/overthere/issues/40).
	* Upgraded to latest jCIFS to fix issues with windows domain names and stability using tunnels.
	* Set default pty to true in case of interactive sudo and no pty set.
* Overthere 1.0.17 (20-Mar-2012)
    * Fixed [#39](https://github.com/xebialabs/overthere/issues/39) and [#40](https://github.com/xebialabs/overthere/issues/40).
* Overthere 2.0.0-beta-1 (05-Mar-2012)
    * Re-implemented SSH tunnels. Tunnels are now created on demand instead of the user having to specify the localPortForwards explicitly. This makes management of tunnels easier and prevents clashes.
    * Ported Overthere tests to use TestNG instead of JUnit.
* Overthere 1.0.16 (23-Feb-2012)
    * Reverted changes made to support SSH tunnels in 1.0.14 and 1.0.15 because it did not work as well as we hoped. We are reimplementing it for Overthere 2.0 to be released early March.
    * Fixed command line encoding bugs for SSH/CYGWIN on Windows:
        * Now transforming the first element of the command line to a Cygwin path so that batch files (and executables) in specific directories (instead of on the PATH) can be executed.
        * Encoding the command line as if the target OS is UNIX because OpenSSH on Cygwin uses Unix encoding.
* Overthere 1.0.15 (21-Feb-2012)
    * Added explicit close() method to the new `com.xebialabs.overthere.OverthereConnection` interface (it was a class in 1.0.13) that does not throw `java.io.IOException`.
* Overthere 1.0.14 (20-Feb-2012)
    * Added support for SSH tunnels to jumpstations.
    * Added support for NTLM authentication.
    * Upgraded to SSH/J 0.7.0.
* Overthere 1.0.13 (18-Jan-2012)
    * Masked passwords in logging.
    * Made ItestHostFactory also look for itest.properties in `~/.itest` (in addition to the classpath and the current working directory).
* Overthere 1.0.12 (12-Jan-2012)
    * Allowed forward slashes (`/`) to be used in Windows paths.
    * Made it possible to access non-administrative shares on Windows so that the CIFS connection methods can be used with regular user accounts. See the [__pathShareMappings__](#cifs_pathShareMappings) connection option.
    * Added the [__allocatePty__](#ssh_allocatePty) connection option to specify an explicit pseudo terminal to use.
* Overthere 1.0.11 (09-Dec-2011)
    * Fixes to the SSH/WinSSHD implementation and a few other minor fixes.
    * Added a lot of documentation.
    * Added examples project.
    * Changed license to ASLv2.
* Overthere 1.0.10 (23-Nov-2011)
    * Added support for SSH/WinSSHD on Windows.
* Overthere 1.0.9 (22-Nov-2011)
    * Initial public release with support for SSH on Unix as well as CIFS/TELNET, CIFS/WinRM and SSH/CYGWIN on Windows.
