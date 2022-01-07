import 'dart:convert';

import 'package:flutter/foundation.dart';
import 'package:storage_inspector/src/driver/storage_server.dart';
import 'package:storage_inspector/src/servers/file_server.dart';

class FileProtocol {
  static const _commandList = 'list';
  static const _commandReadContents = 'read';
  static const _commandWriteContents = 'write';
  static const _commandDelete = 'remove';

  final StorageProtocolServer _server;

  FileProtocol(this._server);

  Future<Map<String, dynamic>> identify(
    FileServer server,
  ) {
    return SynchronousFuture(<String, dynamic>{
      'type': 'identify',
      'data': {
        'id': server.id,
        'name': server.name,
        'icon': server.icon,
      },
    });
  }

  Future<Map<String, dynamic>> onMessage(Map<String, dynamic> jsonData) {
    switch (jsonData['type']) {
      case _commandList:
        return _handleList(
          jsonData['data']['id'] as String,
          jsonData['data']['root'] as String,
        );
      case _commandReadContents:
        return _handleRead(
          jsonData['data']['id'] as String,
          jsonData['data']['path'] as String,
        );
      case _commandWriteContents:
        return _handleWrite(
          jsonData['data']['id'] as String,
          jsonData['data']['path'] as String,
          jsonData['data']['data'] as String,
        );
      case _commandDelete:
        return _handleRemove(
          jsonData['data']['id'] as String,
          jsonData['data']['path'] as String,
        );
      default:
        return Future.error(ArgumentError(
            'Unknown file protocol command: ${jsonData['type']}'));
    }
  }

  Future<Map<String, dynamic>> _handleList(String id, String root) async {
    final server = getServerWithId(id);
    final fileList = await server.browse(root);

    return <String, dynamic>{
      'id': id,
      'data': fileList
          .map((file) => {
                'path': file.path,
                'size': file.size,
                'isDir': file.isDir,
              })
          .toList(),
    };
  }

  Future<Map<String, dynamic>> _handleRead(String id, String path) async {
    final server = getServerWithId(id);
    final fileData = await server.read(path);

    return <String, dynamic>{
      'id': id,
      'data': base64.encode(fileData),
    };
  }

  Future<Map<String, dynamic>> _handleWrite(
      String id, String path, String data) async {
    final server = getServerWithId(id);
    await server.write(path, base64.decode(data));

    return <String, dynamic>{
      'id': id,
      'data': {
        'success': true,
      },
    };
  }

  Future<Map<String, dynamic>> _handleRemove(String id, String path) async {
    final server = getServerWithId(id);
    await server.delete(path, recursive: true);

    return <String, dynamic>{
      'id': id,
      'data': {
        'success': true,
      },
    };
  }

  FileServer getServerWithId(String id) {
    return _server.fileServers.firstWhere(
      (element) => element.id == id,
      orElse: () => throw ArgumentError('No server with id $id found'),
    );
  }
}
