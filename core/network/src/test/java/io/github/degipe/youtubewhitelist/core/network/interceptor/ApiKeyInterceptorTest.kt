package io.github.degipe.youtubewhitelist.core.network.interceptor

import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import org.junit.Test

class ApiKeyInterceptorTest {

    private val apiKey = "test-api-key-123"
    private val interceptor = ApiKeyInterceptor(apiKey)

    @Test
    fun `interceptor adds api key to request`() {
        val requestSlot = slot<Request>()
        val chain = mockk<Interceptor.Chain>()
        val response = mockk<Response>()
        val originalRequest = Request.Builder()
            .url("https://www.googleapis.com/youtube/v3/channels")
            .build()

        every { chain.request() } returns originalRequest
        every { chain.proceed(capture(requestSlot)) } returns response

        interceptor.intercept(chain)

        val modifiedUrl = requestSlot.captured.url
        assertThat(modifiedUrl.queryParameter("key")).isEqualTo(apiKey)
        assertThat(modifiedUrl.toString()).contains("key=test-api-key-123")
    }

    @Test
    fun `interceptor preserves existing query parameters`() {
        val requestSlot = slot<Request>()
        val chain = mockk<Interceptor.Chain>()
        val response = mockk<Response>()
        val originalRequest = Request.Builder()
            .url("https://www.googleapis.com/youtube/v3/channels?part=snippet&id=UC123")
            .build()

        every { chain.request() } returns originalRequest
        every { chain.proceed(capture(requestSlot)) } returns response

        interceptor.intercept(chain)

        val modifiedUrl = requestSlot.captured.url
        assertThat(modifiedUrl.queryParameter("key")).isEqualTo(apiKey)
        assertThat(modifiedUrl.queryParameter("part")).isEqualTo("snippet")
        assertThat(modifiedUrl.queryParameter("id")).isEqualTo("UC123")
    }
}
