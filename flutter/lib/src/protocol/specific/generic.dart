import 'package:storage_inspector/storage_inspector.dart';

Map<String, dynamic> mapValueWithType(ValueWithType data) {
  return {
    'type': typeString(data.type),
    'value': data.value,
  };
}

ValueWithType decodeValueWithType(dynamic data) {
  assert(data is Map<String, dynamic>);
  return ValueWithType(
    _stringToType(data['type'] as String),
    data['value'],
  );
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

StorageType _stringToType(String typeString) {
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
