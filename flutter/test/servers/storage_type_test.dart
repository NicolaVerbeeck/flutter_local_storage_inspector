import 'package:flutter_test/flutter_test.dart';
import 'package:storage_inspector/src/servers/storage_type.dart';

void main() {
  group('ValueWithType tests', () {
    test('Test equals', () {
      expect(const ValueWithType(StorageType.string, 'test') == const ValueWithType(StorageType.string, 'test'), true);
      expect(const ValueWithType(StorageType.string, 'test') == const ValueWithType(StorageType.string, 'test2'), false);
      expect(const ValueWithType(StorageType.integer, 'test') == const ValueWithType(StorageType.string, 'test'), false);
    });
    test('Test hashcode', () {
      expect(const ValueWithType(StorageType.string, 'test').hashCode == const ValueWithType(StorageType.string, 'test').hashCode, true);
      expect(const ValueWithType(StorageType.string, 'test').hashCode == const ValueWithType(StorageType.string, 'test2').hashCode, false);
      expect(const ValueWithType(StorageType.integer, 'test').hashCode == const ValueWithType(StorageType.string, 'test').hashCode, false);
    });
  });
}
