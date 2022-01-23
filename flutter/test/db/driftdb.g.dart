// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'driftdb.dart';

// **************************************************************************
// MoorGenerator
// **************************************************************************

// ignore_for_file: unnecessary_brace_in_string_interps, unnecessary_this
class Todo extends DataClass implements Insertable<Todo> {
  final int id;
  final String textWithRestrictions;
  final double realTest;
  final int? category;
  final bool booleanTest;
  final DateTime dateTimeTest;
  final Uint8List? blobTest;
  Todo(
      {required this.id,
      required this.textWithRestrictions,
      required this.realTest,
      this.category,
      required this.booleanTest,
      required this.dateTimeTest,
      this.blobTest});
  factory Todo.fromData(Map<String, dynamic> data, {String? prefix}) {
    final effectivePrefix = prefix ?? '';
    return Todo(
      id: const IntType()
          .mapFromDatabaseResponse(data['${effectivePrefix}id'])!,
      textWithRestrictions: const StringType().mapFromDatabaseResponse(
          data['${effectivePrefix}text_with_restrictions'])!,
      realTest: const RealType()
          .mapFromDatabaseResponse(data['${effectivePrefix}real_test'])!,
      category: const IntType()
          .mapFromDatabaseResponse(data['${effectivePrefix}category']),
      booleanTest: const BoolType()
          .mapFromDatabaseResponse(data['${effectivePrefix}boolean_test'])!,
      dateTimeTest: const DateTimeType()
          .mapFromDatabaseResponse(data['${effectivePrefix}date_time_test'])!,
      blobTest: const BlobType()
          .mapFromDatabaseResponse(data['${effectivePrefix}blob_test']),
    );
  }
  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    map['id'] = Variable<int>(id);
    map['text_with_restrictions'] = Variable<String>(textWithRestrictions);
    map['real_test'] = Variable<double>(realTest);
    if (!nullToAbsent || category != null) {
      map['category'] = Variable<int?>(category);
    }
    map['boolean_test'] = Variable<bool>(booleanTest);
    map['date_time_test'] = Variable<DateTime>(dateTimeTest);
    if (!nullToAbsent || blobTest != null) {
      map['blob_test'] = Variable<Uint8List?>(blobTest);
    }
    return map;
  }

  TodosCompanion toCompanion(bool nullToAbsent) {
    return TodosCompanion(
      id: Value(id),
      textWithRestrictions: Value(textWithRestrictions),
      realTest: Value(realTest),
      category: category == null && nullToAbsent
          ? const Value.absent()
          : Value(category),
      booleanTest: Value(booleanTest),
      dateTimeTest: Value(dateTimeTest),
      blobTest: blobTest == null && nullToAbsent
          ? const Value.absent()
          : Value(blobTest),
    );
  }

  factory Todo.fromJson(Map<String, dynamic> json,
      {ValueSerializer? serializer}) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return Todo(
      id: serializer.fromJson<int>(json['id']),
      textWithRestrictions:
          serializer.fromJson<String>(json['textWithRestrictions']),
      realTest: serializer.fromJson<double>(json['realTest']),
      category: serializer.fromJson<int?>(json['category']),
      booleanTest: serializer.fromJson<bool>(json['booleanTest']),
      dateTimeTest: serializer.fromJson<DateTime>(json['dateTimeTest']),
      blobTest: serializer.fromJson<Uint8List?>(json['blobTest']),
    );
  }
  @override
  Map<String, dynamic> toJson({ValueSerializer? serializer}) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return <String, dynamic>{
      'id': serializer.toJson<int>(id),
      'textWithRestrictions': serializer.toJson<String>(textWithRestrictions),
      'realTest': serializer.toJson<double>(realTest),
      'category': serializer.toJson<int?>(category),
      'booleanTest': serializer.toJson<bool>(booleanTest),
      'dateTimeTest': serializer.toJson<DateTime>(dateTimeTest),
      'blobTest': serializer.toJson<Uint8List?>(blobTest),
    };
  }

  Todo copyWith(
          {int? id,
          String? textWithRestrictions,
          double? realTest,
          int? category,
          bool? booleanTest,
          DateTime? dateTimeTest,
          Uint8List? blobTest}) =>
      Todo(
        id: id ?? this.id,
        textWithRestrictions: textWithRestrictions ?? this.textWithRestrictions,
        realTest: realTest ?? this.realTest,
        category: category ?? this.category,
        booleanTest: booleanTest ?? this.booleanTest,
        dateTimeTest: dateTimeTest ?? this.dateTimeTest,
        blobTest: blobTest ?? this.blobTest,
      );
  @override
  String toString() {
    return (StringBuffer('Todo(')
          ..write('id: $id, ')
          ..write('textWithRestrictions: $textWithRestrictions, ')
          ..write('realTest: $realTest, ')
          ..write('category: $category, ')
          ..write('booleanTest: $booleanTest, ')
          ..write('dateTimeTest: $dateTimeTest, ')
          ..write('blobTest: $blobTest')
          ..write(')'))
        .toString();
  }

  @override
  int get hashCode => Object.hash(id, textWithRestrictions, realTest, category,
      booleanTest, dateTimeTest, blobTest);
  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      (other is Todo &&
          other.id == this.id &&
          other.textWithRestrictions == this.textWithRestrictions &&
          other.realTest == this.realTest &&
          other.category == this.category &&
          other.booleanTest == this.booleanTest &&
          other.dateTimeTest == this.dateTimeTest &&
          other.blobTest == this.blobTest);
}

class TodosCompanion extends UpdateCompanion<Todo> {
  final Value<int> id;
  final Value<String> textWithRestrictions;
  final Value<double> realTest;
  final Value<int?> category;
  final Value<bool> booleanTest;
  final Value<DateTime> dateTimeTest;
  final Value<Uint8List?> blobTest;
  const TodosCompanion({
    this.id = const Value.absent(),
    this.textWithRestrictions = const Value.absent(),
    this.realTest = const Value.absent(),
    this.category = const Value.absent(),
    this.booleanTest = const Value.absent(),
    this.dateTimeTest = const Value.absent(),
    this.blobTest = const Value.absent(),
  });
  TodosCompanion.insert({
    this.id = const Value.absent(),
    required String textWithRestrictions,
    this.realTest = const Value.absent(),
    this.category = const Value.absent(),
    required bool booleanTest,
    required DateTime dateTimeTest,
    this.blobTest = const Value.absent(),
  })  : textWithRestrictions = Value(textWithRestrictions),
        booleanTest = Value(booleanTest),
        dateTimeTest = Value(dateTimeTest);
  static Insertable<Todo> custom({
    Expression<int>? id,
    Expression<String>? textWithRestrictions,
    Expression<double>? realTest,
    Expression<int?>? category,
    Expression<bool>? booleanTest,
    Expression<DateTime>? dateTimeTest,
    Expression<Uint8List?>? blobTest,
  }) {
    return RawValuesInsertable({
      if (id != null) 'id': id,
      if (textWithRestrictions != null)
        'text_with_restrictions': textWithRestrictions,
      if (realTest != null) 'real_test': realTest,
      if (category != null) 'category': category,
      if (booleanTest != null) 'boolean_test': booleanTest,
      if (dateTimeTest != null) 'date_time_test': dateTimeTest,
      if (blobTest != null) 'blob_test': blobTest,
    });
  }

  TodosCompanion copyWith(
      {Value<int>? id,
      Value<String>? textWithRestrictions,
      Value<double>? realTest,
      Value<int?>? category,
      Value<bool>? booleanTest,
      Value<DateTime>? dateTimeTest,
      Value<Uint8List?>? blobTest}) {
    return TodosCompanion(
      id: id ?? this.id,
      textWithRestrictions: textWithRestrictions ?? this.textWithRestrictions,
      realTest: realTest ?? this.realTest,
      category: category ?? this.category,
      booleanTest: booleanTest ?? this.booleanTest,
      dateTimeTest: dateTimeTest ?? this.dateTimeTest,
      blobTest: blobTest ?? this.blobTest,
    );
  }

  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    if (id.present) {
      map['id'] = Variable<int>(id.value);
    }
    if (textWithRestrictions.present) {
      map['text_with_restrictions'] =
          Variable<String>(textWithRestrictions.value);
    }
    if (realTest.present) {
      map['real_test'] = Variable<double>(realTest.value);
    }
    if (category.present) {
      map['category'] = Variable<int?>(category.value);
    }
    if (booleanTest.present) {
      map['boolean_test'] = Variable<bool>(booleanTest.value);
    }
    if (dateTimeTest.present) {
      map['date_time_test'] = Variable<DateTime>(dateTimeTest.value);
    }
    if (blobTest.present) {
      map['blob_test'] = Variable<Uint8List?>(blobTest.value);
    }
    return map;
  }

  @override
  String toString() {
    return (StringBuffer('TodosCompanion(')
          ..write('id: $id, ')
          ..write('textWithRestrictions: $textWithRestrictions, ')
          ..write('realTest: $realTest, ')
          ..write('category: $category, ')
          ..write('booleanTest: $booleanTest, ')
          ..write('dateTimeTest: $dateTimeTest, ')
          ..write('blobTest: $blobTest')
          ..write(')'))
        .toString();
  }
}

class $TodosTable extends Todos with TableInfo<$TodosTable, Todo> {
  final GeneratedDatabase _db;
  final String? _alias;
  $TodosTable(this._db, [this._alias]);
  final VerificationMeta _idMeta = const VerificationMeta('id');
  @override
  late final GeneratedColumn<int?> id = GeneratedColumn<int?>(
      'id', aliasedName, false,
      type: const IntType(),
      requiredDuringInsert: false,
      defaultConstraints: 'PRIMARY KEY AUTOINCREMENT');
  final VerificationMeta _textWithRestrictionsMeta =
      const VerificationMeta('textWithRestrictions');
  @override
  late final GeneratedColumn<String?> textWithRestrictions =
      GeneratedColumn<String?>('text_with_restrictions', aliasedName, false,
          additionalChecks: GeneratedColumn.checkTextLength(
              minTextLength: 6, maxTextLength: 32),
          type: const StringType(),
          requiredDuringInsert: true);
  final VerificationMeta _realTestMeta = const VerificationMeta('realTest');
  @override
  late final GeneratedColumn<double?> realTest = GeneratedColumn<double?>(
      'real_test', aliasedName, false,
      type: const RealType(),
      requiredDuringInsert: false,
      defaultValue: const Constant(3.14));
  final VerificationMeta _categoryMeta = const VerificationMeta('category');
  @override
  late final GeneratedColumn<int?> category = GeneratedColumn<int?>(
      'category', aliasedName, true,
      type: const IntType(), requiredDuringInsert: false);
  final VerificationMeta _booleanTestMeta =
      const VerificationMeta('booleanTest');
  @override
  late final GeneratedColumn<bool?> booleanTest = GeneratedColumn<bool?>(
      'boolean_test', aliasedName, false,
      type: const BoolType(),
      requiredDuringInsert: true,
      defaultConstraints: 'CHECK (boolean_test IN (0, 1))');
  final VerificationMeta _dateTimeTestMeta =
      const VerificationMeta('dateTimeTest');
  @override
  late final GeneratedColumn<DateTime?> dateTimeTest =
      GeneratedColumn<DateTime?>('date_time_test', aliasedName, false,
          type: const IntType(), requiredDuringInsert: true);
  final VerificationMeta _blobTestMeta = const VerificationMeta('blobTest');
  @override
  late final GeneratedColumn<Uint8List?> blobTest = GeneratedColumn<Uint8List?>(
      'blob_test', aliasedName, true,
      type: const BlobType(), requiredDuringInsert: false);
  @override
  List<GeneratedColumn> get $columns => [
        id,
        textWithRestrictions,
        realTest,
        category,
        booleanTest,
        dateTimeTest,
        blobTest
      ];
  @override
  String get aliasedName => _alias ?? 'todos';
  @override
  String get actualTableName => 'todos';
  @override
  VerificationContext validateIntegrity(Insertable<Todo> instance,
      {bool isInserting = false}) {
    final context = VerificationContext();
    final data = instance.toColumns(true);
    if (data.containsKey('id')) {
      context.handle(_idMeta, id.isAcceptableOrUnknown(data['id']!, _idMeta));
    }
    if (data.containsKey('text_with_restrictions')) {
      context.handle(
          _textWithRestrictionsMeta,
          textWithRestrictions.isAcceptableOrUnknown(
              data['text_with_restrictions']!, _textWithRestrictionsMeta));
    } else if (isInserting) {
      context.missing(_textWithRestrictionsMeta);
    }
    if (data.containsKey('real_test')) {
      context.handle(_realTestMeta,
          realTest.isAcceptableOrUnknown(data['real_test']!, _realTestMeta));
    }
    if (data.containsKey('category')) {
      context.handle(_categoryMeta,
          category.isAcceptableOrUnknown(data['category']!, _categoryMeta));
    }
    if (data.containsKey('boolean_test')) {
      context.handle(
          _booleanTestMeta,
          booleanTest.isAcceptableOrUnknown(
              data['boolean_test']!, _booleanTestMeta));
    } else if (isInserting) {
      context.missing(_booleanTestMeta);
    }
    if (data.containsKey('date_time_test')) {
      context.handle(
          _dateTimeTestMeta,
          dateTimeTest.isAcceptableOrUnknown(
              data['date_time_test']!, _dateTimeTestMeta));
    } else if (isInserting) {
      context.missing(_dateTimeTestMeta);
    }
    if (data.containsKey('blob_test')) {
      context.handle(_blobTestMeta,
          blobTest.isAcceptableOrUnknown(data['blob_test']!, _blobTestMeta));
    }
    return context;
  }

  @override
  Set<GeneratedColumn> get $primaryKey => {id};
  @override
  Todo map(Map<String, dynamic> data, {String? tablePrefix}) {
    return Todo.fromData(data,
        prefix: tablePrefix != null ? '$tablePrefix.' : null);
  }

  @override
  $TodosTable createAlias(String alias) {
    return $TodosTable(_db, alias);
  }
}

abstract class _$MyDatabase extends GeneratedDatabase {
  _$MyDatabase(QueryExecutor e) : super(SqlTypeSystem.defaultInstance, e);
  late final $TodosTable todos = $TodosTable(this);
  @override
  Iterable<TableInfo> get allTables => allSchemaEntities.whereType<TableInfo>();
  @override
  List<DatabaseSchemaEntity> get allSchemaEntities => [todos];
}
