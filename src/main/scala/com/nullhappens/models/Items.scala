package com.nullhappens.models

import cats.effect.{Resource, Sync}
import cats.implicits._
import eu.timepit.refined.types.numeric.PosInt
import skunk._
import skunk.codec.all._
import skunk.implicits._
import squants.market.{Money, USD}

import com.nullhappens.effect.GenUUID
import com.nullhappens.models.skunkx._

trait Items[F[_]] {
  def findAll: F[List[Item]]
  def findBy(brand: BrandName): F[List[Item]]
  def findById(itemId: ItemId): F[Option[Item]]
  def create(item: CreateItem): F[Unit]
  def update(item: UpdateItem): F[Unit]
}

case class Item(
    uuid: ItemId,
    name: ItemName,
    description: ItemDescription,
    price: Money,
    brand: Brand,
    category: Category)

case class CreateItem(
    name: ItemName,
    description: ItemDescription,
    price: Money,
    brandId: BrandId,
    categoryId: CategoryId)

case class UpdateItem(id: ItemId, price: Money)

final class LiveItems[F[_]: Sync] private (sessionPool: Resource[F, Session[F]])
  extends Items[F] {
  import ItemQueries._

  def findAll: F[List[Item]] = sessionPool.use(_.execute(selectAll))

  def findBy(brand: BrandName): F[List[Item]] =
    sessionPool.use {
      _.prepare(selectByBrand).use { ps =>
        ps.stream(brand, 1024).compile.toList
      }
    }

  // Excercise
  def findBy(brand: BrandName, pageSize: PosInt): F[List[Item]] =
    sessionPool.use(_.prepare(selectByBrand).use { ps =>
      ps.cursor(brand).use { cur =>
        cur.fetch(pageSize.value).map(_._1)
      }
    })

  def findById(itemId: ItemId): F[Option[Item]] =
    sessionPool.use(_.prepare(selectById).use { ps =>
      ps.option(itemId)
    })

  def create(item: CreateItem): F[Unit] =
    sessionPool.use(_.prepare(insertItem).use { cmd =>
      GenUUID[F].make[ItemId].flatMap(id => cmd.execute(id ~ item).void)
    })

  def update(item: UpdateItem): F[Unit] =
    sessionPool.use(_.prepare(updateItem).use { cmd =>
      cmd.execute(item).void
    })
}

object LiveItems {
  def make[F[_]: Sync](sessionPool: Resource[F, Session[F]]): F[Items[F]] =
    Sync[F].delay(new LiveItems[F](sessionPool))
}

private object ItemQueries {
  val decoder: Decoder[Item] = (
    uuid ~ varchar ~ varchar ~ numeric ~ uuid ~ varchar ~ uuid ~ varchar
  ).map {
    case i ~ n ~ d ~ p ~ bi ~ bn ~ ci ~ cn =>
      Item(
        ItemId(i),
        ItemName(n),
        ItemDescription(d),
        USD(p),
        Brand(BrandId(bi), BrandName(bn)),
        Category(CategoryId(ci), CategoryName(cn))
      )
  }

  val selectAll: Query[Void, Item] = sql"""
      SELECT i.uuid, i.name, i.description, i.price, b.uuid, b.name, c.uuid, c.name
      FROM items AS i
      INNER JOIN brands AS b ON i.brand_id = b.uuid
      INNER JOIN categories AS c ON i.category_id = c.uuid
    """.query(decoder)

  val selectByBrand: Query[BrandName, Item] = sql"""
      SELECT i.uuid, i.name, i.description, i.price, b.uuid, b.name, c.uuid, c.name
      FROM items AS i
      INNER JOIN brands AS b ON i.brand_id = b.uuid
      INNER JOIN categories AS c ON i.category_id = c.uuid WHERE b.name LIKE ${varchar
    .cimap[BrandName]}
    """.query(decoder)

  val selectById: Query[ItemId, Item] = sql"""
      SELECT i.uuid, i.name, i.description, i.price, b.uuid, b.name, c.uuid, c.name
      FROM items AS i
      INNER JOIN brands AS b ON i.brand_id = b.uuid
      INNER JOIN categories AS c ON i.category_id = c.uuid WHERE i.uuid = ${uuid
    .cimap[ItemId]}
    """.query(decoder)

  val insertItem: Command[ItemId ~ CreateItem] = sql"""
      INSERT INTO items
      VALUES ($uuid, $varchar, $varchar, $numeric, $uuid, $uuid) """.command
    .contramap {
      case id ~ i =>
        id.value ~ i.name.value ~ i.description.value ~ i.price.amount ~ i.brandId.value ~ i.categoryId.value
    }

  val updateItem: Command[UpdateItem] = sql"""
      UPDATE items
      SET price = $numeric
      WHERE uuid = ${uuid.cimap[ItemId]}
    """.command.contramap(i => i.price.amount ~ i.id)
}
