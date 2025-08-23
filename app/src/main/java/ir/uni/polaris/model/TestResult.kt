package ir.uni.polaris.model
data class TestResult(
    val id: Long = 0L,
    val definitionId: Long,
    val timeMillis: Long,
    val value: Double,
    val unit: String,
    val ok: Boolean,
    val note: String? = null
)
