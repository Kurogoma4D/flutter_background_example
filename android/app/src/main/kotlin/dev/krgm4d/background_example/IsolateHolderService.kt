package dev.krgm4d.background_example

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import io.flutter.FlutterInjector
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.dart.DartExecutor
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.view.FlutterCallbackInformation
import java.util.concurrent.atomic.AtomicBoolean

class IsolateHolderService : Service(), MethodChannel.MethodCallHandler {
    private lateinit var mBackgroundChannel: MethodChannel

    companion object {
        @JvmStatic
        private val TAG = "IsolateHolderService"

        @JvmStatic
        private var sBackgroundFlutterEngine: FlutterEngine? = null

        @JvmStatic
        private val sServiceStarted = AtomicBoolean(false)

        @JvmStatic
        private var handler: Handler? = null

        @JvmStatic
        private var runnableTask: Runnable? = null

        @JvmStatic
        private var callbackHandle: Long = 0L

        @JvmStatic
        fun stopTimer() {
            if (runnableTask == null) return
            handler?.removeCallbacks(runnableTask!!)
        }
    }

    override fun onBind(p0: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val id = "isolate_holder"

        if (manager.getNotificationChannel(id) == null) {
            val mChannel = NotificationChannel(id, "title", NotificationManager.IMPORTANCE_DEFAULT)
            mChannel.apply {
                description = "description"
            }
            manager.createNotificationChannel(mChannel)
        }

        val notification = NotificationCompat.Builder(this, id).apply {
            setContentTitle("サービス")
            setContentText("サービス")
            setSmallIcon(R.mipmap.ic_launcher)
        }.build()

        handler = Handler(Looper.getMainLooper())
        callbackHandle = this.getSharedPreferences(
            MainActivity.SHARED_PREFERENCES_KEY,
            Context.MODE_PRIVATE
        )
            .getLong(MainActivity.CALLBACK_HANDLE_KEY, 0)

        runnableTask = object : Runnable {
            var count = 0
            override fun run() {
                if (sServiceStarted.get()) {
                    count++
                    mBackgroundChannel.invokeMethod("", listOf(callbackHandle, count))
                }
                handler?.postDelayed(this, 1000)
            }
        }
        handler?.post(runnableTask!!)

        startForeground(1, notification)
        return START_STICKY
    }

    private fun startIsolateHolderService(context: Context) {
        if (sBackgroundFlutterEngine == null) {
            val callbackHandle = context.getSharedPreferences(
                MainActivity.SHARED_PREFERENCES_KEY,
                Context.MODE_PRIVATE
            )
                .getLong(MainActivity.CALLBACK_DISPATCHER_HANDLE_KEY, 0)
            if (callbackHandle == 0L) {
                Log.e(TAG, "Fatal: no callback registered")
                return
            }

            val callbackInfo =
                FlutterCallbackInformation.lookupCallbackInformation(callbackHandle)
            if (callbackInfo == null) {
                Log.e(TAG, "Fatal: failed to find callback")
                return
            }
            Log.i(TAG, "Starting IsolateHolderService...")
            sBackgroundFlutterEngine = FlutterEngine(context)

            val args = DartExecutor.DartCallback(
                context.assets,
                FlutterInjector.instance().flutterLoader().findAppBundlePath(),
                callbackInfo
            )
            sBackgroundFlutterEngine!!.dartExecutor.executeDartCallback(args)
        }

        mBackgroundChannel = MethodChannel(
            sBackgroundFlutterEngine!!.dartExecutor.binaryMessenger,
            "dev.krgm4d/timer_manager_background"
        )
        mBackgroundChannel.setMethodCallHandler(this)
    }

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        when (call.method) {
            "TimerService.initialized" -> {
                Log.d(TAG, "Initialized service!")
                sServiceStarted.set(true)
            }
            else -> result.notImplemented()
        }
        result.success(null)
    }

    override fun onCreate() {
        super.onCreate()
        startIsolateHolderService(this)
    }
}