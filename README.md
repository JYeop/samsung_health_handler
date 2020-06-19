# samsung_health_handler

This package is currently on development. So, if you want stability, i do not recommend you to use this. 

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

## Installation
```yaml
  samsung_health_handler:
    git:
      url: git://github.com/JYeop/samsung-health-handler.git
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
import 'package:samsung_health_handler/StepCountDataType.dart';
import 'package:samsung_health_handler/samsung_health_handler.dart';

void main() {
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
        body: Center(
          child: Column(
            children: <Widget>[
              StreamBuilder<StepCountDataType>(
                stream: stepStream,
                builder: (BuildContext context, AsyncSnapshot<StepCountDataType> snapshot) {
                  try {
                    if (snapshot.data != null) {
                      var timestamp = snapshot.data.timestamp;
                      var date = DateTime.fromMillisecondsSinceEpoch(timestamp);
                      var steps = snapshot.data.stepCount;
//                      Data of today only delivers stepCount
                      var calorie = snapshot.data.calorie;
                      var distance = snapshot.data.distance;
                      return Column(
                        children: <Widget>[
                          Text('date: $date'),
                          Text('steps: $steps'),
                          Text('calorie: $calorie'),
                          Text('distance: $distance'),
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
//                  gets date of 2020/06/01
                    StepCountDataType res = await SamsungHealthHandler.getStepCount(1590969600000);
                    print(DateTime.fromMillisecondsSinceEpoch(res.timestamp));
                    print(res.stepCount);
                    print(res.distance);
                    print(res.calorie);
                  } catch (error) {
                    print(error);
                  }
                },
                child: Text('getStepCount once'),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
```# samsung_health_flutter_sdk
