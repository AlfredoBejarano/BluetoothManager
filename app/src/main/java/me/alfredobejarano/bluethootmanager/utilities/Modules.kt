package me.alfredobejarano.bluethootmanager.utilities

import android.bluetooth.BluetoothAdapter
import android.content.Context
import dagger.Module
import dagger.Provides
import me.alfredobejarano.bluethootmanager.BuildConfig
import me.alfredobejarano.bluethootmanager.data.AppDatabase
import me.alfredobejarano.bluethootmanager.data.DeviceRepository
import me.alfredobejarano.bluethootmanager.data.DeviceService
import me.alfredobejarano.bluethootmanager.viewmodel.DeviceDiscoverViewModel
import me.alfredobejarano.bluethootmanager.viewmodel.DeviceListViewModel
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 *
 * Kotlin file that contains all the classes
 * that defines the modules for dagger.
 *
 * @author Alfredo Bejarano
 * @since November 06, 2018 - 19:10
 * @version 1.0
 **/
@Module
class RetrofitModule {
    /**
     * Tells to dagger how to provide injection for a [DeviceService] object.
     */
    @Provides
    @Singleton
    fun provideDeviceService(): DeviceService = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .client(createOkHttpClient())
        .baseUrl(BuildConfig.BASE_URL)
        .build()
        .create(DeviceService::class.java)

    /**
     * Creates an [OkHttpClient] object to be
     * added to a [Retrofit] client.
     */
    private fun createOkHttpClient() = OkHttpClient.Builder()
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .connectTimeout(10, TimeUnit.SECONDS)
        .also {
        if (BuildConfig.DEBUG) {
            it.addInterceptor(HttpLoggingInterceptor().apply {
                this.level = HttpLoggingInterceptor.Level.BODY
            })
        }
    }.build()
}

@Module
class DeviceRepositoryModule(private val ctx: Context) {
    /**
     * Tells to dagger how to provide injection
     * for a [DeviceRepository] object.
     */
    @Provides
    @Singleton
    fun provideDeviceRepository() =
        DeviceRepository(
            ctx = ctx,
            adapter = BluetoothAdapter.getDefaultAdapter(),
            dao = AppDatabase.getInstance(ctx).getDeviceDao(),
            service = Injector.component.provideDeviceService(),
            sharedPreferences = ctx.getSharedPreferences(
                "${BuildConfig.APPLICATION_ID}_cache_file",
                Context.MODE_PRIVATE
            )
        )
}

@Module
class ViewModelFactoryModule {
    /**
     * Tells to dagger how to provide injection
     * for a [DeviceDiscoverViewModel.Factory] object.
     */
    @Provides
    fun provideDeviceDiscoverViewModelFactory() =
        DeviceDiscoverViewModel.Factory(Injector.component.provideDeviceRepository())

    /**
     * Tells to dagger how to provide injection
     * for a [DeviceListViewModel.Factory] object.
     */
    @Provides
    fun provideDeviceListViewModelFactory() =
        DeviceListViewModel.Factory(Injector.component.provideDeviceRepository())
}