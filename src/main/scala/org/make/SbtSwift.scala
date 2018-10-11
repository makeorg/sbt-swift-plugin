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

import java.nio.file.Files

import sbt.File
import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import org.make.swift.SwiftClient
import org.make.swift.model.Bucket
import sbt.internal.util.complete.DefaultParsers
import sbt.{AutoPlugin, Command, Def, Keys, Project, State}

import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global

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

  override def globalSettings: Seq[Def.Setting[_]] = {
    Keys.commands += sendReportsCommand
  }

  override def projectSettings: Seq[Def.Setting[_]] = Seq(
    swiftContainerDirectory := None
  )

  private lazy val sendReportsCommand =
    Command("send-reports")(_ => DefaultParsers.EOF) { (state: State, _) =>
      sendReports(state)
      state
    }

  private def sendReports(state: State): Unit = {
    val extracted = Project.extract(state)
    import extracted._

    val configFile =
      swiftConfigurationPath.in(currentRef).get(structure.data).get

    val configuration = ConfigFactory.load(ConfigFactory.parseFile(configFile))
    val client = SwiftClient.create(ActorSystem("SbtSwift", configuration))
    Await.result(client.init(), 20.seconds)

    state.log.info("Swift client initialized successfully, sending reports")

    val bucketName: String =
      swiftContainerName.in(currentRef).get(structure.data).get

    val reportsPath: File =
      swiftReportsToSendPath.in(currentRef).get(structure.data).get

    val bucket = Bucket(0, 0, bucketName)

    val filesToSend: Seq[String] = listSubfiles(reportsPath)

    val baseDirectory = {
      if (reportsPath.isDirectory) {
        reportsPath
      } else {
        reportsPath.getParentFile
      }
    }

    Await.result(
      sendFiles(state.log.info(_), client, bucket, filesToSend, baseDirectory),
      30.minutes)
  }

  def sendFiles(info: => String => Unit,
                client: SwiftClient,
                bucket: Bucket,
                filesToSend: Seq[String],
                baseDirectory: File): Future[Unit] = {

    var future = Future.successful {}
    filesToSend.foreach { fileName =>
      future = future.flatMap { _ =>
        val source = new File(baseDirectory, fileName)
        val contentType =
          Option(Files.probeContentType(source.toPath))
            .getOrElse("application/octet-stream")
        info(s"Sending $fileName")
        client.sendFile(bucket, fileName, contentType, source)
      }
    }
    future
  }

  def listSubfiles(file: File, root: Boolean = true): Seq[String] = {
    if (file.isFile) {
      Seq(file.getName)
    } else {
      file.listFiles().toSeq.flatMap { child =>
        listSubfiles(file = child, root = false).map { name =>
          if (root) {
            name
          } else {
            s"${file.getName}/$name"
          }
        }
      }
    }
  }

}
