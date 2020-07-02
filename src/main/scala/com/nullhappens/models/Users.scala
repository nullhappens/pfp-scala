package com.nullhappens.models

trait Users[F[_]] {
  def find(username: UserName, password: Password): F[Option[User]]
  def create(username: UserName, password: Password): F[UserId]
}

case class User(userId: UserId, username: UserName, password: Password)
