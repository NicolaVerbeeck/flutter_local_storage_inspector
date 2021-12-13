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
      PreferencesKeyValueServer(preferences, "Preferences", keySuggestions: {
    const ValueWithType(StorageType.string, "testBool"),
    const ValueWithType(StorageType.string, "testInt"),
    const ValueWithType(StorageType.string, "testFloat"),
  });
  driver.addKeyValueServer(keyValueServer);

  await driver.start();

  // run app

  await driver.stop(); //Optional when main ends
}