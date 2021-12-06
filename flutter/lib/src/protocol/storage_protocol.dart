import 'package:flutter/foundation.dart';

class StorageProtocol {
  static const int version = 1;

  final Set<StorageProtocolExtension> extensions;

  StorageProtocol(this.extensions);
}

@immutable
class StorageProtocolExtension {
  final String name;

  const StorageProtocolExtension(this.name);
}
