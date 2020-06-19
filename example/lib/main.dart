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
