import 'dart:convert';

import 'package:flutter/foundation.dart';
import 'package:storage_inspector/storage_inspector.dart';

Map<String, dynamic> mapValueWithType(ValueWithType data) {
  return <String, dynamic>{
    'type': typeString(data.type),
    'value': _serializeValue(data),
  };
}

dynamic _serializeValue(ValueWithType data) {
  switch (data.type) {
    case StorageType.string:
    case StorageType.integer:
    case StorageType.double:
    case StorageType.boolean:
    case StorageType.stringList:
      return data.value;
    case StorageType.datetime:
      return (data.value as DateTime).millisecondsSinceEpoch;
    case StorageType.binary:
      if (data.value == null) return null;
      return base64.encode(data.value as Uint8List);
  }
}

ValueWithType decodeValueWithType(dynamic data) {
  assert(data is Map<String, dynamic>);
  final type = stringToType(data['type'] as String);
  return ValueWithType(
    type,
    _deserializeValue(type, data['value']),
  );
}

dynamic _deserializeValue(StorageType type, dynamic data) {
  switch (type) {
    case StorageType.string:
    case StorageType.integer:
    case StorageType.double:
    case StorageType.boolean:
      return data;
    case StorageType.stringList:
      if (data is List<String>) return data;
      if (data is List<dynamic>) return data.cast<String>();
      return (data as Iterable).map((dynamic e) => e.toString()).toList();
    case StorageType.binary:
      return base64.decode(data as String);
    case StorageType.datetime:
      return DateTime.fromMillisecondsSinceEpoch(data as int, isUtc: true);
  }
}

String typeString(StorageType type) {
  switch (type) {
    case StorageType.string:
      return 'string';
    case StorageType.integer:
      return 'int';
    case StorageType.double:
      return 'double';
    case StorageType.datetime:
      return 'datetime';
    case StorageType.binary:
      return 'binary';
    case StorageType.boolean:
      return 'bool';
    case StorageType.stringList:
      return 'stringlist';
  }
}

@visibleForTesting
StorageType stringToType(String typeString) {
  switch (typeString) {
    case 'string':
      return StorageType.string;
    case 'int':
      return StorageType.integer;
    case 'double':
      return StorageType.double;
    case 'datetime':
      return StorageType.datetime;
    case 'binary':
      return StorageType.binary;
    case 'bool':
      return StorageType.boolean;
    case 'stringlist':
      return StorageType.stringList;
    default:
      throw StateError('Unknown type string: $typeString');
  }
}
