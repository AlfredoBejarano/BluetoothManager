package me.alfredobejarano.bluethootmanager

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import androidx.test.runner.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 *
 * UI test class for the [MainActivity],
 * this class will test the app flows.
 *
 * @author Alfredo Bejarano
 * @since November 07, 2018 - 13:18
 * @version 1.0
 **/
@RunWith(AndroidJUnit4::class)
class MainActivityTest {
    /**
     * Rule that creates the MainActivity.
     */
    @get:Rule
    val activityTestRule = ActivityTestRule(MainActivity::class.java)

    @get:Rule
    val permissionRule: GrantPermissionRule =
        GrantPermissionRule.grant(android.Manifest.permission.ACCESS_COARSE_LOCATION)

    /**
     * Test that requesting permissions works.
     */
    @Test
    fun openApp_withPermissions_CheckDeviceList_SortList_Test() {
        // Wait for 5 seconds to discover devices.
        Thread.sleep(5000)
        // Assert if the SnackBar displayed.
        onView(withId(R.id.discovered_device_list))
            .check(matches(isDisplayed()))
            .perform(swipeDown())
            .perform(swipeDown())
            .perform(swipeUp())
    }

    @Test
    fun openApp_GoToStoredDeviceList_Test() {
        onView(withId(R.id.my_devices))
            .perform(click())
        // Wait for 8 seconds for the network to respond.
        Thread.sleep(8000)
        // Sort the list
        onView(withId(R.id.sort_by_date))
            .perform(click())
        // Swipe the list.
        onView(withId(R.id.stored_device_list))
            .check(matches(isDisplayed()))
            .perform(swipeDown())
    }
}