package io.github.rlazoti.servicestats.services

import akka.http.scaladsl.model.StatusCodes
import io.github.rlazoti.servicestats.repositories._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.server.Directives._
import spray.json._

// Exposes the API endpoint
trait CallService extends CallRepository with EnableCORSDirectives {

  val routes =
    (enableCORS & path("calling") & post & entity(as[ServiceCall])) { call =>
      /**
        * The method **addServiceCalling**  will run within another Future,
        * so this way the service will return instantly without wait its result.
        * It's basically a request and forget method, so the clients doesn't need
        * to wait for the process time.
        */
      addServiceCalling(call)
      complete(StatusCodes.OK)
    }~
    (enableCORS & path("graphdata") & get) {
      parameters('time) { time =>
        complete(getGraph(time).map(_.toJson))
      }
    }

}
