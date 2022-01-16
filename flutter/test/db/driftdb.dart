import 'package:drift/drift.dart';

part 'driftdb.g.dart';

class Todos extends Table {
  IntColumn get id => integer().autoIncrement()();

  TextColumn get textWithRestrictions => text().withLength(min: 6, max: 32)();

  RealColumn get realTest => real().withDefault(const Constant(3.14))();

  IntColumn get category => integer().nullable()();

  BoolColumn get booleanTest => boolean()();

  DateTimeColumn get dateTimeTest => dateTime()();

  BlobColumn get blobTest => blob().nullable()();
}

@DriftDatabase(tables: [Todos])
class MyDatabase extends _$MyDatabase {
  MyDatabase(QueryExecutor e) : super(e);

  @override
  int get schemaVersion => 1;
}
