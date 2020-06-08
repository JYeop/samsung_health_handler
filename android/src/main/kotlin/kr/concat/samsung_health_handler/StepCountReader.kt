package kr.concat.samsung_health_handler

import com.samsung.android.sdk.healthdata.*
import com.samsung.android.sdk.healthdata.HealthDataResolver.*
import com.samsung.android.sdk.healthdata.HealthDataResolver.AggregateRequest.AggregateFunction
import com.samsung.android.sdk.healthdata.HealthDataResolver.AggregateRequest.TimeGroupUnit
import java.util.*

class StepCountReader(store: HealthDataStore, observer: StepCountObserver) {
  private val mResolver: HealthDataResolver = HealthDataResolver(store, null)
  private val mObserver: StepCountObserver? = observer

  companion object {
    const val STEP_SUMMARY_DATA_TYPE_NAME = "com.samsung.shealth.step_daily_trend"
    var TODAY_START_UTC_TIME: Long = 0
    const val ONE_DAY = 24 * 60 * 60 * 1000.toLong()
    private const val PROPERTY_TIME = "day_time"
    private const val PROPERTY_COUNT = "count"
    private const val PROPERTY_BINNING_DATA = "binning_data"
    private const val PROPERTY_CALORIE = "CALORIE"
    private const val PROPERTY_DISTANCE = "DISTANCE"
    private const val ALIAS_TOTAL_COUNT = "count"
    private const val ALIAS_DEVICE_UUID = "deviceuuid"
    private const val ALIAS_BINNING_TIME = "binning_time"
    private val todayStartUtcTime: Long
      get() {
        val today = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        today[Calendar.HOUR_OF_DAY] = 0
        today[Calendar.MINUTE] = 0
        today[Calendar.SECOND] = 0
        today[Calendar.MILLISECOND] = 0
        return today.timeInMillis
      }

    private fun getBinningData(zip: ByteArray): List<StepBinningData> {
      val binningDataList =
          HealthDataUtil.getStructuredDataList(zip, StepBinningData::class.java)
      for (i in binningDataList.indices.reversed()) {
        if (binningDataList[i].count == 0) {
          binningDataList.removeAt(i)
        } else {
          binningDataList[i].time =
              String.format(Locale.US, "%02d:%02d", i / 6, i % 6 * 10)
        }
      }
      return binningDataList
    }

    init {
      TODAY_START_UTC_TIME = todayStartUtcTime
    }
  }

  // Get the daily total step count of a specified day
  fun requestDailyStepCount(startTime: Long) {
    println("$startTime /// $TODAY_START_UTC_TIME")
    if (startTime >= TODAY_START_UTC_TIME) {
      // Get today step count
      readStepCount(startTime)
    } else {
      // Get historical step count
      readStepDailyTrend(startTime)
    }
  }

  private fun readStepCount(startTime: Long) {
    // Get sum of step counts by device
    val request = AggregateRequest.Builder()
        .setDataType(HealthConstants.StepCount.HEALTH_DATA_TYPE)
        .addFunction(AggregateFunction.SUM, HealthConstants.StepCount.COUNT, ALIAS_TOTAL_COUNT)
        .addGroup(HealthConstants.StepCount.DEVICE_UUID, ALIAS_DEVICE_UUID)
        .setLocalTimeRange(HealthConstants.StepCount.START_TIME, HealthConstants.StepCount.TIME_OFFSET,
            startTime, startTime + ONE_DAY)
        .setSort(ALIAS_TOTAL_COUNT, SortOrder.DESC)
        .build()
    try {
      mResolver.aggregate(request).setResultListener { result: AggregateResult ->
        var totalCount = 0
        var totalCalorie = 0
        var totalDistance: Double = 0.0
        var deviceUuid: String? = null
        result.use { result ->
          val iterator: Iterator<HealthData> = result.iterator()
          if (iterator.hasNext()) {
            val data = iterator.next()
            totalCount = data.getInt(ALIAS_TOTAL_COUNT)
            deviceUuid = data.getString(ALIAS_DEVICE_UUID)
            totalCalorie = data.getInt(PROPERTY_CALORIE)
            totalDistance = data.getDouble(PROPERTY_DISTANCE)
            println("오늘꺼??$totalCount // $totalCalorie // $totalDistance")
          }
        }
        mObserver?.onChanged(totalCount, totalCalorie, totalDistance)
        if (deviceUuid != null) {
          readStepCountBinning(startTime, deviceUuid!!)
        }
      }
    } catch (e: Exception) {
      println(e)
    }
  }

  private fun readStepDailyTrend(startTime: Long) {
    val filter = Filter.and(Filter.eq(PROPERTY_TIME, startTime),  // filtering source type "combined(-2)"
        Filter.eq("source_type", -2))
    val request = ReadRequest.Builder()
        .setDataType(STEP_SUMMARY_DATA_TYPE_NAME)
        .setProperties(arrayOf(PROPERTY_COUNT, PROPERTY_BINNING_DATA, PROPERTY_CALORIE, PROPERTY_DISTANCE))
        .setFilter(filter)
        .build()
    try {
      mResolver.read(request).setResultListener { result: ReadResult ->
        var totalCount = 0
        var totalCalorie = 0
        var totalDistance: Double = 0.0
        var binningDataList: List<StepBinningData> = emptyList()
        result.use { result ->
          val iterator: Iterator<HealthData> = result.iterator()
          if (iterator.hasNext()) {
            val data = iterator.next()
            totalCount = data.getInt(PROPERTY_COUNT)
            val binningData = data.getBlob(PROPERTY_BINNING_DATA)
            binningDataList = getBinningData(binningData)
            totalCalorie = data.getInt(PROPERTY_CALORIE)
            totalDistance = data.getDouble(PROPERTY_DISTANCE)
          }
        }
        if (mObserver != null) {
          mObserver.onChanged(totalCount, totalCalorie, totalDistance)
          mObserver.onBinningDataChanged(binningDataList)
        }
      }
    } catch (e: Exception) {
      println(e)
    }
  }

  private fun readStepCountBinning(startTime: Long, deviceUuid: String) {
    val filter = Filter.eq(HealthConstants.StepCount.DEVICE_UUID, deviceUuid)

    // Get 10 minute binning data of a particular device
    val request = AggregateRequest.Builder()
        .setDataType(HealthConstants.StepCount.HEALTH_DATA_TYPE)
        .addFunction(AggregateFunction.SUM, HealthConstants.StepCount.COUNT, ALIAS_TOTAL_COUNT)
        .setTimeGroup(TimeGroupUnit.MINUTELY, 10, HealthConstants.StepCount.START_TIME,
            HealthConstants.StepCount.TIME_OFFSET, ALIAS_BINNING_TIME)
        .setLocalTimeRange(
            HealthConstants.StepCount.START_TIME,
            HealthConstants.StepCount.TIME_OFFSET,
            startTime, startTime + ONE_DAY
        )
        .setFilter(filter)
        .setSort(ALIAS_BINNING_TIME, SortOrder.ASC)
        .build()
    try {
      mResolver.aggregate(request).setResultListener { result: AggregateResult ->
        val binningCountArray: MutableList<StepBinningData> = ArrayList()
        result.use { result ->
          for (data in result) {
            val binningTime = data.getString(ALIAS_BINNING_TIME)
            val binningCount = data.getInt(ALIAS_TOTAL_COUNT)
            if (binningTime != null) {
              binningCountArray.add(StepBinningData(
                  binningTime.split(" ".toRegex()).toTypedArray()[1], binningCount)
              )
            }
          }
          mObserver?.onBinningDataChanged(binningCountArray)
        }
      }
    } catch (e: Exception) {
    }
  }

  class StepBinningData(var time: String, val count: Int)

  interface StepCountObserver {
    fun onChanged(count: Int, calorie: Int, distance: Double)
    fun onBinningDataChanged(binningCountList: List<StepBinningData>?)
  }

}