import 'package:shared_preferences/shared_preferences.dart';
import 'package:storage_inspector/storage_inspector.dart';
import 'package:tuple/tuple.dart';
import 'package:uuid/uuid.dart';

/// Key value server that serves values to/from the given shared preferences
/// instance
class PreferencesKeyValueServer implements KeyValueServer {
  final SharedPreferences _preferences;

  @override
  final String name;

  @override
  final String? icon;

  @override
  final String id = const Uuid().v4();

  @override
  final Set<ValueWithType> keySuggestions;

  @override
  final Set<ValueWithType> keyOptions;

  @override
  final Map<ValueWithType, StorageType> typeForKey;

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

  @override
  final Map<ValueWithType, String> keyIcons;

  PreferencesKeyValueServer(
    this._preferences,
    this.name, {
    this.keySuggestions = const {},
    this.keyOptions = const {},

    /// Icon hints to show for specific keys. See [iconForKey] for more information
    Map<String, String> keyIcons = const {},

    /// Hints indicating for which specific key, which type is expected
    Map<String, StorageType> typeForKey = const {},
    this.icon,
  })  : typeForKey = typeForKey.map(
          (key, value) =>
              MapEntry(ValueWithType(StorageType.string, key), value),
        ),
        keyIcons = keyIcons.map(
          (key, value) =>
              MapEntry(ValueWithType(StorageType.string, key), value),
        );

  @override
  Future<List<Tuple2<ValueWithType, ValueWithType>>> get allValues async {
    await _preferences.reload();

    return _preferences.getKeys().map((key) {
      final rawValue = _preferences.get(key);

      return Tuple2(
          ValueWithType(StorageType.string, key), fromObject(rawValue));
    }).toList(growable: false);
  }

  @override
  Future<void> clear() => _preferences.clear();

  @override
  Future<void> remove(ValueWithType key) =>
      _preferences.remove(key.value.toString());

  @override
  Future<void> set(ValueWithType key, ValueWithType newValue) async {
    final preferenceKey = key.value.toString();

    switch (newValue.type) {
      case StorageType.string:
        await _preferences.setString(preferenceKey, newValue.value as String);
        break;
      case StorageType.integer:
        await _preferences.setInt(preferenceKey, newValue.value as int);
        break;
      case StorageType.double:
        await _preferences.setDouble(preferenceKey, newValue.value as double);
        break;
      case StorageType.boolean:
        await _preferences.setBool(preferenceKey, newValue.value as bool);
        break;
      case StorageType.stringList:
        await _preferences.setStringList(
            preferenceKey, newValue.value as List<String>);
        break;
      case StorageType.datetime:
        throw ArgumentError(
            'Shared preferences does not support datetime values');
      case StorageType.binary:
        throw ArgumentError(
            'Shared preferences does not support binary values');
    }
  }

  @override
  Future<ValueWithType> get(ValueWithType key) async {
    final preferenceKey = key.value.toString();
    return fromObject(_preferences.get(preferenceKey));
  }

  static ValueWithType fromObject(Object? rawValue) {
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
    return ValueWithType(type, rawValue);
  }
}
