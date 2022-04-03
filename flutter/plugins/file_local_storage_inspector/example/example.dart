import 'package:file_local_storage_inspector/file_local_storage_inspector.dart';
import 'package:flutter/material.dart';
import 'package:storage_inspector/storage_inspector.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();

  // ignore: avoid_print
  storageInspectorLogger = (e) => print(e);

  final driver = StorageServerDriver(
    bundleId: 'com.example.test',
    icon: '<some icon>',
  );

  final fileServer = DefaultFileServer(_documentsDirectory(), 'App Documents');
  driver.addFileServer(fileServer);

  // Don't wait for a connection from the instrumentation driver
  await driver.start(paused: false);

  // run app
  await Future<void>.delayed(const Duration(minutes: 15));

  await driver.stop(); //Optional when main ends
}

//Use path_provider to provide this
String _documentsDirectory() {
  return '.';
}
