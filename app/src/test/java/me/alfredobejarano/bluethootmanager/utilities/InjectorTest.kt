package me.alfredobejarano.bluethootmanager.utilities

import android.app.Application
import me.alfredobejarano.bluethootmanager.DeviceDiscoverFragment
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/**
 * Test suite for the [Injector] object.
 *
 * @author Alfredo Bejarano
 * @version 1.0
 * @since November 06, 2018 - 19:53
 */
@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class)
class InjectorTest {
    @Before
    fun setup() {
        Injector.initialize(RuntimeEnvironment.application)
    }

    /**
     * Asserts that the inject function fails when initializing
     * the Injector object and passing an object that don't have its
     * inject function in the app component.
     */
    @Test(expected = RuntimeException::class)
    fun inject_NoFunctionFound() {
        Injector.inject(this)
    }

    /**
     * Asserts that the inject function succeeds when initializing
     * the Injector object and passing an object that contains its
     * inject function in the app component.
     */
    @Test()
    fun inject_functionForClassFound() {
        Injector.inject(mock(DeviceDiscoverFragment::class.java))
    }
}