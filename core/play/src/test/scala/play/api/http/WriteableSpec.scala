/*
 * Copyright (C) from 2022 The Play Framework Contributors <https://github.com/playframework>, 2011-2021 Lightbend Inc. <https://www.lightbend.com>
 */

package play.api.http

import java.io.File

import akka.util.ByteString
import org.specs2.mutable.Specification
import play.api.libs.Files.TemporaryFile
import play.api.mvc.Codec
import play.api.mvc.MultipartFormData
import play.api.mvc.MultipartFormData.FilePart

import play.api.libs.Files.SingletonTemporaryFileCreator._

class WriteableSpec extends Specification {
  "Writeable" in {
    "of multipart" should {
      "work for temporary files" in {
        val multipartFormData = createMultipartFormData[TemporaryFile](
          create(new File("src/test/resources/multipart-form-data-file.txt").toPath)
        )
        val contentType = Some("text/plain")
        val codec       = Codec.utf_8

        val writeable               = Writeable.writeableOf_MultipartFormData(codec, contentType)
        val transformed: ByteString = writeable.transform(multipartFormData)

        transformed.utf8String must contain("Content-Disposition: form-data; name=name")
        transformed.utf8String must contain(
          """Content-Disposition: form-data; name="thefile"; filename="something.text""""
        )
        transformed.utf8String must contain("Content-Type: text/plain")
        transformed.utf8String must contain("multipart-form-data-file")
      }

      "work composing with another writeable" in {
        val multipartFormData = createMultipartFormData[String]("file part value")
        val contentType       = Some("text/plain")
        val codec             = Codec.utf_8

        val writeable = Writeable.writeableOf_MultipartFormData(
          codec,
          Writeable[FilePart[String]]((f: FilePart[String]) => codec.encode(f.ref), contentType)
        )
        val transformed: ByteString = writeable.transform(multipartFormData)

        transformed.utf8String must contain("Content-Disposition: form-data; name=name")
        transformed.utf8String must contain(
          """Content-Disposition: form-data; name="thefile"; filename="something.text""""
        )
        transformed.utf8String must contain("Content-Type: text/plain")
        transformed.utf8String must contain("file part value")
      }

      "use multipart/form-data content-type" in {
        val contentType = Some("text/plain")
        val codec       = Codec.utf_8
        val writeable = Writeable.writeableOf_MultipartFormData(
          codec,
          Writeable[FilePart[String]]((f: FilePart[String]) => codec.encode(f.ref), contentType)
        )

        writeable.contentType must beSome(startWith("multipart/form-data; boundary="))
      }
    }

    "of urlEncodedForm" should {
      "encode keys and values" in {
        val codec                   = Codec.utf_8
        val writeable               = Writeable.writeableOf_urlEncodedForm(codec)
        val transformed: ByteString = writeable.transform(Map("foo$bar" -> Seq("ba$z")))

        transformed.utf8String must contain("foo%24bar=ba%24z")
      }
    }
  }

  def createMultipartFormData[A](ref: A): MultipartFormData[A] = {
    MultipartFormData[A](
      dataParts = Map(
        "name" -> Seq("value")
      ),
      files = Seq(
        FilePart[A](
          key = "thefile",
          filename = "something.text",
          contentType = Some("text/plain"),
          ref = ref
        )
      ),
      badParts = Seq.empty
    )
  }
}
