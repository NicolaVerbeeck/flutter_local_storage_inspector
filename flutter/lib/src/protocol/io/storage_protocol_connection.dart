import 'dart:async';
import 'dart:io';

import 'package:flutter/foundation.dart';
import 'package:storage_inspector/src/driver/storage_server.dart';

class IOStorageProtocolConnection implements StorageProtocolConnection {
  final WebSocket _socket;
  late final StorageProtocolServer _server;
  late final ValueChanged<StorageProtocolConnection> _connectionListener;

  StreamSubscription<dynamic>? _subscription;

  IOStorageProtocolConnection(
    this._socket,
  );

  @override
  void start() {
    _subscription = _socket.listen(
      (dynamic data) => _onMessage(data as String),
      onDone: () => _server.onConnectionClosed(this),
      onError: (Object _) => _server.onConnectionClosed(this),
      cancelOnError: true,
    );
    _connectionListener(this);
  }

  void _onMessage(String data) => _server.onMessage(data, this);

  @override
  void send(List<int> message) => _socket.addUtf8Text(message);

  @override
  void close() {
    _subscription?.cancel();
    _subscription = null;
    _socket.close();
  }

  @override
  Future<void> init(ValueChanged<StorageProtocolConnection> onConnectionReady,
      StorageProtocolServer server) {
    _connectionListener = onConnectionReady;
    _server = server;
    return Future.value();
  }
}
