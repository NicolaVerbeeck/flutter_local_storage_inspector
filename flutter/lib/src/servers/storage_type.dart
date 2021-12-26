import 'package:flutter/foundation.dart';

/// Type of values stored in a storage server
enum StorageType {
  /// Strings
  string,

  /// Integer values
  integer,

  /// Double values
  double,

  /// Datetime values. Serialized as milliseconds since epoch on the wire
  datetime,

  /// Binary data. Serialized as base64 on the wire, Uint8List internally
  binary,

  /// Boolean values
  boolean,

  /// List of strings. Serialized as json array
  stringList,
}

/// Helper class describing a typed value
@immutable
class ValueWithType {
  /// The type of data represented
  final StorageType type;

  /// The actual data. Must be in its native format. Eg: double
  final dynamic value;

  /// Create new value with type
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
