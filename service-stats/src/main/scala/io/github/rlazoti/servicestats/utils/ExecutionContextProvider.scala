package io.github.rlazoti.servicestats.utils

import scala.concurrent.ExecutionContext

trait ExecutionContextProvider extends ActorSystemProvider {
  implicit val executionContext: ExecutionContext = appActorSystem.dispatcher
}
