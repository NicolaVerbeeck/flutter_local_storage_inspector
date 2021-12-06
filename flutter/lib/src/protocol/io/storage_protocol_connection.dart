import 'dart:io';

import 'package:storage_inspector/src/protocol/io/storage_protocol_server.dart';

/// Listener for new storage server client connections
// ignore: one_member_abstracts
abstract class StorageProtocolServerConnectionListener {
  /// Called when a new connection is made
  void onNewConnection(StorageProtocolConnection connection);
}

class StorageProtocolConnection {
  final WebSocket _socket;
  final StorageProtocolServer _server;
  final StorageProtocolServerConnectionListener _connectionListener;

  StorageProtocolConnection(
    this._socket,
    this._connectionListener,
    this._server,
  );

  void start() => _connectionListener.onNewConnection(this);

  void onMessage(String data) {}

  void send(List<int> message) => _socket.addUtf8Text(message);

  void close() => _socket.close();
}
