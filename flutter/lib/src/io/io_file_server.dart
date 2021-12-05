import 'dart:io';
import 'dart:typed_data';

import 'package:flutter/foundation.dart';
import 'package:path/path.dart';
import 'package:storage_inspector/src/file_server.dart';

/// File server that serves files using dart:io framework. By default all files
/// are returned without filtering.
///
/// NOTE: No security checks are performed on the input paths
class IOFileServer implements FileServer {
  final Directory _root;

  @override
  final String? icon = null;

  @override
  final String name;

  IOFileServer(this._root, this.name);

  @override
  Future<List<String>> browse(String root) async {
    final newPath = join(_root.path, root);
    if (File(newPath).existsSync()) return [root];

    final dir = Directory(newPath);
    if (!dir.existsSync()) throw ArgumentError('Path "$root" does not exist');

    return dir
        .list(recursive: true)
        .map((path) => relative(path.path, from: newPath))
        .toList();
  }

  @override
  Future<void> delete(String path, {required bool recursive}) {
    final newPath = join(_root.path, path);
    File(newPath).deleteSync(recursive: recursive);
    return SynchronousFuture(null);
  }

  @override
  Future<Uint8List> read(String path) {
    final filePath = File(join(_root.path, path));
    if (!filePath.existsSync()) {
      throw ArgumentError('File "$path" does not exist');
    }
    return filePath.readAsBytes();
  }

  @override
  Future<void> write(String path, Uint8List data) {
    final filePath = File(join(_root.path, path));
    if (!filePath.parent.existsSync()) {
      filePath.parent.createSync(recursive: true);
    }
    return filePath.writeAsBytes(data);
  }
}
