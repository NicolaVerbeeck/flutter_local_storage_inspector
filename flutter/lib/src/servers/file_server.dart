import 'dart:typed_data';

import 'package:storage_inspector/src/servers/storage_server.dart';

/// Storage server that serves local files
abstract class FileServer implements StorageServerInfo {
  /// Recursively browse the file system at the given [root].
  /// Returned paths are relative to the provided [root]
  Future<List<String>> browse(String root);

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
