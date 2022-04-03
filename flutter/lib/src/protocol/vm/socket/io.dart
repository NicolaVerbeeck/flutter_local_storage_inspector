import 'dart:async';
import 'dart:io';
import 'dart:typed_data';

class WebSocketWrapper {
  final String uri;
  late final WebSocket _socket;

  Stream<dynamic> get onMessage => _socket;

  WebSocketWrapper({required this.uri});

  Future<void> init() async {
    _socket = await WebSocket.connect(uri);
  }

  void close([int? reason]) {
    unawaited(_socket.close(reason));
  }

  void sendBuffer(ByteBuffer buffer) {
    _socket.add(buffer.asUint8List());
  }

  void sendBytes(List<int> bytes) {
    _socket.add(bytes);
  }

  void sendString(String string) {
    _socket.add(string);
  }
}
