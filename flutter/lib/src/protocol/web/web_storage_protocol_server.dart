import 'package:flutter/foundation.dart';
import 'package:storage_inspector/src/driver/storage_server.dart';

RawStorageProtocolServer createRawProtocolServer(int port) {
  return _NoOpRawProtocolServer();
}

class _NoOpRawProtocolServer implements RawStorageProtocolServer {
  @override
  int get port => -1;

  @override
  Future<void> shutdown() => Future.value();

  @override
  Future<void> start(ValueChanged<StorageProtocolConnection> onNewConnection) =>
      Future.value();
}
