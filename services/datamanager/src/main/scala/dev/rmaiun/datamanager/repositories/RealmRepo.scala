package dev.rmaiun.datamanager.repositories

import cats.Monad
import dev.rmaiun.datamanager.db.entities.Realm
import dev.rmaiun.datamanager.db.queries.RealmQueries
import doobie.ConnectionIO

trait RealmRepo[F[_]] {
  def getById(id: Long): ConnectionIO[Option[Realm]]
  def getByName(name: String): ConnectionIO[Option[Realm]]
  def create(realm: Realm): ConnectionIO[Realm]
  def update(realm: Realm): ConnectionIO[Realm]
  def listAll: ConnectionIO[List[Realm]]
  def removeN(idList: List[Long] = Nil): ConnectionIO[Int]
}

object RealmRepo {
  def apply[F[_]](implicit ev: RealmRepo[F]): RealmRepo[F] = ev

  def impl[F[_]: Monad]: RealmRepo[F] = new RealmRepo[F] {
    override def getById(id: Long): ConnectionIO[Option[Realm]] = RealmQueries.getById(id).option

    override def create(realm: Realm): ConnectionIO[Realm] = RealmQueries
      .insert(realm)
      .withUniqueGeneratedKeys[Long]("id")
      .map(id => realm.copy(id = id))

    override def update(realm: Realm): ConnectionIO[Realm] = RealmQueries
      .update(realm)
      .run
      .map(_ => realm)

    override def listAll: ConnectionIO[List[Realm]] = RealmQueries.listAll.to[List]

    override def removeN(idList: List[Long]): ConnectionIO[Int] = RealmQueries.deleteByIdList(idList).run

    override def getByName(name: String): ConnectionIO[Option[Realm]] = RealmQueries.getByName(name).option
  }
}
