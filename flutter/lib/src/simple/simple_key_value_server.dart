import 'package:flutter/foundation.dart';
import 'package:storage_inspector/src/key_value_server.dart';
import 'package:tuple/tuple.dart';
import 'package:storage_inspector/src/storage_type.dart';

/// Helper class for simple key-value servers that only support string keys
/// and string values
abstract class SimpleStringKeyValueServer implements KeyValueServer {
  @override
  final String name;

  @override
  final String? icon = null;

  @override
  final Set<StorageType> supportedValueTypes = const {StorageType.string};

  @override
  final Set<StorageType> supportedKeyTypes = const {StorageType.string};

  SimpleStringKeyValueServer(this.name);

  /// (Re-)Loads all values
  @protected
  Future<Iterable<MapEntry<String, String>>> get values;

  /// Sets the value for this key
  @protected
  Future<void> setValue(String key, String value);

  /// Removes the value for this key
  @protected
  Future<void> removeValue(String key);

  /// Remove all values for this key
  @protected
  Future<void> clearValues();

  @override
  Future<List<Tuple2<ValueWithType, ValueWithType>>> get allValues =>
      values.then(
        (values) => values
            .map(
              (e) => Tuple2(
                ValueWithType(StorageType.string, e.key),
                ValueWithType(StorageType.string, e.value),
              ),
            )
            .toList(growable: false),
      );

  @override
  Future<void> clear() => clearValues();

  @override
  Future<void> remove(String key) => removeValue(key);

  @override
  Future<void> set(ValueWithType key, ValueWithType newValue) {
    return setValue(key.value.toString(), newValue.value.toString());
  }
}
