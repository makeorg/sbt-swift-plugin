/*
 * Copyright 2018 Make.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

organization := "org.make"
name := "sbt-swift-plugin"
version := "1.0.3-SNAPSHOT"

description :=
  """
    |SBT plugin allowing to send reports to a swift (openstack) bucket
  """.stripMargin

scalaVersion := "2.12.6"

sbtPlugin := true

publishMavenStyle := false

licenses += "Apache-2.0" -> url("https://www.apache.org/licenses/LICENSE-2.0.html")

developers := List(
  Developer(
    id = "flaroche",
    name = "François LAROCHE",
    email = "fl@make.org",
    url = url("https://github.com/larochef")
  ),
  Developer(
    id = "cpestoury",
    name = "Charley PESTOURY",
    email = "cp@make.org",
    url = url("https://gitlab.com/cpestoury")
  ),
  Developer(
    id = "csalmon-legagneur",
    name = "Colin SALMON-LEGAGNEUR",
    email = "salmonl.colin@gmail.com",
    url = url("https://gitlab.com/csalmon-")
  ),
  Developer(
    id = "pda",
    name = "Philippe de ARAUJO",
    email = "pa@make.org",
    url = url("https://gitlab.com/philippe.da")
  )
)

scmInfo := Some(ScmInfo(
  browseUrl = url("https://gitlab.com/makeorg/devtools/sbt-swift-plugin"),
  connection = "scm:git:git://gitlab.com:makeorg/devtools/sbt-swift-plugin.git",
  devConnection = Some("scm:git:ssh://gitlab.com:makeorg/devtools/sbt-swift-plugin.git")))


bintrayOrganization := Some("make-org")
bintrayRepository := "public"

scalastyleConfig := baseDirectory.value / "scalastyle-config.xml"

libraryDependencies ++= Seq(
  "org.apache.tika" % "tika-core" % "1.19.1",
  "org.make" %% "openstack-swift-client" % "1.0.3",
  "com.typesafe" % "config" % "1.3.2",
  "com.typesafe.akka" %% "akka-actor" % "2.5.17",
  "com.typesafe.akka" %% "akka-stream" % "2.5.17",
  "com.typesafe.akka" %% "akka-http" % "10.1.5",
  "com.whisk" %% "docker-testkit-scalatest" % "0.9.6" % "test",
  ("com.whisk" %% "docker-testkit-impl-docker-java" % "0.9.6" % "test").exclude("log4j", "log4j"),
  "org.mockito" % "mockito-core" % "2.13.0" % "test",
  "org.slf4j" % "slf4j-simple" % "1.7.25" % "test",
)
