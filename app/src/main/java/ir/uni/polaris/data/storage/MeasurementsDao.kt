package ir.uni.polaris.data.storage

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MeasurementsDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: MeasurementEntity)

    @Query("SELECT * FROM measurements ORDER BY timeMillis DESC")
    fun observeAll(): Flow<List<MeasurementEntity>>
}
