// ignore_for_file: avoid_print

import 'dart:convert';
import 'dart:io';
import 'dart:typed_data';

import 'package:async/async.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:storage_inspector/storage_inspector.dart';

void main() {
  late StorageServerDriver driver;
  late SimpleFileServer fileServer;
  late WebSocket socket;
  late StreamQueue<dynamic> socketQueue;

  setUp(() async {
    driver = StorageServerDriver(
        bundleId: 'com.chimerapps.test', port: 0, icon: 'iconData');
    fileServer = SimpleFileServer({});
    driver.addFileServer(fileServer);

    await driver.start();

    socket = await WebSocket.connect('ws://localhost:${driver.port}');
    socketQueue = StreamQueue<dynamic>(socket);
  });

  tearDown(() async {
    await socket.close();
    await driver.stop();
  });

  group('File protocol and driver tests', () {
    test('Test identify', () async {
      await socketQueue.skip(1);

      final serverIdMessage =
          json.decode(await socketQueue.next as String) as Map<String, dynamic>;

      expect(serverIdMessage['messageId'], isNotNull);
      expect(serverIdMessage['serverType'], 'file');
      expect(serverIdMessage['data'], isNotNull);

      final idMessage = serverIdMessage['data'] as Map<String, dynamic>;
      expect(idMessage['type'], 'identify');
      expect(idMessage['data'], isNotNull);

      final idDataString = json.encode(idMessage['data']);
      expect(idDataString, '{"id":"123","name":"Test Server","icon":null}');

      await socket.close();
    });

    test('Test list empty', () async {
      await socketQueue.skip(2);
      socket.addUtf8Text(
        utf8.encode(
          json.encode(
            {
              'requestId': '1234',
              'serverType': 'file',
              'data': {
                'type': 'list',
                'data': {
                  'id': '123',
                  'root': '/',
                }
              },
            },
          ),
        ),
      );
      expect(
          await socketQueue.next,
          matches(
              '{"messageId":".*","serverType":"file","requestId":"1234","data":{"id":"123","data":\\[\\]}}'));
    });
    test('Test write', () async {
      await socketQueue.skip(2);
      expect(fileServer.backingMap.isEmpty, true);

      socket.addUtf8Text(
        utf8.encode(
          json.encode(
            {
              'requestId': '1234',
              'serverType': 'file',
              'data': {
                'type': 'write',
                'data': {
                  'id': '123',
                  'path': '/cache/test',
                  'data': 'VGVzdCAxMjM=',
                }
              },
            },
          ),
        ),
      );

      await socketQueue.next;
      expect(
          fileServer.backingMap[
              const FileInfo(path: '/cache/test', size: 8, isDir: false)],
          'Test 123');
    });
    test('Test read', () async {
      await socketQueue.skip(2);
      fileServer.backingMap[
              const FileInfo(path: '/cache/test', size: 8, isDir: false)] =
          'Test 123';

      socket.addUtf8Text(
        utf8.encode(
          json.encode(
            {
              'requestId': '1234',
              'serverType': 'file',
              'data': {
                'type': 'read',
                'data': {
                  'id': '123',
                  'path': '/cache/test',
                }
              },
            },
          ),
        ),
      );

      expect(
          await socketQueue.next,
          matches(
              '{"messageId":".*","serverType":"file","requestId":"1234","data":{"id":"123","data":"VGVzdCAxMjM="}'));
    });

    test('Test actions wrong server', () async {
      await socketQueue.skip(2);
      expect(fileServer.backingMap.isEmpty, true);

      socket.addUtf8Text(
        utf8.encode(
          json.encode(
            {
              'requestId': '1234',
              'serverType': 'file',
              'data': {
                'type': 'list',
                'data': {
                  'id': '1234',
                  'root': '/',
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
              'serverType': 'file',
              'data': {
                'type': 'remove',
                'data': {
                  'id': '1234',
                  'path': '/',
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
              'serverType': 'file',
              'data': {
                'type': 'read',
                'data': {
                  'id': '1234',
                  'path': '/',
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
              'serverType': 'file',
              'data': {
                'type': 'write',
                'data': {
                  'id': '1234',
                  'path': '/',
                  'data': 'VGVzdCAxMjM=',
                }
              },
            },
          ),
        ),
      );

      expect(
          await socketQueue.next,
          matches(
              '{"messageId":".*","serverType":"file","requestId":"1234","error":"Invalid argument\\(s\\): No server with id 1234 found.*}'));
      expect(
          await socketQueue.next,
          matches(
              '{"messageId":".*","serverType":"file","requestId":"1234","error":"Invalid argument\\(s\\): No server with id 1234 found.*}'));
      expect(
          await socketQueue.next,
          matches(
              '{"messageId":".*","serverType":"file","requestId":"1234","error":"Invalid argument\\(s\\): No server with id 1234 found.*}'));
      expect(
          await socketQueue.next,
          matches(
              '{"messageId":".*","serverType":"file","requestId":"1234","error":"Invalid argument\\(s\\): No server with id 1234 found.*}'));
    });

    test('Test remove', () async {
      await socketQueue.skip(2);
      fileServer.backingMap[
          const FileInfo(path: '/hello', size: 5, isDir: false)] = 'world';
      expect(fileServer.backingMap.isNotEmpty, true);
      socket.addUtf8Text(
        utf8.encode(
          json.encode(
            {
              'requestId': '1234',
              'serverType': 'file',
              'data': {
                'type': 'remove',
                'data': {
                  'id': '123',
                  'path': '/hello',
                }
              },
            },
          ),
        ),
      );
      await socketQueue.next;
      expect(fileServer.backingMap.isEmpty, true);
    });

    test('Test unknown command', () async {
      await socketQueue.skip(2);
      fileServer.backingMap[
          const FileInfo(path: 'hello', size: 5, isDir: false)] = 'world';
      socket.addUtf8Text(
        utf8.encode(
          json.encode(
            {
              'requestId': '1234',
              'serverType': 'file',
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
              '{"messageId":".*","serverType":"file","requestId":"1234","error":"Invalid argument\\(s\\): Unknown file protocol command: getSome.*}'));
    });
  });
}

class SimpleFileServer extends FileServer {
  final Map<FileInfo, String> backingMap;
  var throwGetAll = false;

  SimpleFileServer(this.backingMap);

  @override
  final String id = '123';

  @override
  final icon = null;

  @override
  final name = 'Test Server';

  @override
  Future<List<FileInfo>> browse(String root) {
    return Future.value(backingMap.keys.toList());
  }

  @override
  Future<void> delete(String path, {required bool recursive}) {
    if (recursive) {
      backingMap.removeWhere((key, value) => key.path.startsWith(path));
    } else {
      backingMap.remove(FileInfo(path: path, size: 0, isDir: false));
    }
    return Future.value();
  }

  @override
  Future<Uint8List> read(String path) {
    return Future.value(
      Uint8List.fromList(
        utf8.encode(
          backingMap[FileInfo(
            path: path,
            size: 0,
            isDir: false,
          )]!,
        ),
      ),
    );
  }

  @override
  Future<void> write(String path, Uint8List data) {
    backingMap[FileInfo(path: path, size: 0, isDir: false)] = utf8.decode(data);
    return Future.value();
  }

  @override
  Future<void> move({required String path, required String newPath}) {
    final oldData =
        backingMap.remove(FileInfo(path: path, size: 0, isDir: false))!;
    backingMap[FileInfo(path: newPath, size: 0, isDir: false)] = oldData;
    return Future.value();
  }
}
