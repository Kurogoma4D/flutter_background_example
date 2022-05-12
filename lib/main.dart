import 'dart:isolate';
import 'dart:ui';

import 'package:background_example/timer_manager.dart';
import 'package:flutter/material.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Flutter Demo',
      theme: ThemeData(
        primarySwatch: Colors.blue,
        useMaterial3: true,
        splashFactory: InkSparkle.splashFactory,
      ),
      home: const MyHomePage(title: 'Flutter Demo Home Page'),
    );
  }
}

class MyHomePage extends StatefulWidget {
  const MyHomePage({Key? key, required this.title}) : super(key: key);

  final String title;

  @override
  State<MyHomePage> createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  ReceivePort? receivePort;
  int count = 0;

  @override
  void initState() {
    super.initState();
    TimerManager.initialize();

    receivePort = ReceivePort();
    final sendPort = receivePort!.sendPort;
    IsolateNameServer.registerPortWithName(sendPort, 'background');

    receivePort!.listen((message) {
      if (message is! int) return;
      setState(() => count = message);
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(widget.title),
      ),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: <Widget>[
            Text('Count: $count', style: Theme.of(context).textTheme.headline3),
            ElevatedButton(
              onPressed: () => TimerManager.startTimer(timerCallback),
              child: const Text('Start'),
            ),
            ElevatedButton(
              onPressed: () => TimerManager.stopTimer(),
              child: const Text('Stop'),
            ),
          ],
        ),
      ),
    );
  }
}

void timerCallback(int time) {
  debugPrint('time: $time');
  final port = IsolateNameServer.lookupPortByName('background');
  port?.send(time);
}
