package ir.uni.polaris.data.storage

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        MeasurementEntity::class,
        TestDefinitionEntity::class,
        TestResultEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class PolarisDatabase : RoomDatabase() {
    abstract fun measurementsDao(): MeasurementsDao
    abstract fun testsDao(): TestsDao
}
