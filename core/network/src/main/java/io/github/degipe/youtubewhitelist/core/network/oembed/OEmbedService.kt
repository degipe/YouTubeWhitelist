package io.github.degipe.youtubewhitelist.core.network.oembed

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface OEmbedService {

    @GET("oembed")
    suspend fun getOEmbed(
        @Query("url") url: String,
        @Query("format") format: String = "json"
    ): Response<OEmbedResponse>
}
