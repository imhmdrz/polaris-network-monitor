package ir.uni.polaris.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ir.uni.polaris.data.storage.MeasurementsDao
import ir.uni.polaris.data.storage.PolarisDatabase
import ir.uni.polaris.data.storage.TestsDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDb(@ApplicationContext ctx: Context): PolarisDatabase =
        Room.databaseBuilder(ctx, PolarisDatabase::class.java, "polaris.db")
            .fallbackToDestructiveMigration(true)
            .build()

    @Provides
    @Singleton
    fun provideMeasurementsDao(db: PolarisDatabase): MeasurementsDao = db.measurementsDao()

    @Provides
    @Singleton
    fun provideTestDao(db: PolarisDatabase): TestsDao = db.testsDao()
}
