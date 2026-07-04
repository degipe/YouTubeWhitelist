package io.github.degipe.youtubewhitelist.core.network.util

import retrofit2.Response

/**
 * Closes the error body of an unsuccessful Retrofit response.
 *
 * Per OkHttp's contract, an HTTP response body must always be consumed or closed —
 * even on error — or the underlying connection is never released back to the pool,
 * leaking a connection/file descriptor. Retrofit's converter already consumes the
 * body for successful responses, so this is a no-op in that case.
 */
fun <T> Response<T>.closeOnError() {
    if (!isSuccessful) errorBody()?.close()
}
