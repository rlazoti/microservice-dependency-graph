package io.github.rlazoti.servicestats

import akka.http.scaladsl.Http
import io.github.rlazoti.servicestats.services.CallService
import io.github.rlazoti.servicestats.utils.ActorMaterializerProvider

object WebServer extends App with CallService with ActorMaterializerProvider {
  //TODO use config or args to get the host and port's value
  Http().bindAndHandle(routes, "localhost", 8080)
}
