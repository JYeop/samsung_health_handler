import 'dart:async';

import 'package:flutter/services.dart';
import 'package:rxdart/rxdart.dart';
import 'package:samsung_health_handler/StepCountDataType.dart';
//
// class SamsungHealthHandlerInitialize {
//   bool isConnected;
//   bool permissionAcquired;
//
//   SamsungHealthHandlerInitialize({required this.isConnected, required this.permissionAcquired});
// }

class SamsungHealthHandler {
  static const MethodChannel channel = const MethodChannel('samsung_health_handler');
  static const EventChannel stepChannel =
      EventChannel('samsung_health_handler_event_steps_channel');
  static const EventChannel connectionChannel =
      EventChannel('samsung_health_handler_event_connection_channel');

  // ignore: close_sinks
  static StreamController<StepCountDataType> streamController = StreamController.broadcast();

  static Stream<StepCountDataType> get stream =>
      SamsungHealthHandler.stepChannel.receiveBroadcastStream().map((event) {
        print('@@@@@@@@@@@@@@@@@');
        print(event);
        print(event.runtimeType.toString());
        print(event.runtimeType.toString() != 'List<dynamic>');
        print('!!!!!!!!!!!!!!!!!!!!!!!');
        if (event.runtimeType.toString().contains('List<dynamic>')) {
          // print('!!!!들갔누!!!!!!!!!!!!!!!!!!!');
          List<dynamic> newArr = List.from(event);
          var stepCountBinningData = newArr.map((e) {
//            print(e);
            return StepCountBinningDataType.fromJson({
              ...e,
              'receivedAt': DateTime.now().millisecondsSinceEpoch,
            });
          }).toList();
          var stepCountData = samsungHandlerValueHandler.stepCountState.value!.toJson();
          samsungHandlerValueHandler.stepCountState.add(StepCountDataType.fromJson({
            ...stepCountData,
            ...{
              'binningData': stepCountBinningData,
            }
          }));
        } else {
          final Map<String, dynamic> data = Map.from(event);
          var today = DateTime.now();
          if (DateTime.fromMillisecondsSinceEpoch(data['timestamp']).difference(today).inDays >=
              0) {
            samsungHandlerValueHandler.stepCountState.add(StepCountDataType.fromJson({
              ...data,
              ...{'binningData': null}
            }));
          } else {
            samsungHandlerValueHandler.stepCountState.add(StepCountDataType.fromJson({
              ...data,
              ...{
                'binningData': samsungHandlerValueHandler.stepCountState.value?.binningData,
              }
            }));
          }
        }
        return samsungHandlerValueHandler.stepCountState.value!;
      });

  // ignore: top_level_function_literal_block
  static Stream<dynamic> get connectionStream =>
      SamsungHealthHandler.connectionChannel.receiveBroadcastStream().map((event) {
        final Map<String, dynamic> data = Map.from(event);
        return data;
      });

  static Future<bool> initialize() async {
    bool isConnected = false;
    // bool permissionAcquired;
    channel.invokeMethod('initialize');
    // var result = SamsungHealthHandlerInitialize(isConnected: false, permissionAcquired: false);
    var res = SamsungHealthHandler.connectionStream.takeWhile((element) {
      // print('@@@@@@@@@@@@');
      // print(element);
      // print('@@!12121212');
      // if (element['requestPermissionResult'] != null) result.permissionAcquired = true;
      // permissionAcquired = element['requestPermissionResult'];
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

  static Future<Map<String, dynamic>> isPermissionAcquired() async {
    final result = await channel.invokeMethod('isPermissionAcquired');
    return new Map<String, dynamic>.from(result);
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

  static Future<StepCountDataType> getStepCount(int millisecondTimestamp) async {
    passTimestamp(millisecondTimestamp);
    StepCountDataType result = StepCountDataType.fromJson({});
    var today = DateTime.now();
    try {
      var intList = new List<int>.generate(10, (i) => i + 1);
      await Future.forEach(intList, (_) async {
        await Future.delayed(Duration(milliseconds: 30));
        var passedTime = DateTime.fromMillisecondsSinceEpoch(millisecondTimestamp);
        var value = samsungHandlerValueHandler.stepCountState.value;
        if (value?.timestamp != null) {
          var dateTime = DateTime.fromMillisecondsSinceEpoch(value!.timestamp);
          // var newValue = samsungHandlerValueHandler.stepCountState.value!.toJson();
          // print('@@@@@@@@@@@@22222223');
          // print(newValue);
          if (dateTime.day == passedTime.day &&
              dateTime.month == passedTime.month &&
              dateTime.year == passedTime.year) {
            if (passedTime.difference(today).inDays >= 0) {
              var newValue = samsungHandlerValueHandler.stepCountState.value!.toJson();
              // print('@@@@@@@@@@@@');
              // print(newValue);
              newValue['binningData'] = null;
//            [StepCountBinningDataType.fromJson({})];
              result = StepCountDataType.fromJson(newValue);
            }
            result = value;
            return;
          }
        }
        return;
      });
      return result;
    } catch (error) {
      throw error;
    }
  }
}

class SamsungHandlerValueHandler {
  // ignore: close_sinks
  BehaviorSubject<StepCountDataType> stepCountState =
      new BehaviorSubject<StepCountDataType>.seeded(StepCountDataType.fromJson({}));
}

var samsungHandlerValueHandler = SamsungHandlerValueHandler();
