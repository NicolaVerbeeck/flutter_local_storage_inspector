import 'dart:convert';

import 'package:flutter/foundation.dart';
import 'package:storage_inspector/src/protocol/io/storage_protocol_connection.dart';
import 'package:storage_inspector/src/protocol/io/storage_protocol_server.dart';
import 'package:storage_inspector/src/protocol/specific/key_value/key_value_protocol.dart';
import 'package:storage_inspector/storage_inspector.dart';
import 'package:uuid/uuid.dart';

class StorageProtocol {
  static const int version = 1;
  static const serverTypeKeyValue = 'key_value';

  final Set<StorageProtocolExtension> extensions;
  final KeyValueProtocol _keyValueProtocol;
  final String bundleId;
  final String? icon;

  StorageProtocol({
    required this.extensions,
    required this.bundleId,
    required StorageProtocolServer server,
    this.icon,
  }) : _keyValueProtocol = KeyValueProtocol(server);

  List<int> get serverIdentificationMessage => utf8.encode(
        json.encode(
          {
            'messageId': '1',
            'serverType': 'id',
            'data': {
              'version': version,
              'bundleId': bundleId,
              if (icon != null) 'icon': icon,
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
      default:
        onConnection.send(encodeWithBody(serverType,
            requestId: requestId, error: 'Unknown server type: $serverType'));
    }
  }

  Future<List<int>> keyValueServerIdentification(KeyValueServer server) async =>
      encodeWithBody(serverTypeKeyValue,
          data: await _keyValueProtocol.identify(server));

  List<int> encodeWithBody(String serverType,
      {dynamic data, String? requestId, String? error}) {
    return utf8.encode(
      json.encode(
        {
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
}

@immutable
class StorageProtocolExtension {
  final String name;

  const StorageProtocolExtension(this.name);
}
