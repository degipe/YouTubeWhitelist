package io.github.degipe.youtubewhitelist.core.auth.pin

interface PinHasher {
    fun hash(pin: String): String
    fun verify(pin: String, encoded: String): Boolean
}
