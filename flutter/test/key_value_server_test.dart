// ignore_for_file: avoid_print

import 'dart:convert';
import 'dart:io';

import 'package:async/async.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:storage_inspector/src/protocol/storage_server_driver.dart';
import 'package:storage_inspector/storage_inspector.dart';

void main() {
  late StorageServerDriver driver;
  late _SimpleMemoryKeyValueServer keyValueServer;
  late WebSocket socket;
  late StreamQueue socketQueue;

  setUp(() async {
    storageInspectorLogger = (s) => print(s);
    driver = StorageServerDriver(bundleId: 'com.chimerapps.test', port: 0, icon: null);
    keyValueServer = _SimpleMemoryKeyValueServer({});
    driver.addKeyValueServer(keyValueServer);

    await driver.start();

    socket = await WebSocket.connect('ws://localhost:${driver.port}');
    socketQueue = StreamQueue(socket);
  });

  tearDown(() async {
    socket.close();
    await driver.stop();
  });

  group('Key value protocol and driver tests', () {
    test('Test identify', () async {
      final driverIdMessage = await socketQueue.next as String;
      expect(driverIdMessage, '{"messageId":"1","serverType":"id","data":{"version":1,"bundleId":"com.chimerapps.test"}}');
      final serverIdMessage = json.decode(await socketQueue.next as String) as Map<String, dynamic>;

      expect(serverIdMessage['messageId'], isNotNull);
      expect(serverIdMessage['serverType'], 'key_value');
      expect(serverIdMessage['data'], isNotNull);

      final idMessage = serverIdMessage['data'] as Map<String, dynamic>;
      expect(idMessage['type'], 'identify');
      expect(idMessage['data'], isNotNull);

      final idDataString = json.encode(idMessage['data']);
      expect(
          idDataString,
          '{"id":"123","name":"Test Server","icon":null,"keySuggestions":[{"type":"string"'
          ',"value":"key1"}],"keyOptions":[{"type":"string","value":"key1"},{"type":"string",'
          '"value":"key2"}],"supportedKeyTypes":["string"],"supportedValueTypes":["string"],"keyTypeHints":[]}');

      socket.close();
    });

    test('Test get all empty', () async {
      await socketQueue.skip(2);
      socket.addUtf8Text(
        utf8.encode(
          json.encode(
            {
              'requestId': '1234',
              'serverType': 'key_value',
              'data': {
                'type': 'getAll',
              },
            },
          ),
        ),
      );
      expect(await socketQueue.next, matches('{"messageId":".*","serverType":"key_value","requestId":"1234","data":{"all":\\[{"id":"123","values":\\[\\]}\\]}}'));
    });

    test('Test get all initial data', () async {
      await socketQueue.skip(2);
      keyValueServer.backingMap['hello'] = 'world';
      socket.addUtf8Text(
        utf8.encode(
          json.encode(
            {
              'requestId': '1234',
              'serverType': 'key_value',
              'data': {
                'type': 'getAll',
              },
            },
          ),
        ),
      );
      expect(
          await socketQueue.next,
          matches(
              '{"messageId":".*","serverType":"key_value","requestId":"1234","data":{"all":\\[{"id":"123","values":\\[{"key":{"type":"string","value":"hello"},"value":{"type":"string","value":"world"}}\\]}\\]}}'));
    });
  });
}

class _SimpleMemoryKeyValueServer extends SimpleStringKeyValueServer {
  final Map<String, String> backingMap;

  _SimpleMemoryKeyValueServer(this.backingMap)
      : super(
          'Test Server',
          keyOptions: {'key1', 'key2'},
          keySuggestions: {'key1'},
        );

  @override
  Future<void> clearValues() {
    backingMap.clear();
    return Future.value();
  }

  @override
  final String id = '123';

  @override
  Future<void> removeValue(String key) {
    backingMap.remove(key);
    return Future.value();
  }

  @override
  Future<void> setValue(String key, String value) {
    backingMap[key] = value;
    return Future.value();
  }

  @override
  Future<Iterable<MapEntry<String, String>>> get values => Future.value(backingMap.entries);
}
