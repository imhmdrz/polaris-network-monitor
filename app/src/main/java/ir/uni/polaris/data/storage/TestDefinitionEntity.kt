package ir.uni.polaris.data.storage

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "test_definitions")
data class TestDefinitionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String,
    val target: String,
    val intervalMillis: Long,
    val payloadBytes: Int?,
    val enabled: Boolean
)

@Entity(tableName = "test_results")
data class TestResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val definitionId: Long,
    val timeMillis: Long,
    val value: Double,
    val unit: String,
    val ok: Boolean,
    val note: String?
)
