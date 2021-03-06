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

description :=
  """
    |SBT plugin allowing to send reports to a swift (openstack) bucket
  """.stripMargin

scalaVersion := "2.12.12"

sbtPlugin := true

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

startYear := Some(2018)

organizationHomepage := Some(url("https://make.org"))
homepage := Some(url("https://gitlab.com/makeorg/devtools/sbt-swift-plugin"))

scalastyleConfig := baseDirectory.value / "scalastyle-config.xml"

libraryDependencies ++= Seq(
  "org.apache.tika" % "tika-core" % "1.26",
  "org.make" %% "openstack-swift-client" % "1.1.0",
  "com.typesafe" % "config" % "1.4.1",
  "com.whisk" %% "docker-testkit-scalatest" % "0.9.9" % "test",
  ("com.whisk" %% "docker-testkit-impl-docker-java" % "0.9.9" % "test").exclude("log4j", "log4j"),
  "org.scalatest"              %% "scalatest"                       % "3.2.7"              % Test,
  "org.mockito"                %% "mockito-scala"                   % "1.16.33"            % Test,
  "org.slf4j" % "slf4j-simple" % "1.7.30" % "test",
  "com.typesafe.scala-logging" %% "scala-logging"                   % "3.9.3"              % Test
)
