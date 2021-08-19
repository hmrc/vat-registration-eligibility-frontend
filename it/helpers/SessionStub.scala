package helpers

import org.scalatest.BeforeAndAfterEach
import play.api.libs.json.{DefaultReads, Format, Json}
import repositories.SessionRepository
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.mongo.MongoSpecSupport

import scala.concurrent.ExecutionContext.Implicits.global

trait SessionStub extends MongoSpecSupport with BeforeAndAfterEach with DefaultReads {
  self: IntegrationSpecBase =>

  lazy val repo = app.injector.instanceOf[SessionRepository]

  override def beforeEach(): Unit = {
    super.beforeEach()
    await(repo.drop)
    await(repo.count) mustBe 0
    resetWiremock()
  }

  def verifySessionCacheData[T](id: String, key: String, data: Option[T])(implicit format: Format[T]): Unit ={
    val dataFromDb = await(repo.get(id)).flatMap(_.getEntry[T](key))
    if (data != dataFromDb) throw new Exception(s"Data in database doesn't match expected data:\n expected data $data was not equal to actual data $dataFromDb")
  }

  def cacheSessionData[T](id: String, key: String, data: T)(implicit format: Format[T]): Unit ={
    await(repo.count)
    val cacheMap = await(repo.get(id))
    val updatedCacheMap =
      cacheMap.fold(CacheMap(id, Map(key -> Json.toJson(data))))(map => map.copy(data = map.data + (key -> Json.toJson(data))))

    await(repo.upsert(updatedCacheMap))
  }
}