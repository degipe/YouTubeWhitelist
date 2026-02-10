package io.github.degipe.youtubewhitelist.core.data.model

data class PaginatedPlaylistResult(
    val videos: List<PlaylistVideo>,
    val nextPageToken: String?
)
