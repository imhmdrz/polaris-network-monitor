package ir.uni.polaris.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ir.uni.polaris.data.LocationDataSource
import ir.uni.polaris.data.LocationDataSourceImpl
import ir.uni.polaris.data.TelephonyDataSource
import ir.uni.polaris.data.TelephonyDataSourceImpl
import ir.uni.polaris.domain.AuthRepository
import ir.uni.polaris.domain.AuthRepositoryImpl
import ir.uni.polaris.domain.MeasurementsRepository
import ir.uni.polaris.domain.MeasurementsRepositoryImpl
import ir.uni.polaris.domain.NetworkTestsRepository
import ir.uni.polaris.domain.NetworkTestsRepositoryImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class BindingsModule {

    @Binds
    @Singleton
    abstract fun bindRepo(impl: MeasurementsRepositoryImpl): MeasurementsRepository

    @Binds
    @Singleton
    abstract fun bindTestsRepo(impl: NetworkTestsRepositoryImpl): NetworkTestsRepository

    @Binds
    @Singleton
    abstract fun bindLoc(impl: LocationDataSourceImpl): LocationDataSource

    @Binds
    @Singleton
    abstract fun bindTel(impl: TelephonyDataSourceImpl): TelephonyDataSource

    @Binds
    @Singleton
    abstract fun bindAuthRepo(impl: AuthRepositoryImpl): AuthRepository

}