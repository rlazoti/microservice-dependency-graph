package io.github.rlazoti.servicestats.utils

import akka.stream.ActorMaterializer

trait ActorMaterializerProvider extends ExecutionContextProvider {
  implicit val materializer: ActorMaterializer = ActorMaterializer()
}
