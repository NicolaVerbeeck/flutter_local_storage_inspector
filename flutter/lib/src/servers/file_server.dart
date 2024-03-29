import 'package:flutter/foundation.dart';
import 'package:storage_inspector/src/servers/storage_server.dart';

/// Storage server that serves local files
abstract class FileServer implements StorageServerInfo {
  /// Recursively browse the file system at the given [root].
  /// Returned paths are relative to the provided [root] and MUST ONLY contain
  /// leaf files or empty directories
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

  /// Moves the contents at [path] to [newPath]
  Future<void> move({required String path, required String newPath});
}

/// Holder for file data
@immutable
class FileInfo {
  /// The path to the file
  final String path;

  /// The size of the file in bytes
  final int size;

  /// Flag indicating that this file denotes a directory
  final bool isDir;

  const FileInfo({
    required this.path,
    required this.size,
    required this.isDir,
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
