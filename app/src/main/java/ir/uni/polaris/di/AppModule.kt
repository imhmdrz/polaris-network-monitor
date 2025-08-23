package ir.uni.polaris.di

import android.content.Context
import android.telephony.TelephonyManager
import com.google.android.gms.location.LocationServices
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFusedClient(@ApplicationContext ctx: Context) =
        LocationServices.getFusedLocationProviderClient(ctx)

    @Provides
    @Singleton
    fun provideTelephony(@ApplicationContext ctx: Context): TelephonyManager =
        ctx.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

    @Provides
    @Singleton
    fun provideOkHttp(): OkHttpClient = OkHttpClient.Builder().build()
}