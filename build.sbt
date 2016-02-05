name := "TweetsStream"

version := "1.0"

scalaVersion := "2.11.7"

// set the main class for 'sbt run'
mainClass in (Compile, run) := Some("com.mrkaspa.tweetstream.Main")

libraryDependencies ++= Seq(
  "com.typesafe.akka" % "akka-stream-experimental_2.11" % "2.0.3",
  "org.twitter4j" % "twitter4j-core" % "4.0.4",
  "org.twitter4j" % "twitter4j-stream" % "4.0.4"
)

