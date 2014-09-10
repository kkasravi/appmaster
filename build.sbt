name := "appmaster"

version := "0.1-SNAPSHOT"

organization := "com.kasravi"

scalaVersion := "2.10.4"

packSettings

packMain := Map("appmaster" -> "com.kasravi.appmaster.AppMaster",
                "consumer" -> "com.kasravi.appmaster.consumer.Main")
				
packResourceDir += (baseDirectory.value / "src/main/resources" -> "conf")		
		
packExtraClasspath := Map("conf" -> Seq("${PROG_HOME}/conf"))

resolvers ++= Seq(
  "maven-repo" at "http://repo.maven.apache.org/maven2",
  "maven1-repo" at "http://repo1.maven.org/maven2",
  "sonatype" at "https://oss.sonatype.org/content/repositories/releases",
  "krasserm at bintray" at "http://dl.bintray.com/krasserm/maven"
)

parallelExecution in Test := false

val akkaVersion = "2.3.4"
val slf4jVersion = "1.7.5"

libraryDependencies ++= Seq(
  "org.slf4j" % "slf4j-api" % slf4jVersion,
  "org.slf4j" % "slf4j-log4j12" % slf4jVersion,
  "org.slf4j" % "jul-to-slf4j" % slf4jVersion,
  "org.slf4j" % "jcl-over-slf4j" % slf4jVersion,
  "com.github.krasserm" %% "akka-persistence-kafka" % "0.3.2"
)
