package kr.concat.samsung_health_handler

import io.flutter.plugin.common.EventChannel

class StepCountStreamHandler(
) : EventChannel.StreamHandler,
    StepCountReader.StepCountObserver {
  private var eventSink: EventChannel.EventSink? = null
  var mCurrentStartTime: Long = 0
  override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
    eventSink = events
  }

  override fun onCancel(arguments: Any?) {
    eventSink = null
  }

  override fun onChanged(count: Int, calorie: Int, distance: Double) {
    val hashMap: HashMap<String, Any> = HashMap<String, Any>()
    hashMap["stepCount"] = count
    hashMap["calorie"] = calorie
    hashMap["distance"] = distance
    hashMap["timestamp"] = mCurrentStartTime
    eventSink?.success(hashMap)
  }

  override fun onBinningDataChanged(binningCountList: List<StepCountReader.StepBinningData>?) {
  }


}