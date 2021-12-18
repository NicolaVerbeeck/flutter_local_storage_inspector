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

  Future<void> _startDriver(bool paused) async {
    didInitSocket = true;
    await driver.start(paused: paused);

    socket = await WebSocket.connect('ws://localhost:${driver.port}');
    socketQueue = StreamQueue(socket);
  }

  setUp(() async {
    driver = StorageServerDriver(
        bundleId: 'com.chimerapps.test', port: 0, icon: 'iconData');
    didInitSocket = false;
  });

  tearDown(() async {
    if (didInitSocket) {
      socket.close();
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
      await _startDriver(false);
      final driverIdMessage = await socketQueue.next as String;

      expect(driverIdMessage,
          '{"messageId":"1","serverType":"id","data":{"version":1,"bundleId":"com.chimerapps.test","icon":"iconData","paused":false}}');
    });
    test('Driver identification paused', () async {
      unawaited(_startDriver(true)); //We can't wait here...
      await Future.delayed(const Duration(seconds: 1));
      final driverIdMessage = await socketQueue.next as String;

      expect(driverIdMessage,
          '{"messageId":"1","serverType":"id","data":{"version":1,"bundleId":"com.chimerapps.test","icon":"iconData","paused":true}}');
    });
    test('Start and stop driver multiple times', () async {
      unawaited(_startDriver(true)); //We can't wait here...
      await Future.delayed(const Duration(seconds: 1));
      await driver.stop();
      await driver.start();
      await driver.stop();
    });
  });
}
