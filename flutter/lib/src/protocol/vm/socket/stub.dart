import 'dart:typed_data';

class WebSocketWrapper {
  final String uri;

  Stream<dynamic> get onMessage =>
      throw ArgumentError('Platform not supported');

  WebSocketWrapper({required this.uri});

  Future<void> init() => throw ArgumentError('Platform not supported');

  void close([int? reason]) => throw ArgumentError('Platform not supported');

  void sendBuffer(ByteBuffer buffer) =>
      throw ArgumentError('Platform not supported');

  void sendBytes(List<int> bytes) =>
      throw ArgumentError('Platform not supported');

  void sendString(String string) =>
      throw ArgumentError('Platform not supported');
}
