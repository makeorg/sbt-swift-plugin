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

import org.apache.tika.Tika
import org.make.swift.SwiftClient
import org.make.swift.model.Bucket

import java.io.File
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object SendSwiftFiles {
  private val tika: Tika = new Tika()

  def sendFiles(
    client: SwiftClient,
    bucket: Bucket,
    filesToSend: Seq[String],
    baseDirectory: File,
    pathPrefix: String
  ): Future[Unit] = {

    var future = Future.successful {}
    filesToSend.foreach { fileName =>
      future = future.flatMap { _ =>
        val source = new File(baseDirectory, fileName)
        val contentType = tika.detect(source)
        println(s"Sending $fileName")
        client.sendFile(bucket, pathPrefix + fileName, contentType, source)
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
