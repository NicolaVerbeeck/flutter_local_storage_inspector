import 'dart:async';
import 'dart:convert';
import 'dart:typed_data';

import 'package:flutter/foundation.dart';
import 'package:storage_inspector/src/driver/storage_server.dart';
import 'package:storage_inspector/src/protocol/vm/socket/stub.dart'
    if (dart.library.html) 'package:storage_inspector/src/protocol/vm/socket/web.dart'
    if (dart.library.io) 'package:storage_inspector/src/protocol/vm/socket/io.dart';

class VMStorageProtocolConnection implements StorageProtocolConnection {
  final WebSocketWrapper _socket;
  late final StorageProtocolServer _server;
  late final ValueChanged<StorageProtocolConnection> _connectionListener;

  StreamSubscription? _subscription;

  VMStorageProtocolConnection(
    String targetIp,
    int targetPort,
  ) : _socket = WebSocketWrapper(uri: 'ws://$targetIp:$targetPort');

  @override
  void close() {
    _subscription?.cancel();
    _subscription = null;
    _socket.close(1005); // noStatusReceived reason
  }

  @override
  Future<void> init(
    ValueChanged<StorageProtocolConnection> onConnectionReady,
    StorageProtocolServer server,
  ) async {
    await _socket.init();
    _connectionListener = onConnectionReady;
    _server = server;
  }

  @override
  void send(List<int> message) {
    if (message is Uint8List) {
      _socket.sendBuffer(message.buffer);
    } else {
      _socket.sendBytes(message);
    }
  }

  @override
  void start() {
    _subscription = _socket.onMessage.listen(
      (dynamic data) {
        if (data is String) _onMessage(data);
        if (data is List<int>) _onMessage(utf8.decode(data));
      },
      onDone: () => _server.onConnectionClosed(this),
      onError: (Object _) => _server.onConnectionClosed(this),
      cancelOnError: true,
    );
    _connectionListener(this);
  }

  void _onMessage(String data) => _server.onMessage(data, this);
}
