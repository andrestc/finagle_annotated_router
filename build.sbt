import java.io.File

import spray.revolver.RevolverPlugin._

name := """finagle_annotated_router"""

fork in run := true

resolvers ++= Seq("Twitter Repo" at "http://maven.twttr.com/")

resolvers ++= Seq("Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots")

resolvers += "Mesosphere Public Repository" at "http://downloads.mesosphere.io/maven"

scalaVersion := "2.11.5"

val finagleVersion = "6.25.0"

organization := "com.rigon"

libraryDependencies ++= Seq(
  "com.sksamuel.elastic4s" %% "elastic4s" % "1.4.0",
  "com.twitter" %% "twitter-server" % "1.10.0",
  "com.twitter" % "finagle-core_2.11" % "6.2.0",
  "com.twitter" %% "finagle-http" % finagleVersion,
  "com.twitter" %% "finagle-mysql" % finagleVersion,
  "redis.clients" % "jedis" % "2.6.2",
  ("com.twitter" %% "finagle-stats" % finagleVersion).exclude("asm", "asm"),
  "com.codahale.metrics" % "metrics-graphite" % "3.0.1",
  "com.twitter" %% "finagle-zipkin" % finagleVersion,
  "org.slf4j" % "slf4j-log4j12" % "1.7.10",
  "com.fasterxml.jackson.module" % "jackson-module-scala_2.11" % "2.4.4",
  "com.typesafe.akka" %% "akka-actor" % "2.3.6",
  "com.typesafe" % "config" % "1.2.1",
  "mesosphere" %% "jackson-case-class-module" % "0.1.2",
  "org.springframework" % "spring-core" % "4.1.6.RELEASE",
  "org.springframework" % "spring-context" % "4.1.6.RELEASE"
)

// test

libraryDependencies ++= Seq(
  "org.mockito" % "mockito-core" % "1.9.5" % "test",
  "org.scalatest" % "scalatest_2.11" % "2.2.1" % "test",
  "org.codehaus.groovy" % "groovy-all" % "2.4.3" % "test"
)

assemblyJarName in assembly := s"${name.value}.jar"

parallelExecution in Test := true

testOptions in Test += Tests.Cleanup(() => {
  val files = new File("/tmp").listFiles().filter(_.getName.startsWith("search_api_"))
  def delete(file: File): Array[(String, Boolean)] = Option(file.listFiles).map(_.flatMap(delete)).getOrElse(Array()) :+ (file.getPath -> file.delete)
  files map delete
})

ScoverageSbtPlugin.ScoverageKeys.coverageMinimum := 65

ScoverageSbtPlugin.ScoverageKeys.coverageFailOnMinimum := true

addCommandAlias("test", "testQuick")

addCommandAlias("devrun", "~re-start")

addCommandAlias("cov", "; clean; coverage; test")

Revolver.settings

test in assembly := {}

assemblyMergeStrategy in assembly <<= (mergeStrategy in assembly) { (old) =>
{
  case "META-INF/spring.tooling" => MergeStrategy.first
  case x => old(x)
}
}

addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0-M5" cross CrossVersion.full)

releaseSettings

publishTo := Some(Resolver.file("Local repo", Path.userHome / ".m2" / "repository" asFile ))
