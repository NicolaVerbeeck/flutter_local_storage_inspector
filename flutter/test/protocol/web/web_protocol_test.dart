import 'package:flutter_test/flutter_test.dart';
import 'package:storage_inspector/src/protocol/web/web_storage_protocol_server.dart';

void main() {
  group('Web protocol tests', () {
    test('Test server, no side effects', () {
      final server = createRawProtocolServer(0);
      server.start((_) {});
      server.shutdown();
      expect(server.port, -1);
    });
  });
}
