import ir.uni.polaris.data.storage.TestDefinitionEntity
import ir.uni.polaris.model.TestDefinition
import ir.uni.polaris.model.TestType

fun TestDefinitionEntity.toDomain() = TestDefinition(
    id = id,
    type = TestType.valueOf(type),
    target = target,
    intervalMillis = intervalMillis,
    payloadBytes = payloadBytes,
    enabled = enabled
)

fun TestDefinition.toEntity() = TestDefinitionEntity(
    id = id,
    type = type.name,
    target = target,
    intervalMillis = intervalMillis,
    payloadBytes = payloadBytes,
    enabled = enabled
)
