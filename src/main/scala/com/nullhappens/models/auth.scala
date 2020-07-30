package com.nullhappens.models

import scala.util.control.NoStackTrace

import eu.timepit.refined.types.string.NonEmptyString
import io.estatico.newtype.macros.newtype

object auth {
  @newtype case class UserNameParam(value: NonEmptyString) {
    def toDomain: UserName = UserName(value.value.toLowerCase)
  }

  @newtype case class PasswordParam(value: NonEmptyString) {
    def toDomain: Password = Password(value.value)
  }

  case class UserNameInUse(username: UserName) extends NoStackTrace
  case class InvalidUserOrPassword(username: UserName) extends NoStackTrace
  case object UnsupportedOperation extends NoStackTrace

  case object TokenNotFound extends NoStackTrace
  case class LoginUser(username: UserNameParam, password: PasswordParam)
}
