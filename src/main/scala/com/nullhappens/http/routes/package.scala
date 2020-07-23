package com.nullhappens.http

import io.estatico.newtype.macros.newtype
import eu.timepit.refined.types.string.NonEmptyString
import com.nullhappens.models._

package object routes {
  @newtype case class BrandParam(value: NonEmptyString) {
    def toDomain: BrandName = BrandName(value.value.toLowerCase.capitalize)
  }
}
