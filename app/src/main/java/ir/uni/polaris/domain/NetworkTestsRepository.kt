package ir.uni.polaris.domain

import ir.uni.polaris.model.TestDefinition
import ir.uni.polaris.model.TestResult
import kotlinx.coroutines.flow.Flow

interface NetworkTestsRepository {
    fun observeDefinitions(): Flow<List<TestDefinition>>
    suspend fun upsertDefinition(def: TestDefinition): Long
    fun observeLatestResult(definitionId: Long): Flow<TestResult?>

    fun start()
    fun stop()
}
