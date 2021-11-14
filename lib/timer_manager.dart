import 'dart:ui';

import 'package:background_example/callback_dispatcher.dart';
import 'package:flutter/services.dart';

class TimerManager {
  static const _channel = MethodChannel('dev.krgm4d/timer_manager');

  static Future<void> initialize() async {
    final callback = PluginUtilities.getCallbackHandle(callbackDispatcher);

    if (callback == null) {
      assert(false, 'Callback dispatcher handle has not been obtained.');
      return;
    }

    await _channel.invokeMethod(
        'TimerManager.initializeService', [callback.toRawHandle()]);
  }

  static Future<void> startTimer(void Function(int time) callback) async {
    final handle = PluginUtilities.getCallbackHandle(callback);

    if (handle == null) {
      assert(false, 'Callback handle has not been obtained.');
      return;
    }

    await _channel.invokeMethod(
      'TimerManager.startTimer',
      [handle.toRawHandle()],
    );
  }

  static Future<bool> stopTimer() async =>
      await _channel.invokeMethod('TimerManager.stopTimer');
}
