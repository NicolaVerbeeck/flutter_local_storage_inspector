/// Identification information for storage servers
abstract class StorageServerInfo {
  /// The name of this server, eg: the name of the preference file
  String get name;

  /// The icon to use for this server.
  /// Must be an svg or a base64 encoded png (square, preferably 16x16 or 32x32)
  String? get icon;

  /// Unique id for this session
  String get id;
}
