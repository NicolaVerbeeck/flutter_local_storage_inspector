import 'dart:typed_data';

import 'package:flutter/foundation.dart';
import 'package:storage_inspector/src/servers/storage_server.dart';

/// Storage server that serves local files
abstract class FileServer implements StorageServerInfo {
  /// Recursively browse the file system at the given [root].
  /// Returned paths are relative to the provided [root]
  Future<List<FileInfo>> browse(String root);

  /// Read the contents of the file at the given [path]
  Future<Uint8List> read(String path);

  /// Writes the given bytes to the file at the given [path]. If the file/path
  /// does not exists, it will be created recursively
  Future<void> write(String path, Uint8List data);

  /// Delete the file system contents at the given [path].
  ///
  /// If the target is a non-empty directory and [recursive] is not specified,
  /// an error will be returned.
  Future<void> delete(String path, {required bool recursive});
}

/// Holder for file data
@immutable
class FileInfo {
  /// The path to the file
  final String path;

  /// The size of the file in bytes
  final int size;

  const FileInfo({
    required this.path,
    required this.size,
  });

  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      other is FileInfo &&
          runtimeType == other.runtimeType &&
          path == other.path;

  @override
  int get hashCode => path.hashCode;
}
