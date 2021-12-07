import 'dart:io';

import 'package:flutter/foundation.dart';
import 'package:storage_inspector/src/protocol/io/storage_protocol_server.dart';

class StorageProtocolConnection {
  final WebSocket _socket;
  final StorageProtocolServer _server;
  final ValueChanged<StorageProtocolConnection> _connectionListener;

  StorageProtocolConnection(
    this._socket,
    this._connectionListener,
    this._server,
  );

  void start() => _connectionListener(this);

  void onMessage(String data) => _server.onMessage(data, this);

  void send(List<int> message) => _socket.addUtf8Text(message);

  void close() => _socket.close();
}
