import 'package:flutter/material.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:secure_storage_local_storage_inspector/secure_storage_local_storage_inspector.dart';
import 'package:storage_inspector/storage_inspector.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();

  // ignore: avoid_print
  storageInspectorLogger = (e) => print(e);

  const storage = FlutterSecureStorage();

  final driver = StorageServerDriver(
    bundleId: 'com.example.test',
    icon: '<some icon>',
  );
  final keyValueServer =
      SecureStorageKeyValueServer(storage, 'Secure', keySuggestions: {
    'testBool',
    'testInt',
    'testFloat',
  });
  driver.addKeyValueServer(keyValueServer);

  // Don't wait for a connection from the instrumentation driver
  await driver.start(paused: false);

  // run app
  await Future<void>.delayed(const Duration(minutes: 15));

  await driver.stop(); //Optional when main ends
}
