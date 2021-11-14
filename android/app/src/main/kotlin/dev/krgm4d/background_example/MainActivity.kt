package dev.krgm4d.background_example

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

class MainActivity : FlutterActivity() {
    companion object {
        @JvmStatic
        private val TAG = "TimerManager"

        @JvmStatic
        val SHARED_PREFERENCES_KEY = "timer_manager_cache"

        @JvmStatic
        val CALLBACK_HANDLE_KEY = "callback_handle"

        @JvmStatic
        val CALLBACK_DISPATCHER_HANDLE_KEY = "callback_dispatch_handler"

        @JvmStatic
        private fun initializeService(context: Context, args: ArrayList<*>?) {
            Log.d(TAG, "Initializing TimerService")
            val callbackHandle = args!![0] as Long
            context.getSharedPreferences(SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE).edit()
                .putLong(
                    CALLBACK_DISPATCHER_HANDLE_KEY, callbackHandle
                ).apply()
        }

        @JvmStatic
        private fun startTimer(
            context: Context,
            args: ArrayList<*>?,
            result: MethodChannel.Result
        ) {
            Log.d(TAG, "Start Timer!")
            val callbackHandle = args!![0] as Long
            context.getSharedPreferences(SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE).edit()
                .putLong(
                    CALLBACK_HANDLE_KEY, callbackHandle
                ).apply()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(Intent(context, IsolateHolderService::class.java))
            }
            result.success(true)
        }


        @JvmStatic
        private fun stopTimer(context: Context, result: MethodChannel.Result) {
            Log.d(TAG, "Stop Timer!")
            val intent = Intent(context, IsolateHolderService::class.java)
            IsolateHolderService.stopTimer()
            context.stopService(intent)
            result.success(true)
        }
    }

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        MethodChannel(
            flutterEngine.dartExecutor.binaryMessenger,
            "dev.krgm4d/timer_manager"
        ).setMethodCallHandler { call, result ->
            run {
                val args = call.arguments<ArrayList<*>>()
                when (call.method) {
                    "TimerManager.initializeService" -> {
                        initializeService(context, args)
                        result.success(true)
                    }
                    "TimerManager.startTimer" -> startTimer(context, args, result)
                    "TimerManager.stopTimer" -> stopTimer(context, result)
                    else -> result.notImplemented()
                }
            }
        }
    }
}
