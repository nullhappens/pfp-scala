package com.nullhappens.models

import cats.effect.{ Resource, Sync }
import cats.implicits._
import skunk._
import skunk.codec.all._
import skunk.implicits._

import com.nullhappens.effect.{ BracketThrow, GenUUID }
import com.nullhappens.models.skunkx._

trait Categories[F[_]] {
  def findAll: F[List[Category]]
  def create(name: CategoryName): F[Unit]
}

case class Category(uuid: CategoryId, name: CategoryName)

private final class LiveCategories[F[_]: BracketThrow: GenUUID] private (
    sessionPool: Resource[F, Session[F]])
  extends Categories[F] {
  import CategoryQueries._

  def findAll: F[List[Category]] = sessionPool.use(_.execute(selectAll))

  def create(name: com.nullhappens.models.CategoryName): F[Unit] =
    sessionPool.use { session =>
      session.prepare(insertCategory).use { cmd =>
        GenUUID[F].make[CategoryId].flatMap { id =>
          cmd.execute(Category(id, name)).void
        }
      }
    }
}

object LiveCategories {
  def make[F[_]: Sync](sessionPool: Resource[F, Session[F]]): F[Categories[F]] =
    Sync[F].delay(new LiveCategories[F](sessionPool))
}

private object CategoryQueries {

  val codec: Codec[Category] =
    (uuid.cimap[CategoryId] ~ varchar.cimap[CategoryName]).imap {
      case i ~ n => Category(i, n)
    }(c => c.uuid ~ c.name)

  val selectAll: Query[Void, Category] =
    sql""" SELECT * FROM categories """.query(codec)

  val insertCategory: Command[Category] =
    sql""" INSERT INTO categories VALUES ($codec) """.command
}
