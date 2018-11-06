package me.alfredobejarano.bluethootmanager.utilities

import java.util.concurrent.Executors

/**
 *
 * Utils class that provides easy to use
 * usage of worker threads for long operations.
 *
 * @author Alfredo Bejarano
 * @since November 06, 2018 - 13:10
 * @version 1.0
 **/

/**
 * Single threaded executor that will do a given job
 * in a worker thread using the execute() function.
 */
private val SINGLE_THREAD_EXECUTOR = Executors.newSingleThreadExecutor()

/**
 * Executes a block of code in a worker thread.
 * @param f the code to be executed in a worker thread.
 */
fun runOnIOThread(f: () -> Unit) = SINGLE_THREAD_EXECUTOR.execute(f)
