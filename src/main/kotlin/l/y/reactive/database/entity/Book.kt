package l.y.reactive.database.entity

import io.smallrye.mutiny.Uni
import io.smallrye.mutiny.subscription.UniSubscriber
import io.smallrye.mutiny.subscription.UniSubscription
import org.hibernate.Hibernate
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import javax.persistence.*

@Entity
@Table(schema = "reactive")
data class Book(
  @Id @GeneratedValue(strategy = GenerationType.AUTO) val id: Long? = null,
  @Column(nullable = true) val price: Double,
  val name: String,
  val isbn: String
) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
    other as Book

    return id != null && id == other.id
  }

  override fun hashCode(): Int = javaClass.hashCode()

  @Override
  override fun toString(): String {
    return this::class.simpleName + "(id = $id , price = $price , name = $name , isbn = $isbn )"
  }
}


fun main() {
  val executor = Executors.newSingleThreadExecutor()
  println(Thread.currentThread().name +"1")
  Uni.createFrom().item("aa")
    .emitOn(executor)
    .subscribe()
    .withSubscriber(object : UniSubscriber<String> {
      override fun onSubscribe(subscription: UniSubscription?) {
        println("sub $this ${Thread.currentThread().name}3")
      }
      override fun onItem(item: String?) {
        println("item $item ${Thread.currentThread().name}4")
      }
      override fun onFailure(failure: Throwable?) {
        println("fail $this ${Thread.currentThread().name}5")
      }
    })
  println(Thread.currentThread().name +"2")
  CountDownLatch(1).await()
}
