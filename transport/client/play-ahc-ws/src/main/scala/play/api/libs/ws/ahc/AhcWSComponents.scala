/*
 * Copyright (C) from 2022 The Play Framework Contributors <https://github.com/playframework>, 2011-2021 Lightbend Inc. <https://www.lightbend.com>
 */

package play.api.libs.ws.ahc

import akka.stream.Materializer
import play.api._
import play.api.inject.ApplicationLifecycle
import play.api.libs.ws.WSClient

import scala.concurrent.ExecutionContext

/**
 * AsyncHttpClient WS API implementation components.
 */
trait AhcWSComponents {
  def environment: Environment

  def configuration: Configuration

  def applicationLifecycle: ApplicationLifecycle

  def materializer: Materializer

  def executionContext: ExecutionContext

  lazy val wsClient: WSClient = {
    implicit val mat    = materializer
    implicit val ec     = executionContext
    val asyncHttpClient = new AsyncHttpClientProvider(environment, configuration, applicationLifecycle).get
    new AhcWSClientProvider(asyncHttpClient).get
  }
}
