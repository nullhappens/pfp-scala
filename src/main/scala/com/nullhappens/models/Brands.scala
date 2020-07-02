package com.nullhappens.models

import io.estatico.newtype.macros.newtype

trait Brands[F[_]] {
  def findAll: F[List[Brand]]
  def create(name: BrandName): F[Unit]
}

case class Brand(uuid: BrandId, name: BrandName)
