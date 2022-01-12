import 'package:flutter/foundation.dart';
import 'package:storage_inspector/src/servers/storage_server.dart';
import 'package:storage_inspector/src/servers/storage_type.dart';

/// Inspector server for sql style databases
abstract class SQLDatabaseServer implements StorageServerInfo {
  /// If available, the schema version of the database
  Future<int?> get schemaVersion;

  /// The tables contained in this database
  Future<List<SQLTableDefinition>> get tables;

  /// Query the database using the provided
  /// [query] and return the result
  /// [variables] are used to provide safe
  /// way to bind variables to the [query] statement
  Future<QueryResult> query(
    String query,
    List<ValueWithType> variables,
  );
}

/// Definition of a table in an SQL database
@immutable
class SQLTableDefinition {
  /// The table name
  final String name;

  /// The primary key of this table if set.
  /// Expressed as list of column names
  final List<String> primaryKey;

  /// The columns making up this table
  final List<SQLColumnDefinition> columns;

  /// List of optional extensions to the table
  final List<SQLTableExtension> extensions;

  const SQLTableDefinition({
    required this.name,
    required this.primaryKey,
    required this.columns,
    required this.extensions,
  });
}

/// Definition of a column in an SQL table
@immutable
class SQLColumnDefinition {
  /// The name of the column
  final String name;

  /// Marker indicating that the column is defined
  /// with a default value
  final bool optional;

  /// The underlying raw data type
  final SQLDataType type;

  /// Flag indicating if this is a nullable column
  final bool nullable;

  /// Flag indicating that this column auto-increments
  final bool autoIncrement;

  const SQLColumnDefinition({
    required this.name,
    required this.optional,
    required this.type,
    required this.nullable,
    required this.autoIncrement,
  });
}

/// Raw data type of sql column
enum SQLDataType {
  /// Textual data
  text,

  /// Binary data
  blob,

  /// Real (double) data
  real,

  /// Integer data
  integer,

  /// Special case of integer data for
  /// boolean values
  boolean,

  /// Special case for datetime values
  datetime,
}

/// Possible extensions to the table
enum SQLTableExtension {
  withoutRowId,
}

/// Result of a query operation
@immutable
class QueryResult {
  /// List of all the columns in the result
  final List<String> columns;

  /// All rows, mapped by column
  final List<Map<String, dynamic>> rows;

  const QueryResult({
    required this.columns,
    required this.rows,
  });
}
