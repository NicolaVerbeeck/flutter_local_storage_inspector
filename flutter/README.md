# Flutter local storage inspector
[![pub package](https://img.shields.io/pub/v/storage_inspector.svg?color=blue)](https://pub.dev/packages/storage_inspector)
[![pub points](https://badges.bar/sentry/pub%20points)](https://pub.dev/packages/storage_inspector/score)


This library allows you to inspect your apps local storage at runtime. During development, use the "Local storage inspector" plugin (pending) in your IntelliJ based IDE to inspect
and modify the local files, databases, models, ... of your app

## Features

* Bindings for shared preferences
* Bindings for secure storage
* Bindings for local files (based on dart:io)
* Bindings for drift databases (WIP)

## Getting started

The local storage inspector can handle any number of server instances you pass to it. To start, create the storage inspector driver:

```dart

final driver = StorageServerDriver(
    bundleId: 'com.example.test', //Used for identification
    port: 0, //Default 0, use 0 to automatically use a free port
    icon: '...' //Optional icon to visually identify the server. Base64 png or plain svg string
);
```

### Adding servers

Once the driver is created, you can register the individual servers you wish to expose:

```dart
final preferencesServer =
  PreferencesKeyValueServer(preferences, 'Base Preferences');

final secureStorageServer =
  SecureStorageKeyValueServer(secureStorage, 'Super secret storage');

final fileServer = DefaultFileServer('<cache dir path>', "Cache files");

driver.addKeyValueServer(preferencesServer);
driver.addKeyValueServer(secureStorageServer);
driver.addFileServer(fileServer);
```

### Built-in servers

The following servers are built-in with this package:

```dart
class PreferencesKeyValueServer{} //Wraps Flutter's SharedPreferences

class SecureStorageKeyValueServer{} //Wraps Flutter's FlutterSecureStorage package

class DefaultFileServer{} //Uses dart:io to serve a specified directory
```

### Start inspector

Once all the servers have been configured, you can start the driver to start the inspector and announcement server for the plugin:

```dart
await driver.start();
```

### Shutdown

If so required, you can shut down the server:

```dart
await driver.stop();
```

## Advanced usage

### Pausing

If you want to instrument some local storage before the application fully starts, you can start the driver with the `paused: true` argument. This will ensure that
`await driver.start(paused: true)` only returns when the inspector plugin/API sends the `resume` signal. The server does handle requests before this signal, allowing you to change
initial values on the fly.

### Writing custom integrations

To write your own integration into the system (eg: modify pure in-memory key value store, ...), simply implement the correct server interface that most closely matches your
requirements.

```dart
abstract class KeyValueServer{} //Most generic key-value server. Supports arbitrary key and value types

abstract class SimpleKeyValueServer{} //Simpler variant of the key value server, supports only string keys and values

abstract class FileServer{} //Server that server hierarchical storage of binary data   
```
