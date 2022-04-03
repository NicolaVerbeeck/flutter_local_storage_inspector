// ignore_for_file: avoid_web_libraries_in_flutter

import 'dart:async';
import 'dart:html';
import 'dart:typed_data';

class WebSocketWrapper {
  final String uri;
  late final WebSocket _socket;

  Stream<dynamic> get onMessage => _socket.onMessage.map<dynamic>((event) {
        final dynamic data = event.data;
        if (data is String) return data;
        if (data is ByteBuffer) return data.asUint8List();
        if (data is Uint8List) return data;
        if (data is List<int>) return data;
        return '';
      });

  WebSocketWrapper({required this.uri});

  Future<void> init() async {
    _socket = WebSocket(uri);
    await _socket.onOpen.first;
  }

  void close([int? reason]) {
    _socket.close(reason);
  }

  void sendBuffer(ByteBuffer buffer) {
    _socket.sendByteBuffer(buffer);
  }

  void sendBytes(List<int> bytes) {
    sendBuffer(Uint8List.fromList(bytes).buffer);
  }

  void sendString(String string) {
    _socket.sendString(string);
  }
}
