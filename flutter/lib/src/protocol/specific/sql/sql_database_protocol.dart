import 'package:storage_inspector/src/driver/storage_server.dart';
import 'package:storage_inspector/src/servers/sql_database_server.dart';

class SQLDatabaseProtocol {
  static const _commandQuery = 'query';

  final StorageProtocolServer _server;

  SQLDatabaseProtocol(this._server);

  Future<Map<String, dynamic>> identify(
    SQLDatabaseServer server,
  ) async {
    final tables = await server.tables;
    final version = await server.schemaVersion;

    return <String, dynamic>{
      'type': 'identify',
      'data': {
        'id': server.id,
        'name': server.name,
        'icon': server.icon,
        'schemaVersion': version,
        'tables': tables.map(_tableToJson).toList(),
      },
    };
  }

  Future<Map<String, dynamic>> onMessage(Map<String, dynamic> jsonData) {
    switch (jsonData['type']) {
      case _commandQuery:
        return _handleQuery(
          jsonData['data']['id'] as String,
          jsonData['data']['query'] as String,
        );
      default:
        return Future.error(
          ArgumentError(
            'Unknown sql database protocol command: ${jsonData['type']}',
          ),
        );
    }
  }

  Future<Map<String, dynamic>> _handleQuery(
    String id,
    String query,
  ) async {
    final sqlServer = _server.sqlServers.firstWhere(
      (element) => element.id == id,
      orElse: () => throw ArgumentError('No server with id $id found'),
    );
    final result = await sqlServer.query(query, const []);
    return <String, dynamic>{
      'id': id,
      'columns': result.columns,
      'rows': result.rows,
    };
  }
}

Map<String, dynamic> _tableToJson(SQLTableDefinition e) {
  return <String, dynamic>{
    'name': e.name,
    'primaryKey': e.primaryKey,
    'extensions': e.extensions.map(_extensionToJson).toList(),
    'columns': e.columns.map(_columnToJson).toList()
  };
}

String _extensionToJson(SQLTableExtension e) {
  switch (e) {
    case SQLTableExtension.withoutRowId:
      return 'withoutRowId';
  }
}

Map<String, dynamic> _columnToJson(SQLColumnDefinition e) {
  return <String, dynamic>{
    'name': e.name,
    'optional': e.optional,
    'nullable': e.nullable,
    'autoIncrement': e.autoIncrement,
    'type': _typeToJson(e.type),
  };
}

String _typeToJson(SQLDataType type) {
  switch (type) {
    case SQLDataType.text:
      return 'text';
    case SQLDataType.blob:
      return 'blob';
    case SQLDataType.real:
      return 'real';
    case SQLDataType.integer:
      return 'integer';
    case SQLDataType.boolean:
      return 'boolean';
    case SQLDataType.datetime:
      return 'datetime';
  }
}
