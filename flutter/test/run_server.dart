// ignore_for_file: avoid_print

import 'package:flutter_test/flutter_test.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:storage_inspector/storage_inspector.dart';

void main() async {
  TestWidgetsFlutterBinding.ensureInitialized();

  SharedPreferences.setMockInitialValues({
    'testBool': true,
    'testInt': 1,
  });
  final preferences = await SharedPreferences.getInstance();

  final driver = StorageServerDriver(
    bundleId: 'com.chimerapps.test',
    port: 0,
    icon:
        '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',
  );
  final keyValueServer =
      PreferencesKeyValueServer(preferences, "Preferences", keySuggestions: {
    const ValueWithType(StorageType.string, "testBool"),
    const ValueWithType(StorageType.string, "testInt"),
    const ValueWithType(StorageType.string, "testFloat"),
  });
  driver.addKeyValueServer(keyValueServer);

  driver.addFileServer(DefaultFileServer('/', 'Test folder'));

  print('Starting driver');
  await driver.start(paused: true);
  print('Driver started');

  await Future<void>.delayed(const Duration(seconds: 10000));
  await driver.stop();
}
