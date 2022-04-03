import 'package:flutter/material.dart';
import 'package:preferences_local_storage_inspector/preferences_local_storage_inspector.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:storage_inspector/storage_inspector.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();

  // ignore: avoid_print
  storageInspectorLogger = (e) => print(e);

  final preferences = await SharedPreferences.getInstance();

  final driver = StorageServerDriver(
    bundleId: 'com.example.test',
    icon: '<some icon>',
  );
  final keyValueServer =
      PreferencesKeyValueServer(preferences, 'Preferences', keySuggestions: {
    const ValueWithType(StorageType.string, 'testBool'),
    const ValueWithType(StorageType.string, 'testInt'),
    const ValueWithType(StorageType.string, 'testFloat'),
  });
  driver.addKeyValueServer(keyValueServer);

  // Don't wait for a connection from the instrumentation driver
  await driver.start(paused: false);

  // run app
  await Future<void>.delayed(const Duration(minutes: 15));

  await driver.stop(); //Optional when main ends
}
