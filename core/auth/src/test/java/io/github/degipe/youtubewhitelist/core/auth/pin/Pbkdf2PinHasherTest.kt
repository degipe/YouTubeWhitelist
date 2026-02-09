package io.github.degipe.youtubewhitelist.core.auth.pin

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

class Pbkdf2PinHasherTest {

    private lateinit var hasher: Pbkdf2PinHasher

    @Before
    fun setUp() {
        hasher = Pbkdf2PinHasher()
    }

    @Test
    fun `hash returns encoded string in salt colon hash format`() {
        val result = hasher.hash("1234")
        val parts = result.split(":")
        assertThat(parts).hasSize(2)
        assertThat(parts[0]).isNotEmpty()
        assertThat(parts[1]).isNotEmpty()
    }

    @Test
    fun `hash produces different salts for same pin`() {
        val hash1 = hasher.hash("1234")
        val hash2 = hasher.hash("1234")
        assertThat(hash1).isNotEqualTo(hash2)
    }

    @Test
    fun `verify returns true for correct pin`() {
        val encoded = hasher.hash("5678")
        assertThat(hasher.verify("5678", encoded)).isTrue()
    }

    @Test
    fun `verify returns false for incorrect pin`() {
        val encoded = hasher.hash("1234")
        assertThat(hasher.verify("9999", encoded)).isFalse()
    }

    @Test
    fun `verify returns false for tampered hash`() {
        val encoded = hasher.hash("1234")
        val tampered = encoded.replaceFirst("A", "B")
        // Tampered hash may fail to decode or produce wrong result
        // Either way, verify should not return true
        val result = try {
            hasher.verify("1234", tampered)
        } catch (_: Exception) {
            false
        }
        // If the salt part was tampered, the hash won't match
        // If the hash part was tampered, it also won't match
        // The only false positive case is if the tampered character wasn't actually changed
        // which is extremely unlikely with the replacement
        assertThat(result).isFalse()
    }

    @Test
    fun `hash supports 4 digit pin`() {
        val encoded = hasher.hash("0000")
        assertThat(hasher.verify("0000", encoded)).isTrue()
    }

    @Test
    fun `hash supports 6 digit pin`() {
        val encoded = hasher.hash("123456")
        assertThat(hasher.verify("123456", encoded)).isTrue()
    }

    @Test
    fun `verify is case sensitive for different pins`() {
        val encoded = hasher.hash("1234")
        assertThat(hasher.verify("1235", encoded)).isFalse()
        assertThat(hasher.verify("2234", encoded)).isFalse()
        assertThat(hasher.verify("123", encoded)).isFalse()
        assertThat(hasher.verify("12345", encoded)).isFalse()
    }
}
