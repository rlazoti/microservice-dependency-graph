package io.github.rlazoti.servicestats.infrastructure

import io.github.rlazoti.servicestats.utils.ExecutionContextProvider
import redis.RedisClient

trait Redis extends ExecutionContextProvider {
  //TODO use config to get host and port from a properties file
  private val redisHost = "localhost"
  private val redisPort = 6379
  implicit val client = RedisClient(host = redisHost, port = redisPort)
}
