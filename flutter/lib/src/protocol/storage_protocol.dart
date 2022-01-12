import 'dart:convert';

import 'package:flutter/foundation.dart';
import 'package:storage_inspector/src/driver/storage_server.dart';
import 'package:storage_inspector/src/protocol/specific/file/file_protocol.dart';
import 'package:storage_inspector/src/protocol/specific/key_value/key_value_protocol.dart';
import 'package:storage_inspector/src/protocol/specific/sql/sql_database_protocol.dart';
import 'package:storage_inspector/storage_inspector.dart';
import 'package:uuid/uuid.dart';

class StorageProtocol {
  static const int version = 1;
  static const serverTypeKeyValue = 'key_value';
  static const serverTypeFile = 'file';
  static const serverTypeSql = 'sql';
  static const serverTypeInspector = 'inspector';
  static const _inspectorCommandUnpause = 'resume';

  final Set<StorageProtocolExtension> extensions;
  final KeyValueProtocol _keyValueProtocol;
  final FileProtocol _fileProtocol;
  final SQLDatabaseProtocol _dbProtocol;
  final String bundleId;
  final String? icon;
  final StorageProtocolListener listener;

  StorageProtocol({
    required this.extensions,
    required this.bundleId,
    required StorageProtocolServer server,
    required this.listener,
    this.icon,
  })  : _keyValueProtocol = KeyValueProtocol(server),
        _fileProtocol = FileProtocol(server),
        _dbProtocol = SQLDatabaseProtocol(server);

  List<int> serverIdentificationMessage({required bool paused}) => utf8.encode(
        json.encode(
          {
            'messageId': '1',
            'serverType': 'id',
            'data': {
              'version': version,
              'bundleId': bundleId,
              if (icon != null) 'icon': icon,
              'paused': paused,
            },
          },
        ),
      );

  Future<void> onMessage(
    String messageData,
    StorageProtocolConnection onConnection,
  ) async {
    final envelope = jsonDecode(messageData) as Map<String, dynamic>;
    final requestId = envelope['requestId'] as String?;
    final serverType = envelope['serverType'] as String;

    switch (serverType) {
      case serverTypeKeyValue:
        try {
          onConnection.send(encodeWithBody(
            serverType,
            data: await _keyValueProtocol
                .onMessage(envelope['data'] as Map<String, dynamic>),
            requestId: requestId,
          ));
        } catch (e, trace) {
          storageInspectorLogger(
              'Failed to handle key value message: $e\n $trace');
          onConnection.send(_reportError(serverType,
              requestId: requestId, error: e, stackTrace: trace));
        }
        break;
      case serverTypeFile:
        try {
          onConnection.send(encodeWithBody(
            serverType,
            data: await _fileProtocol
                .onMessage(envelope['data'] as Map<String, dynamic>),
            requestId: requestId,
          ));
        } catch (e, trace) {
          storageInspectorLogger('Failed to handle file message: $e\n $trace');
          onConnection.send(_reportError(serverType,
              requestId: requestId, error: e, stackTrace: trace));
        }
        break;
      case serverTypeSql:
        try {
          onConnection.send(encodeWithBody(
            serverType,
            data: await _dbProtocol
                .onMessage(envelope['data'] as Map<String, dynamic>),
            requestId: requestId,
          ));
        } catch (e, trace) {
          storageInspectorLogger('Failed to handle sql message: $e\n $trace');
          onConnection.send(_reportError(serverType,
              requestId: requestId, error: e, stackTrace: trace));
        }
        break;
      case serverTypeInspector:
        try {
          onConnection.send(encodeWithBody(
            serverType,
            data: await _handleInspectorMessage(
                envelope['data'] as Map<String, dynamic>),
            requestId: requestId,
          ));
        } catch (e, trace) {
          storageInspectorLogger(
              'Failed to handle key value message: $e\n $trace');
          onConnection.send(_reportError(serverType,
              requestId: requestId, error: e, stackTrace: trace));
        }
        break;
      default:
        onConnection.send(encodeWithBody(serverType,
            requestId: requestId, error: 'Unknown server type: $serverType'));
    }
  }

  Future<List<int>> keyValueServerIdentification(KeyValueServer server) async =>
      encodeWithBody(serverTypeKeyValue,
          data: await _keyValueProtocol.identify(server));

  Future<List<int>> fileServerIdentification(FileServer server) async =>
      encodeWithBody(serverTypeFile,
          data: await _fileProtocol.identify(server));

  Future<List<int>> sqlServerIdentification(SQLDatabaseServer server) async =>
      encodeWithBody(serverTypeSql,
          data: await _dbProtocol.identify(server));

  List<int> encodeWithBody(String serverType,
      {dynamic data, String? requestId, String? error}) {
    return utf8.encode(
      json.encode(
        <String, dynamic>{
          'messageId': const Uuid().v4(),
          'serverType': serverType,
          if (requestId != null) 'requestId': requestId,
          if (data != null) 'data': data,
          if (error != null) 'error': error,
        },
      ),
    );
  }

  List<int> _reportError(
    String serverType, {
    String? requestId,
    required Object error,
    required StackTrace stackTrace,
  }) {
    return encodeWithBody(
      serverType,
      requestId: requestId,
      error: '${error.toString()}\n${stackTrace.toString()}',
    );
  }

  Future<Map<String, dynamic>> _handleInspectorMessage(
      Map<String, dynamic> envelope) async {
    switch (envelope['type']) {
      case _inspectorCommandUnpause:
        await listener.onResumeSignal();
        return <String, dynamic>{'success': 'true'};
      default:
        return throw ArgumentError(
            'Unknown key-value protocol command: ${envelope['type']}');
    }
  }
}

@immutable
class StorageProtocolExtension {
  final String name;

  const StorageProtocolExtension(this.name);
}

abstract class StorageProtocolListener {
  Future<void> onResumeSignal();
}
