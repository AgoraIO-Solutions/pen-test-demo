package io.agora.myapplication.utils

import android.util.Log
import kotlinx.coroutines.CoroutineExceptionHandler

private var LogLevel = android.util.Log.WARN

interface EZLogger {
    private val TAG: String get() = javaClass.simpleName

    fun error(msg: String) {
        if (LogLevel <= Log.ERROR) Log.e(TAG, msg)
    }

    fun info(msg: String) {
        if (LogLevel <= Log.INFO) Log.i(TAG, msg)
    }

    fun debug(msg: String) {
        if (LogLevel <= Log.DEBUG) Log.d(TAG, msg)
    }
}

interface EZExceptionHandler: EZLogger {
    val exceptionHandler: CoroutineExceptionHandler
        get() =  CoroutineExceptionHandler { ctx, throwable ->
            error("Error in coroutine context: $ctx, exception $throwable")
        }
}
