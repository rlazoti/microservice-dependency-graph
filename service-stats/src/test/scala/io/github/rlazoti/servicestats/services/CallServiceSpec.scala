package io.github.rlazoti.servicestats.services

import akka.http.scaladsl.model._
import akka.http.scaladsl.model.ContentTypes._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.util.ByteString
import org.scalatest._

class CallServiceSpec extends WordSpec with Matchers with ScalatestRouteTest {

  val API = new Object with CallService

  "CallService API" should {

    "Send POST to /calling should add the CallService" in {
      val jsonRequest = ByteString(
        s"""
        |{
        |"serviceCaller":"checkout",
        |"serviceCalled":"users",
        |"endpointCalled":"findById"
        |}
        """.stripMargin)

      val postRequest = HttpRequest(
        HttpMethods.POST,
        uri = "/calling",
        entity = HttpEntity(MediaTypes.`application/json`, jsonRequest))

      postRequest ~> API.routes ~> check {
        status.isSuccess() shouldEqual true
      }
    }

    // This test needs a Redis instance running...
    "Send GET to /graphdata?time=01.01 should return the graph nodes ands links for that time" in {
      val getRequest = HttpRequest(
        HttpMethods.GET,
        uri = "/graphdata?time=01.01"
      )

      getRequest ~> API.routes ~> check {
        status.isSuccess() shouldEqual true
        contentType shouldBe `application/json`
      }
    }

    "Send GET to /graphdata should be rejected by a missing parameter 'time'" in {
      val getRequest = HttpRequest(
        HttpMethods.GET,
        uri = "/graphdata"
      )

      getRequest ~> API.routes ~> check {
        handled shouldBe false
        rejections.size shouldBe 1
      }
    }

  }

}
