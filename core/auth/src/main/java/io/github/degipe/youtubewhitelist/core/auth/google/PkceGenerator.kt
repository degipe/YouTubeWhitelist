package io.github.degipe.youtubewhitelist.core.auth.google

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

/**
 * PKCE (Proof Key for Code Exchange, RFC 7636) verifier/challenge pair.
 *
 * Used to turn the app into a public OAuth client that needs no embedded
 * client secret: the [verifier] is kept in memory for the lifetime of the
 * sign-in attempt and sent to the token endpoint, while the [challenge]
 * (its SHA-256 hash) is sent to the authorization endpoint up front.
 */
data class PkcePair(val verifier: String, val challenge: String)

object PkceGenerator {

    private const val VERIFIER_BYTE_LENGTH = 32
    private val secureRandom = SecureRandom()

    /**
     * Generates a new random [PkcePair].
     *
     * verifier: base64url-no-padding encoding of [VERIFIER_BYTE_LENGTH] random bytes
     * (43 chars for 32 bytes, satisfying RFC 7636's 43-128 char requirement).
     *
     * challenge: base64url-no-padding encoding of SHA-256(verifier), per the
     * "S256" code_challenge_method.
     */
    fun generate(): PkcePair {
        val randomBytes = ByteArray(VERIFIER_BYTE_LENGTH)
        secureRandom.nextBytes(randomBytes)
        val verifier = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes)

        val digest = MessageDigest.getInstance("SHA-256")
            .digest(verifier.toByteArray(Charsets.US_ASCII))
        val challenge = Base64.getUrlEncoder().withoutPadding().encodeToString(digest)

        return PkcePair(verifier = verifier, challenge = challenge)
    }
}
