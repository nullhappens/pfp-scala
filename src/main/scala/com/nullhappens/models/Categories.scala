package com.nullhappens.models

trait Categories[F[_]] {
  def findAll: F[List[Category]]
  def create(name: CategoryName): F[Unit]
}

case class Category(uuid: CategoryId, name: CategoryName)
