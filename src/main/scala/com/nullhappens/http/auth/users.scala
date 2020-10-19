package com.nullhappens.http.auth

import scala.util.control.NoStackTrace

import dev.profunktor.auth.jwt.JwtSymmetricAuth
import eu.timepit.refined.types.string.NonEmptyString
import io.estatico.newtype.macros.newtype

import com.nullhappens.http.auth.users.PasswordParam
import com.nullhappens.http.auth.users.UserNameParam
import com.nullhappens.models._

object users {
  @newtype case class AdminJwtAuth(value: JwtSymmetricAuth)
  @newtype case class UserJwtAuth(value: JwtSymmetricAuth)
  @newtype case class CommonUser(value: User)
  @newtype case class AdminUser(value: User)
  @newtype case class UserNameParam(value: NonEmptyString) {
    def toDomain: UserName = UserName(value.value.toLowerCase)
  }

  @newtype case class PasswordParam(value: NonEmptyString) {
    def toDomain: Password = Password(value.value)
  }

}

final case class User(id: UserId, name: UserName)
final case class CreateUser(username: UserNameParam, password: PasswordParam)
final case class LoginUser(username: UserNameParam, password: PasswordParam)
final case class UserNameInUse(username: UserName) extends NoStackTrace
final case class InvalidUserOrPassword(username: UserName) extends NoStackTrace
final case object UnsupportedOperation extends NoStackTrace
final case object TokenNotFound extends NoStackTrace
