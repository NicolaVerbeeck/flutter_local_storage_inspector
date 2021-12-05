import 'package:shared_preferences/shared_preferences.dart';
import 'package:storage_inspector/src/key_value_server.dart';
import 'package:tuple/tuple.dart';
import 'package:storage_inspector/src/storage_type.dart';

/// Key value server that serves values to/from the given shared preferences
/// instance
class PreferencesKeyValueServer implements KeyValueServer {
  final SharedPreferences _preferences;

  @override
  final String name;

  @override
  final String? icon = null;

  @override
  final Set<StorageType> supportedValueTypes = const {
    StorageType.string,
    StorageType.integer,
    StorageType.double,
    StorageType.boolean,
    StorageType.stringList,
  };

  @override
  final Set<StorageType> supportedKeyTypes = const {StorageType.string};

  PreferencesKeyValueServer(
    this._preferences,
    this.name,
  );

  @override
  Future<List<Tuple2<ValueWithType, ValueWithType>>> get allValues async {
    await _preferences.reload();

    return _preferences.getKeys().map((key) {
      final rawValue = _preferences.get(key);

      final StorageType type;
      if (rawValue is bool) {
        type = StorageType.boolean;
      } else if (rawValue is String) {
        type = StorageType.string;
      } else if (rawValue is List<String>) {
        type = StorageType.stringList;
      } else if (rawValue is int) {
        type = StorageType.integer;
      } else if (rawValue is double) {
        type = StorageType.double;
      } else {
        throw ArgumentError('Failed to serialize preferences, '
            'unknown type: ${rawValue.runtimeType}');
      }
      final value = ValueWithType(type, rawValue);

      return Tuple2(ValueWithType(StorageType.string, key), value);
    }).toList(growable: false);
  }

  @override
  Future<void> clear() => _preferences.clear();

  @override
  Future<void> remove(String key) => _preferences.remove(key);

  @override
  Future<void> set(ValueWithType key, ValueWithType newValue) async {
    final preferenceKey = key.value.toString();

    switch (newValue.type) {
      case StorageType.string:
        await _preferences.setString(preferenceKey, newValue.value);
        break;
      case StorageType.integer:
        await _preferences.setInt(preferenceKey, newValue.value);
        break;
      case StorageType.double:
        await _preferences.setDouble(preferenceKey, newValue.value);
        break;
      case StorageType.boolean:
        await _preferences.setBool(preferenceKey, newValue.value);
        break;
      case StorageType.stringList:
        await _preferences.setStringList(preferenceKey, newValue.value);
        break;
      case StorageType.datetime:
        throw ArgumentError(
            'Shared preferences does not support datetime values');
      case StorageType.binary:
        throw ArgumentError(
            'Shared preferences does not support binary values');
    }
  }
}
