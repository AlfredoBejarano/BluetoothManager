package me.alfredobejarano.bluethootmanager.utilities

import android.content.Context
import androidx.annotation.StringRes
import me.alfredobejarano.bluethootmanager.data.DeviceRepository
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

/**
 *
 * Utilities for displaying and managing text.
 *
 * @author Alfredo Bejarano
 * @since November 06, 2018 - 21:46
 * @version 1.0
 **/
/**
 * Formats a given string using a given set of parameters.
 */
fun formatString(string: String, vararg params: Any?) =
    String.format(Locale.getDefault(), string, params)

/**
 * Formats a given string resource Id with a set of parameters.
 */
fun formatString(ctx: Context, @StringRes resource: Int, vararg params: Any?) =
    formatString(ctx.getString(resource), params)

/**
 * Formats a string into a desired pattern, if it
 * fails it returns the String itself.
 */
fun String.fromTimeStamp(pattern: String): String {
    val locale = Locale.getDefault()
    val format = SimpleDateFormat(DeviceRepository.TIMESTAMP_FORMAT, locale)
    val displayFormat = SimpleDateFormat(pattern, locale)
    return try {
        displayFormat.format(format.parse(this))
    } catch (t: ParseException) {
        this
    }
}