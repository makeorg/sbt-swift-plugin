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

package org.make

import java.net.URLClassLoader

import sbt.Keys._
import sbt.OutputStrategy.LoggedOutput
import sbt.util.Logger
import sbt.{AutoPlugin, Def, File, Fork, ForkOptions}

object SbtSwift extends AutoPlugin {

  object autoImport {
    val swiftConfigurationPath = sbt.settingKey[File]("Swift configuration")
    val swiftContainerName =
      sbt.settingKey[String]("Swift container / bucket name")
    val swiftReportsToSendPath = sbt.settingKey[File](
      "Directory containing the files to send or single file to send")
    val swiftContainerDirectory =
      sbt.settingKey[Option[String]]("Destination directory for the sent files")

    val swiftSendReports =
      sbt.taskKey[Unit]("Send reports to configured OpenStack bucket")
  }

  import autoImport._

  override def projectSettings: Seq[Def.Setting[_]] = Seq(
    swiftContainerDirectory := None,
    swiftSendReports := {
      val logger: Logger = streams.value.log
      val configFile =
        swiftConfigurationPath.value

      val bucketName: String =
        swiftContainerName.value

      val reportsPath: File =
        swiftReportsToSendPath.value

      val containerDirectory = swiftContainerDirectory.value

      val classpath: Array[File] = getClasspathString
      val fork = new Fork("java", Some("org.make.SendSwiftFiles"))

      val options =
        Seq(configFile.getAbsolutePath,
            bucketName,
            reportsPath.getAbsolutePath,
            containerDirectory.getOrElse(""))

      logger.debug(
        s"Calling SendSwiftFiles with options: ${options.mkString("[", ", ", "]")}")

      val forkProcessOptions =
        ForkOptions()
          .withOutputStrategy(LoggedOutput(logger))
          .withBootJars(classpath.toVector)

      fork(forkProcessOptions, options)
    }
  )

  def getClasspathString: Array[File] = {
    val applicationClassLoader = getClass.getClassLoader
    val urls = applicationClassLoader.asInstanceOf[URLClassLoader].getURLs
    urls.map(url => new File(url.getFile))
  }
}
