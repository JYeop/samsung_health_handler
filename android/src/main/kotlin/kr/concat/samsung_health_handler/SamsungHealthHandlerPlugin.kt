package kr.concat.samsung_health_handler

import android.app.Activity
import androidx.annotation.NonNull
import com.samsung.android.sdk.healthdata.HealthDataStore
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry.Registrar

/** SamsungHealthHandlerPlugin */
class SamsungHealthHandlerPlugin : FlutterPlugin, MethodCallHandler, ActivityAware {
  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity

  private lateinit var mActivity: Activity
  private lateinit var channel: MethodChannel
  private lateinit var stepChannel: EventChannel
  private lateinit var connectionChannel: EventChannel
  private lateinit var stepCountStreamHandler: StepCountStreamHandler

  private lateinit var mConnectionHandler: ConnectionHandler


  override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    channel = MethodChannel(flutterPluginBinding.flutterEngine.dartExecutor,
        "samsung_health_handler")
    stepChannel = EventChannel(flutterPluginBinding.flutterEngine.dartExecutor,
        "samsung_health_handler_event_steps_channel")
    connectionChannel = EventChannel(flutterPluginBinding.flutterEngine.dartExecutor,
        "samsung_health_handler_event_connection_channel")
    channel.setMethodCallHandler(this)
  }

  // This static function is optional and equivalent to onAttachedToEngine. It supports the old
  // pre-Flutter-1.12 Android projects. You are encouraged to continue supporting
  // plugin registration via this function while apps migrate to use the new Android APIs
  // post-flutter-1.12 via https://flutter.dev/go/android-project-migration.
  //
  // It is encouraged to share logic between onAttachedToEngine and registerWith to keep
  // them functionally equivalent. Only one of onAttachedToEngine or registerWith will be called
  // depending on the user's project. onAttachedToEngine or registerWith must both be defined
  // in the same class.
  companion object {
    @JvmStatic
    fun registerWith(registrar: Registrar) {
      val channel = MethodChannel(registrar.messenger(), "samsung_health_handler")
      channel.setMethodCallHandler(SamsungHealthHandlerPlugin())
    }
  }


  override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
    when (call.method) {
      "initialize" -> {
        stepCountStreamHandler.mCurrentStartTime = StepCountReader.TODAY_START_UTC_TIME
        mConnectionHandler.mStore.connectService()
        result.success(true)
      }
      "dispose" -> {
        mConnectionHandler.mStore.disconnectService()
        result.success(true)
      }
      "isPermissionAcquired" -> {
        val permissionResult = mConnectionHandler.isPermissionAcquired()
        result.success(permissionResult)
      }
      "prevDate" -> {
        stepCountStreamHandler.mCurrentStartTime -= StepCountReader.ONE_DAY
        mConnectionHandler.mReporter.requestDailyStepCount(stepCountStreamHandler.mCurrentStartTime)
        result.success(true)
      }
      "passTimestamp" -> {
        val timestampData = call.argument<Long>("timestamp")!!
        stepCountStreamHandler.mCurrentStartTime = timestampData
        mConnectionHandler.mReporter.requestDailyStepCount(timestampData)
        result.success(true)
      }
      "nextDate" -> {
        stepCountStreamHandler.mCurrentStartTime += StepCountReader.ONE_DAY
        mConnectionHandler.mReporter.requestDailyStepCount(stepCountStreamHandler.mCurrentStartTime)
        result.success(true)
      }
      else -> {
        result.notImplemented()
      }
    }
  }

  override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
    channel.setMethodCallHandler(null)
  }

  override fun onDetachedFromActivity() {
  }

  override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
  }

  override fun onAttachedToActivity(binding: ActivityPluginBinding) {
    mActivity = binding.activity
    stepCountStreamHandler = StepCountStreamHandler()
    mConnectionHandler = ConnectionHandler(mActivity, stepCountStreamHandler)
    connectionChannel.setStreamHandler(mConnectionHandler)
    stepChannel.setStreamHandler(stepCountStreamHandler)
  }

  override fun onDetachedFromActivityForConfigChanges() {

  }
}
