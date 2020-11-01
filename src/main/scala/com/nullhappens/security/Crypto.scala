package com.nullhappens.security

import com.nullhappens.models.Password
import com.nullhappens.models.EncryptedPassword

trait Crypto {
  def encrypt(value: Password): EncryptedPassword
  def decrypt(value: EncryptedPassword): Password
}
