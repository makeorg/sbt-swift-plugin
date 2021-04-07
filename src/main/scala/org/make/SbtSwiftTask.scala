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

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import com.typesafe.config.ConfigFactory
import org.make.SendSwiftFiles.{listSubfiles, sendFiles}
import org.make.swift.SwiftClient
import org.make.swift.model.Bucket

import scala.concurrent.duration.DurationInt
import java.io.File
import scala.concurrent.Await
import scala.util.{Failure, Success, Try}

object SbtSwiftTask extends App {

  val swiftConfigurationPath = args(0)
  val swiftContainerName = args(1)
  val swiftReportsToSendPath = args(2)
  val swiftReportsDirectory = {
    if (args.length >= 4 && args(3).nonEmpty) {
      args(3) + "/"
    } else {
      ""
    }
  }
  val configuration =
    ConfigFactory.load(
      ConfigFactory.parseFile(new File(swiftConfigurationPath))
    )
  val actorSystem = ActorSystem[Nothing](Behaviors.empty, "SbtSwift", configuration)

  Try {
    val client = SwiftClient.create(actorSystem)
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

    Await.result(sendFiles(client,
      bucket,
      filesToSend,
      baseDirectory,
      swiftReportsDirectory),
      30.minutes)
  } match {
    case Success(_) =>
      actorSystem.terminate()
      System.exit(0)
    case Failure(e) =>
      e.printStackTrace()
      actorSystem.terminate()
      System.exit(1)
  }
}
