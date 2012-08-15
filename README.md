# Table of Contents

* [Introduction](#introduction)
* [Getting Overthere](#getting_overthere)
	* [Depending on Overthere](#depending_on_overthere)
	* [Building Overthere](#building_overthere)
	* [Running the Examples](#running_the_examples)
* [Programming Overthere](#programming_overthere) 
* [Configuring Overthere](#configuring_overthere)
	* [Protocols](#protocols)
	* [Connection options](#common_connection_options)
	* [Local](#local)
	* [SSH](#ssh)
	* [CIFS](#cifs) (includes Telnet and WinRM)
	* [Tunnelling](#tunnelling)
* [Troubleshooting Overthere](#troubleshooting)
* [Release History](#release_history)


<a name="introduction"></a>
# Introduction

Overthere is a Java library to manipulate files and execute processes on remote hosts, i.e. do stuff "over there". It was originally developed for and is still used in the [XebiaLabs](http://xebialabs.com/) deployment automation product Deployit as a way to perform tasks on remote hosts, e.g. copy configuration files, install EAR files or restart web servers. Another way of looking at it is to say that Overthere gives you `java.io.File` and `java.lang.Process` as they should've been: as interfaces, created by a factory and extensible through an SPI mechanism.

Overthere is available under the [GPLv2 with XebiaLabs FLOSS License Exception](https://raw.github.com/xebialabs/overthere/master/LICENSE).

__P.S.:__ Check the [Overthere Ohloh page](http://www.ohloh.net/p/overthere) for some interesting code analysis statistics. If you use Overthere, don't forget to tell Ohloh! And while you're at it, you might want to vote for Overthere on the [Overthere Freecode page](http://freecode.com/projects/overthere) too! ;-)

<a name="getting_overthere"></a>
# Getting Overthere

To get Overthere, you have two options:

1. Add a dependency to Overthere to your project.
2. Build Overthere yourself.

And, if you want, you can also run the Overthere examples used in the Overthere presentation mentioned above.

Binary releases of Overthere are not provided here, but you can download it [straight from the Maven Central repository](http://search.maven.org/#artifactdetails%7Ccom.xebialabs.overthere%7Coverthere%7C2.0.0-beta-7%7Cjar) if you want to.

<a name="depending_on_overthere"></a>
## Depending on Overthere

1. If your project is built with Maven, add the following dependency to the pom.xml:

		<dependency>
			<groupId>com.xebialabs.overthere</groupId>
			<artifactId>overthere</artifactId>
			<version>2.0.0-beta-7</version>
		</dependency>

2. If your project is built using another build tool that uses the Maven Central repository, translate these dependencies into the format used by your build tool.

<a name="building_overthere"></a>
## Building Overthere

1. Install [Gradle 1.0](http://www.gradle.org/).
2. Clone the Overthere repository.
3. Run the command `gradle clean build`.

<a name="running_the_examples"></a>
## Running the examples

1. Install [Maven 2.2.1 or up](http://maven.apache.org/).
2. Clone the Overthere repository.
3. Go into the `examples` directory and run the command `mvn eclipse:eclipse`.
4. Import the `examples` project into Eclipse.
5. Change the login details in the example classes (address, username and password) and run them!

<a name="programming_overthere"></a>
# Programming Overthere

To program Overthere, browse the source code, check the examples and browse the Overthere <a href="http://docs.xebialabs.com/overthere/javadoc/">Javadoc</a>.

For a more thorough introduction to Overthere, check the [presentation on Overthere](http://www.slideshare.net/vpartington/presentation-about-overthere-for-jfall-2011) that I gave for J-Fall 2011, a Java conference in the Netherlands (in English).

<a name="configuring_overthere"></a>
# Configuring Overthere

The protocols that Overthere uses to connect to remote hosts, such as SSH, CIFS, Telnet and WinRM, are existing protocols for which support is built into many platforms. As such you will not need to install any custom software on the target hosts. Nevertheless in some cases the target platforms have to be configured to correctly work with Overthere. Also, Overthere has a number of configuration features that allow you tweak the way it interfaces with the remote hosts.

<a name="protocols"></a>
## Protocols

Overthere supports a number of protocols to connect to remote hosts:

* [__local__](#local) - a connection to the local host. This is a wrapper around <a href="http://download.oracle.com/javase/6/docs/api/java/io/File.html"></code>java.io.File</code></a> and <a href="http://docs.oracle.com/javase/6/docs/api/java/lang/Process.html"></code>java.lang.Process</code></a>.
* [__ssh__](#ssh) - a connection using the [SSH protocol](http://en.wikipedia.org/wiki/Secure_Shell), to a Unix host or to a Windows host running either OpenSSH on Cygwin (i.e. COPSSH) or WinSSHD.
* [__cifs__](#cifs) - a connection using the [CIFS protocol](http://en.wikipedia.org/wiki/Server_Message_Block), also known as SMB, for file manipulation and, depending on the settings, using either [WinRM](http://en.wikipedia.org/wiki/WS-Management) or [Telnet](http://en.wikipedia.org/wiki/Telnet) for process execution. This protocol is only supported for Windows hosts.

<a name="common_connection_options"></a>
## Connection options

Apart from selecting a protocol to use, you will also need to supply a number of connection options when creating a connection. Common connection options are:

<table>
<tr>
	<th align="left" valign="top"><a name="os"></a>os</th>
	<td>The operating system of the remote host, either <code>UNIX</code> or <code>WINDOWS</code>. This property is required for all protocols, except for the <strong>local</strong> protocol.</td>
</tr>
<tr>
	<th align="left" valign="top"><a name="address"></a>address</th>
	<td>The address of the remote host, either an IP address or a DNS name. This property is required for all protocols, except for the <strong>local</strong> protocol.</td>
</tr>
<tr>
	<th align="left" valign="top"><a name="port"></a>port</th>
	<td>The port to use when connecting to the remote host. The interpretation and the default value for this connection option depend on the protocol that is used.</td>
</tr>
<tr>
	<th align="left" valign="top"><a name="username"></a>username</th>
	<td>The username to use when connecting to the remote host. This property is required for all protocols, except for the <strong>local</strong> protocol.</td>
</tr>
<tr>
	<th align="left" valign="top"><a name="password"></a>password</th>
	<td>The password to use. This property is required for all protocols, except for the <strong>local</strong> protocol.</td>
</tr>
<tr>
	<th align="left" valign="top"><a name="tmp"></a>tmp</th>
	<td>The temporary directory. For each connection, a <em>connection temporary directory</em> with a name like <code>overthere-20111128T132600-7234435.tmp</code> is created within this temporary directory, e.g. <code>/tmp/overthere-20111128T132600-7234435.tmp</code>, to store temporary files for the duration of the connection.<br/>The default value is <code>tmp</code> for UNIX hosts and <code>C:\windows\temp</code> for Windows hosts, except for the <strong>local</strong> protocol where the default is the value of the <code>java.io.tmpdir</code> system property.</td>
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
	<td>The number of milliseconds Overthere waits for a connection to a remote host to be established. The default value is <code>120000</code>, i.e. 2 minutes.</td>
</tr>
<tr>
	<th align="left" valign="top"><a name="jumpstation"></a>jumpstation</th>
	<td>If set to a non-null value, this property contains the connection options used to connect to an SSH jumpstation (See <a href="#tunnelling">Tunnelling</a>). Recursive configuration is possible, i.e. this property is also available for the connection options of a jumpstation.</td>
</tr>
</table>
Apart from these common connection options, some protocols define additional protocol-specific connection options. These are documented below, with the corresponding protocol.

<a name="local"></a>
## LOCAL

The local protocol implementation uses the local file manipulation and local process execution capabilities built-in to Java. The [__os__](#os) connection option is hardcoded to the operating system of the local host and the [__tmp__](#tmp) connection option defaults to the system temporary directory as specified by the `java.io.tmpdir` <a href="http://docs.oracle.com/javase/6/docs/api/java/lang/System.html#getProperties()">system property</a>. There are no protocol-specific connection options.

<a name="ssh"></a>
## SSH

The SSH protocol implementation of Overthere uses the [SSH](http://en.wikipedia.org/wiki/Secure_Shell) protocol to connect to remote hosts to manipulate files and execute commands. Most Unix systems already have an SSH server installed and configured and a number of different SSH implementations are available for Windows although not all of them are supported by Overthere.

See the [section on the host setup](#ssh_host_setup) for more information on how to setup the remote hosts.

<a name="ssh_connection_options"></a>
### Connection options

The SSH protocol implementation of Overthere defines a number of additional connection options, in addition to the [common connection options](#common_connection_options).

<table>
<tr>
	<th align="left" valign="top"><a name="ssh_connectionType"></a>connectionType</th>
	<td>Specifies how the SSH protocol is used. One of the following values must be set:<ul>
		<li><strong><a href="#ssh_host_setup_sftp">SFTP</a></strong> - uses SFTP to transfer files, to a Unix host. Unless <code>sudo</code> or a similar command is needed to execute commands, this is the best and fastest option to choose for Unix hosts.</li>
		<li><strong><a href="#ssh_host_setup_sftp_cygwin">SFTP_CYGWIN</a></strong> -  uses SFTP to transfer files, to a Windows host running OpenSSH on Cygwin.</li>
		<li><strong><a href="#ssh_host_setup_sftp_winsshd">SFTP_WINSSHD</a></strong> - uses SFTP to transfer files, to a Windows host running WinSSHD.</li>
		<li><strong>SCP</strong> - uses SCP to transfer files, to a Unix host. Not needed unless your SSH server has disabled the SFTP subsystem.</li>
		<li><strong><a href="#ssh_host_setup_sudo">SUDO</a></strong> - uses SCP to transfer files, to a Unix host. Uses the <a href="http://en.wikipedia.org/wiki/Sudo"><code>sudo</code></a> command, configured with <strong>NOPASSWD</strong> for all commands, to execute commands. Select this connection type if the <strong>username</strong> you are connecting with does not have the right permissions to manipulate the files that need to be manipulated and/or to execute the commands that need to be executed. <br/>If this connection type is selected, the <strong>sudoUsername</strong> connection option is required and specifies that user that <em>does</em> have the necessary permissions. See below for a more detailed description.</li>
	<li><strong><a href="#ssh_host_setup_interactive_sudo">INTERACTIVE_SUDO</a></strong> - uses SCP to transfer files, to a Unix host. Uses the <code>sudo</code> command, <em>not</em> been configured with <strong>NOPASSWD</strong> for all commands, to execute commands. This is similar to the <strong>SUDO</strong> connection type but also detects the password prompt that is shown by the <code>sudo</code> command when the login user (<strong>username</strong>) tries to execute a commands as the privileged user (<strong>sudoUsername</strong>) when that command has not been configured in <code>/etc/sudoers</code> with <strong>NOPASSWD</strong>. <br/><strong>N.B.:</strong> Because the password of the login user is needed to answer this prompt, this connection type is incompatible with the <strong>privateKeyFile</strong> option that can be used to authenticate with a private key file.</li>
	</ul></td>
</tr>
<tr>
	<th align="left" valign="top"><a name="ssh_sudoUsername"></a>sudoUsername</th>
	<td>The username of the user that can manipulate the files that need to be manipulated and that can execute the commands that need to be executed.
	<br/>
	<strong>N.B.:</strong> This connection option is only applicable for the <strong>SUDO</strong> and <strong>INTERACTIVE_SUDO</strong> connection types.</td>
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
	<td>If set, the SSH server is requested to allocate a pty (<a href="http://en.wikipedia.org/wiki/Pseudo_terminal">pseudo terminal</a>) for the process with the setting specified by this option. The format is <code>TERM:COLS:ROWS:WIDTH:HEIGHT</code>, e.g. <code>vt220:80:24:0:0</code>. If set, this option overrides the <a href="#ssh_allocateDefaultPty"><strong>allocateDefaultPty</strong></a> option. The default value is unset.
	</td>
</tr>
<tr>
	<th align="left" valign="top"><a name="ssh_interactiveKeyboardAuthRegex"></a>interactiveKeyboardAuthRegex</th>
	<td>The regular expression to look for in keyboard-interactive prompts before sending the password. The default value is <code>.*Password:[ ]?</code>. When the SSH server is configured to not allow <a href="http://www.ietf.org/rfc/rfc4252.txt">password authentication</a> but is configured to allow <a href="http://www.ietf.org/rfc/rfc4256.txt">keyboard-interactive authentication</a> using passwords, Overthere will compare the interactive-keyboard prompt against this regular expression and send the value of the <strong>password</strong> option when they match. The default value is <code>.*Password:[ ]?</code></td>
</tr>
<tr>
	<th align="left" valign="top"><a name="ssh_sudoCommandPrefix"></a>sudoCommandPrefix</th>
	<td>The command to prefix to the command to be executed to execute it as <strong>sudoUsername</strong>. The string <code>{0}</code> is replaced with the vaulue of <strong>sudoUsername</strong>. The default value is <code>sudo -u {0}</code>.
	<br/>
	<strong>N.B.:</strong> This connection option is only applicable for the <strong>SUDO</strong> and <strong>INTERACTIVE_SUDO</strong> connection types.</td>
</tr>
<tr>
	<th align="left" valign="top"><a name="ssh_sudoOverrideUmask"></a>sudoOverrideUmask</th>
	<td>If set to <code>true</code>, Overthere will explicitly change the permissions with <code>chmod -R go+rX</code> after uploading a file or directory with scp. The default value is <code>false</code>.
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
	<th align="left" valign="top"><a name="ssh_sudoQuoteCommand"></a>sudoQuoteCommand</th>
	<td>If set to <code>true</code>, the original command is added as one argument to the prefix configured with the <code>sudoCommandPrefix</code> connection option. This has the result of quoting the original command, which is needed for commands like <code>su</code>. Compare <code>sudo -u privilegeduser start server1</code> to <code>su privilegeduser 'start server1'</code>. The default value is <code>false</code>.
	<br/>
	<strong>N.B.:</strong> This connection option is only applicable for the <strong>SUDO</strong> and <strong>INTERACTIVE_SUDO</strong> connection types.</td>
</tr>
</table>

<a name="ssh_host_setup"></a>
### Host setup

<a name="ssh_host_setup_ssh"></a>
#### SSH
To connect to a remote host using the SSH protocol, you will need to install an SSH server on that remote host. For Unix platforms, we recommend [OpenSSH](http://www.openssh.com/). It is included in all Linux distributions and most other Unix flavours. For Windows platforms two SSH servers are supported:

* OpenSSH on [Cygwin](http://www.cygwin.com/). We recommend [COPSSH](http://www.itefix.no/i2/copssh) as a convenient packaging of OpenSSH and Cygwin. It is a free source download but since 22/11/2011 the binary installers are a paid solution.
* [WinSSHD](http://www.bitvise.com/winsshd) is a commercial SSH server that has a lot of configuration options.

__N.B.:__ The __SFTP__, __SCP__, __SUDO__ and __INTERACTIVE_SUDO__ connection types are only available for Unix hosts. To use SSH with Windows hosts, choose either the __SFTP_CYGWIN__ or the __SFTP_WINSSHD__ connection type.

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

* `ls`
* `cp`
* `mv`
* `mkdir`
* `rmdir`
* `rm`
* `chmod`
* Any other command that you want to execute.
    
The commands mentioned above must be configured with the __NOPASSWD__ setting in the `/etc/sudoers` file. Otherwise you will have to use the __INTERACTIVE_SUDO__ connection type. When the __INTERACTIVE_SUDO__ connection type is used, every line of the output will be matched against the regular expression configured with the __sudoPasswordPromptRegex__ connection option. If a match is found, the value of the __password__ connection option is sent.

<a name="cifs"></a>
## CIFS

The CIFS protocol implementation of Overthere uses the [CIFS protocol](http://en.wikipedia.org/wiki/Server_Message_Block), also known as SMB, for file manipulation and, depending on the settings, uses either [WinRM](http://en.wikipedia.org/wiki/WS-Management) or [Telnet](http://en.wikipedia.org/wiki/Telnet) for process execution. You will most likely not need to install new software although you might need to enable and configure some services:

* The built-in file sharing capabilities of Windows are based on CIFS and are therefore available and enabled by default.
* WinRM is available on Windows Server 2008 and up. Overthere supports basic authentication for local accounts and Kerberos authentication for domain accounts.
* A Telnet Server is available on all Windows Server versions although it might not be enabled.

See the [section on the host setup](#cifs_host_setup) for more information on how to setup the remote hosts.

### Domain accounts
Windows domain accounts are support for CIFS and WinRM connections. Domain accounts must be specified as `USER@FULL.DOMAIN` and not as <strike>`DOMAIN\USER`</strike>. Local accounts must be specified without an at-sign (`@`) or a backslash (`\`).

__N.B.:__ When using WinRM, Kerberos authentication is used for domain accounts. Please read the section on how to set up Kerberos [for the source machine](#cifs_host_setup_krb5) and [the target machines](#cifs_host_setup_spn).

### Administrative shares
By default Overthere will access the [administrative shares](http://en.wikipedia.org/wiki/Administrative_share) on the target CIFS machine. These shares are only accessible for users that are part of the __Administrators__ on the target machine. If you want to access the target machine using a regular account, use the [__pathShareMapping__](#cifs_pathShareMappings) connection option to configure the shares to use for the paths Overthere will be connecting to. Of course, the user configured with the __username__ connection options should have access to those shares and the underlying directories and files.

__N.B.:__ Overthere will take care of the translation from Windows paths, e.g. `C:\Program Files\IBM\WebSphere\AppServer`, to SMB URLs that use the administrative shares, e.g. `smb://username:password@hostname/C$/Program%20Files/IBM/WebSphere/AppServer` (which corresponds to the UNC path `\\hostname\C$\Program Files\IBM\WebSphere\AppServer`), so that your code can use Windows style paths.



<a name="cifs_connection_options"></a>
### Connection options

The CIFS protocol implementation of Overthere defines a number of additional connection options, in addition to the [common connection options](#common_connection_options).

<table>
<tr>
	<th align="left" valign="top"><a name="cifs_connectionType"></a>connectionType</th>
	<td>Specifies what protocol is used to execute commands on the remote hsots. One of the following values must be set:<ul>
		<li><strong><a href="#cifs_host_setup_winrm">WINRM</a></strong> - uses WinRM over HTTP(S) to execute remote commands. The <strong>port</strong> connection option specifies the Telnet port to connect to. The default value is <code>5985</code> for HTTP and <code>5986</code> for HTTPS.</li>
		<li><strong><a href="#cifs_host_setup_telnet">TELNET</a></strong> - uses Telnet to execute remote commands. The <strong>port</strong> connection option specifies the Telnet port to connect to. The default value is <code>23</code>.</li>
	</ul></td>
</tr>
<tr>
	<th align="left" valign="top"><a name="cifs_cifsPort"></a>cifsPort</th>
	<td>The CIFS port to connect to. The default value is <code>445</code>.</td>
</tr>
<tr>
	<th align="left" valign="top"><a name="cifs_pathShareMappings"></a>pathShareMappings</a></th>
	<td>The path to share mappings to use for CIFS specified as a <code>Map&lt;String, String&gt;</code>, e.g. <code>C:\IBM\WebSphere</code> -> <code>WebSphere</code>. If a path is not explicitly mapped to a share, an administrative share will be used. The default value is to use no path/share mappings, i.e. to use only administrative shares.</td>
</tr>
<tr>
	<th align="left" valign="top"><a name="cifs_winrmEnableHttps"></a>winrmEnableHttps</th>
	<td>If set to <code>true</code>, HTTPS is used to connect to the WinRM server. Otherwise HTTP is used. The default value is <code>false</code>.
	<br/>
	<strong>N.B.:</strong> This connection option is only applicable for the <strong>WINRM</strong> connection type.</td>
<tr>
	<th align="left" valign="top"><a name="cifs_winrmContext"></a>winrmContext</th>
	<td>The context used by the WinRM server. The default value is <code>/wsman</code>.
	<br/>
	<strong>N.B.:</strong> This connection option is only applicable for the <strong>WINRM</strong> connection type.</td>
</tr>
<tr>
	<th align="left" valign="top"><a name="cifs_winrmDebugKerberosAuth"></a>winrmDebugKerberosAuth</th>
	<td>If set to <code>true</code>, enables debug output for the <a href="http://en.wikipedia.org/wiki/Java_Authentication_and_Authorization_Service">JAAS</a>-based Kerberos authentication within the OverThere connector. The default value is <code>false</code>.
	<br/>
	<strong>N.B.:</strong> This connection option is only applicable for the <strong>WINRM</strong> connection type, when a Windows domain acount is used.</td>
</tr>
<tr>
	<th align="left" valign="top"><a name="cifs_winrmEnvelopSize"></a>winrmEnvelopSize</th>
	<td>The WinRM envelop size in bytes to use. The default value is <code>153600</code>.
	<br/>
	<strong>N.B.:</strong> This connection option is only applicable for the <strong>WINRM</strong> connection type.</td>
</tr>
<tr>
	<th align="left" valign="top"><a name="cifs_winrmHttpsCertificateTrustStrategy"></a>winrmHttpsCertificateTrustStrategy</th>
	<td>The certificate trust strategy for WinRM HTTPS connections. One of the following values can be set:<ul>
		<li><strong>STRICT</strong> (default) - use Java's trusted certificate chains.</li>
		<li><strong>SELF_SIGNED</strong> - self-signed certificates are allowed (see <a href="http://hc.apache.org/httpcomponents-client-ga/httpclient/apidocs/org/apache/http/conn/ssl/TrustSelfSignedStrategy.html">TrustSelfSignedStrategy</a>)</li>
		<li><strong>ALLOW_ALL</strong> - trust all certificates.</li>
	</ul>
	<strong>N.B.:</strong> This connection option is only applicable for the <strong>WINRM</strong> connection type, when <a href="#cifs_winrmEnableHttps"><strong>winrmEnableHttps</strong></a> is set to <code>true</code>.</td>
</tr>
<tr>
	<th align="left" valign="top"><a name="cifs_winrmHttpsHostnameVerificationStrategy"></a>winrmHttpsHostnameVerificationStrategy</th>
	<td>The hostname verification strategy for WinRM HTTPS connections. One of the following values can be set:<ul>
		<li><strong>STRICT</strong> - strict verification (see <a href="http://hc.apache.org/httpcomponents-client-ga/httpclient/apidocs/org/apache/http/conn/ssl/StrictHostnameVerifier.html">StrictHostnameVerifier</a>)</li>
		<li><strong>BROWSER_COMPATIBLE</strong> (default) - wilcards in certifactes are matched (see <a href="http://hc.apache.org/httpcomponents-client-ga/httpclient/apidocs/org/apache/http/conn/ssl/BrowserCompatHostnameVerifier.html">BrowserCompatHostnameVerifier.html</a>)</li>
		<li><strong>ALLOW_ALL</strong> - trust all hostnames (see <a href="http://hc.apache.org/httpcomponents-client-ga/httpclient/apidocs/org/apache/http/conn/ssl/AllowAllHostnameVerifier.html">AllowAllHostnameVerifier</a>)</li>
	</ul>
	See the <a href="http://hc.apache.org/httpcomponents-client-ga/tutorial/html/connmgmt.html#d5e535">Apache HttpComponent HttpClient documentation</a> for more information about the hostname verifications strategies.
	<br/>
	<strong>N.B.:</strong> This connection option is only applicable for the <strong>WINRM</strong> connection type, when <a href="#cifs_winrmEnableHttps"><strong>winrmEnableHttps</strong></a> is set to <code>true</code>.</td>
</tr>
<tr>
	<th align="left" valign="top"><a name="cifs_winrmLocale"></a>winrmLocale</th>
	<td>The WinRM locale to use. The default value is <code>en-US</code>.
	<br/>
	<strong>N.B.:</strong> This connection option is only applicable for the <strong>WINRM</strong> connection type.</td>
</tr>
<tr>
	<th align="left" valign="top"><a name="cifs_winrmTimeout"></a>winrmTimeout</th>
	<td>The WinRM timeout to use in <a href="http://www.w3.org/TR/xmlschema-2/#isoformats">XML schema duration format</a>. The default value is <code>PT60.000S</code>.
	<br/>
	<strong>N.B.:</strong> This connection option is only applicable for the <strong>WINRM</strong> connection type.</td>
</tr>
</table>

<a name="cifs_host_setup"></a>
### Host setup

<a name="cifs_host_setup_cifs"></a>
#### CIFS
To connect to a remote host using the __CIFS__ protocol, ensure the host is reachable on port 445.

If you will be connecting as an administrative user, ensure the administrative shares are configured. Otherwise, ensure that the user you will be using to connect has access to shares that correspond to the directory you want to access and that the [__pathShareMappings__](#cifs_pathShareMappings) connection option is configured accordingly.

<a name="cifs_host_setup_telnet"></a>
#### TELNET

To use the __TELNET__ connection type, enable the Telnet Server Service according to <a href="http://technet.microsoft.com/en-us/library/cc732046(WS.10).aspx">these instructions on the Microsoft Technet site</a>. If the remote host is running Windows Server 2003 SP1 or an x64-based version of Windows Server 2003, you will have to install the Telnet server 	according to [these instructions from the Microsoft Support site](http://support.microsoft.com/kb/899260). After you have started the Telnet Server, open a command prompt as the __Administrator__ user and enter the command `tlntadmn config mode=stream` to enable stream mode.

When the Telnet server is enabled any user that is in the __Administrators__ group or that is in the __TelnetClients__ group and that has the "Allow logon locally" privilege can log in using Telnet. See the Microsoft Technet to learn <a href="http://technet.microsoft.com/en-us/library/ee957044(WS.10).aspx">how to grant a user or group the right to logon locally</a> on Windows Server 2008 R2.

<a name="cifs_host_setup_winrm"></a>
#### WINRM

_For a PowerShell script to do what is described below in one go, check [Richard Downer's blog](http://www.frontiertown.co.uk/2011/12/overthere-control-windows-from-java/)_

To use the __WINRM__ connection type, you'll need to setup WinRM on the remote host by following these instructions:

1. If the remote host is running Windows Server 2003 R2, you will need to enable WinRM. As the Administrator user, go to the __Add/Remove System Components__ feature in the __Control Panel__ and add WinRm under the section __Management and Monitoring Tools__.

2. On the remote host, as the Administrator user, open a Command Prompt and follow the steps below.

3. (Optional) If you wish to use local accounts to access the system, configure WinRM to allow basic authentication:

		winrm set winrm/config/service/Auth @{Basic="true"}

4. (Optional) If you do not with to use domain accounts to access the system and you are not going to configure Kerberos on your client machine, configure WinRM to not allow Kerberos authentication:

		winrm set winrm/config/service/Auth @{Kerberos="false"}

	__N.B.__ Do not disallow Negotiate authentication as the `winrm` command itself uses that to configure the WinRM subsystem!
	
5. Configure WinRM to allow unencrypted SOAP messages:

		winrm set winrm/config/service @{AllowUnencrypted="true"}

6. Configure WinRM to provide enough memory to the commands that you are going to run, e.g. 1024 MB:

		winrm set winrm/config/winrs @{MaxMemoryPerShellMB="1024"}

7. To use the __WINRM__ connection type with [__winrmEnableHttps__](#cifs_winrmEnableHttps) set to `false`, create an HTTP WinRM listener:

		winrm create winrm/config/listener?Address=*+Transport=HTTP

8. To use the __WINRM__ connection type with [__winrmEnableHttps__](#cifs_winrmEnableHttps) set to `true`, follow the steps below:

	1. (Optional) Create a self signed certificate for the remote host by installing `selfssl.exe` from [the IIS 6 resource kit](http://www.microsoft.com/download/en/details.aspx?displaylang=en&id=17275) and running the command below or by following the instructions [in this blog by Hans Olav](http://www.hansolav.net/blog/SelfsignedSSLCertificatesOnIIS7AndCommonNames.aspx):

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

	3. Create an HTTPS WinRM listener for the remote host with the thumbprint you've just found:

			winrm create winrm/config/Listener?Address=*+Transport=HTTPS @{Hostname="HOSTNAME"; CertificateThumbprint="THUMBPRINT"}


For more information on WinRM, please refer to <a href="http://msdn.microsoft.com/en-us/library/windows/desktop/aa384426(v=vs.85).aspx">the online documentation at Microsoft's DevCenter</a>. As a quick reference, have a look at the list of useful commands below:

* Do a quickconfig for WinRM: `winrm qc`
* Do a quickconfig for WinRM with HTTPS: `winrm qc -transport:https`
* Dump the complete WinRM configuration: `winrm get winrm/config`
* View the listeners that have been configured: `winrm enumerate winrm/config/listener`
* Allow all hosts to connect to the WinRM listener: `winrm set winrm/config/client @{TrustedHosts="*"}`
* Allow a fixed set of hosts to connect to the WinRM listener: `winrm set winrm/config/client @{TrustedHosts="host1,host2..."}`

<a name="cifs_host_setup_krb5"></a>
#### Kerberos - source machine

In addition to the setup described in [the WINRM section](#cifs_host_setup_winrm), using Kerberos authentication requires that you follow the [Kerberos Requirements for Java](http://docs.oracle.com/javase/6/docs/technotes/guides/security/jgss/tutorials/KerberosReq.html) on the machine from which the Overthere connections are initiated, i.e. the source machine.

The simplest configuration is with a single domain/realm, and involves adding the following Java system properties to your commandline: `-Djava.security.krb5.realm=EXAMPLE.COM; -Djava.security.krb5.kdc=KDC.EXAMPLE.COM`. Replace the values with the name of your domain/realm and the hostname of your domain controller.

For a more complex setup, e.g. one involving multiple domains, create a file called `krb5.conf` with at least the following content: 

<pre>
[realms]
EXAMPLE.COM = {
    kdc = KDC.EXAMPLE.COM
}
</pre>
and add the following Java system property to the command line: `-Djava.security.krb5.conf=/path/to/krb5.conf`. Replace the path with the location of the `krb5.conf` file you just created. Multiple entries can be added to allow the Overthere source machine to connect to multiple domains.

<a name="cifs_host_setup_spn"></a>
#### Kerberos - target machines

In addition to the setup described in [the WINRM section](#cifs_host_setup_winrm), using Kerberos authentication requires that you add <a href="http://msdn.microsoft.com/en-us/library/windows/desktop/ms677949(v=vs.85).aspx">service principal names</a> for the WinRM servers you want to connect to, i.e. the target machines.

This can be achieved by invoking the <a href="http://technet.microsoft.com/en-us/library/cc731241(v=ws.10).aspx">setspn</a> command, as an Administrator, on any machine in the domain, as follows:
<pre>
setspn -A HTTP/<em>ADDRESS</em>:<em>PORT</em> <em>WINDOWS-HOST</em>
</pre>
where `ADDRESS` is the <a href="#address"><strong>address</strong></a> used to connect to the target machine, `PORT` is the <a href="#port"><strong>port</strong></a> used to connect to the target machine (usually 5985 or 5986), and `WINDOWS-HOST` is the short Windows hostname, i.e. the _CN_, of the target machine.

<a name="tunnelling"></a>
## Tunnelling

Overthere supports the tunnelling of every protocol over SSH. This can be used to reach hosts that live in a DMZ which can only be reached by connecting to a different host first. This in-between host is called the jump station. In order to configure an SSH tunnel, you need to provide a set of nested connection options specifying which host is used as the jump station.

When using a jumpstation to connect to the remote host, Overthere will dynamically allocate an available local port to use for the connection to the end station. Using an additional connection option, you can configure from which port onwards Overthere starts the allocation.

<table>
<tr>
	<th align="left" valign="top"><a name="tunneling_portAllocationRangeStart"></a>portAllocationRangeStart</th>
	<td>The port number Overthere starts with to find an available local port for setting up a tunnel. The default value is <code>1025</code>.</td>
</tr>
</table>

<a name="troubleshooting"></a>
# Troubleshooting Overthere

This section lists a number of common configuration errors that can occur when using Overthere. Please let us know by [creating a ticket](https://github.com/xebialabs/overthere/issues) or by [sending us a pull request](https://github.com/xebialabs/overthere/pulls) if you have run into other connectivity issues when using Overthere.

<a name="troubleshooting_ssh"></a>
## SSH

#### Cannot start a process on an SSH server because the server disconnects immediately.

If the terminal type requested using the [__allocatePty__](#ssh_allocatePty) connection option or the [__allocateDefaultPty__](#ssh_allocateDefaultPty) connection option is not recognized by the SSH server, the connection will be dropped. Specifically, the `dummy` terminal type configured by [__allocateDefaultPty__] connection option, will cause OpenSSH on AIX and WinSSHD to drop the connection. Try a safe terminal type such as `vt220` instead.

To verify the behaviour of your SSH server with respect to pty allocation, you can manually execute the <code>ssh</code> command with the `-T` (disable pty allocation) or `-t` (force pty allocation) flags.


#### Command executed using SUDO or INTERACTIVE_SUDO fails with the message ` sudo: sorry, you must have a tty to run sudo`

The `sudo` command requires a tty to run. Set the [__allocatePty__](#ssh_allocatePty) connection option or the [__allocateDefaultPty__](#ssh_allocateDefaultPty) connection option to ask the SSH server allocate a pty.

#### Command executed using SUDO or INTERACTIVE_SUDO appears to hang.

This may be caused by the sudo command waiting for the user to enter his password to confirm his identity. There are two ways to solve this:

1. Use the [`NOPASSWD`](http://www.gratisoft.us/sudo/sudoers.man.html#nopasswd_and_passwd) tag in your `/etc/sudoers` file.
2. Use the [__INTERACTIVE_SUDO__](#ssh_host_setup_interactive_sudo) connection type instead of the [__SUDO__](ssh_host_setup_sudo) connection type.
3. If you are already using the __INTERACTIVE_SUDO__ connection type and you still get this error, please verify that you have correctly configured the [__sudoPasswordPromptRegex__](#ssh_sudoPasswordPromptRegex) option. If you have trouble determining the proper value for the __sudoPasswordPromptRegex__ connection option, set the log level for the `com.xebialabs.overthere.ssh.SshInteractiveSudoPasswordHandlingStream` category to `TRACE` and examine the output.


<a name="troubleshooting_cifs"></a>
## CIFS

#### CIFS connections are very slow to set up.

The [JCIFS library](http://jcifs.samba.org), which Overthere uses to connect to CIFS shares, will try and query the Windows domain controller to resolve the hostname in SMB URLs. JCIFS will send packets over port 139 (one of the [NetBIOS over TCP/IP] ports) to query the <a href="http://en.wikipedia.org/wiki/Distributed_File_System_(Microsoft)">DFS</a>. If that port is blocked by a firewall, JCIFS will only fall back to using regular hostname resolution after a timeout has occurred.

Set the following Java system property to prevent JCIFS from sending DFS query packets:
`-Djcifs.smb.client.dfs.disabled=true`.

See [this article on the JCIFS mailing list](http://lists.samba.org/archive/jcifs/2009-December/009029.html) for a more detailed explanation.

<a name="troubleshooting_telnet"></a>
## Telnet

#### Telnet connection fails with the message `VT100/ANSI escape sequence found in output stream. Please configure the Windows Telnet server to use stream mode (tlntadmn config mode=stream).`

The Telnet service has been configured to be in "Console" mode. Did you configure it as described in [the section on Telnet setup](#cifs_host_setup_telnet)?

<a name="troubleshooting_krb"></a>
## Kerberos

#### Kerberos authentication fails with the message `Cannot get kdc for realm …`

The Kerberos subsystem of Java cannot find the information for the realm in the `krb5.conf` file. The realm name is case sensitive. Does the case match the case in the `krb5.conf` file?

#### Kerberos authentication fails with the message `Server not found in Kerberos database (7)`

The service principal name for the target machine has not been added to Active Directory. Did you add the SPN as described in [the section on Kerberos setup for target machines](#cifs_host_setup_spn)?

#### Kerberos authentication fails with the message `Pre-authentication information was invalid (24)` or `Identifier doesn't match expected value (906)`

The username or the password supplied was invalid. Did you supply the correct credentials?

#### Kerberos authentication fails with the message `Unable to load realm info from SCDynamicStore`

The Kerberos subsystem of Java cannot start up. Did you configure it as described in [the section on Kerberos setup for the source machine](#cifs_host_setup_krb5)?

#### I am not using Kerberos authentication and I still see messages saying `Unable to load realm info from SCDynamicStore`

The Kerberos subsystem of Java cannot start up and the remote WinRM server is sending a Kerberos authentication challenge. If you are using local accounts, the authentication will proceed succesfully despite this message. To remove these messages either configure Kerberos as described in [the section on Kerberos setup for the source machine](#cifs_host_setup_krb5) or disallow Kerberos on the WinRM server as described in step 4 of [the section on WinRM setup](#cifs_host_setup_winrm).

<a name="release_history"></a>
# Release History

* Overthere 2.0.0-beta-8
	* Added description of WinRM options and how to set up Kerberos authentication.
	* Added troubleshooting section.
    * Fixed Kerberos authentication for WinRM connections which was broken by overzealouos code cleanup.
* Overthere 2.0.0-beta-7 (02-Aug-2012)
    * Fixed bug in WinRM implementation: It was not sending individual stdout chars to OverthereProcessOutputHandler.handleOutput.
* Overthere 2.0.0-beta-6 (02-Aug-2012)
    * Renamed CIFS_PATH_SHARE_MAPPING back to PATH_SHARE_MAPPINGS.
* Overthere 2.0.0-beta-5 (02-Aug-2012)
    * Added support for Windows domain accounts to CIFS and WinRM connection methods.
    * Renamed a few options.
    * Fixed bug in SSH tunnel port allocation code that caused the same local port to be allocated multiple times on Windows.
    * Changed license to GPLv2 with XebiaLabs FLOSS License Exception.
* Overthere 2.0.0-beta-4 (19-Jun-2012)
    * Fixed issue #42.
    * Moved itest-support project out to new Github repository [Overcast](https://github.com/xebialabs/overcast)
    * Updated documentation.
* Overthere 2.0.0-beta-3 (27-Mar-2012)
    * Updated documentation.
* Overthere 2.0.0-beta-2 (23-Mar-2012)
	* Fixed issues #39 and #40.
	* Upgraded to latest jCIFS to fix issues with windows domain names and stability using tunnels.
	* Set default pty to true in case of interactive sudo and no pty set.
* Overthere 2.0.0-beta-1 (05-Mar-2012)
    * Re-implemented SSH tunnels. Tunnels are now created on demand instead of the user having to specify the localPortForwards explicitly. This makes management of tunnels easier and prevents clashes.
    * Ported Overthere tests to use TestNG instead of JUnit.
* Overthere 1.0.16 (23-Feb-2012)
    * Reverted changes made to support SSH tunnels in 1.0.14 and 1.0.15 because it did not work as well as we hoped. We are reimplementing it for Overthere 2.0 to be released early March.
    * Fixed command line encoding bugs for SSH/CYGWIN on Windows:
        * Now transforming the first element of the command line to a Cygwin path so that batch files (and executables) in specific directories (instead of on the PATH) can be executed.
        * Encoding the command line as if the target OS is UNIX because OpenSSH on Cygwin uses Windows encoding.
* Overthere 1.0.15 (21-Feb-2012)
    * Added explicit close() method to the new OverthereConnection interface (it was a class in 1.0.13) that does not throw java.io.IOException.
* Overthere 1.0.14 (20-Feb-2012)
    * Added support for SSH tunnels to jumpstations.
    * Added support for NTLM authentication.
    * Upgraded to SSH/J 0.7.0.
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


