class StepCountDataType {
//  final bool isConnected;
  final int stepCount;
  final int calorie;
  final double distance;
  final int timestamp;
  final String error;
  final List<StepCountBinningDataType> binningData;

  StepCountDataType({
    this.stepCount,
    this.calorie,
    this.distance,
    this.timestamp,
    this.error,
    this.binningData,
  });

  factory StepCountDataType.fromJson(Map json) {
    return StepCountDataType(
      stepCount: json['stepCount'],
      distance: json['distance'],
      calorie: json['calorie'],
      timestamp: json['timestamp'],
      binningData: json['binningData'],
      error: json['error'],
    );
  }

  Map<String, dynamic> toJson() => {
        'stepCount': stepCount,
        'distance': distance,
        'calorie': calorie,
        'timestamp': timestamp,
        'binningData': binningData,
        'error': error,
      };
}

class StepCountBinningDataType {
//  final bool isConnected;
  final int stepCount;
  final String time;
  final int receivedAt;
  final String error;

  StepCountBinningDataType({
    this.stepCount,
    this.time,
    this.receivedAt,
    this.error,
  });

  factory StepCountBinningDataType.fromJson(Map json) {
    return StepCountBinningDataType(
      stepCount: json['stepCount'],
      time: json['time'],
      receivedAt: json['receivedAt'],
      error: json['error'],
    );
  }

  Map<String, dynamic> toJson() => {
        'stepCount': stepCount,
        'time': time,
        'receivedAt': receivedAt,
        'error': error,
      };
}
