# Introduction 

Overthere is a Java library to manipulate files and execute processes on remote machines, i.e. do stuff "over there". It was built for and is used in the [XebiaLabs](http://xebialabs.com/) deployment automation product Deployit as a way to perform tasks on remote machines, e.g. copy configuration files, install EAR files or restart web servers. Another way of looking it at is to say that Overthere gives you java.io.File and java.lang.Process as they should've been: as interfaces, created by a factory and extensible through an SPI mechanism.

For a more thorough introducion to Overthere, check the [presentation on Overthere](http://www.slideshare.net/vpartington/presentation-about-overthere-for-jfall-2011) that I gave for J-Fall 2011, a Java conference in the Netherlands. Don't worry, the presentation is in English. :-)

# Getting Overthere

To get Overthere, you have two options:

1. Add a dependency to Overthere to your project.
2. Build Overthere yourself.

Additionally you can also:

3. Run the Overthere examples used in the Overthere presentation mentioned above.

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

# Setting up the target machines

## WinRM

Please refer to [README document](https://github.com/xebialabs/overthere/blob/master/overthere/winrmdoc/README.md) and the [WinRM setup document](https://github.com/xebialabs/overthere/blob/master/overthere/winrmdoc/WinRM.md).

