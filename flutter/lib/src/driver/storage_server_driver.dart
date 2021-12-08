import 'package:dart_service_announcement/dart_service_announcement.dart';
import 'package:storage_inspector/src/driver/storage_server.dart';
import 'package:storage_inspector/src/protocol/io/storage_protocol_server.dart'
    if (dart.library.html) 'package:storage_inspector/src/protocol/web/web_storage_protocol_server.dart';
import 'package:storage_inspector/src/protocol/storage_protocol.dart';
import 'package:storage_inspector/src/servers/key_value_server.dart';
import 'package:uuid/uuid.dart';

const _announcementPort = 6396;

class StorageServerDriver extends ToolingServer {
  final tag = const Uuid().v4().substring(0, 6);

  final StorageProtocolServer _server;
  late final BaseServerAnnouncementManager _announcementManager;

  @override
  int get port => _server.port;

  @override
  int get protocolVersion => StorageProtocol.version;

  StorageServerDriver({
    required int port,
    String? icon,
    required String bundleId,
  }) : _server = StorageProtocolServer(
          server: createRawProtocolServer(port),
          icon: icon,
          bundleId: bundleId,
        ) {
    _announcementManager = ServerAnnouncementManager(bundleId, _announcementPort, this);
    if (icon != null) {
      _announcementManager.addExtension(IconExtension(icon));
    }
    _announcementManager.addExtension(TagExtension(tag));
  }

  Future<void> start() async {
    await _server.start();
    await _announcementManager.start();

    // ignore: avoid_print
    print('Storage Inspector server running on $port [$tag]');
  }

  Future<void> stop() async {
    await _server.shutdown();
    await _announcementManager.stop();
  }

  void addKeyValueServer(KeyValueServer server) {
    _server.addKeyValueServer(server);
  }
}
