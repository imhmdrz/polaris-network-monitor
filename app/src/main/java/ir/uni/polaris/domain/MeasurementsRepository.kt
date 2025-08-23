package ir.uni.polaris.domain

import ir.uni.polaris.model.Measurement
import kotlinx.coroutines.flow.Flow

interface MeasurementsRepository {
    val measurements: Flow<List<Measurement>>
    fun start(intervalMillis: Long = 5000L)
    fun stop()
}

