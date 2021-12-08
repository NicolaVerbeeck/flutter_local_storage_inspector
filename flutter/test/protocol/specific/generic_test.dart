import 'package:flutter_test/flutter_test.dart';
import 'package:storage_inspector/src/protocol/specific/generic.dart';
import 'package:storage_inspector/src/servers/storage_type.dart';

void main() {
  group('Test mapping', () {
    test('Test to string', () {
      expect(typeString(StorageType.string), 'string');
      expect(typeString(StorageType.integer), 'int');
      expect(typeString(StorageType.double), 'double');
      expect(typeString(StorageType.datetime), 'datetime');
      expect(typeString(StorageType.binary), 'binary');
      expect(typeString(StorageType.boolean), 'bool');
      expect(typeString(StorageType.stringList), 'stringlist');
    });
    test('Test from string', () {
      expect(stringToType('string'), StorageType.string);
      expect(stringToType('int'), StorageType.integer);
      expect(stringToType('double'), StorageType.double);
      expect(stringToType('datetime'), StorageType.datetime);
      expect(stringToType('binary'), StorageType.binary);
      expect(stringToType('bool'), StorageType.boolean);
      expect(stringToType('stringlist'), StorageType.stringList);
      expect(() => stringToType('araewe'), throwsA(isA<StateError>()));
    });
  });
}
