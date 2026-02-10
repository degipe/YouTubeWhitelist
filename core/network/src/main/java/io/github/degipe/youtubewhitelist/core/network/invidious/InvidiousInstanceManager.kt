package io.github.degipe.youtubewhitelist.core.network.invidious

class InvidiousInstanceManager(
    private val instances: List<String> = DEFAULT_INSTANCES,
    private val healthResetMs: Long = HEALTH_RESET_MS,
    private val timeProvider: () -> Long = { System.currentTimeMillis() }
) {
    private data class InstanceHealth(
        var consecutiveFailures: Int = 0,
        var lastFailureTime: Long = 0
    )

    private val healthMap = mutableMapOf<String, InstanceHealth>()
    private var currentIndex = 0

    @Synchronized
    fun getHealthyInstance(): String? {
        val now = timeProvider()
        // Reset instances that have been unhealthy long enough
        healthMap.forEach { (_, health) ->
            if (health.consecutiveFailures >= MAX_FAILURES &&
                now - health.lastFailureTime >= healthResetMs
            ) {
                health.consecutiveFailures = 0
            }
        }

        // Find first healthy instance starting from currentIndex
        for (i in instances.indices) {
            val index = (currentIndex + i) % instances.size
            val instance = instances[index]
            val health = healthMap[instance]
            if (health == null || health.consecutiveFailures < MAX_FAILURES) {
                currentIndex = (index + 1) % instances.size
                return "https://$instance"
            }
        }
        return null
    }

    @Synchronized
    fun reportFailure(baseUrl: String) {
        val host = baseUrl.removePrefix("https://").removePrefix("http://")
        val health = healthMap.getOrPut(host) { InstanceHealth() }
        health.consecutiveFailures++
        health.lastFailureTime = timeProvider()
    }

    @Synchronized
    fun reportSuccess(baseUrl: String) {
        val host = baseUrl.removePrefix("https://").removePrefix("http://")
        healthMap[host]?.consecutiveFailures = 0
    }

    companion object {
        private const val MAX_FAILURES = 2
        private const val HEALTH_RESET_MS = 5 * 60 * 1000L // 5 minutes

        val DEFAULT_INSTANCES = listOf(
            "vid.puffyan.us",
            "yewtu.be",
            "invidious.namazso.eu",
            "inv.nadeko.net"
        )
    }
}
