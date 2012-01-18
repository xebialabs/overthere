# Table of Contents

* [Introduction](#introduction)
* [Getting Overthere](#getting_overthere)
	* [Depending on Overthere](#depending_on_overthere)
	* [Building Overthere](#building_overthere)
	* [Running the Examples](#running_the_examples)
* [Configuring Overthere](#configuring_overthere)
	* [Protocols](#protocols)
	* [Connection options](#common_connection_options)
	* [Local](#local)
	* [SSH](#ssh)
	* [CIFS](#cifs) (includes Telnet and WinRM)
* [Release History](#release_history)

<a name="introduction"/>
# Introduction 

Overthere is a Java library to manipulate files and execute processes on remote hosts, i.e. do stuff "over there". It was built for and is used in the [XebiaLabs](http://xebialabs.com/) deployment automation product Deployit as a way to perform tasks on remote hosts, e.g. copy configuration files, install EAR files or restart web servers. Another way of looking at it is to say that Overthere gives you `java.io.File` and `java.lang.Process` as they should've been: as interfaces, created by a factory and extensible through an SPI mechanism.

For a more thorough introduction to Overthere, check the [presentation on Overthere](http://www.slideshare.net/vpartington/presentation-about-overthere-for-jfall-2011) that I gave for J-Fall 2011, a Java conference in the Netherlands. Don't worry, the presentation is in English. :-)

Overthere is available under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0).

<a name="getting_overthere"/>
# Getting Overthere

To get Overthere, you have two options:

1. Add a dependency to Overthere to your project.
2. Build Overthere yourself.

And, if you want, you can also run the Overthere examples used in the Overthere presentation mentioned above.

Binary releases of Overthere are not provided here, but you can download it [straight from the Maven Central repository](http://search.maven.org/#artifactdetails%7Ccom.xebialabs.overthere%7Coverthere%7C1.0.13%7Cjar) if you want to.

<a name="depending_on_overthere"/>
## Depending on Overthere

1. If your project is built with Maven, add the following dependency to the pom.xml:

		<dependency>
			<groupId>com.xebialabs.overthere</groupId>
			<artifactId>overthere</artifactId>
			<version>1.0.13</version>
		</dependency>

2. If your project is built using another build tool that uses the Maven Central repository, translate these dependencies into the format used by your build tool.

<a name="building_overthere"/>
## Building Overthere

1. Install [Gradle 1.0-milestone-7](http://www.gradle.org/).
2. Clone the Overthere repository.
3. Run the command `gradle clean build`.

<a name="running_the_examples"/>
## Running the examples

1. Install [Maven 2.2.1 or up](http://maven.apache.org/).
2. Clone the Overthere repository.
3. Go into the `examples` directory and run the command `mvn eclipse:eclipse`.
4. Import the `examples` project into Eclipse.
5. Change the login details in the example classes (address, username and password) and run them!

<a name="configuring_overthere"/>
# Configuring Overthere

The protocols that Overthere uses to connect to remote hosts, such as SSH, CIFS, Telnet and WinRM, are existing protocols for which support is built into many platforms. As such you will not need to install any custom software on the target hosts. Nevertheless in some cases the target platforms have to be configured to correctly work with Overthere. Also, Overthere has a number of configuration features that allow you tweak the way it interfaces with the remote hosts.

<a name="protocols"/>
## Protocols

Overthere supports a number of protocols to connect to remote hosts:

* [__local__](#local) - a connection to the local host. This is a wrapper around <a href="http://download.oracle.com/javase/6/docs/api/java/io/File.html"></code>java.io.File</code></a> and <a href="http://docs.oracle.com/javase/6/docs/api/java/lang/Process.html"></code>java.lang.Process</code></a>.
* [__ssh__](#ssh) - a connection using the [SSH protocol](http://en.wikipedia.org/wiki/Secure_Shell), to a Unix host or to a Windows host running either OpenSSH on Cygwin (i.e. COPSSH) or WinSSHD.
* [__cifs__](#cifs) - a connection using the [CIFS protocol](http://en.wikipedia.org/wiki/Server_Message_Block), also known as SMB, for file manipulation and, depending on the settings, using either [Telnet](http://en.wikipedia.org/wiki/Telnet) or [WinRM](http://en.wikipedia.org/wiki/WS-Management) for process execution. This protocol is only supported for Windows hosts.

<a name="common_connection_options"/>
## Connection options

Apart from selecting a protocol to use, you will also need to supply a number of connection options when creating a connection. Common connection options are:

<table>
<tr>
	<th align="left" valign="top"><a name="os"/>os</th>
	<td>The operating system of the remote host, either <code>UNIX</code> or <code>WINDOWS</code>. This property is required for all protocols, except for the <strong>local</strong> protocol.</td>
</tr>
<tr>
	<th align="left" valign="top"><a name="address"/>address</th>
	<td>The address of the remote host, either an IP address or a DNS name.</td>
</tr>
<tr>
	<th align="left" valign="top"><a name="port"/>port</th>
	<td>The port to use when connecting to the remote host. The interpretation and the default value for this connection option depend on the protocol that is used.</td>
</tr>
<tr>
	<th align="left" valign="top"><a name="username"/>username</th>
	<td>The username to use when connecting to the remote host.</td>
</tr>
<tr>
	<th align="left" valign="top"><a name="password"/>password</th>
	<td>The password to use.</td>
</tr>
<tr>
	<th align="left" valign="top"><a name="tmp"/>tmp</th>
	<td>The temporary directory. For each connection, a <em>connection temporary directory</em> with a name like <code>overthere-20111128T132600-7234435.tmp</code> is created. By default that directory is removed when the connection is closed.</td>
</tr>
<tr>
	<th align="left" valign="top"><a name="tmpFileCreationRetries"/>tmpFileCreationRetries</th>
	<td>The number of times Overthere attempts to create a temporary file with a unique name. The default value is <code>100</code>.</td>
</tr>
<tr>
	<th align="left" valign="top"><a name="tmpDeleteOnDisconnect"/>tmpDeleteOnDisconnect</th>
	<td>If set to <code>false</code>, the connection temporary directory is not removed when the connection. The default value is <code>true</code>.</td>
</tr>
<tr>
	<th align="left" valign="top"><a name="connectionTimeoutMillis"/>connectionTimeoutMillis</th>
	<td>The number of milliseconds Overthere waits for a connection to a remote host to be established. The default value is <code>120000</code>, i.e. 2 minutes.</td>
</tr>
</table>
Apart from these common connection options, some protocols define additional protocol-specific connection options. These are documented below, with the protocol.

<a name="local"/>
## LOCAL

The local protocol implementation uses the local file manipulation and local process execution capabilities built-in to Java. The __os__ connection property is hardcoded to the operating system of the local host and the `tmp` property defaults to the system temporary directory as specified by the `java.io.tmpdir` [system property](http://docs.oracle.com/javase/6/docs/api/java/lang/System.html#getProperties()). There are no protocol-specific connection properties.

<a name="ssh"/>
## SSH

The SSH protocol implementation of Overthere uses the [SSH](http://en.wikipedia.org/wiki/Secure_Shell) protocol to connect to remote hosts to manipulate files and execute commands. Most Unix systems already have an SSH server installed and configured and a number of different SSH implementations are available for Windows although not all of them are supported by Overthere.

See the [section on the host setup](#ssh_host_setup) for more information on how to setup the remote hosts.

<a name="ssh_connection_options"/>
### Connection options

The SSH protocol implementation of Overthere defines a number of additional connection properties, in addition to the [common connection options](#common_connection_options).

<table>
<tr>
	<th align="left" valign="top"><a name="ssh_connectionType"/>connectionType</th>
	<td>Specifies how the SSH protocol is used. One of the following values must be set:
<ul>
<li><strong><a href="#ssh_host_setup_sftp">SFTP</a></strong> - uses SFTP to transfer files, to a Unix host. Unless <code>sudo</code> or a similar command is needed to execute commands, this is the best and fastest option to choose for Unix hosts.</li>
<li><strong><a href="#ssh_host_setup_sftp_cygwin">SFTP_CYGWIN</a></strong> -  uses SFTP to transfer files, to a Windows host running OpenSSH on Cygwin.</li>
<li><strong><a href="#ssh_host_setup_sftp_winsshd">SFTP_WINSSHD</a></strong> - uses SFTP to transfer files, to a Windows host running WinSSHD.</li>
<li><strong>SCP</strong> - uses SCP to transfer files, to a Unix host. Not needed unless your SSH server has disabled the SFTP subsystem.</li>
<li><strong><a href="#ssh_host_setup_sudo">SUDO</a></strong> - uses SCP to transfer files, to a Unix host. Uses the <a href="http://en.wikipedia.org/wiki/Sudo"><code>sudo</code></a> command, configured with <strong>NOPASSWD</strong> for all commands, to execute commands. Select this connection type if the <strong>username</strong> you are connecting with does not have the right permissions to manipulate the files that need to be manipulated and/or to execute the commands that need to be executed. <br/>If this connection type is selected, the <strong>sudoUsername</strong> connection property is required and specifies that user that <em>does</em> have the necessary permissions. See below for a more detailed description.</li>
<li><strong><a href="#ssh_host_setup_interactive_sudo">INTERACTIVE_SUDO</a></strong> - uses SCP to transfer files, to a Unix host. Uses the <code>sudo</code> command, <em>not</em> been configured with <strong>NOPASSWD</strong> for all commands, to execute commands. This is similar to the <code>SUDO</code> connection type but also detects the password prompt that is shown by the <code>sudo</code> command when the login user (<strong>username</strong>) tries to execute a commands as the privileged user (<strong>sudoUsername</strong>) when that command has not been configured in <code>/etc/sudoers</code> with <strong>NOPASSWD</strong>. <br/><strong>N.B.:</strong> Because the password of the login user is needed to answer this prompt, this connection type is incompatible with the <strong>privateKeyFile</strong> option that can be used to authenticate with a private key file.</li>
</ul></td>
</tr>
<tr>
	<th align="left" valign="top"><a name="ssh_sudoUsername"/>sudoUsername</th>
	<td>The username of the user that can manipulate the files that need to be manipulated and that can execute the commands that need to be executed. This connection options is only applicable for the <strong>SUDO</strong> and <strong>INTERACTIVE_SUDO</strong> connection types.</td>
</tr>
<tr>
	<th align="left" valign="top"><a name="ssh_privateKeyFile"/>privateKeyFile</th>
	<td>The RSA private key file to use when connecting to the remote host. When this connection option is specified, the <strong>password</strong> connection option is ignored.</td>
</tr>
<tr>
	<th align="left" valign="top"><a name="ssh_passphrase"/>passphrase</th>
	<td>The passphrase to unlock the RSA private key file specified with the <strong>privateKeyFile</strong> connection option. If this connection option is not specified, the RSA private key file must have an empty passphrase.</td>
</tr>
<tr>
	<th align="left" valign="top"><a name="ssh_allocateDefaultPty"/>allocateDefaultPty</th>
	<td>If set to <code>true</code>, the SSH server is requested to allocate a pty (pseudo terminal) for the process, as if the <a href="#ssh_allocatePty"><strong>allocatePty</strong></a> option were set to the value <code>dummy:80:24:0:0</code>. The default value is <code>false</code>.
	<br/><strong>N.B.:</strong> This option is needed for some commands when they perform interaction with the user, most notably many implementations of `sudo` (the error message <code>sorry, you must have a tty to run sudo</code> will appear in the output otherwise). 
	<br/><strong>N.B.:</strong> Some SSH servers will crash when they are requested to allocate a pty, most notably OpenSSH on AIX. To verify the behaviour of your SSH server, you can manually execute the <code>ssh</code> command with the <code>-T</code> (disable pty allocation) or <code>-t</code> (force pty allocation) flags.</td>
</tr>
<tr>
	<th align="left" valign="top"><a name="ssh_allocatePty"/>allocatePty</th>
	<td>If set to a non-null value, the SSH server is requested to allocate a pty (pseudo terminal) for the process with the setting specified by this option. The format is <code>TERM:COLS:ROWS:WIDTH:HEIGHT</code>, e.g. <code>xterm:80:24:0:0</code>. If set, this option overrides the <a href="#ssh_allocateDefaultPty"><code>allocateDefaultPty</code></a> option. The default value is <code>null</code>.
	<br/><strong>N.B.:</strong> This option is needed for some commands when they perform interaction with the user, most notably many implementations of `sudo` (the error message <code>sorry, you must have a tty to run sudo</code> will appear in the output otherwise). 
	<br/><strong>N.B.:</strong> Some SSH servers will crash when they are requested to allocate a pty, most notably OpenSSH on AIX. To verify the behaviour of your SSH server, you can manually execute the <code>ssh</code> command with the <code>-T</code> (disable pty allocation) or <code>-t</code> (force pty allocation) flags.</td>
</tr>
<tr>
	<th align="left" valign="top"><a name="ssh_interactiveKeyboardAuthRegex"/>interactiveKeyboardAuthRegex</th>
	<td>The regular expression to look for in keyboard-interactive prompts before sending the password. The default value is <code>.*Password:[ ]?</code>. When the SSH server is configured to not allow <a href="http://www.ietf.org/rfc/rfc4252.txt">password authentication</a> but is configured to allow <a href="http://www.ietf.org/rfc/rfc4256.txt">keyboard-interactive authentication</a> using passwords, Overthere will compare the interactive-keyboard prompt against this regular expression and send the `password` when they match.</td>
</tr>
<tr>
	<th align="left" valign="top"><a name="ssh_sudoCommandPrefix"/>sudoCommandPrefix</th>
	<td>The command to prefix to the command to be executed to execute it as <strong>sudoUsername</strong>. The string <code>{0}</code> is replaced with the vaulue of <strong>sudoUsername</strong>. The default value is <code>sudo -u {0}</code>. This connection option is only applicable for the <strong>SUDO</strong> and <strong>INTERACTIVE_SUDO</strong> connection types.</td>
</tr>
<tr>
	<th align="left" valign="top"><a name="ssh_sudoOverrideUmask"/>sudoOverrideUmask</th>
	<td>If set to <code>true</code>, Overthere will explicitly change the permissions with <code>chmod -R go+rX</code> after uploading a file or directory with scp. This connection options is only applicable for the <strong>SUDO</strong> and <strong>INTERACTIVE_SUDO</strong> connection types.</td>
</tr>
<tr>
	<th align="left" valign="top"><a name="ssh_sudoPasswordPromptRegex"/>sudoPasswordPromptRegex</th>
	<td>The regular expression to be used when looking for sudo password prompts. When the connection type is set to <strong>INTERACTIVE_SUDO</strong>, Overthere will look for strings that match this regular expression in the first line of the output of a command, and send the password if a match occurs. The default value is <code>.*[Pp]assword.*:</code> This connection option is only applicable for the <strong>INTERACTIVE_SUDO</strong> connection type.</td>
</tr>
<tr>
	<th align="left" valign="top"><a name="ssh_sudoQuoteCommand"/>sudoQuoteCommand</th>
	<td>If set to <code>true</code>, the original command is added as one argument to the prefix configured with the <code>sudoCommandPrefix</code> connection option. This has the result of quoting the original command, which is needed for commands like <code>su</code>. Compare <code>sudo -u privilegeduser start server1</code> to <code>su privilegeduser 'start server1'</code>. The default value is <code>false</code>. This connection options is only applicable for the <strong>SUDO</strong> and <strong>INTERACTIVE_SUDO</strong> connection types.</td>
</tr>
</table>

<a name="ssh_host_setup"/>
### Host setup

<a name="ssh_host_setup_ssh"/>
#### SSH
To connect to a remote host using the SSH protocol, you will need to install an SSH server on that remote host. For Unix platforms, we recommend [OpenSSH](http://www.openssh.com/). It is included in all Linux distributions and most other Unix flavours. For Windows platforms two SSH servers are supported:

* OpenSSH on [Cygwin](http://www.cygwin.com/). We recommend [COPSSH](http://www.itefix.no/i2/copssh) as a convenient packaging of OpenSSH and Cygwin. It is a free source download but since 22/11/2011 the binary installers are a paid solution.
* [WinSSHD](http://www.bitvise.com/winsshd) is a commercial SSH server that has a lot of configuration options.

__N.B.:__ The __SFTP__, __SCP__, __SUDO__ and __INTERACTIVE_SUDO__ connection types are only available for Unix hosts. To use SSH with Windows hosts, choose either the __SFTP_CYGWIN__ or the __SFTP_WINSSHD__ connection type.

<a name="ssh_host_setup_sftp"/>
#### SFTP

To use the __SFTP__ connection type, make sure SFTP is enabled in the SSH server. This is enabled by default in most SSH servers.

<a name="ssh_host_setup_sftp_cygwin"/>
#### SFTP_CYGWIN

To use the __SFTP_CYGWIN__ connection type, install [COPSSH](http://www.itefix.no/i2/copssh) on your Windows host. In the COPSSH control panel, add the users as which you want to connect and select _Linux shell and Sftp_ in the _shell_ dropdown box. Check _Password authentication_ and/or _Public key authentication_ depending on the authentication method you want to use.<br/>__N.B.:__ Overthere will take care of the translation from Windows style paths, e.g. `C:\Program Files\IBM\WebSphere\AppServer`, to Cygwin-style paths, e.g. `/cygdrive/C/Program Files/IBM/WebSphere/AppServer`, so that your code can use Windows style paths.

<a name="ssh_host_setup_sftp_winsshd"/>
#### SFTP_WINSSHD

To use the __SFTP_WINSSHD__ connection type, install [WinSSHD](http://www.bitvise.com/winsshd) on your Windows host. In the Easy WinSSHD Settings control panel, add the users as which you want to connect, check the _Login allowed_ checkbox and select _Allow full access_ in the _Virtual filesystem layout_ dropdown box. Alternatively you can check the _Allow login to any Windows account_ to allow access to all Windows accounts.<br/>__N.B.:__ Overthere will take care of the translation from Windows style paths, e.g. `C:\Program Files\IBM\WebSphere\AppServer`, to WinSSHD-style paths, e.g. `/C/Program Files/IBM/WebSphere/AppServer`, so that your code can use Windows style paths.
 
<a name="ssh_host_setup_sudo"/>
<a name="ssh_host_setup_interactive_sudo"/>
#### SUDO and INTERACTIVE_SUDO

To use the __SUDO__ connection type, the `/etc/sudoers` configuration will have to be set up in such a way that the user configured with the connection option __username__ can execute the commands below as the user configured with the connection option __sudoUsername__. The arguments passed to these commands depend on the exact usage of the Overthere connection. Check the `INFO` messages on the `com.xebialabs.overthere.ssh.SshConnection` category to see what commands get executed.

* `ls`
* `cp`
* `mv`
* `mkdir`
* `rmdir`
* `rm`
* `chmod`
* Any other command that you want to execute.
    
The commands mentioned above must be configured with the __NOPASSWD__ setting in the `/etc/sudoers` file. Otherwise you will have to use the __INTERACTIVE_SUDO__ connection type. When the __INTERACTIVE_SUDO__ connection type is used, the first line of the output will be matched against the regular expression configured with the __sudoPasswordPromptRegex__ connection option. If a match is found, the value of the __password__ connection option is sent. <br/>If the __sudoPasswordPromptRegex__ was set incorrectly, the most common symptom is for the command to appear to hang. If you have trouble determining the proper value for the __sudoPasswordPromptRegex__ connection option, set the log level for the `com.xebialabs.overthere.ssh.SshInteractiveSudoPasswordHandlingStream` category to `TRACE` and examine the output.

<a name="cifs"/>
## CIFS

The CIFS protocol implementation of Overthere uses the [CIFS protocol](http://en.wikipedia.org/wiki/Server_Message_Block), also known as SMB, for file manipulation and, depending on the settings, uses either [Telnet](http://en.wikipedia.org/wiki/Telnet) or [WinRM](http://en.wikipedia.org/wiki/WS-Management) for process execution. You will most likely not need to install new software although you might need to enable and configure some services:

* The built-in file sharing capabilities of Windows are based on CIFS and are therefore available and enabled by default.
* A Telnet Server is available on all Windows Server versions although it might not be enabled.
* WinRM is available on Windows Server 2008 and up. 

See the [section on the host setup](#cifs_host_setup) for more information on how to setup the remote hosts.

<a name="cifs_connection_options"/>
### Connection options

The CIFS protocol implementation of Overthere defines a number of additional connection properties, in addition to the [common connection options](#common_connection_options).

<table>
<tr>
	<th align="left" valign="top"><a name="cifs_connectionType"/>connectionType</th>
	<td>Specifies what protocol is used to execute commands on the remote hsots. One of the following values must be set: <ul>
		<li><strong><a href="#cifs_host_setup_telnet">TELNET</a></strong> - uses Telnet to execute remote commands. The <strong>port</strong> connection property specifies the Telnet port to connect to. The default value is <code>23</code>.</li>
		<li><strong><a href="#cifs_host_setup_winrm_http">WINRM_HTTP</a></strong> - uses WinRM over HTTP to execute remote commands. The <strong>port</strong> connection property specifies the Telnet port to connect to. The default value is <code>5985</code>.</li>
		<li><strong><a href="#cifs_host_setup_winrm_https">WINRM_HTTPS</a></strong> - uses WinRM over HTTPS to execute remote commands. The <strong>port</strong> connection property specifies the Telnet port to connect to. The default value is <code>5986</code>.</li>
	</ul></td>
</tr>
<tr>
	<th align="left" valign="top"><a name="cifs_cifsPort"/>cifsPort</th>
	<td>The CIFS port to connect to. The default value is <code>445</code>.</td>
</tr>
<tr>
	<th align="left" valign="top"><a name="cifs_pathShareMappings"/>pathShareMappings</a></th>
	<td>The path to share mappings to use for CIFS specified as a <code>Map&lt;String, String&gt;</code>, e.g. <code>C:\IBM\WebSphere</code> -> <code>WebSphere</code>. If a path is not explicitly mapped to a share the administrative share will be used. The default value is to use no path/share mappings, i.e. to use only administrative shares.</td>
</tr>
<tr>
	<th align="left" valign="top"><a name="cifs_winrmContext"/>winrmContext</th>
	<td>The context used by the WinRM server. The default value is <code>/wsman</code>. This connection options is only applicable for the <strong>WINRM_HTTP</strong> and <strong>WINRM_HTTPS</strong> connection types.</td>
</tr>
<tr>
	<th align="left" valign="top"><a name="cifs_winrmEnvelopSize"/>winrmEnvelopSize</th>
	<td>The WinRM envelop size in bytes to use. The default value is <code>153600</code>. This connection options is only applicable for the <strong>WINRM_HTTP</strong> and <strong>WINRM_HTTPS</strong> connection types.</td>
</tr>
<tr>
	<th align="left" valign="top"><a name="cifs_winrmLocale"/>winrmLocale</th>
	<td>The WinRM locale to use. The default value is <code>en-US</code>. This connection options is only applicable for the <strong>WINRM_HTTP</strong> and <strong>WINRM_HTTPS</strong> connection types.</td>
</tr>
<tr>
	<th align="left" valign="top"><a name="cifs_winrmTimeout"/>winrmTimeout</th>
	<td>The WinRM timeout to use in <a href="http://www.w3.org/TR/xmlschema-2/#isoformats">XML schema duration format</a>. The default value is <code>PT60.000S</code>. This connection options is only applicable for the <strong>WINRM_HTTP</strong> and <strong>WINRM_HTTPS</strong> connection types.</td>
</tr>
</table>

<a name="cifs_host_setup"/>
### Host setup

<a name="cifs_host_setup_cifs"/>
#### CIFS
To connect to a remote host using the __CIFS__ protocol, make sure the host is reachable on port 445 and add the __username__ you are using to connect to the __Administrators__ group so that that user can access the [__administrative shares__](http://en.wikipedia.org/wiki/Administrative_share).<br/>__N.B.:__ Overthere will take care of the translation from Windows paths, e.g. `C:\Program Files\IBM\WebSphere\AppServer`, to SMB URLs that use the administrative shares, e.g. <code>smb://<strong>username</strong>:<strong>password</strong>@<strong>hostname</strong>/C$/Program%20Files/IBM/WebSphere/AppServer</code> (which corresponds to the UNC path <code>\\<strong>hostname</strong>\C$\Program Files\IBM\WebSphere\AppServer</code>), so that your code can use Windows style paths. 

<a name="cifs_host_setup_telnet"/>
#### TELNET

To use the __TELNET__ connection type, enable the Telnet Server Service according to <a href="http://technet.microsoft.com/en-us/library/cc732046(WS.10).aspx">these instructions on the Microsoft Technet site</a>. If the remote host is running Windows Server 2003 SP1 or an x64-based version of Windows Server 2003, you will have to install the according to [these instructions from the Microsoft Support site](http://support.microsoft.com/kb/899260). After you have started the Telnet Server, open a command prompt as the __Administrator__ user and enter the command `tlntadmn config mode=stream` to enable stream mode.

When the Telnet server is enabled any user that is in the <strong>Administrators</strong> group or that is in the <strong>TelnetClients</strong> group and that has the "Allow logon locally" privilege can log in using Telnet. See the Microsoft Technet to learn <a href="http://technet.microsoft.com/en-us/library/ee957044(WS.10).aspx">how to grant a user or group the right to logon locally</a> on Windows Server 2008 R2.

<a name="cifs_host_setup_winrm_http"/>
<a name="cifs_host_setup_winrm_https"/>
#### WINRP_HTTP and WINRM_HTTPS

To use the __WINRM_HTTP__ or the __WINRM_HTTPS__ connection type, you'll need to setup WinRM on the remote host by following these instructions:

1. If the remote host is running Windows Server 2003 R2, you will need to enable WinRM. As the Administrator user, go to the __Add/Remove System Components__ feature in the __Control Panel__ and add WinRm under the section __Management and Monitoring Tools__.

2. On the remote host, as the Administrator user, open a Command Prompt and follow the steps below.

3. Configure WinRM to allow basic authentication:

		winrm set winrm/config/service/Auth @{Basic="true"}

4. Configure WinRM to allow unencrypted SOAP messages:

		winrm set winrm/config/service @{AllowUnencrypted="true"}

5. Configure WinRM to provide enough memory to the commands that you are going to run, e.g. 1024 MB:

		winrm set winrm/config/winrs @{MaxMemoryPerShellMB="1024"}

6. To use the __WINRM_HTTP__ connection type, create an HTTP WinRM listener:

		winrm create winrm/config/listener?Address=*+Transport=HTTP

7. To use the __WINRM_HTTPS__ connection type, follow the steps below:

	1. (optional) Create a self signed certificate for the remote host by installing `selfssl.exe` from [the IIS 6 resource kit](http://www.microsoft.com/download/en/details.aspx?displaylang=en&id=17275) and running the command below or by following the instructions [in this blog by Hans Olav](http://www.hansolav.net/blog/SelfsignedSSLCertificatesOnIIS7AndCommonNames.aspx):

        	C:\Program Files\IIS Resources\SelfSSL>selfssl.exe /T /N:cn=HOSTNAME /V:3650
        	Microsoft (R) SelfSSL Version 1.0
        	Copyright (C) 2003 Microsoft Corporation. All rights reserved.

        	Do you want to replace the SSL settings for site 1 (Y/N)?Y
        	The self signed certificate was successfully assigned to site 1.

	2. Open a PowerShell window and enter the command below to find the thumbprint for the certificate for the remote host:

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

	3. Create an HTTPS WinRM listener for the remote host using the certificate you've just found:

			winrm create winrm/config/Listener?Address=*+Transport=HTTPS @{Hostname="HOSTNAME"; CertificateThumbprint="THUMBPRINT"}


For more information on WinRM, please refer to <a href="http://msdn.microsoft.com/en-us/library/windows/desktop/aa384426(v=vs.85).aspx">the online documentation at Microsoft's DevCenter</a>. As a quick reference, have a look at the list of useful commands below:

* Do a quickconfig for WinRM: `winrm qc`
* Do a quickconfig for WinRM with HTTPS: `winrm qc -transport:https`
* Dump the complete WinRM configuration: `winrm get winrm/config`
* View the listeners that have been configured: `winrm enumerate winrm/config/listener`
* Allow all hosts to connect to the WinRM listener: `winrm set winrm/config/client @{TrustedHosts="*"}`
* Allow a fixed set of hosts to connect to the WinRM listener: `winrm set winrm/config/client @{TrustedHosts="host1,host2..."}`

<a name="release_history"/>
# Release History

* Overthere 1.0.13 (18-Jan-2012)
    * Masked passwords in logging.
    * Made ItestHostFactory also look for itest.properties in ~/.itest (in addition to the classpath and the current working directory).
* Overthere 1.0.12 (12-Jan-2012)
    * Allowed forward slashes (/) to be used in Windows paths.
    * Made it possible to access non-administrative shares on Windows so that the CIFS connection methods can be used with regular user accounts. See the <a href="#cifs_pathShareMappings"><strong>pathShareMappings</strong></a> connection option.
    * Added the <a href="#ssh_allocatePty"><strong>allocatePty</strong></a> connection option to specify an explicit pseudo terminal to use.
* Overthere 1.0.11 (09-Dec-2011)
    * Fixes to the SSH/WinSSHD implementation and a few other little fixes.
    * Added a lot of documentation.
    * Added examples project.
    * Changed license to ASLv2.
* Overthere 1.0.10 (23-Nov-2011)
    * Added support for SSH/WinSSHD on Windows.
* Overthere 1.0.9 (22-Nov-2011)
    * Initial public release with support for SSH on Unix as well as CIFS/TELNET, CIFS/WinRM and SSH/CYGWIN on Windows.


