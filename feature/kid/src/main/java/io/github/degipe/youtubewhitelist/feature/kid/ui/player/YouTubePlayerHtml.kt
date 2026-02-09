package io.github.degipe.youtubewhitelist.feature.kid.ui.player

object YouTubePlayerHtml {

    fun generate(youtubeVideoId: String): String = """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
            <style>
                * { margin: 0; padding: 0; }
                html, body { width: 100%; height: 100%; background: #000; overflow: hidden; }
                #player { position: absolute; top: 0; left: 0; width: 100%; height: 100%; }
            </style>
        </head>
        <body>
            <div id="player"></div>
            <script>
                var tag = document.createElement('script');
                tag.src = "https://www.youtube.com/iframe_api";
                var firstScriptTag = document.getElementsByTagName('script')[0];
                firstScriptTag.parentNode.insertBefore(tag, firstScriptTag);

                var player;
                function onYouTubeIframeAPIReady() {
                    player = new YT.Player('player', {
                        videoId: '$youtubeVideoId',
                        playerVars: {
                            'autoplay': 1,
                            'controls': 1,
                            'rel': 0,
                            'modestbranding': 1,
                            'iv_load_policy': 3,
                            'playsinline': 1,
                            'fs': 1
                        },
                        events: {
                            'onStateChange': onPlayerStateChange
                        }
                    });
                }

                function onPlayerStateChange(event) {
                    if (event.data == YT.PlayerState.ENDED) {
                        var duration = Math.floor(player.getDuration());
                        AndroidBridge.onVideoEnded(duration);
                    }
                }
            </script>
        </body>
        </html>
    """.trimIndent()
}
