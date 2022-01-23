import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:storage_inspector/storage_inspector.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();

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

  final fileServer = DefaultFileServer(_documentsDirectory(), 'App Documents');
  driver.addFileServer(fileServer);

  // Don't wait for a connection from the instrumentation driver
  await driver.start(paused: false);

  // run app

  await driver.stop(); //Optional when main ends
}

//Use path_provider to provide this
String _documentsDirectory() {
  return '.';
}
