package com.nullhappens.models

import skunk._
import cats.implicits._
import skunk.implicits._
import skunk.codec.all._
import com.nullhappens.models.skunkx._
import com.nullhappens.effect.BracketThrow
import com.nullhappens.effect.GenUUID
import com.nullhappens.security.Crypto
import com.nullhappens.http.auth.User
import cats.effect.Resource
import com.nullhappens.http.auth.UserNameInUse
import cats.effect.Sync

trait Users[F[_]] {
  def find(username: UserName, password: Password): F[Option[User]]
  def create(username: UserName, password: Password): F[UserId]
}

object LiveUsers {
  def make[F[_]: Sync](
      sessionPool: Resource[F, Session[F]],
      crypto: Crypto
    ): F[Users[F]] =
    Sync[F].delay(
      new LiveUsers[F](sessionPool, crypto: Crypto)
    )
}

private final class LiveUsers[F[_]: BracketThrow: GenUUID] private (
    sessionPool: Resource[F, Session[F]],
    crypto: Crypto)
  extends Users[F] {

  import UserQueries._

  def find(username: UserName, password: Password): F[Option[User]] =
    sessionPool.use(_.prepare(selectUser).use { ps =>
      ps.option(username).map {
        case Some(u ~ p) if p.value == crypto.encrypt(password).value => u.some
        case _ => none[com.nullhappens.http.auth.User]
      }
    })

  def create(username: UserName, password: Password): F[UserId] =
    sessionPool.use(_.prepare(insertUser).use { cmd =>
      GenUUID[F].make[UserId].flatMap { id =>
        cmd
          .execute(
            com.nullhappens.http.auth
              .User(id, username) ~ crypto
              .encrypt(password)
          )
          .as(id)
          .handleErrorWith {
            case SqlState.UniqueViolation(_) =>
              UserNameInUse(username).raiseError[F, UserId]
          }
      }
    })

}

private object UserQueries {
  val codec: Codec[com.nullhappens.http.auth.User ~ EncryptedPassword] =
    (
      uuid.cimap[UserId] ~ varchar.cimap[UserName] ~ varchar
        .cimap[EncryptedPassword]
    ).imap {
      case i ~ n ~ p =>
        com.nullhappens.http.auth.User(i, n) ~ p
    } { case u ~ p => u.id ~ u.name ~ p }

  val selectUser
      : Query[UserName, com.nullhappens.http.auth.User ~ EncryptedPassword] =
    sql"""
      SELECT * FROM users
      WHERE name = ${varchar.cimap[UserName]}
    """.query(codec)

  val insertUser: Command[com.nullhappens.http.auth.User ~ EncryptedPassword] =
    sql"""
      INSERT INTO users
      VALUES ($codec)
    """.command
}
