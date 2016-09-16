package lib

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsyncClient
import com.gu.scanamo.query._
import com.gu.scanamo.syntax._
import com.gu.scanamo.{Scanamo, ScanamoAsync, Table}
import model.YouTubeLiveStream

import scala.concurrent.ExecutionContext.Implicits.global

sealed abstract class DataStoreError(val msg: String) extends Exception(msg)
case object ReadError extends DataStoreError("Read error")

object DataStore {
  private lazy val client = Config.awsRegion.createClient(
    classOf[AmazonDynamoDBAsyncClient],
    Config.awsCredentialsProviderChain,
    null
  )

  private def key(id: String) = UniqueKey(KeyEquals('id, id))

  def get(id: String): Option[YouTubeLiveStream] = {
    Scanamo.get[YouTubeLiveStream](client)(Config.dynamoTableName)(key(id)).flatMap(_.toOption)
  }

  def create(stream: YouTubeLiveStream) = {
    ScanamoAsync.put[YouTubeLiveStream](client)(Config.dynamoTableName)(stream)
  }

  def list(): List[YouTubeLiveStream] = {
    Scanamo.scan[YouTubeLiveStream](client)(Config.dynamoTableName).flatMap(_.toOption)
  }

  def update(stream: YouTubeLiveStream) = {
    val table = Table[YouTubeLiveStream](Config.dynamoTableName)

    val ops = for {
      _ <- table.update(key(stream.id), set('status -> true))
      updated <- table.get(key(stream.id))
    } yield updated

    Scanamo.exec(client)(ops)
  }
}
