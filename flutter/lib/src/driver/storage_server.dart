import 'dart:async';

import 'package:flutter/foundation.dart';
import 'package:storage_inspector/src/protocol/storage_protocol.dart';
import 'package:storage_inspector/src/util/observable_server_list.dart';
import 'package:storage_inspector/storage_inspector.dart';
import 'package:synchronized/synchronized.dart';

abstract class StorageProtocolConnection {
  void init(ValueChanged<StorageProtocolConnection> onConnectionReady,
      StorageProtocolServer server);

  void start();

  void onMessage(String data);

  void send(List<int> message);

  void close();
}

abstract class RawStorageProtocolServer {
  int get port;

  Future<void> start(ValueChanged<StorageProtocolConnection> onNewConnection);

  Future<void> shutdown();
}

class StorageProtocolServer implements StorageProtocolListener {
  final RawStorageProtocolServer _server;

  final _connections = <StorageProtocolConnection>[];
  final _lock = Lock();
  late final StorageProtocol _protocol;
  final _keyValueServers = ObservableList<KeyValueServer>();
  final _fileServers = ObservableList<FileServer>();
  var _paused = false;
  late Completer<void> _resumeFuture;

  int get port => _server.port;

  List<KeyValueServer> get keyValueServers => _keyValueServers.servers;

  List<FileServer> get fileServers => _fileServers.servers;

  StorageProtocolServer({
    String? icon,
    required String bundleId,
    required RawStorageProtocolServer server,
  }) : _server = server {
    _protocol = StorageProtocol(
      extensions: {},
      bundleId: bundleId,
      server: this,
      icon: icon,
      listener: this,
    );
  }

  /// Starts the server
  Future<void> start({bool paused = false}) async {
    _resumeFuture = Completer();
    _paused = paused;
    _server.start(_onNewConnection);
    if (!paused) {
      _resumeFuture.complete();
    }
  }

  /// Stops the server
  Future<void> shutdown() async {
    if (!_resumeFuture.isCompleted) {
      _resumeFuture.complete();
    }
    await _server.shutdown();
    await _lock.synchronized(() async {
      for (final connection in _connections) {
        connection.close();
      }
      _connections.clear();
    });
  }

  Future<void> waitForResume() => _resumeFuture.future;

  void _onNewConnection(StorageProtocolConnection connection) {
    connection.init(_onNewConnectionReady, this);
    _lock.synchronized(() async {
      _connections.add(connection);
    });

    connection.start();
  }

  Future<void> _onNewConnectionReady(StorageProtocolConnection value) async {
    try {
      value.send(_protocol.serverIdentificationMessage(paused: _paused));

      for (final server in _keyValueServers.servers) {
        value.send(await _protocol.keyValueServerIdentification(server));
      }
      for (final server in _fileServers.servers) {
        value.send(await _protocol.fileServerIdentification(server));
      }
    } catch (e, trace) {
      storageInspectorLogger('Failed to send message: $e\n $trace');
      try {
        value.close();
      } catch (_) {}
      return;
    }
  }

  Future<void> onMessage(
      String data, StorageProtocolConnection connection) async {
    try {
      await _protocol.onMessage(data, connection);
    } catch (e, trace) {
      storageInspectorLogger('Failed to handle message: $e\n $trace');
      try {
        connection.close();
      } catch (_) {}
    }
  }

  void onConnectionClosed(StorageProtocolConnection connection) {
    _lock.synchronized(() async {
      _connections.remove(connection);
    });
  }

  void addKeyValueServer(KeyValueServer server) {
    _keyValueServers.add(server);
  }

  void addFileServer(FileServer server) {
    _fileServers.add(server);
  }

  @override
  Future<void> onResumeSignal() {
    if (!_resumeFuture.isCompleted) {
      _resumeFuture.complete();
    }
    return Future.value();
  }
}
