% WinRM Library Manual
%
% Mai, 2011

# Preface #

This manual describes the WinRM Library.

# Introduction #

The Windows Remote Management (WinRM) is the Microsoft implementation of WS-Management Protocol, a standard Simple Object Access Protocol (SOAP)-based, firewall-friendly protocol that allows hardware and operating systems, from different vendors, to interoperate.

This library allows anay java program to become a WinRM client to run commands on a remote server configured with WinRM.

# HTTPS Configuration
Two HTTPS configurations are available
1. Lazy HTTPS.
	select HTTPS_LAZY as the WinRMHost propotocol. This configuration does not check any serveurs certifcates and does not verify the hostname. For Test only.
2. Real HTTPS. You need to have the server certificate in your truststore.
* Fetch the server certificate.
        /tmp/openssl s_client  -connect WIN-2MGY3RY6XSH.deployit.local:5986
        ONNECTED(00000003)
		depth=0 /CN=WIN-2MGY3RY6XSH.deployit.local
		verify error:num=20:unable to get local issuer certificate
		verify return:1
		depth=0 /CN=WIN-2MGY3RY6XSH.deployit.local
		verify error:num=21:unable to verify the first certificate
		verify return:1
		---
		Certificate chain
		 0 s:/CN=WIN-2MGY3RY6XSH.deployit.local
		   i:/CN=WIN-2MGY3RY6XSH.deployit.local
		---
		Server certificate
		-----BEGIN CERTIFICATE-----
		MIIB/jCCAWegAwIBAgIQUn56+RQtlq1JoQRpomTnZjANBgkqhkiG9w0BAQUFADAp
		MScwJQYDVQQDEx5XSU4tMk1HWTNSWTZYU0guZGVwbG95aXQubG9jYWwwHhcNMTEw
		NTIzMDgyMzMzWhcNMjEwNTIwMDgyMzMzWjApMScwJQYDVQQDEx5XSU4tMk1HWTNS
		WTZYU0guZGVwbG95aXQubG9jYWwwgZ8wDQYJKoZIhvcNAQEBBQADgY0AMIGJAoGB
		ANMrObs4gTUtFuiSFZEWD+tiy2sNEhlGt78VOIwZz1zQ8YxoSGnepMHpPc38QFI5
		VUZWabXV7B7ErlWXp0mvxW1VSrALjknTJtdqOttjZZx1w7w0shD9wKefZYLCdGF2
		CZpiY2P7kmB5dBKlm6YqE/MVaMUxuKUqzYOASjt4lx+ZAgMBAAGjJzAlMBMGA1Ud
		JQQMMAoGCCsGAQUFBwMBMA4GA1UdDwQHAwUAsAAAADANBgkqhkiG9w0BAQUFAAOB
		gQBvu3hgn/pmwY5asKIoZMEsteBEcaGisowbojegClEy/5m3OZJ9LKekHRW+Kum5
		98/mQFgFAEJMbsl+X8a8abNj4L+H0VjP+scllXmRZR9tIkXk8TLoQUSljhvhF0Uz
		lzMHXeo71mVcMg8dvIRY0xK7Dc5uk+NFzeu7RAbRE8m3oA==
		-----END CERTIFICATE-----
		subject=/CN=WIN-2MGY3RY6XSH.deployit.local
* Put the content of '-----BEGIN CERTIFICATE-----' and '-----END CERTIFICATE-----' (including tags) in a file: ex: src/test/resources/key/remote.host.pem
* Import the key into a new (or existing) keystore
        keytool -import -keystore src/test/resources/key/cacerts -alias WIN-2MGY3RY6XSH -file src/test/resources/key/remote.host.pem
* Define this keystore as the trustore
        -Djavax.net.ssl.trustStore=src/test/resources/key/cacerts

Reference (In French) http://blog.ippon.fr/2008/10/20/certificats-auto-signe-et-communication-ssl-en-java/

# Kerberos Configuration
1. Create (or edit) the JAAS configuration file. Add the following entry

		WinRMClient {
		  com.sun.security.auth.module.Krb5LoginModule required
			doNotPrompt=false
			useTicketCache=false
			debug=true;
		};

2. Create a file called krb5.conf or krb5.ini with the following content
		[libdefaults]
			default_realm = DEPLOYIT.LOCAL


		[realms]
			DEPLOYIT.LOCAL = {
				kdc = WIN-2MGY3RY6XSH.deployit.local
			}

		[domain_realm]
			.deployit.local = DEPLOYIT.LOCAL
			deployit.local = DEPLOYIT.LOCAL

Replace the values of
* the 'kdc' by your key domain controler name,
* the '.deployit.local' and 'deployit.local' by your domain name

More information {http://support.microsoft.com/kb/555092}
Alternate configuration:
* java.security.krb5.realm defines the default_realm
* java.security.krb5.kdc defines the kdc ( key domain controller ?) of the default realm


3. Add the following property to the JVM -Djava.security.auth.login.config=/path/to/login.conf
4. Add the following property to the JVM -Djava.security.krb5.conf=/path/to/krb5.conf
5. Add the following property to the JVM -Djavax.security.auth.useSubjectCredsOnly=false

Note: if java.security.krb5.conf is not defined, Sun implementation will search in file named krb5.conf
- $(java.home}/lib/security/
- Windows c:/winnt/ or ${WindowHome}
- SunOS: /etc/krb5/krb5.conf
- Linux: /etc/krb5.conf
- MacOs: $(user.home)/Library/Preferences/edu.mit.Kerberos, /Library/Preferences/edu.mit.Kerberos, /etc/krb5.conf



# Known Limitations

* This implementation does not support encrypted communication between the client and the WinRM server. You need to switch it off by running this command
	winrm set winrm/config/service  @{AllowUnencrypted="true"}


# TroubleShooting
1. javax.security.auth.login.LoginException: KDC has no support for encryption type (14)
* See http://download.oracle.com/javase/6/docs/technotes/guides/security/jgss/tutorials/Troubleshooting.html
* Add the option 'Use Kerberos DES type for this account' to the account used to connect to the remote Windows.

# Thanks
* ZendChild to provide me a ruby implementation and to give me wise advices https://github.com/zenchild/WinRM



* JNA and http://code.google.com/p/jnaerator/ mvn clean com.jnaerator:maven-jnaerator-plugin:jnaerate
http://download.oracle.com/docs/cd/E19963-01/html/819-2145/gssclient-3.html
http://download.oracle.com/docs/cd/E19082-01/819-2145/overview-22/index.html


http://cr.openjdk.java.net/~weijun/special/krb5winguide-2/raw_files/new/kwin