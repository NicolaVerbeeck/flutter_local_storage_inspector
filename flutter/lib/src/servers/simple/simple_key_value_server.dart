import 'package:flutter/foundation.dart';
import 'package:storage_inspector/src/servers/key_value_server.dart';
import 'package:storage_inspector/src/servers/storage_type.dart';
import 'package:tuple/tuple.dart';

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

  @override
  final Set<ValueWithType> keySuggestions;

  @override
  final Set<ValueWithType> keyOptions;

  @override
  final typeForKey = const {};

  @override
  final Map<ValueWithType, String> keyIcons;

  SimpleStringKeyValueServer(
    this.name, {
    Set<String> keySuggestions = const {},
    Set<String> keyOptions = const {},
    Map<String, String> keyIcons = const {},
  })  : keySuggestions = keySuggestions
            .map((e) => ValueWithType(StorageType.string, e))
            .toSet(),
        keyOptions =
            keyOptions.map((e) => ValueWithType(StorageType.string, e)).toSet(),
        keyIcons = keyIcons.map(
          (key, value) =>
              MapEntry(ValueWithType(StorageType.string, key), value),
        );

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
  Future<void> remove(ValueWithType key) => removeValue(key.value.toString());

  @override
  Future<void> set(ValueWithType key, ValueWithType newValue) {
    return setValue(key.value.toString(), newValue.value.toString());
  }
}
