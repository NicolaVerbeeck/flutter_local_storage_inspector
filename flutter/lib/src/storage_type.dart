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
}
