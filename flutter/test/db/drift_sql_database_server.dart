import 'package:drift/drift.dart';
import 'package:flutter/foundation.dart';
import 'package:storage_inspector/storage_inspector.dart';

class DriftSQLDatabaseServer implements SQLDatabaseServer {
  final GeneratedDatabase _database;

  @override
  final String? icon = null;

  @override
  final String id = '1230';

  @override
  final String name = 'Test drift db';

  @override
  final Future<List<SQLTableDefinition>> tables;

  DriftSQLDatabaseServer(this._database) : tables = SynchronousFuture(_buildTables(_database));

  @override
  Future<QueryResult> query(String query, List<ValueWithType> variables) async {
    final select = _database.customSelect(query, variables: variables.map(_mapVariable).toList());
    final data = await select.get();

    final columns = <String>{};
    final rows = data.map((row) {
      final rowItem = <String, dynamic>{};
      row.data.forEach((key, dynamic value) {
        columns.add(key);
        rowItem[key] = value;
      });
      return rowItem;
    }).toList();
    return QueryResult(
      columns: columns.toList(),
      rows: rows,
    );
  }

  @override
  Future<int?> get schemaVersion => SynchronousFuture(_database.schemaVersion);
}

List<SQLTableDefinition> _buildTables(GeneratedDatabase database) {
  return database.allTables.map((tableInfo) {
    final extensions = <SQLTableExtension>[];
    if (tableInfo.withoutRowId) {
      extensions.add(SQLTableExtension.withoutRowId);
    }
    final columns = tableInfo.$columns.map((column) {
      return SQLColumnDefinition(
        name: column.$name,
        optional: !column.requiredDuringInsert,
        type: _makeType(column),
        nullable: column.$nullable,
        autoIncrement: column.hasAutoIncrement,
      );
    }).toList();
    return SQLTableDefinition(
      name: tableInfo.actualTableName,
      extensions: extensions,
      columns: columns,
      primaryKey: tableInfo.$primaryKey.map((column) => column.$name).toList(),
    );
  }).toList();
}

SQLDataType _makeType(GeneratedColumn<dynamic> column) {
  final type = column.type;
  if (type is BoolType) return SQLDataType.boolean;
  if (type is RealType) return SQLDataType.real;
  if (type is StringType) return SQLDataType.text;
  if (type is DateTimeType) return SQLDataType.datetime;
  if (type is IntType) return SQLDataType.integer;
  if (type is BlobType) return SQLDataType.blob;
  throw ArgumentError('Unknown column type: $type');
}

Variable<dynamic> _mapVariable(ValueWithType e) {
  if (e.value == null) {
    return const Variable<dynamic>(null);
  }
  switch (e.type) {
    case StorageType.string:
      return Variable.withString(e.value as String);
    case StorageType.integer:
      return Variable.withInt(e.value as int);
    case StorageType.double:
      return Variable.withReal(e.value as double);
    case StorageType.datetime:
      return Variable.withDateTime(e.value as DateTime);
    case StorageType.binary:
      return Variable.withBlob(e.value as Uint8List);
    case StorageType.boolean:
      return Variable.withBool(e.value as bool);
    case StorageType.stringList:
      throw ArgumentError('String lists are not supported by SQL');
  }
}
