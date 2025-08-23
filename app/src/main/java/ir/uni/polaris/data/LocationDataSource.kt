package ir.uni.polaris.data

import ir.uni.polaris.model.GeoPoint
import kotlinx.coroutines.flow.Flow

interface LocationDataSource {
    fun locationFlow(intervalMillis: Long = 5000L): Flow<GeoPoint>
}
