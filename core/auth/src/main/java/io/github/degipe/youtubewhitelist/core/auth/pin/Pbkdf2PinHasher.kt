package io.github.degipe.youtubewhitelist.core.auth.pin

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.inject.Inject

class Pbkdf2PinHasher @Inject constructor() : PinHasher {

    companion object {
        private const val ALGORITHM = "PBKDF2WithHmacSHA256"
        private const val ITERATIONS = 120_000
        private const val KEY_LENGTH_BITS = 256
        private const val SALT_LENGTH_BYTES = 16
    }

    override fun hash(pin: String): String {
        val salt = ByteArray(SALT_LENGTH_BYTES).also { SecureRandom().nextBytes(it) }
        val hash = deriveKey(pin, salt)
        val saltB64 = Base64.getEncoder().encodeToString(salt)
        val hashB64 = Base64.getEncoder().encodeToString(hash)
        return "$saltB64:$hashB64"
    }

    override fun verify(pin: String, encoded: String): Boolean {
        val parts = encoded.split(":")
        if (parts.size != 2) return false
        val salt = Base64.getDecoder().decode(parts[0])
        val expectedHash = Base64.getDecoder().decode(parts[1])
        val actualHash = deriveKey(pin, salt)
        return MessageDigest.isEqual(actualHash, expectedHash)
    }

    private fun deriveKey(pin: String, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(pin.toCharArray(), salt, ITERATIONS, KEY_LENGTH_BITS)
        val factory = SecretKeyFactory.getInstance(ALGORITHM)
        return factory.generateSecret(spec).encoded
    }
}
