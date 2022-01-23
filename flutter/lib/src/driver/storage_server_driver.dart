import 'package:dart_service_announcement/dart_service_announcement.dart';
import 'package:storage_inspector/src/driver/storage_server.dart';
import 'package:storage_inspector/src/protocol/io/storage_protocol_server.dart'
    if (dart.library.html) 'package:storage_inspector/src/protocol/web/web_storage_protocol_server.dart';
import 'package:storage_inspector/src/protocol/storage_protocol.dart';
import 'package:storage_inspector/src/servers/sql_database_server.dart';
import 'package:storage_inspector/src/servers/file_server.dart';
import 'package:storage_inspector/src/servers/key_value_server.dart';
import 'package:uuid/uuid.dart';

const _announcementPort = 6396;

/// Tooling server that drives the storage inspector server
/// A single instance needs to be created and all storage
/// servers registered with it.
///
/// The servet does not start automatically upon creation,
/// call [start] first. When you are done with the
/// server, call [stop]
class StorageServerDriver extends ToolingServer {
  final tag = const Uuid().v4().substring(0, 6);

  final StorageProtocolServer _server;
  late final BaseServerAnnouncementManager _announcementManager;
  var _stopped = true;

  @override
  int get port => _server.port;

  @override
  int get protocolVersion => StorageProtocol.version;

  /// Create a new server driver.
  ///
  /// [icon] denotes an SVG or a base64 encoded png (16x16 or 32x32)
  ///
  /// [bundleId] id of the application being instrumented
  ///
  /// [port] port to use for the server instance, use 0 to use an
  /// automatic free port
  StorageServerDriver({
    String? icon,
    required String bundleId,
    int port = 0,
  }) : _server = StorageProtocolServer(
          server: createRawProtocolServer(port),
          icon: icon,
          bundleId: bundleId,
        ) {
    _announcementManager =
        ServerAnnouncementManager(bundleId, _announcementPort, this);
    if (icon != null) {
      _announcementManager.addExtension(IconExtension(icon));
    }
    _announcementManager.addExtension(TagExtension(tag));
  }

  /// Starts the server and announcement system for IDE integration
  ///
  /// You can have the driver start in [paused] mode. In this mode,
  /// the driver will start up but it will wait until it receives
  /// the `resume` command from the inspection tool/API. This allows
  /// you to pre-set (or un-set) some data that modifies your app's
  /// startup behaviour. Defaults to [false]
  Future<void> start({bool paused = false}) async {
    _stopped = false;
    await _server.start(paused: paused);

    _announcementManager.removeExtension(_PauseExtension(false));
    if (paused) {
      _announcementManager.addExtension(_PauseExtension(true));
    }

    await _announcementManager.start();

    // ignore: avoid_print
    print('Storage Inspector server running on $port [$tag][paused=$paused]');

    if (paused) {
      await _server.waitForResume();
      if (!_stopped) {
        _announcementManager.removeExtension(_PauseExtension(false));
        await _announcementManager.stop();
        await _announcementManager.start();
      }
    }
  }

  /// Shuts down the internal server and announcement system
  Future<void> stop() async {
    _stopped = true;
    await _server.shutdown();
    await _announcementManager.stop();
  }

  /// Register a key value server for inspection
  void addKeyValueServer(KeyValueServer server) {
    _server.addKeyValueServer(server);
  }

  /// Register a file server for inspection
  void addFileServer(FileServer server) {
    _server.addFileServer(server);
  }

  /// Register an sql server for inspection
  void addSQLServer(SQLDatabaseServer server) {
    _server.addSQLServer(server);
  }
}

const _pauseExtensionId = extensionUserStart + 1;

class _PauseExtension extends UserExtension {
  _PauseExtension(bool paused)
      : super(_pauseExtensionId, paused ? const [1] : const [0]);

  @override
  String get name => 'paused';

  @override
  // ignore: hash_and_equals
  bool operator ==(Object other) => other is _PauseExtension;
}
