package me.alfredobejarano.bluethootmanager.utilities

import dagger.Component
import me.alfredobejarano.bluethootmanager.DeviceDiscoverFragment
import me.alfredobejarano.bluethootmanager.DeviceListFragment
import me.alfredobejarano.bluethootmanager.MainActivity
import me.alfredobejarano.bluethootmanager.data.DeviceDao
import me.alfredobejarano.bluethootmanager.data.DeviceRepository
import me.alfredobejarano.bluethootmanager.data.DeviceService
import me.alfredobejarano.bluethootmanager.viewmodel.DeviceDiscoverViewModel
import me.alfredobejarano.bluethootmanager.viewmodel.DeviceListViewModel
import javax.inject.Singleton

/**
 *
 * Interface to be implemented by dagger,
 * this interface defines the dependency
 * graph for the application.
 *
 * @author Alfredo Bejarano
 * @since November 06, 2018 - 19:09
 * @version 1.0
 **/
@Singleton
@Component(
    modules = [RetrofitModule::class,
        DeviceRepositoryModule::class,
        ViewModelFactoryModule::class]
)
interface AppComponent {
    /**
     * Provides dependency inversion for a [MainActivity] object.
     */
    fun inject(mainActivity: MainActivity)

    /**
     * Provides dependency inversion for a [DeviceDiscoverFragment] object.
     */
    fun inject(deviceDiscoverFragment: DeviceDiscoverFragment)

    /**
     * Provides dependency inversion for a [DeviceListFragment] object.
     */
    fun inject(deviceListFragment: DeviceListFragment)

    /**
     * This function will tell the dagger implementation
     * of this interface how to provide injection for
     * a [DeviceDao] implementation.
     */
    fun provideDeviceService(): DeviceService

    /**
     * This function will tell the dagger implementation
     * of this interface how to provide injection for
     * a [DeviceRepository] object.
     */
    fun provideDeviceRepository(): DeviceRepository

    /**
     * This function will tell the dagger implementation
     * of this interface how to provide injection for
     * a [DeviceListViewModel.Factory] object.
     */
    fun provideDeviceListViewModelFactory(): DeviceListViewModel.Factory

    /**
     * This function will tell the dagger implementation
     * of this interface how to provide injection for
     * a [DeviceDiscoverViewModel.Factory] object.
     */
    fun provideDeviceDiscoverViewModelFactory(): DeviceDiscoverViewModel.Factory
}