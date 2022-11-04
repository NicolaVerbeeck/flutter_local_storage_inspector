import 'dart:io';

import 'package:file_local_storage_inspector/src/util/extensions.dart';
import 'package:flutter/foundation.dart';
import 'package:path/path.dart';
import 'package:storage_inspector/storage_inspector.dart';
import 'package:uuid/uuid.dart';

/// File server that serves files using dart:io framework. By default all files
/// are returned without filtering.
///
/// NOTE: No security checks are performed on the input paths
class DefaultFileServer implements FileServer {
  final String _root;

  @override
  final String? icon;

  @override
  final String name;

  @override
  final String id = const Uuid().v4();

  DefaultFileServer(
    this._root,
    this.name, {
    this.icon,
  });

  @override
  Future<List<FileInfo>> browse(String root) async {
    final newPath = join(_root, _sanitizePath(root));
    final findRoot = FileSystemEntity.typeSync(newPath);
    if (findRoot != FileSystemEntityType.directory) {
      if (findRoot == FileSystemEntityType.file) {
        return [
          FileInfo(
              path: root, size: File(newPath).statSync().size, isDir: false),
        ];
      }
      return [FileInfo(path: root, size: 0, isDir: false)];
    }

    final dir = Directory(newPath);
    if (!dir.existsSync()) throw ArgumentError('Path "$root" does not exist');

    return dir.list(recursive: true).mapNotNull(
      (path) {
        final fullPath = relative(path.path, from: newPath);
        final stat = path.statSync();
        if (stat.type == FileSystemEntityType.file) {
          return FileInfo(path: fullPath, size: stat.size, isDir: false);
        } else if (stat.type == FileSystemEntityType.directory &&
            Directory(path.path).listSync(recursive: false).isEmpty) {
          return FileInfo(path: fullPath, size: 0, isDir: true);
        } else {
          return null;
        }
      },
    ).then((r) => r.toList());
  }

  @override
  Future<void> delete(String path, {required bool recursive}) {
    final newPath = join(_root, _sanitizePath(path));
    File(newPath).deleteSync(recursive: recursive);
    return SynchronousFuture(null);
  }

  @override
  Future<Uint8List> read(String path) {
    final filePath = File(join(_root, _sanitizePath(path)));
    if (!filePath.existsSync()) {
      throw ArgumentError('File "$path" does not exist');
    }
    return filePath.readAsBytes();
  }

  @override
  Future<void> write(String path, Uint8List data) {
    final filePath = File(join(_root, _sanitizePath(path)));
    if (!filePath.parent.existsSync()) {
      filePath.parent.createSync(recursive: true);
    }
    return filePath.writeAsBytes(data);
  }

  @override
  Future<void> move({
    required String path,
    required String newPath,
  }) {
    final filePath = File(join(_root, _sanitizePath(path)));
    final toPath = File(join(_root, _sanitizePath(newPath)));
    if (!toPath.parent.existsSync()) {
      toPath.parent.createSync(recursive: true);
    }
    return filePath.rename(toPath.path);
  }
}

String _sanitizePath(String path) {
  var newPath = path;
  while (newPath.startsWith('/')) {
    newPath = newPath.substring(1);
  }
  return newPath;
}
