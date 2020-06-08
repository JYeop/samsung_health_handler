import 'dart:async';

import 'package:flutter/services.dart';
import 'package:samsung_health_handler/StepCountDataType.dart';

class SamsungHealthHandler {
  static const MethodChannel channel = const MethodChannel('samsung_health_handler');
  static const EventChannel stepChannel = EventChannel('samsung_health_handler_event_steps_channel');
  static const EventChannel connectionChannel = EventChannel('samsung_health_handler_event_connection_channel');

  // ignore: close_sinks
  static StreamController<StepCountDataType> streamController = StreamController.broadcast();

  // ignore: top_level_function_literal_block
  static Stream<StepCountDataType> get stream => SamsungHealthHandler.stepChannel.receiveBroadcastStream().map((event) {
        final Map<String, dynamic> data = Map.from(event);
//        print(data);
        return StepCountDataType.fromJson(data);
      });

  // ignore: top_level_function_literal_block
  static Stream<dynamic> get connectionStream =>
      SamsungHealthHandler.connectionChannel.receiveBroadcastStream().map((event) {
        final Map<String, dynamic> data = Map.from(event);
        return data;
      });

  static Future<bool> initialize() async {
    bool isConnected = false;
    channel.invokeMethod('initialize');
    var res = SamsungHealthHandler.connectionStream.takeWhile((element) {
      if (element['isConnected'] == true) isConnected = true;
      return element['isConnected'] == null;
    });
//    돌기까지 기다림
    await res.isEmpty;
    return isConnected;
  }

  static void dispose() async {
    channel.invokeMethod('dispose');
  }

  static Future<dynamic> isPermissionAcquired() async {
    final dynamic result = await channel.invokeMethod('isPermissionAcquired');
    return result;
  }

  static Future<dynamic> requestPermission() async {
    final dynamic result = await channel.invokeMethod('requestPermission');
    return result;
  }

  static passTimestamp(int timestampInMillisecond) {
    channel.invokeMethod('passTimestamp', {'timestamp': timestampInMillisecond});
  }

  static Future<dynamic> prevDate() async {
    final dynamic result = await channel.invokeMethod('prevDate');
    return result;
  }

  static Future<dynamic> nextDate() async {
    final dynamic result = await channel.invokeMethod('nextDate');
    return result;
  }
}
