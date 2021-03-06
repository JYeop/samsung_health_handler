# samsung_health_handler

This package is currently on development. So, if you want stability, i do not recommend you to use this. 
##Important
To use samsung-health api, you must be authorized partner.
https://stackoverflow.com/a/41677496

On debug mode, you can use it.
https://developer.samsung.com/health/android/data/guide/dev-mode.html

## Coverage
Currently, this package only supports step count data.  
No iOS support.

### StepCount payload
|name | type| etc|
|---|:---:|:---:|
|stepCount |int | |
|calorie|int | Cal |
|distance|double | meter |
|timestamp|int | millisecond|
|binningData|StepCountBinningDataType | Supports time(String - HH:mm), stepCount(int)|

## Installation
```yaml
  samsung_health_handler: <latest-version>
```
### Configuration
####Android
Add in AndroidManifest.xml
```xml
<meta-data
    android:name="com.samsung.android.health.permission.read"
    android:value="com.samsung.health.step_count;com.samsung.shealth.step_daily_trend"
/>
```
## Notice

### Lifecycle
1. Set stream
2. initialize
3. dispose

Sample usage is on bottom.

### Why initialize callback does not arrive when performs "Hot Restart"?
On hot restart, dispose method of stateful widget does not work.
So, If you want to reinitialize SamsungHealthHandler, **you must manually dispose and reinitialize**.


## Sample Usage
```Dart
import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import 'package:samsung_health_handler/StepCountDataType.dart';
import 'package:samsung_health_handler/samsung_health_handler.dart';

void main() {
  Intl.defaultLocale = 'ko_KR';
  runApp(MyApp());
}

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  Stream<StepCountDataType> stepStream = SamsungHealthHandler.stream;
  bool loading = true;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) async {
      await initialize();
    });
  }

  @override
  void dispose() {
    super.dispose();
    disposeSamsungHealth();
  }

  initialize() async {
    var isInitialized = await SamsungHealthHandler.initialize();
    if (isInitialized) {
      setState(() {
        loading = false;
      });
    }
  }

  disposeSamsungHealth() async {
    SamsungHealthHandler.dispose();
    setState(() {
      loading = true;
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: Text('Plugin example app'),
        ),
        body: SingleChildScrollView(
          child: Center(
            child: Column(
              children: <Widget>[
                Row(
                  children: <Widget>[
//                        Calls data of 2020/04/05
                    RaisedButton(
                      child: Text('4/5'),
                      onPressed: () {
                        SamsungHealthHandler.passTimestamp(DateTime.now().millisecondsSinceEpoch);
                      },
                    ),
                    RaisedButton(
                      child: Text('prevDate'),
                      onPressed: () {
                        SamsungHealthHandler.prevDate();
                      },
                    ),
                    RaisedButton(
                      child: Text('nextDate'),
                      onPressed: () {
                        SamsungHealthHandler.nextDate();
                      },
                    ),
                  ],
                ),
                Text('On hot restart, dispose method of stateful widget does not work.'
                    '\n So, If you want to reinitialize SamsungHealthHandler,'
                    '\n you have to manually dispose and reinitialize.'),
                RaisedButton(
                  onPressed: () {
                    disposeSamsungHealth();
                  },
                  child: Text('dispose'),
                ),
                RaisedButton(
                  onPressed: () async {
                    try {
//                    Must be called after initialized
//                    gets date of 2020/07/01
                      int timestampFromLocalTime = DateTime.parse('2020-07-01T00:00:00.000Z').millisecondsSinceEpoch;
                      StepCountDataType res = await SamsungHealthHandler.getStepCount(timestampFromLocalTime);
                      print(res.timestamp);
                      print(DateTime.fromMillisecondsSinceEpoch(res.timestamp));
                      print(res.stepCount);
                      print(res.distance);
                      print(res.calorie);
                      if (res.binningData != null) {
                        res.binningData.forEach((element) {
                          print(element.toJson());
                        });
                      }
//                      print('binningData // ${res.binningData}');
                    } catch (error) {
                      print(error);
                    }
                  },
                  child: Text('getStepCount once'),
                ),
                StreamBuilder<StepCountDataType>(
                  stream: stepStream,
                  builder: (BuildContext context, AsyncSnapshot<StepCountDataType> snapshot) {
                    try {
                      print(snapshot.data.stepCount);
                      if (snapshot.data != null) {
                        var timestamp = snapshot.data.timestamp;
                        var date = DateTime.fromMillisecondsSinceEpoch(timestamp);
                        var steps = snapshot.data.stepCount;
//                      Data of today only delivers stepCount
                        var calorie = snapshot.data.calorie;
                        var distance = snapshot.data.distance;
                        var binningData = snapshot.data.binningData;
//                        binningData.forEach((element) {
//                          print(element.toJson());
//                        });
                        return Column(
                          children: <Widget>[
                            Text('date: $date'),
                            Text('steps: $steps'),
                            Text('calorie: $calorie'),
                            Text('distance: $distance'),
                            if (binningData != null)
                              ListView.separated(
                                shrinkWrap: true,
                                physics: NeverScrollableScrollPhysics(),
                                itemCount: binningData.length,
                                separatorBuilder: (BuildContext context, int index) => Divider(
                                  height: 3,
                                ),
                                itemBuilder: (BuildContext context, int index) {
                                  var binningValue = binningData[index];
                                  var binningTime = binningValue.time;
                                  var binningStepCount = binningValue.stepCount;
                                  return ListTile(
                                    title: Column(
                                      children: <Widget>[
                                        Text('time: $binningTime'),
                                        Text('steps: $binningStepCount'),
                                      ],
                                    ),
                                  );
                                },
                              ),
                          ],
                        );
                      } else {
                        return Text('data of current date does not exist.');
                      }
                    } catch (error) {
                      return Text('error: $error');
                    }
                  },
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}
```# samsung_health_flutter_sdk
