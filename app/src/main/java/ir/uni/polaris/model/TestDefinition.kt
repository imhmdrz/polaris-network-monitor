package ir.uni.polaris.model

data class TestDefinition(
    val id: Long = 0L,
    val type: TestType,
    val target: String,
    val intervalMillis: Long,
    val payloadBytes: Int? = null,
    val enabled: Boolean = true
)