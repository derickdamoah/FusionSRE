
ThisBuild / scalaVersion := "2.13.14"
ThisBuild / version := "1.0-SNAPSHOT"

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .settings(
    name := """FusionSRE""",
    libraryDependencies ++= Seq(
      guice,
      "org.scalatestplus.play" %% "scalatestplus-play" % "7.0.1" % Test
    )
  )

libraryDependencies += "org.mongodb.scala" %% "mongo-scala-driver" % "5.1.1"
libraryDependencies += "io.github.cdimascio" % "dotenv-java" % "3.0.0"
libraryDependencies += "com.amazonaws" % "aws-java-sdk-dynamodb" % "1.12.748"
libraryDependencies += "com.fasterxml.jackson.core" % "jackson-databind" % "2.14.3"
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.5.6"
libraryDependencies += "org.slf4j" % "slf4j-api" % "2.0.12"


ThisBuild / assemblyMergeStrategy :=  {
  case PathList("META-INF", _*) => MergeStrategy.discard
  case "play/reference-overrides.conf" => MergeStrategy.first
  case "module-info.class" => MergeStrategy.first
  case PathList("org", "apache", "commons", "logging", _ @_ , _ *) => MergeStrategy.first
  case other => (assembly / assemblyMergeStrategy).value(other)
}
assembly / assemblyOutputPath := new File(System.getProperty("user.dir") + "/FusionSRE-1.0-SNAPSHOT.jar")
