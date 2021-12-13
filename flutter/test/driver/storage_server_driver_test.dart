import 'package:flutter_test/flutter_test.dart';
import 'package:storage_inspector/src/driver/storage_server_driver.dart';
import 'package:storage_inspector/src/protocol/storage_protocol.dart';

void main() {
  group('Driver tests', () {
    test('Test protocol version', () {
      expect(
          StorageServerDriver(
                  bundleId: 'com.chimerapps.test', port: 0, icon: 'iconData')
              .protocolVersion,
          StorageProtocol.version);
    });
  });
}
