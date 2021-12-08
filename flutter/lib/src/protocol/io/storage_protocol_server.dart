import 'dart:io';

import 'package:flutter/foundation.dart';
import 'package:storage_inspector/src/driver/storage_server.dart';
import 'package:storage_inspector/src/protocol/io/storage_protocol_connection.dart';

RawStorageProtocolServer createRawProtocolServer(int port) =>
    IOStorageProtocolServer(port: port);

class IOStorageProtocolServer implements RawStorageProtocolServer {
  final int _requestedPort;
  late ValueChanged<StorageProtocolConnection> _onNewConnection;

  HttpServer? _server;

  @override
  int get port => _server?.port ?? -1;

  IOStorageProtocolServer({
    required int port,
  }) : _requestedPort = port;

  /// Starts the server
  @override
  Future<void> start(
      ValueChanged<StorageProtocolConnection> onNewConnection) async {
    _onNewConnection = onNewConnection;
    _server =
        await HttpServer.bind(InternetAddress.loopbackIPv4, _requestedPort)
          ..transform(WebSocketTransformer()).listen(_onNewSocketConnection);
  }

  /// Stops the server
  @override
  Future<void> shutdown() async {
    final server = _server;
    _server = null;
    if (server != null) {
      await server.close(force: true);
    }
  }

  void _onNewSocketConnection(WebSocket socket) {
    _onNewConnection(IOStorageProtocolConnection(socket));
  }
}
