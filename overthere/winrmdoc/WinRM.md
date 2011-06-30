% WinRM Setup and configuration
%
% May, 2011
* Benoit Moussaud.

# Simple Setup
This manual describes how to set up and to configuration a WinRM server.

# Basic Setup
		winrm quickconfig
		winrm qc

# Basic Winrm configuration compatible with WinRM Java Cli

1. Create a new winrm listener on the target machine by running the command from a command prompt.

		winrm create winrm/config/listener?Address=*+Transport=HTTP

2. Configure the winrm listener on the target machine to permit executed command to be executed using HTTP by running the command from a command prompt.

		winrm set winrm/config/service @{AllowUnencrypted="true"}

3. Configure the winrm listener on the target machine to use kerberos authentication by running the command from a command prompt.

		winrm set winrm/config/service/Auth @{Keberos="true"}

4. Configure the winrm listener on the targer machine to not use basic authentication by running the command from a command prompt.

	winrm set winrm/config/service/Auth @{Basic="true"}

5. Enable 'Kerberos DES encryption'options in AD on all accounts used with Winrm Java Cli.

6. Don't forget to configure the firewall.


# Winrm and Https

_If you really need this_ Don't forget the username & password are transmitted using encrypted security protocol (Kerberos).

1. Checking listeners

        C:\>winrm enumerate winrm/config/listener
        Listener
            Address = *
            Transport = HTTP
            Port = 5985
            Hostname
            Enabled = true
            URLPrefix = wsman
            CertificateThumbprint
            ListeningOn = 127.0.0.1, 172.16.74.129, ::1, fe80::5efe:172.16.74.129%11, fe80::6c46:386d:27be:2b29%10

This output shows there is only one listener using HTTP transport.

2. (optional) create a self signed certificate for your server.
Install selfcert.exe (II6 resource kit, for II7 activate II6 compatibily kit).
Usefull links:
* http://www.hansolav.net/blog/SelfsignedSSLCertificatesOnIIS7AndCommonNames.aspx (How to install SelfSSL)


        C:\Program Files\IIS Resources\SelfSSL>selfssl.exe /T /N:cn=WIN-2MGY3RY6XSH.deployit.local /V:3650
        Microsoft (R) SelfSSL Version 1.0
        Copyright (C) 2003 Microsoft Corporation. All rights reserved.

        Do you want to replace the SSL settings for site 1 (Y/N)?Y
        The self signed certificate was successfully assigned to site 1.

        PS C:\Windows\system32> Get-childItem cert:\LocalMachine\Root\ | Select-String -pattern WIN-2MGY3RY6XSH.deployit.local

		[Subject]
		  CN=WIN-2MGY3RY6XSH.deployit.local

		[Issuer]
		  CN=WIN-2MGY3RY6XSH.deployit.local

		[Serial Number]
		  527E7AF9142D96AD49A10469A264E766

		[Not Before]
		  5/23/2011 10:23:33 AM

		[Not After]
		  5/20/2021 10:23:33 AM

		[Thumbprint]
		  5C36B638BC31F505EF7F693D9A60C01551DD486F

3. Create an HTTPS listener automaticly using a non-self signed certificate that match perfectly your hostname

		winrm qc -transport:https

            If you have an error (for example this one, use the manual installation)

        C:\itest>winrm qc -transport:https
        WinRM already is set up to receive requests on this machine.
        WSManFault
            Message
                ProviderFault
                    WSManFault
                        Message = Cannot create a WinRM listener on HTTPS because this machine does not have an appropriate certificate. To be used for
         SSL, a certificate must have a CN matching the hostname, be appropriate for Server Authentication, and not be expired, revoked, or self-signed.

        Error number:  -2144108267 0x80338115
        Cannot create a WinRM listener on HTTPS because this machine does not have an appropriate certificate. To be used for SSL, a certificate must h
        ave a CN matching the hostname, be appropriate for Server Authentication, and not be expired, revoked, or self-signed.

4. Create an HTTPS listener manualy with an powershell run as Administrator:

		PS C:\Windows\system32> winrm create winrm/config/Listener?Address=*+Transport=HTTPS `@`{Hostname=`"`WIN-2MGY3RY6XSH.deployit.local`"`; CertificateThumbprint=`"`5C36B638BC31F505EF7F693D9A60C01551DD486F`"`}
		ResourceCreated
			Address = http://schemas.xmlsoap.org/ws/2004/08/addressing/role/anonymous
			ReferenceParameters
			ResourceURI = http://schemas.microsoft.com/wbem/wsman/1/config/listener
			SelectorSet
				Selector: Address = *, Transport = HTTPS

5. Check you have an HTTPS listener

        PS C:\Windows\system32> winrm enumerate winrm/config/listener
        Listener
            Address = *
            Transport = HTTP
            Port = 5985
            Hostname
            Enabled = true
            URLPrefix = wsman
            CertificateThumbprint
            ListeningOn = 127.0.0.1, 172.16.74.129, ::1, fe80::5efe:172.16.74.129%16, fe80::6c46:386d:27be:2b29%10

        Listener
            Address = *
            Transport = HTTPS
            Port = 5986
            Hostname = WIN-2MGY3RY6XSH.deployit.local
            Enabled = true
            URLPrefix = wsman
            CertificateThumbprint = 5C36B638BC31F505EF7F693D9A60C01551DD486F
            ListeningOn = 127.0.0.1, 172.16.74.129, ::1, fe80::5efe:172.16.74.129%16, fe80::6c46:386d:27be:2b29%10





# Usefull commands

* Dump Winrm config
		winrm get winrm/config

* Allowing all hosts to connect to winrm
		winrm set winrm/config/client @{TrustedHosts="*"}

* Allowing some host to connect to Winrm
        winrm set winrm/config/client @{TrustedHosts="system1,system2..."}

* Allowing uncrypted content exchange
		winrm set winrm/config/service  @{AllowUnencrypted="true"}

* Enumerate listeners
		winrm e winrm/config/listener



# Useful links
* http://www.symantec.com/business/support/index?page=content&id=TECH156625
