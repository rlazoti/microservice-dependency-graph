package io.github.rlazoti.servicestats.utils

import akka.actor.ActorSystem

trait ActorSystemProvider {
  implicit val appActorSystem: ActorSystem = ActorSystem("service-stats")
}
