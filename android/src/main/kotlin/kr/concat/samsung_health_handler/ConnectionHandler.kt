package kr.concat.samsung_health_handler

import android.app.Activity
import android.app.AlertDialog
import com.samsung.android.sdk.healthdata.HealthConnectionErrorResult
import com.samsung.android.sdk.healthdata.HealthConstants.StepCount
import com.samsung.android.sdk.healthdata.HealthDataStore
import com.samsung.android.sdk.healthdata.HealthPermissionManager
import com.samsung.android.sdk.healthdata.HealthPermissionManager.PermissionKey
import com.samsung.android.sdk.healthdata.HealthPermissionManager.PermissionType
import com.samsung.android.sdk.healthdata.HealthResultHolder.ResultListener
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodChannel
import java.util.*
import kotlin.collections.HashMap

class ConnectionHandler(
    activity: Activity,
    stepCountStreamHandler: StepCountStreamHandler
//        store: HealthDataStore,
//        reporter: StepCountReader
) : HealthDataStore.ConnectionListener, EventChannel.StreamHandler {
  private var eventSink: EventChannel.EventSink? = null
  private var mActivity = activity;
  val mStore: HealthDataStore = HealthDataStore(mActivity.applicationContext, this)
  val mReporter: StepCountReader = StepCountReader(mStore, stepCountStreamHandler)

  //    private var mCurrentStartTime: Long = 0
  private fun generatePermissionKeySet(): Set<PermissionKey>? {
    val pmsKeySet: MutableSet<PermissionKey> = HashSet()
    pmsKeySet.add(PermissionKey(StepCountReader.STEP_SUMMARY_DATA_TYPE_NAME, PermissionType.READ))
    pmsKeySet.add(PermissionKey(StepCount.HEALTH_DATA_TYPE, PermissionType.READ))
    return pmsKeySet
  }

  private fun requestPermission() {
    val pmsManager = HealthPermissionManager(mStore)
    try {
      // Show user permission UI for allowing user to change options
      pmsManager.requestPermissions(generatePermissionKeySet(), mActivity)
          .setResultListener(mPermissionListener)
    } catch (e: Exception) {
      println(e);
    }
  }

  fun isPermissionAcquired(): HashMap<String, Any> {
    val pmsManager = HealthPermissionManager(mStore)
    val hashMap: HashMap<String, Any> = HashMap<String, Any>()
    return try {
      // Check whether the permissions that this application needs are acquired
      val resultMap =
          pmsManager.isPermissionAcquired(generatePermissionKeySet())
      hashMap["result"] = !resultMap.values.contains(java.lang.Boolean.FALSE);
//            return !resultMap.values.contains(java.lang.Boolean.FALSE)
      hashMap;
    } catch (e: java.lang.Exception) {
      hashMap["result"] = false;
      hashMap["error"] = e.toString();
      hashMap
    }
  }

  private val mPermissionListener = ResultListener<HealthPermissionManager.PermissionResult> { result ->
    val resultMap = result.resultMap
    val hashMap = HashMap<String, Boolean>()
    // Show a permission alarm and clear step count if permissions are not acquired
    if (resultMap.containsValue(false)) {
      hashMap["requestPermissionResult"] = false
      eventSink?.success(hashMap);
    } else {
      mReporter.requestDailyStepCount(StepCountReader.TODAY_START_UTC_TIME)
      hashMap["requestPermissionResult"] = true
      eventSink?.success(hashMap);
    }
  }

  override fun onConnected() {
      println("온커넥티드!!")
      val hashMap = HashMap<String, Boolean>()
      hashMap["isConnected"] = true;
      eventSink?.success(hashMap);
    if (isPermissionAcquired()["result"] == true) {
      mReporter.requestDailyStepCount(StepCountReader.TODAY_START_UTC_TIME)
    } else {
      requestPermission()
    }
  }

  override fun onConnectionFailed(error: HealthConnectionErrorResult) {
    val hashMap = HashMap<String, Boolean>()
    hashMap["isConnected"] = false
    eventSink?.success(hashMap)
  }

  override fun onDisconnected() {
    if (!mActivity.isFinishing) {
      mStore.disconnectService()
    }
  }

  override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
    println("온리슨!!!")
    eventSink = events
  }

  override fun onCancel(arguments: Any?) {
    eventSink = null
  }

}