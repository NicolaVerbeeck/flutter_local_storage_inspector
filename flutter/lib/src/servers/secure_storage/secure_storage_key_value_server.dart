import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:storage_inspector/src/servers/simple/simple_key_value_server.dart';
import 'package:storage_inspector/storage_inspector.dart';
import 'package:uuid/uuid.dart';

/// Key value server that serves values from the given secure storage object
class SecureStorageKeyValueServer extends SimpleStringKeyValueServer {
  final FlutterSecureStorage _storage;

  @override
  final String id = const Uuid().v4();

  SecureStorageKeyValueServer(
    this._storage,
    String name, {
    Set<String> keySuggestions = const {},
    Set<String> keyOptions = const {},
  }) : super(
          name,
          keyOptions: keyOptions,
          keySuggestions: keySuggestions,
        );

  @override
  Future<void> clearValues() => _storage.deleteAll();

  @override
  Future<void> removeValue(String key) => _storage.delete(key: key);

  @override
  Future<void> setValue(String key, String value) =>
      _storage.write(key: key, value: value);

  @override
  Future<Iterable<MapEntry<String, String>>> get values =>
      _storage.readAll().then((value) => value.entries);

  @override
  Future<String> getValue(String key) => _storage.read(key: key).then(
      // ignore: prefer_if_null_operators
      (value) => value == null ? throw ArgumentError('Value not set') : value);
}
