// ignore_for_file: avoid_print

import 'dart:convert';
import 'dart:io';

import 'package:async/async.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:storage_inspector/src/driver/storage_server_driver.dart';
import 'package:storage_inspector/storage_inspector.dart';

void main() {
  late StorageServerDriver driver;
  late SimpleMemoryKeyValueServer keyValueServer;
  late WebSocket socket;
  late StreamQueue socketQueue;

  setUp(() async {
    driver = StorageServerDriver(
        bundleId: 'com.chimerapps.test', port: 0, icon: 'iconData');
    keyValueServer = SimpleMemoryKeyValueServer({});
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
      expect(driverIdMessage,
          '{"messageId":"1","serverType":"id","data":{"version":1,"bundleId":"com.chimerapps.test","icon":"iconData"}}');
      final serverIdMessage =
          json.decode(await socketQueue.next as String) as Map<String, dynamic>;

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
          '"value":"key2"}],"supportedKeyTypes":["string"],"supportedValueTypes":["string"],'
          '"keyTypeHints":[{"key":{"type":"string","value":"2"},"type":"string"}],"keyIcons"'
          ':[{"key":{"type":"string","value":"key1"},"icon":"base64"}]}');

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
      expect(
          await socketQueue.next,
          matches(
              '{"messageId":".*","serverType":"key_value","requestId":"1234","data":{"all":\\[{"id":"123","values":\\[\\]}\\]}}'));
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

    test('Test get all error', () async {
      await socketQueue.skip(2);
      keyValueServer.backingMap['hello'] = 'world';
      keyValueServer.throwGetAll = true;
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
              '{"messageId":".*","serverType":"key_value","requestId":"1234","data":{"all":\\[\\]}}'));
    });

    test('Test get single server', () async {
      await socketQueue.skip(2);
      keyValueServer.backingMap['hello'] = 'world';
      socket.addUtf8Text(
        utf8.encode(
          json.encode(
            {
              'requestId': '1234',
              'serverType': 'key_value',
              'data': {
                'type': 'get',
                'data': {
                  'id': '123',
                }
              },
            },
          ),
        ),
      );
      expect(
          await socketQueue.next,
          matches(
              '{"messageId":".*","serverType":"key_value","requestId":"1234","data":{"id":"123","values":\\[{"key":{"type":"string","value":"hello"},"value":{"type":"string","value":"world"}}\\]}}'));
    });

    test('Test set', () async {
      await socketQueue.skip(2);
      expect(keyValueServer.backingMap.isEmpty, true);

      socket.addUtf8Text(
        utf8.encode(
          json.encode(
            {
              'requestId': '1234',
              'serverType': 'key_value',
              'data': {
                'type': 'set',
                'data': {
                  'id': '123',
                  'key': {'type': 'string', 'value': 'hello'},
                  'value': {'type': 'string', 'value': 'world'},
                }
              },
            },
          ),
        ),
      );

      await socketQueue.next;
      expect(keyValueServer.backingMap['hello'], 'world');
    });

    test('Test actions wrong server', () async {
      await socketQueue.skip(2);
      expect(keyValueServer.backingMap.isEmpty, true);

      socket.addUtf8Text(
        utf8.encode(
          json.encode(
            {
              'requestId': '1234',
              'serverType': 'key_value',
              'data': {
                'type': 'set',
                'data': {
                  'id': '1234',
                  'key': {'type': 'string', 'value': 'hello'},
                  'value': {'type': 'string', 'value': 'world'},
                }
              },
            },
          ),
        ),
      );
      socket.addUtf8Text(
        utf8.encode(
          json.encode(
            {
              'requestId': '1234',
              'serverType': 'key_value',
              'data': {
                'type': 'clear',
                'data': {
                  'id': '1234',
                  'key': {'type': 'string', 'value': 'hello'},
                  'value': {'type': 'string', 'value': 'world'},
                }
              },
            },
          ),
        ),
      );
      socket.addUtf8Text(
        utf8.encode(
          json.encode(
            {
              'requestId': '1234',
              'serverType': 'key_value',
              'data': {
                'type': 'remove',
                'data': {
                  'id': '1234',
                  'key': {'type': 'string', 'value': 'hello'},
                  'value': {'type': 'string', 'value': 'world'},
                }
              },
            },
          ),
        ),
      );
      socket.addUtf8Text(
        utf8.encode(
          json.encode(
            {
              'requestId': '1234',
              'serverType': 'key_value',
              'data': {
                'type': 'get',
                'data': {
                  'id': '1234',
                  'key': {'type': 'string', 'value': 'hello'},
                  'value': {'type': 'string', 'value': 'world'},
                }
              },
            },
          ),
        ),
      );

      expect(
          await socketQueue.next,
          matches(
              '{"messageId":".*","serverType":"key_value","requestId":"1234","error":"Invalid argument\\(s\\): No server with id 1234 found.*}'));
      expect(
          await socketQueue.next,
          matches(
              '{"messageId":".*","serverType":"key_value","requestId":"1234","error":"Invalid argument\\(s\\): No server with id 1234 found.*}'));
      expect(
          await socketQueue.next,
          matches(
              '{"messageId":".*","serverType":"key_value","requestId":"1234","error":"Invalid argument\\(s\\): No server with id 1234 found.*}'));
    });

    test('Test remove', () async {
      await socketQueue.skip(2);
      keyValueServer.backingMap['hello'] = 'world';
      expect(keyValueServer.backingMap.isNotEmpty, true);
      socket.addUtf8Text(
        utf8.encode(
          json.encode(
            {
              'requestId': '1234',
              'serverType': 'key_value',
              'data': {
                'type': 'remove',
                'data': {
                  'id': '123',
                  'key': {'type': 'string', 'value': 'hello'},
                }
              },
            },
          ),
        ),
      );
      await socketQueue.next;
      expect(keyValueServer.backingMap.isEmpty, true);
    });

    test('Test remove non-existing', () async {
      await socketQueue.skip(2);
      keyValueServer.backingMap['hello'] = 'world';
      expect(keyValueServer.backingMap.isNotEmpty, true);
      socket.addUtf8Text(
        utf8.encode(
          json.encode(
            {
              'requestId': '1234',
              'serverType': 'key_value',
              'data': {
                'type': 'remove',
                'data': {
                  'id': '123',
                  'key': {'type': 'string', 'value': 'hello2'},
                }
              },
            },
          ),
        ),
      );
      await socketQueue.next;
      expect(keyValueServer.backingMap.isNotEmpty, true);
    });

    test('Test remove non-existing', () async {
      await socketQueue.skip(2);
      keyValueServer.backingMap['hello'] = 'world';
      keyValueServer.backingMap['how'] = 'ya doing';
      expect(keyValueServer.backingMap.isNotEmpty, true);
      socket.addUtf8Text(
        utf8.encode(
          json.encode(
            {
              'requestId': '1234',
              'serverType': 'key_value',
              'data': {
                'type': 'clear',
                'data': {
                  'id': '123',
                }
              },
            },
          ),
        ),
      );
      await socketQueue.next;
      expect(keyValueServer.backingMap.isEmpty, true);
    });

    test('Test unknown command', () async {
      await socketQueue.skip(2);
      keyValueServer.backingMap['hello'] = 'world';
      socket.addUtf8Text(
        utf8.encode(
          json.encode(
            {
              'requestId': '1234',
              'serverType': 'key_value',
              'data': {
                'type': 'getSome',
              },
            },
          ),
        ),
      );
      expect(
          await socketQueue.next,
          matches(
              '{"messageId":".*","serverType":"key_value","requestId":"1234","error":"Invalid argument\\(s\\): Unknown key-value protocol command: getSome.*}'));
    });
  });
}

class SimpleMemoryKeyValueServer extends SimpleStringKeyValueServer {
  final Map<String, String> backingMap;
  var throwGetAll = false;

  SimpleMemoryKeyValueServer(this.backingMap)
      : super(
          'Test Server',
          keyOptions: {'key1', 'key2'},
          keySuggestions: {'key1'},
          keyIcons: {'key1': 'base64'},
        );

  @override
  Map<ValueWithType, StorageType> get typeForKey =>
      {const ValueWithType(StorageType.string, '2'): StorageType.string};

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
  Future<Iterable<MapEntry<String, String>>> get values => throwGetAll
      ? Future.error(ArgumentError('Error getting all'))
      : Future.value(backingMap.entries);
}
