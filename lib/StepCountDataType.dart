class StepCountDataType {
//  final bool isConnected;
  final int stepCount;
  final int calorie;
  final double distance;
  final int timestamp;
  final String error;

  StepCountDataType({
//    this.isConnected,
    this.stepCount,
    this.calorie,
    this.distance,
    this.timestamp,
    this.error,
  });

  factory StepCountDataType.fromJson(Map json) {
    return StepCountDataType(
//      isConnected: json['isConnected'],
      stepCount: json['stepCount'],
      distance: json['distance'],
      calorie: json['calorie'],
      timestamp: json['timestamp'],
      error: json['error'],
    );
  }
}
