import 'dart:io';

import 'package:dart_service_announcement/dart_service_announcement.dart';
import 'package:storage_inspector/src/protocol/io/storage_protocol_connection.dart';
import 'package:storage_inspector/storage_inspector.dart';
import 'package:synchronized/synchronized.dart';
import 'package:uuid/uuid.dart';

class StorageProtocolServer extends ToolingServer {
  HttpServer? _server;
  final int _requestedPort;
  final String tag = const Uuid().v4().substring(0, 6);
  final _lock = Lock();
  final _connections = <StorageProtocolConnection>[];
  final _keyValueServers = <KeyValueServer>[];

  @override
  int get port => _server?.port ?? -1;

  @override
  final int protocolVersion;

  List<KeyValueServer> get keyValueServers => _keyValueServers;

  late final StorageProtocolServerConnectionListener connectionListener;

  StorageProtocolServer({
    required int port,
    required this.protocolVersion,
  }) : _requestedPort = port;

  /// Starts the server
  Future<void> start() async {
    _server =
        await HttpServer.bind(InternetAddress.loopbackIPv4, _requestedPort)
          ..transform(WebSocketTransformer()).listen(_onNewConnection);
    storageInspectorLogger('Storage Inspector server running on $port [$tag]');
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

  void sendToAll(List<int> message) {
    _lock.synchronized(() async {
      for (final socket in _connections) {
        socket.send(message);
      }
    });
  }

  void _onNewConnection(WebSocket socket) {
    final connection =
        StorageProtocolConnection(socket, connectionListener, this);
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
}
