import sbt._
import Keys._

object Overthere extends Build {

  val Itest = config("itest") extend(Test)

  lazy val rootProject = Project(
    "root",
    file("."),
    settings = Defaults.defaultSettings
  ) aggregate(overthere, itestSupport)

  lazy val overthere = Project(
    "overthere",
    file("overthere"),
    settings = Defaults.defaultSettings ++ Seq(
      libraryDependencies ++= Seq(
        Dependency.guava,
        Dependency.scannit,
        Dependency.slf4jApi,
        Dependency.sshj,
        Dependency.cifs,
        Dependency.dom4j,
        Dependency.commonsNet,
        Dependency.commonsCodec,
        Dependency.jaxen,
        Dependency.Test.mockito
      ) ++ Dependency.Test.deps,
      parallelExecution in Test := false,
      testOptions in Test := Seq(Tests.Filter(_.endsWith("Test"))),
      testOptions in Itest := Seq(Tests.Filter(_.endsWith("Itest")))
    )
  ) dependsOn(itestSupport % "test")

  lazy val itestSupport = Project(
    "itest-support",
    file("itest-support"),
    settings = Defaults.defaultSettings ++ Seq(
      libraryDependencies ++= Seq(
        Dependency.guava,
        Dependency.awsSdk,
        Dependency.slf4jApi,
        Dependency.sshj
      ) ++ Dependency.Test.deps,
      parallelExecution in Test := false

    )
  )

  object Dependency {
    object V {
      val Guava = "10.0.1"
    }

    val guava = "com.google.guava" % "guava" % V.Guava
    val scannit = "nl.javadude.scannit" % "scannit" % "0.13"
    val slf4jApi = "org.slf4j" % "slf4j-api" % "1.6.3"
    val awsSdk = "com.amazonaws" % "aws-java-sdk" % "1.2.2" exclude("commons-codec", "commons-codec")
    val sshj = "net.schmizz" % "sshj" % "0.6.1"
    val cifs = "org.samba.jcifs" % "jcifs" % "1.3.3"
    val dom4j = "dom4j" % "dom4j" % "1.6.1" exclude("xml-apis", "xml-apis")
    val commonsNet = "commons-net" % "commons-net" % "2.0"
    val commonsCodec = "commons-codec" % "commons-codec" % "1.5"
    val jaxen = "jaxen" % "jaxen" % "1.1.1" exclude("xml-apis", "xml-apis") exclude("xalan", "xalan") exclude("xerces", "xmlParserAPIs") exclude("xerces", "xercesImpl")

    object Test {
      val junit = "junit" % "junit-dep" % "4.10" % "test"
      val hamcrestCore = "org.hamcrest" % "hamcrest-core" % "1.2.1" % "test"
      val hamcrestLib = "org.hamcrest" % "hamcrest-library" % "1.2.1" % "test"
      val logback = "ch.qos.logback" % "logback-classic" % "0.9.30" % "test"
      val mockito = "org.mockito" % "mockito-all" % "1.8.5" % "test"
      val scalaTest = "org.scalatest" %% "scalatest" % "1.6.1" % "test"
      val junitInterface = "com.novocode" % "junit-interface" % "0.7" % "test" exclude("junit", "junit")
      val deps = Seq(junit, hamcrestCore, hamcrestLib, logback, junitInterface)
    }
  }
}
