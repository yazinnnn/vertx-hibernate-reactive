package l.y.reactive.database.entity

import org.hibernate.Hibernate
import javax.persistence.*
import javax.persistence.CascadeType.PERSIST

@Entity
@Table(schema = "reactive")
data class Author(
  @Id @GeneratedValue val id: Int,
  val name: String,
  @OneToMany(cascade = [PERSIST]) val books: MutableList<Book> = ArrayList()
) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
    other as Author
    return id == other.id
  }

  override fun hashCode(): Int = javaClass.hashCode()

  @Override
  override fun toString(): String {
    return this::class.simpleName + "(id = $id , name = $name )"
  }

}
