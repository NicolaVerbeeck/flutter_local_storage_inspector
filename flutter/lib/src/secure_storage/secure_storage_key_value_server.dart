import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:storage_inspector/src/simple/simple_key_value_server.dart';

/// Key value server that serves values from the given secure storage object
class SecureStorageKeyValueServer extends SimpleStringKeyValueServer {
  final FlutterSecureStorage _storage;

  SecureStorageKeyValueServer(
    this._storage,
    String name,
  ) : super(name);

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
}
