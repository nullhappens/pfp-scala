package com.nullhappens

import cats.MonadError

package object services {
  type MonadThrow[F[_]] = MonadError[F, Throwable]
}

