import 'dart:ui';

import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart';

void callbackDispatcher() {
  const backgroundChannel =
      MethodChannel('dev.krgm4d/timer_manager_background');
  WidgetsFlutterBinding.ensureInitialized();

  backgroundChannel.setMethodCallHandler((call) async {
    final List<dynamic> args = call.arguments;
    final callback = PluginUtilities.getCallbackFromHandle(
      CallbackHandle.fromRawHandle(args[0]),
    );

    if (callback == null) {
      assert(false, 'Callback is null.');
      return;
    }

    if (args.length < 2 || args.last is! int) {
      assert(false, 'Time has not found from arguments.');
      return;
    }

    final time = args[1] as int;
    callback(time);
  });

  backgroundChannel.invokeMethod('TimerService.initialized');
}
