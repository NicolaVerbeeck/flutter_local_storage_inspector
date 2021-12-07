import 'package:storage_inspector/src/protocol/io/storage_protocol_server.dart';
import 'package:storage_inspector/src/protocol/specific/generic.dart';
import 'package:storage_inspector/storage_inspector.dart';

class KeyValueProtocol {
  static const getAll = 'getAll';

  final StorageProtocolServer _server;

  KeyValueProtocol(this._server);

  Future<Map<String, dynamic>> identify(
    KeyValueServer server,
  ) async {
    return {
      'type': 'identify',
      'data': {
        'id': server.id,
        'name': server.name,
        'icon': server.icon,
        'keySuggestions': server.keySuggestions,
        'keyOptions': server.keyOptions,
        'supportedKeyTypes':
            server.supportedKeyTypes.map(typeString).toList(growable: false),
        'supportedValueTypes':
            server.supportedValueTypes.map(typeString).toList(growable: false),
      },
    };
  }

  Future<Map<String, dynamic>> onMessage(Map<String, dynamic> jsonData) {
    switch (jsonData['type']) {
      case getAll:
        return _handleGetAll();
      case 'get':
        return _handleGet(jsonData['data']['id'] as String);
      case 'clear':
        return _handleClear(jsonData['data']['id'] as String);
      case 'set':
        return _handleSet(jsonData['data']['id'] as String,
            jsonData['data']['key'], jsonData['data']['value']);
      case 'remove':
        return _handleRemove(
            jsonData['data']['id'] as String, jsonData['data']['key']);
      default:
        return Future.error(
            ArgumentError('Unknown key-value command: ${jsonData['type']}'));
    }
  }

  Future<Map<String, dynamic>> _handleGetAll() async {
    final returnData = <String, dynamic>{};

    final serverData = <Map<String, dynamic>>[];

    for (final element in _server.keyValueServers) {
      try {
        serverData.add(await _getAllFromServer(element));
      } catch (e) {
        storageInspectorLogger('Failed to get all from ${element.name}: $e');
      }
    }

    return returnData;
  }

  Future<Map<String, dynamic>> _getAllFromServer(KeyValueServer element) async {
    final allValues = await element.allValues;

    return {
      'id': element.id,
      'values': allValues
          .map(
            (e) => {
              'key': mapValueWithType(e.item1),
              'value': mapValueWithType(e.item2),
            },
          )
          .toList(growable: false),
    };
  }

  Future<Map<String, dynamic>> _handleGet(String id) async {
    final keyValueServer =
        _server.keyValueServers.firstWhere((element) => element.id == id);
    return await _getAllFromServer(keyValueServer);
  }

  Future<Map<String, dynamic>> _handleClear(String id) async {
    final keyValueServer =
        _server.keyValueServers.firstWhere((element) => element.id == id);
    await keyValueServer.clear();
    return {
      'id': id,
      'data': {
        'success': true,
      },
    };
  }

  Future<Map<String, dynamic>> _handleSet(
    String id,
    dynamic key,
    dynamic value,
  ) async {
    final keyData = decodeValueWithType(key);
    final valueData = decodeValueWithType(value);

    final keyValueServer =
        _server.keyValueServers.firstWhere((element) => element.id == id);

    await keyValueServer.set(keyData, valueData);
    return {
      'id': id,
      'data': {
        'success': true,
      },
    };
  }

  Future<Map<String, dynamic>> _handleRemove(
    String id,
    dynamic key,
  ) async {
    final keyData = decodeValueWithType(key);

    final keyValueServer =
        _server.keyValueServers.firstWhere((element) => element.id == id);

    await keyValueServer.remove(keyData);
    return {
      'id': id,
      'data': {
        'success': true,
      },
    };
  }
}