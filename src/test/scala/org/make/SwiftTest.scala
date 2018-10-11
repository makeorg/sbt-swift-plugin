package org.make

import java.nio.file.{Files, Path}

import akka.actor.ActorSystem
import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.StrictLogging
import org.make.swift.SwiftClient
import org.make.swift.model.Bucket
import org.scalatest.concurrent.PatienceConfiguration.Timeout

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

class SwiftTest extends DockerSwiftAllInOne with MakeTest with StrictLogging {

  var client: SwiftClient = _

  override def externalPort: Option[Int] = Some(8080)

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    startAllOrFail()
    client = SwiftClient.create(SwiftTest.actorSystem)
    Await.result(client.init(), 1.minute)
  }

  override protected def afterAll(): Unit = {
    super.afterAll()
    stopAllQuietly()
  }

  feature("sending files to swift") {
    scenario("sending a directory") {
      val baseDirectory: Path = Files.createTempDirectory("test1")
      Files.write(Files.createFile(baseDirectory.resolve("test-1.json")),
                  "{}".getBytes("UTF-8"))
      Files.write(Files.createFile(baseDirectory.resolve("test-2.json")),
                  "{}".getBytes("UTF-8"))
      val subDirectory = Files.createDirectory(baseDirectory.resolve("deeper"))
      Files.write(Files.createFile(subDirectory.resolve("test-3.json")),
                  "{}".getBytes("UTF-8"))

      // Sleep a but to make sure the files are created
      Thread.sleep(200)

      val target = Bucket(0, 0, "test1")

      val filesToSend = SbtSwift.listSubfiles(baseDirectory.toFile)
      val filesAsString =
        Array("deeper/test-3.json", "test-1.json", "test-2.json")
      filesToSend.sorted.toArray should be(filesAsString)

      whenReady(SbtSwift.sendFiles(logger.info(_),
                                   client,
                                   target,
                                   filesToSend,
                                   baseDirectory.toFile),
                Timeout(30.seconds)) { _ =>
        ()
      }

      whenReady(client.listFiles(target), Timeout(30.seconds)) { files =>
        files.map(_.name).sorted.toArray should be(filesAsString)
      }

      Files.delete(baseDirectory.resolve("test-1.json"))
      Files.delete(baseDirectory.resolve("test-2.json"))
      Files.delete(baseDirectory.resolve("deeper/test-3.json"))
      Files.delete(baseDirectory.resolve("deeper"))
      Files.delete(baseDirectory)
    }

    scenario("Sending a single file") {
      val file = java.io.File.createTempFile("test", ".json")
      val filesToSend = SbtSwift.listSubfiles(file)
      filesToSend.toArray should be(Array(file.getName))

      val target = Bucket(0, 0, "test2")

      whenReady(SbtSwift.sendFiles(logger.info(_),
                                   client,
                                   target,
                                   filesToSend,
                                   file.getParentFile),
                Timeout(30.seconds)) { _ =>
        ()
      }

      whenReady(client.listFiles(target), Timeout(30.seconds)) { files =>
        files.map(_.name).sorted.toArray should be(Array(file.getName))
      }

      file.delete()

    }
  }
}

object SwiftTest {

  val configuration: Config =
    ConfigFactory.load(
      ConfigFactory.parseString(
        """
      |make-openstack {
      |  authentication {
      |    keystone-version = "keystone-V1"
      |    base-url = "http://localhost:8080/auth/v1.0"
      |    tenant-name = "test"
      |    username = "tester"
      |    password = "testing"
      |    region = ""
      |  }
      |
      |  storage {
      |    init-containers = ["test1", "test2"]
      |  }
      |}
    """.stripMargin))

  val actorSystem: ActorSystem = ActorSystem("SwiftTest", configuration)
}
