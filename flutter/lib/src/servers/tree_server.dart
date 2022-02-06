import 'package:flutter/foundation.dart';

import '../../storage_inspector.dart';

/// A tree based storage server. The tree consists of single properties,
/// lists or maps of properties nested in arbitrary ways.
///
/// Note: Cycles are prohibited!
abstract class TreeServer implements StorageServerInfo {
  /// Describe the state of the tree.
  /// The returned data is dynamic  where dynamic
  /// refers more Map<String, dynamic, List<dynamic>
  /// or [ValueWithType]. Null is permitted as value if it makes
  /// semantic sense
  Future<dynamic> describe();

  /// Set the item at the given [path]. The provided [value]
  /// can be any type accepted by the tree server (maps, lists, properties, null)
  Future<void> set(TreePath path, dynamic value);

  /// Deletes the value at the given [path]. If the path designates an
  /// entry into a list, delete that entry and shift following items
  /// by 1
  Future<void> delete(TreePath path);
}

/// Uniquely a location in a tree of values
@immutable
class TreePath {
  /// The segments making up the tree path
  final List<TreePathSegment> segments;

  /// Create a new tree path
  const TreePath(this.segments);

  @override
  String toString() {
    final buffer = StringBuffer();
    for (final element in segments) {
      buffer
        ..write('/')
        ..write(element);
    }
    return buffer.toString();
  }
}

/// A segment that describes the relative position of an item
/// within a tree of data. This either describes a property or
/// an entry inside an array
@immutable
class TreePathSegment {
  /// The name of the property
  final String name;

  /// If the property describes an array item:
  /// The index of the item inside said array.
  ///
  /// Eg: entitlements[2] will be described as
  /// [name]: 'entitlements',
  /// [index]: 2
  final int? index;

  /// Creates a new tree path segment
  const TreePathSegment({
    required this.name,
    required this.index,
  });

  @override
  String toString() {
    if (index == null) {
      return name;
    }
    return '$name[$index]';
  }
}
