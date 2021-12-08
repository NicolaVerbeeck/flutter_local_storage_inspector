import 'package:storage_inspector/storage_inspector.dart';

import 'protocol/specific/key_value/key_value_server_test.dart';

void main() async {
  final driver = StorageServerDriver(bundleId: 'com.chimerapps.test', port: 9999, icon: 'iconData');
  final keyValueServer = SimpleMemoryKeyValueServer({});
  driver.addKeyValueServer(keyValueServer);

  await driver.start();

  await Future<void>.delayed(const Duration(seconds: 100));
  await driver.stop();
}
