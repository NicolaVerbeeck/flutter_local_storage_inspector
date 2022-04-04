import 'dart:developer';

import 'package:flutter/foundation.dart';
import 'package:storage_inspector/src/driver/storage_server.dart';
import 'package:storage_inspector/src/protocol/vm/vm_storage_protocol_connection.dart';

class VmServiceRawProtocolServer implements RawStorageProtocolServer {
  static VmServiceRawProtocolServer? _instance;

  factory VmServiceRawProtocolServer() =>
      _instance ??= VmServiceRawProtocolServer._();

  var _started = false;
  ValueChanged<StorageProtocolConnection>? _onNewConnection;

  @override
  int get port => -1;

  VmServiceRawProtocolServer._() {
    registerExtension('ext.storage_inspector.connect', (method, params) async {
      if (!_started) {
        return ServiceExtensionResponse.error(
            -1, 'Storage inspector is not started');
      }
      try {
        final targetPort = int.parse(params['port']!);
        final targetIp = params['ip']!;

        _onNewConnection
            ?.call(VMStorageProtocolConnection(targetIp, targetPort));

        return ServiceExtensionResponse.result('{"ok":true}');
      } catch (e) {
        return ServiceExtensionResponse.error(-2, e.toString());
      }
    });
  }

  @override
  Future<void> shutdown() {
    _started = false;
    _onNewConnection = null;
    return Future.value();
  }

  @override
  Future<void> start(ValueChanged<StorageProtocolConnection> onNewConnection) {
    _started = true;
    _onNewConnection = onNewConnection;
    return Future.value();
  }
}
