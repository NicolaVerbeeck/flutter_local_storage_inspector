import 'package:storage_inspector/src/storage_server.dart';
import 'package:storage_inspector/src/storage_type.dart';
import 'package:tuple/tuple.dart';

/// Storage server that handles key-value storage. Key value storage binds a
/// single key to a single value
abstract class KeyValueServer implements StorageServer {
  /// All the different value types supported by the server
  Set<StorageType> get supportedValueTypes;

  /// All the different key types supported by the server
  Set<StorageType> get supportedKeyTypes;

  /// Gets (reloads if applicable) all values associated with this server
  Future<List<Tuple2<ValueWithType, ValueWithType>>> get allValues;

  /// Sets the value for the given key
  Future<void> set(ValueWithType key, ValueWithType newValue);

  /// Removes the value for the given key
  Future<void> remove(String key);

  /// Clears all key-value pairs
  Future<void> clear();
}
