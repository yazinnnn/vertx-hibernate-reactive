package l.y.reactive.database

import io.smallrye.mutiny.Uni
import io.vertx.core.Launcher
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.jsonObjectOf
import io.vertx.kotlin.core.json.obj
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.await
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.asExecutor
import l.y.reactive.database.entity.Book
import org.hibernate.reactive.mutiny.Mutiny
import org.slf4j.LoggerFactory
import java.util.*
import javax.persistence.Persistence
import kotlin.random.Random

class DatabaseVerticle : CoroutineVerticle() {
  private lateinit var emf: Mutiny.SessionFactory
  private val logger by lazy { LoggerFactory.getLogger(this::class.java) }
  private val executor by lazy { vertx.dispatcher().asExecutor() }
  private val eventBus by lazy { vertx.eventBus() }
  override suspend fun start() {
    emf = initEmf()
    logger.info("emf init succeed!")
    createEventbusConsumer()
    repeat(10) {
      val message = eventBus.request<String>("book.post", json {
        obj(
          "price" to Random.nextDouble(),
          "name" to UUID.randomUUID().toString(),
          "isbn" to UUID.randomUUID().toString()
        )
      }).await().body()
      logger.info("post reply -> $message")
    }

    val message = eventBus.request<String>("book.get", jsonObjectOf()).await().body()
    logger.info("get reply -> $message")

  }

  private suspend fun initEmf(): Mutiny.SessionFactory {
    return vertx.executeBlocking<Mutiny.SessionFactory> {
      runCatching {
        val factory = Persistence.createEntityManagerFactory("postgresql-example")
          .unwrap(Mutiny.SessionFactory::class.java)
        it.complete(factory)
      }.onFailure { t ->
        it.fail(t)
      }
    }.await()
  }

  private fun createEventbusConsumer() {
    eventBus.consumer<JsonObject>("book.get") {
      emf.withSession { session ->
        session.createQuery<Book>("from Book").resultList
      }.onItem().call { books ->
        it.reply(books.toString())
        logger.info("reply it !$books")
        Uni.createFrom().voidItem()
      }.emitOn(executor).subscribe().with { }
    }
    eventBus.consumer<JsonObject>("book.post") {
      val book = it.body().mapTo(Book::class.java)
      emf.withSession { session ->
        session.persist(book)
          .invoke { -> it.reply(book.toString()) }
          .call(session::flush)
      }.emitOn(executor).subscribe().with { }
    }
  }
}


fun main() {
  Launcher.main(arrayOf("run", DatabaseVerticle::class.qualifiedName))
}
