package io.agora.myapplication.utils

import android.util.Log
import kotlinx.coroutines.CoroutineExceptionHandler

interface EZLogger {
    private val TAG: String get() = javaClass.simpleName

    fun error(msg: String) {
        Log.e(TAG, msg)
    }

    fun info(msg: String) {
        Log.i(TAG, msg)
    }
}

interface EZExceptionHandler: EZLogger {
    val exceptionHandler: CoroutineExceptionHandler
        get() =  CoroutineExceptionHandler { ctx, throwable ->
            error("Error in coroutine context: $ctx, exception $throwable")
        }
}
