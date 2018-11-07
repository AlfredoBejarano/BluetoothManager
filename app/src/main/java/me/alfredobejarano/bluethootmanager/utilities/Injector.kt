package me.alfredobejarano.bluethootmanager.utilities

import android.app.Application

/**
 *
 * Kotlin object that provides Singleton access for
 * the app dagger component.
 *
 * @author Alfredo Bejarano
 * @since November 06, 2018 - 19:07
 * @version 1.0
 **/
object Injector {
    @Volatile
    private lateinit var mApp: Application

    val component: AppComponent by lazy {
        DaggerAppComponent
            .builder()
            .retrofitModule(RetrofitModule())
            .viewModelFactoryModule(ViewModelFactoryModule())
            .deviceRepositoryModule(DeviceRepositoryModule(mApp))
            .build()
    }

    /**
     * Uses reflection to invoke an inject function for the
     * given object, if this function is not defined in the
     * [AppComponent] interface, it will throw a [RuntimeException].
     */
    fun inject(injectedObject: Any) {
        try {
            component.javaClass.getMethod("inject", injectedObject.javaClass)
                .invoke(component, injectedObject)
        } catch (t: NoSuchMethodException) {
            throw RuntimeException(
                "No inject function found for a " +
                        "${injectedObject.javaClass.name} instance. " +
                        "is it already added in the AppComponent?"
            )
        }
    }

    /**
     * Provides an application to this injector
     * object, so when the component gets
     * requested for the first time it can be created.
     *
     * **Note:** If this function is not called before using the
     * component of this object, will cause a variable not initialized
     * exception.
     */
    fun initialize(app: Application) {
        mApp = app
    }
}