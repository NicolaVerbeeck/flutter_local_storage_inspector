import 'dart:async';

import 'package:storage_inspector/src/servers/storage_server.dart';
import 'package:storage_inspector/src/servers/storage_type.dart';
import 'package:tuple/tuple.dart';

/// Storage server that handles key-value storage. Key value storage binds a
/// single key to a single value
abstract class KeyValueServer implements StorageServerInfo {
  /// All the different value types supported by the server
  Set<StorageType> get supportedValueTypes;

  /// All the different key types supported by the server
  Set<StorageType> get supportedKeyTypes;

  /// Gets (reloads if applicable) all values associated with this server
  Future<List<Tuple2<ValueWithType, ValueWithType>>> get allValues;

  /// Sets the value for the given key
  Future<void> set(ValueWithType key, ValueWithType newValue);

  /// Removes the value for the given key
  Future<void> remove(ValueWithType key);

  /// Clears all key-value pairs
  Future<void> clear();

  /// List of suggested key values to show in the UI. Useful as hints for the UI
  /// These values are NOT used to enforce [set] restrictions
  Set<ValueWithType> get keySuggestions;

  /// Restrict the keys to this list of keys. Useful as hints for the UI
  /// These values are NOT used to enforce [set] restrictions
  Set<ValueWithType> get keyOptions;

  /// Hint for which keys, which value types is required
  Map<ValueWithType, StorageType> get typeForKey;

  /// Provide an optional icon for the given key.
  /// Should be a square icon provided as base64, either PNG: 16x16 or 32x32 or SVG.
  Map<ValueWithType, String?> get keyIcons;
}
