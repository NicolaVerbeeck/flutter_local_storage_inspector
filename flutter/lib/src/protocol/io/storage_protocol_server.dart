import 'dart:io';

import 'package:storage_inspector/src/protocol/io/storage_protocol_connection.dart';
import 'package:storage_inspector/src/protocol/storage_protocol.dart';
import 'package:storage_inspector/src/util/observable_server_list.dart';
import 'package:storage_inspector/storage_inspector.dart';
import 'package:synchronized/synchronized.dart';

class StorageProtocolServer {
  final int _requestedPort;

  HttpServer? _server;
  final _lock = Lock();
  final _connections = <StorageProtocolConnection>[];

  late final StorageProtocol _protocol;
  final _keyValueServers = ObservableServerList<KeyValueServer>();

  int get port => _server?.port ?? -1;

  List<KeyValueServer> get keyValueServers => _keyValueServers.servers;

  StorageProtocolServer({
    required int port,
    String? icon,
    required String bundleId,
  }) : _requestedPort = port {
    _protocol = StorageProtocol(icon: icon, bundleId: bundleId, extensions: {}, server: this);

    //TODO listen and send new server info
  }

  /// Starts the server
  Future<void> start() async {
    _server = await HttpServer.bind(InternetAddress.loopbackIPv4, _requestedPort)
      ..transform(WebSocketTransformer()).listen(_onNewConnection);
  }

  /// Stops the server
  Future<void> shutdown() async {
    final server = _server;
    _server = null;
    if (server != null) {
      await server.close(force: true);
    }
    await _lock.synchronized(() async {
      for (final socket in _connections) {
        socket.close();
      }
    });
  }

  // void _sendToAll(List<int> message) {
  //   _lock.synchronized(() async {
  //     for (final socket in _connections) {
  //       socket.send(message);
  //     }
  //   });
  // }

  void _onNewConnection(WebSocket socket) {
    final connection = StorageProtocolConnection(socket, _onNewConnectionReady, this);
    _lock.synchronized(() async {
      _connections.add(connection);
      socket.listen(
        (data) => connection.onMessage(data as String),
        onDone: () => _onSocketClosed(connection),
        onError: (_) => _onSocketClosed(connection),
        cancelOnError: true,
      );
    });

    connection.start();
  }

  void _onSocketClosed(StorageProtocolConnection socket) {
    _lock.synchronized(() async {
      _connections.remove(socket);
    });
  }

  Future<void> onMessage(String data, StorageProtocolConnection connection) async {
    try {
      await _protocol.onMessage(data, connection);
    } catch (e, trace) {
      storageInspectorLogger('Failed to handle message: $e\n $trace');
      try {
        connection.close();
      } catch (_) {}
    }
  }

  Future<void> _onNewConnectionReady(StorageProtocolConnection value) async {
    try {
      value.send(_protocol.serverIdentificationMessage);

      for (final server in _keyValueServers.servers) {
        value.send(await _protocol.keyValueServerIdentification(server));
      }
    } catch (e, trace) {
      storageInspectorLogger('Failed to send message: $e\n $trace');
      try {
        value.close();
      } catch (_) {}
      return;
    }
  }

  void addKeyValueServer(KeyValueServer server) {
    _keyValueServers.addServer(server);
  }
}
