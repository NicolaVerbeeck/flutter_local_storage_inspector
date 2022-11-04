import 'dart:async';
import 'dart:io';

import 'package:async/async.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:storage_inspector/src/driver/storage_server_driver.dart';
import 'package:storage_inspector/src/protocol/storage_protocol.dart';

void main() {
  late StorageServerDriver driver;
  late WebSocket socket;
  late StreamQueue socketQueue;
  var didInitSocket = false;

  Future<void> startDriverPaused() async {
    didInitSocket = true;
    await driver.start(paused: true);
  }

  Future<void> initSocket() async {
    socket = await WebSocket.connect('ws://localhost:${driver.port}');
    socketQueue = StreamQueue<dynamic>(socket);
  }

  Future<void> startDriver() async {
    didInitSocket = true;
    await driver.start(paused: false);
    await initSocket();
  }

  setUp(() async {
    driver = StorageServerDriver(
        bundleId: 'com.chimerapps.test', port: 0, icon: 'iconData');
    didInitSocket = false;
  });

  tearDown(() async {
    if (didInitSocket) {
      await socket.close();
      await driver.stop();
    }
  });

  group('Driver tests', () {
    test('Test protocol version', () {
      expect(
          StorageServerDriver(
                  bundleId: 'com.chimerapps.test', port: 0, icon: 'iconData')
              .protocolVersion,
          StorageProtocol.version);
    });
    test('Driver identification not paused', () async {
      await startDriver();
      final driverIdMessage = await socketQueue.next as String;

      expect(driverIdMessage,
          '{"messageId":"1","serverType":"id","data":{"version":1,"bundleId":"com.chimerapps.test","icon":"iconData","paused":false}}');
    });
    test('Driver identification paused', () async {
      unawaited(startDriverPaused()); //We can't wait here...
      await Future<void>.delayed(const Duration(seconds: 1));
      await initSocket();
      final driverIdMessage = await socketQueue.next as String;

      expect(driverIdMessage,
          '{"messageId":"1","serverType":"id","data":{"version":1,"bundleId":"com.chimerapps.test","icon":"iconData","paused":true}}');
    });
    test('Start and stop driver multiple times', () async {
      await startDriver();
      await Future<void>.delayed(const Duration(seconds: 1));
      await driver.stop();
      await driver.start();
      await driver.stop();
    });
  });
}
