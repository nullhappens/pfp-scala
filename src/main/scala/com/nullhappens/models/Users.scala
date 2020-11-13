package com.nullhappens.models

import cats.effect.Resource
import cats.effect.Sync
import cats.Functor
import cats.implicits._
import dev.profunktor.redis4cats.RedisCommands
import io.circe.parser.decode
import pdi.jwt.JwtClaim
import skunk._
import skunk.codec.all._
import skunk.implicits._

import com.nullhappens.effect.BracketThrow
import com.nullhappens.effect.GenUUID
import com.nullhappens.http.auth.User
import com.nullhappens.http.auth.UserNameInUse
import com.nullhappens.http.auth.users
import com.nullhappens.http.auth.users.CommonUser
import com.nullhappens.models.skunkx._
import com.nullhappens.security.Crypto
import com.nullhappens.http.json._
import com.nullhappens.http.auth.users.AdminUser
import cats.Applicative

trait Users[F[_]] {
  def find(username: UserName, password: Password): F[Option[User]]
  def create(username: UserName, password: Password): F[UserId]
}

trait UsersAuth[F[_], A] {
  def findUser(token: JwtToken)(claim: JwtClaim): F[Option[A]]
}

private final class LiveAdminAuth[F[_]: Applicative](
    adminToken: JwtToken,
    adminUser: AdminUser)
  extends UsersAuth[F, AdminUser] {
  def findUser(
      token: com.nullhappens.models.JwtToken
    )(claim: JwtClaim
    ): F[Option[users.AdminUser]] =
    // Same definition but more verbose
    // (if (token == adminToken)
    //    Some(adminUser)
    //  else
    //    None).pure[F]
    (token == adminToken).guard[Option].as(adminUser).pure[F]
}

private final class LiveUsersAuth[F[_]: Functor](
    redis: RedisCommands[F, String, String])
  extends UsersAuth[F, CommonUser] {

  def findUser(token: JwtToken)(claim: JwtClaim): F[Option[users.CommonUser]] =
    redis
      .get(token.value)
      .map(_.flatMap { u =>
        decode[User](u).toOption.map(CommonUser.apply)
      })
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
