package ir.uni.polaris.data.storage

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TestsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDefinition(e: TestDefinitionEntity): Long

    @Query("SELECT * FROM test_definitions")
    fun observeDefinitions(): Flow<List<TestDefinitionEntity>>

    @Query("SELECT * FROM test_definitions WHERE enabled = 1")
    suspend fun getEnabledDefinitionsOnce(): List<TestDefinitionEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertResult(e: TestResultEntity)

    @Query(
        """
  SELECT * FROM test_results
  WHERE definitionId = :defId
  ORDER BY timeMillis DESC
  LIMIT 1
"""
    )
    fun observeLatestResult(defId: Long): Flow<TestResultEntity?>
}
