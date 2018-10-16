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

import java.io.File
import java.nio.file.Files

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import org.make.swift.SwiftClient
import org.make.swift.model.Bucket
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global

object SendSwiftFiles extends App {
  val swiftConfigurationPath = args(0)
  val swiftContainerName = args(1)
  val swiftReportsToSendPath = args(2)

  val configuration = ConfigFactory.load(
    ConfigFactory
      .parseFile(new File(swiftConfigurationPath)))

  val client = SwiftClient.create(ActorSystem("SbtSwift", configuration))
  Await.result(client.init(), 20.seconds)

  val bucket = Bucket(0, 0, swiftContainerName)

  val reportsPath = new File(swiftReportsToSendPath)
  val filesToSend: Seq[String] = listSubfiles(reportsPath)

  val baseDirectory = {
    if (reportsPath.isDirectory) {
      reportsPath
    } else {
      reportsPath.getParentFile
    }
  }

  Await.result(sendFiles(client, bucket, filesToSend, baseDirectory),
               30.minutes)

  def sendFiles(client: SwiftClient,
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
        println(s"Sending $fileName")
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
