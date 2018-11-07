package me.alfredobejarano.bluethootmanager.utilities

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