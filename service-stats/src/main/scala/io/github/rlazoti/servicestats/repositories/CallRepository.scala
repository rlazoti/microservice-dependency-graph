package io.github.rlazoti.servicestats.repositories

import akka.util.ByteString
import io.github.rlazoti.servicestats.infrastructure.Redis
import java.text.SimpleDateFormat
import java.util.Calendar
import scala.concurrent.Future
import spray.json.DefaultJsonProtocol
import scala.language.implicitConversions

case class Edge(source: Long, target: Long, rpm: Long)
case class Node(id: Long, name: String, nodeType: String)
case class Graph(nodes: Seq[Node], edges: Seq[Edge])
case class ServiceCall(serviceCaller: String, serviceCalled: String, endpointCalled: String)

trait CallRepository extends Redis with DefaultJsonProtocol {

  private val ServiceLabel = "Service"
  private val EndpointLabel = "Endpoint"

  protected implicit val EdgeFormat = jsonFormat3(Edge)
  protected implicit val NodeFormat = jsonFormat3(Node)
  protected implicit val GraphFormat = jsonFormat2(Graph)
  protected implicit val serviceCallFormat = jsonFormat3(ServiceCall)

  private def getCurrentTime() = {
    val timeFormat = new SimpleDateFormat("HH.mm")
    timeFormat.format(Calendar.getInstance().getTime())
  }

  private def defineNodeType(value: String) =
    if (value.split("/").length > 1) EndpointLabel
    else ServiceLabel

  private def extractEndpoint(value: String) =
    value.split("/")(1)

  private implicit def byteStringToLong(value: ByteString) =
    value.utf8String.toLong

  private def nextNodeIndex =
    client.incr("nodes:last:id")

  private def nodeExists(time: String, name: String) =
    client.hexists(generateNodesKey(time), name)

  private def getNodeIndex(time: String, name: String) =
    client.hget(generateNodesKey(time), name)

  private def addNewNode(time: String, name: String, value: Long) =
    client.hset(generateNodesKey(time), name, value).map {
      case true => value
      case _ => -1
    }

  private def getAllNodesFieldsAndValues(time: String) =
    client.hgetall(generateNodesKey(time))

  private def getAllEdgesFieldsAndValues(time: String) =
    client.hgetall(generateEdgeKey(time))

  private def generateNodesKey(time: String) =
    s"nodes:$time"

  private def generateEdgeKey(time: String) =
    s"edges:$time"

  private def generateEdgeField(sourceId: Long, targetId: Long) =
    s"$sourceId:$targetId"

  private def generateFullEndpoint(service: String, endpoint: String) =
    s"$service/$endpoint"

  private def addCallerEdge(time: String, sourceId: Long, targetId: Long) =
    client.hincrby(generateEdgeKey(time), generateEdgeField(sourceId, targetId), 1)

  private def addEndpointEdge(time: String, sourceId: Long, targetId: Long) =
    client.hsetnx(generateEdgeKey(time), generateEdgeField(sourceId, targetId), 0)

  private def getEdges(time: String): Future[Seq[Edge]] = {
    getAllEdgesFieldsAndValues(time).map { entries =>
      entries.toSeq.map { tuple =>
        val edgeData = tuple._1.split(":")
        Edge(edgeData(0).toLong, edgeData(1).toLong, byteStringToLong(tuple._2))
      }
    }
  }

  /**
   * Store a microservice call using graph concepts like node (vertex) and link (edge)
   * A microservice request contains:
   *   - the microservice caller's name
   *   - the microservice called name
   *   - the microservice called endpoint's name
   **/
  def addServiceCalling(call: ServiceCall): Future[Unit] = {
    val time = getCurrentTime();

    for {
      serviceCallerIndex <- addNode(time, call.serviceCaller)
      calledServiceIndex <- addNode(time, call.serviceCalled)
      calledEndpointIndex <- addNode(time, generateFullEndpoint(call.serviceCalled, call.endpointCalled))
      _ <- addEndpointEdge(time, calledEndpointIndex, calledServiceIndex)
      _ <- addCallerEdge(time, serviceCallerIndex, calledEndpointIndex)
    } yield Future {}
  }

  // Try to add a new node and return its index or return the index of an existent node
  def addNode(time: String, name: String): Future[Long] = {
    getNodeIndex(time, name).flatMap {
      case None => nextNodeIndex.flatMap(addNewNode(time, name, _))
      case Some(index) => Future { byteStringToLong(index) }
    }
  }

  // Get all nodes from an specific time
  def getNodes(time: String): Future[Seq[Node]] = {
    getAllNodesFieldsAndValues(time).map { entries =>
      entries.toSeq.map { tuple =>
        defineNodeType(tuple._1) match {
          case ServiceLabel => Node(tuple._2, tuple._1, ServiceLabel)
          case EndpointLabel => Node(tuple._2, extractEndpoint(tuple._1), EndpointLabel)
        }
      }
    }
  }

  // Build a graph object with all nodes and edges for an specific time
  def getGraph(time: String): Future[Graph] = {
    for {
      nodes <- getNodes(time)
      edges <- getEdges(time)
    } yield Graph(nodes, edges)
  }

}
