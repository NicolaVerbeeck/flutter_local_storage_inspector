import 'package:flutter/foundation.dart';

enum StorageType {
  string,
  integer,
  double,
  datetime,
  binary,
  boolean,
  stringList,
}

@immutable
class ValueWithType {
  final StorageType type;
  final dynamic value;

  const ValueWithType(
    this.type,
    this.value,
  );

  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      other is ValueWithType &&
          runtimeType == other.runtimeType &&
          type == other.type &&
          value == other.value;

  @override
  int get hashCode => type.hashCode ^ value.hashCode;
}
