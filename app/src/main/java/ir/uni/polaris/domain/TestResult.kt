package ir.uni.polaris.domain

import ir.uni.polaris.data.storage.TestResultEntity
import ir.uni.polaris.model.TestResult


fun TestResultEntity.toDomain() = TestResult(
    id = id,
    definitionId = definitionId,
    timeMillis = timeMillis,
    value = value,
    unit = unit,
    ok = ok,
    note = note
)

fun TestResult.toEntity() = TestResultEntity(
    id = id,
    definitionId = definitionId,
    timeMillis = timeMillis,
    value = value,
    unit = unit,
    ok = ok,
    note = note
)