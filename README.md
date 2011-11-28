# Introduction 

Overthere is a Java library to manipulate files and execute processes on remote hosts, i.e. do stuff "over there". It was built for and is used in the [XebiaLabs](http://xebialabs.com/) deployment automation product Deployit as a way to perform tasks on remote hosts, e.g. copy configuration files, install EAR files or restart web servers. Another way of looking it at is to say that Overthere gives you `java.io.File` and `java.lang.Process` as they should've been: as interfaces, created by a factory and extensible through an SPI mechanism.

For a more thorough introducion to Overthere, check the [presentation on Overthere](http://www.slideshare.net/vpartington/presentation-about-overthere-for-jfall-2011) that I gave for J-Fall 2011, a Java conference in the Netherlands. Don't worry, the presentation is in English. :-)

Overthere is available under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0).

# Getting Overthere

To get Overthere, you have two options:

1. Add a dependency to Overthere to your project.
2. Build Overthere yourself.

And, if you want, you can also run the Overthere examples used in the Overthere presentation mentioned above.

Binary releases of Overthere are not provided here, but you can download it [straight from the Maven Central repository](http://search.maven.org/#artifactdetails%7Ccom.xebialabs.overthere%7Coverthere%7C1.0.10%7Cjar) if you want to.

## Depending on Overthere

1. If your project is built with Maven, add the following dependency to the pom.xml:
	<dependency>
		<groupId>com.xebialabs.overthere</groupId>
		<artifactId>overthere</artifactId>
		<version>1.0.10</version>
	</dependency>
2. If your project is built using another build tool that uses the Maven Central repository, translate these dependencies into the format used by your build tool.

## Building Overthere

1. Install [Gradle 1.0-milestone-3](http://www.gradle.org/).
2. Clone the Overthere repository.
3. Run the command `gradle clean build`.

## Running the examples

1. Install [Maven 2.2.1 or up](http://maven.apache.org/).
2. Clone the Overthere repository.
3. Go into the `examples` directory and run the command `mvn eclipse:eclipse`.
4. Import the `examples` project into Eclipse.
5. Change the login details in the example classes (address, username and password) and run them!

# Configuring Overthere

The protocols that Overthere uses to connect to remote hosts, such as SSH, CIFS, Telnet and WinRM, are existing protocols for which support is built into many platforms. As such you will not need to install any custom software on the target hosts. Nevertheless in some cases the target platforms have to be configured to correctly work with Overthere. Also, Overthere has a number of configuration features that allow you tweak the way it interfaces with the remote hosts.

## Protocols

Overthere supports a number of protocols to connect to remote hosts:

* __local__ - a connection to the local host. This is a wrapper around <a href="http://download.oracle.com/javase/6/docs/api/java/io/File.html"></code>java.io.File</code></a> and <a href="http://docs.oracle.com/javase/6/docs/api/java/lang/Process.html"></code>java.lang.Process</code></a>.
* __ssh__ - a connection using the [SSH protocol](http://en.wikipedia.org/wiki/Secure_Shell), to a Unix host or to a Windows host running either OpenSSH on Cygwin (i.e. COPSSH) or WinSSHD.
* __cifs__ - a connection using the [CIFS protocol](http://en.wikipedia.org/wiki/Server_Message_Block), also known as SMB, for file manipulation and, depending on the settings, using either [Telnet](http://en.wikipedia.org/wiki/Telnet) or [WinRM](http://en.wikipedia.org/wiki/WS-Management) for process execution. This protocol is only supported for Windows hosts.

## Connection options

Apart from selecting a protocol to use, you will also need to supply a number of connection options when creating a connection. Common connection options are:

<table>
<tr>
	<th align="left" valign="top">os</th>
	<td>The operating system of the remote host, either <code>UNIX</code> or <code>WINDOWS</code>. This property is required for all protocols, except for the <strong>local</strong> protocol.</td>
</tr>
<tr>
	<th align="left" valign="top">address</th>
	<td>The address of the remote host.</td>
</tr>
<tr>
	<th align="left" valign="top">port</th>
	<td>The port to use when connecting to the remote host. The interpretation and the default value for this connection option depend on the protocol that is used.</td>
</tr>
<tr>
	<th align="left" valign="top">username</th>
	<td>The username to use when connecting to the remote host.</td>
</tr>
<tr>
	<th align="left" valign="top">password</th>
	<td>The password to use.</td>
</tr>
<tr>
	<th align="left" valign="top">tmp</th>
	<td>The temporary directory. For each connection, a <em>connection temporary directory</em> with a name like <code>deployit-20111128T132600-7234435.tmp</code> is created. By default that directory is removed when the connection is closed.</td>
</tr>
<tr>
	<th align="left" valign="top">tmpDeleteOnDisconnect</th>
	<td>If set to <code>false</code>, the connection temporary directory is not removed when the connection. The default value is <code>true</code>.</td>
</tr>
<tr>
	<th align="left" valign="top">tmpFileCreationRetries</th>
	<td>The number of times Overthere attempts to create a temporary file with a unique name. The default value is <code>100</code>.</td>
</tr>
<tr>
	<th align="left" valign="top">connectionTimeoutMillis</th>
	<td>The number of milliseconds Overthere waits for a connection to a remote host to be established. The default value is <code>120000</code>, i.e. 2 minutes.</td>
</tr>
</table>
Apart from these common connection options, some protocol define additional protocol-specific connection options. These are documented below, with the protocol.

## LOCAL

The local protocol implementation uses the local file manipulation and local process execution capabilities built-in to Java. The __os__ connection property is hardcoded to the operating system of the local host and the `tmp` property defaults to the system temporary directory as specified by the `java.io.tmpdir` [system property](http://docs.oracle.com/javase/6/docs/api/java/lang/System.html#getProperties()). There are no protocol-specific connection properties.

## SSH

The SSH protocol implementation of Overthere uses the [SSH](http://en.wikipedia.org/wiki/Secure_Shell) protocol to connect to remote hosts to manipulate files and execute commands. Most Unix systems already have an SSH server installed and configured and a number of different SSH implementations are available for Windows although not all of them are supported by Overther (see below).

### Connection options

The SSH protocol implementation of Overthere defines a number of additional connection properties:

<table>
<tr>
	<th align="left" valign="top">connectionType</th>
	<td>Specifies how the SSH protocol is used. One of the following values must be set:
<ul>
<li><strong>SFTP</strong> - uses SFTP to transfer files, to a Unix host. Unless <code>sudo</code> or a similar command is needed to execute commands, this is the best and fastest option to choose for Unix hosts.</li>
<li><strong>SFTP__CYGWIN</strong> -  uses SFTP to transfer files, to a Windows host running OpenSSH on Cygwin.</li>
<li><strong>SFTP_WINSSHD</strong> - uses SFTP to transfer files, to a Windows host running WinSSHD.</li>
<li><strong>SCP</strong> - uses SCP to transfer files, to a Unix host. Not needed unless your SSH server has disabled the SFTP subsystem.</li>
<li><strong>SUDO</strong> - uses SCP to transfer files, to a Unix host. Uses the <a href="http://en.wikipedia.org/wiki/Sudo"><code>sudo</code></a> command, configured with <strong>NOPASSWD</strong> for all commands, to execute commands. Select this connection type if the <strong>username</strong> you are connecting with does not have the right permissions to manipulate the files that need to be manipulated and/or to execute the commands that need to be executed. <br/>If this connection type is selected, the <strong>sudoUsername</strong> connection property is required and specifies that user that <em>does</em> have the necessary permissions. See below for a more detailed description.</li>
<li><strong>INTERACTIVE_SUDO</strong> - uses SCP to transfer files, to a Unix host. Uses the <code>sudo</code> command, <em>not</em> been configured with <strong>NOPASSWD</strong> for all commands, to execute commands. This is similar to the <code>SUDO</code> connection type but also detects the password prompt that is shown by the <code>sudo</code> command when the login user (<strong>username</strong>) tries to execute a commands as the privileged user (<strong>sudoUsername</strong>) when that command has not been configured in <code>/etc/sudoers</code> with <strong>NOPASSWD</strong>. <br/><strong>N.B.:</strong> Because the password of the login user is needed to answer this prompt, this connection type is incompatible with the <strong>privateKeyFile</strong> option that can be used to authenticate with a private key file.</li>
</ul>
</td>
</tr>
<tr>
	<th align="left" valign="top">interactiveKeyboardAuthRegex</th>
	<td>The regular expression to look for in keyboard-interactive prompts before sending the password. The default value is <code>.*Password:[ ]?</code>. When the SSH server is configured to not allow <a href="http://www.ietf.org/rfc/rfc4252.txt">password authentication</a> but is configured to allow <a href="http://www.ietf.org/rfc/rfc4256.txt">keyboard-interactive authentication</a> using passwords, Overthere will compare the interactive-keyboard prompt against this regular expression and send the `password` when they match.</td>
</tr>
<tr>
	<th align="left" valign="top">privateKeyFile</th>
	<td>The RSA private key file to use when connecting to the remote host. When this connection option is specified, the <strong>password</strong> connection option is ignored.</td>
</tr>
<tr>
	<th align="left" valign="top">passphrase</th>
	<td>The passphrase to unlock the RSA private key file specified with the <strong>privateKeyFile</strong> connection option. If this connection option is not specified, the RSA private key file must have an empty passphrase.</td>
</tr>
<tr>
	<th align="left" valign="top">allocateDefaultPty</th>
	<td>If set to <code>true</code>, the SSH server is requested to allocate a default pty (pseudo terminal) for the process. This is needed for some commands when they perform interaction with the user, most notably many implementations of `sudo` (the error message <code>sorry, you must have a tty to run sudo</code> will appear in the output).  The default value is <code>false</code>. <br/><strong>N.B.:</strong> Some SSH servers will crash when they are requested to allocate a pty, most notable OpenSSH on AIX. To verify the behaviour of your SSH server, you can manually execute the <code>ssh</code> command with the <code>-T</code> (disable pty allocation) or <code>-t</code> (force pty allocation) flags.</td>
</tr>
<tr>
	<th align="left" valign="top">sudoUsername</th>
	<td>The username of the user that can manipulate the files that need to be manipulated and that can execute the commands that need to be executed. Only applicable for the <strong>SUDO</strong> and <strong>INTERACTIVE_SUDO</strong> connection types.</td>
</tr>
<tr>
	<th align="left" valign="top">sudoCommandPrefix</th>
	<td>The command to prefix to the command to be executed to execute it as <strong>sudoUsername</strong>. The string <code>{0}</code> is replaced with the vaulue of <strong>sudoUsername</strong>. The default value is <code>sudo -u {0}</code>. Only applicable for the <strong>SUDO</strong> and <strong>INTERACTIVE_SUDO</strong> connection types.</td>
</tr>
<tr>
	<th align="left" valign="top">sudoQuoteCommand</th>
	<td>If set to true, the original command is added as one argument to the prefix configured with the <code>sudoCommandPrefix</code> connection option. This has the result of quoting the original command, which is needed for commands like <code>su</code>. Compare <code>sudo -u privilegeduser start server1</code> to <code>su privilegeduser 'start server1'</code>. The default value is <code>false</code>. Only applicable for the <strong>SUDO</strong> and <strong>INTERACTIVE_SUDO</strong> connection types.</td>
</tr>
<tr>
	<th align="left" valign="top">sudoOverrideUmask</th>
	<td>If set to <code>true</code>, Overthere will explicitly change the permissions with chmod -R go+rX after uploading a file or directory with scp.  Only applicable for the <strong>SUDO</strong> and <strong>INTERACTIVE_SUDO</strong> connection types.</td>
</tr>
<tr>
	<th align="left" valign="top">sudoPasswordPromptRegex</th>
	<td>The regular expression to be used when looking for sudo password pomprts. When the connection type is set to <strong>INTERACTIVE_SUDO</strong>, Overthere will look for strings that match this regular expression in the first line of the output of a command, and send the password if a match occurs. The default value is <code>.*[Pp]assword.*:</code> Only applicable for the <strong>INTERACTIVE_SUDO</strong> connection type.</td>
</tr>
</table>

### Host setup

To connect to a remote host using the SSH protocol, you will need to install an SSH server on that remote host. For Unix platforms, we recommend [OpenSSH](http://www.openssh.com/). It is included in all Linux distributions and most other Unix flavours. For Windows platforms, two SSH servers are supported:
    * OpenSSH on [Cygwin](http://www.cygwin.com/). We recommend [copSSH](http://www.itefix.no/i2/copssh) as a convenient packaging of OpenSSH and Cygwin. It is a free source download but since 22/11/2011 the binary installers are a paid solution.
    * [WinSSHD](http://www.bitvise.com/winsshd) is a commercial SSH server that has a lot of configuration options.

* To use the __SFTP__ connection type, make sure SFTP is enabled in the SSH server. This is enabled by default in most SSH servers.
* To use the __SUDO__ connection type, the `/etc/sudoers` coniguration will have to be set up in such a way that the user configured with the connection option __username__ can execute the commands below as the user configured with the connection option __sudoUsername__. The arguments passed to these commands depend on the exact usage of the Overthere connection. Check the `INFO` messages on the `com.xebialabs.overthere.ssh.SshConnection` category to see what commands get executed.
    * `ls`
    * `cp`
    * `mv`
    * `mkdir`
    * `rmdir`
    * `rm`
    * `chmod`
    * Any other command that you want to execute.
    
* To use the __SUDO__ connection type, the commands mentioned above must be configured with the __NOPASSWD__ setting in the `/etc/sudoers` file. Otherwise you will have to use the __INTERACTIVE_SUDO__ connection type. When the __INTERACTIVE_SUDO__ connection type is used, the first line of the output will be matched against the regular expression configured with the __sudoPasswordPromptRegex__ connection option. If a match is found, the value of the __password__ connection option is sent. <br/>If the __sudoPasswordPromptRegex__ was set incorrectly, the most common symptom is for the command to appear to hang. If you have trouble determining the proper value for the __sudoPasswordPromptRegex__ connection option, set the log level for the `com.xebialabs.overthere.ssh.SshInteractiveSudoPasswordHandlingStream` category to `TRACE` and examine the output.


## WinRM

Please refer to [README document](https://github.com/xebialabs/overthere/blob/master/overthere/winrmdoc/README.md) and the [WinRM setup document](https://github.com/xebialabs/overthere/blob/master/overthere/winrmdoc/WinRM.md).

