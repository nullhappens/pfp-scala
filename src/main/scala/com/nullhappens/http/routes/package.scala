package com.nullhappens.http

import eu.timepit.refined.types.string.NonEmptyString
import io.estatico.newtype.macros.newtype

import com.nullhappens.models._

package object routes {
  @newtype case class BrandParam(value: NonEmptyString) {
    def toDomain: BrandName = BrandName(value.value.toLowerCase.capitalize)
  }
}
