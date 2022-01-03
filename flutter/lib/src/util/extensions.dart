import 'dart:async';

extension StreamExtensions<T> on Stream<T> {
  Future<Iterable<R>> mapNotNull<R>(R? Function(T) mapper) {
    final result = <R>[];
    final future = Completer<Iterable<R>>();
    listen(
        (T data) {
          final res = mapper(data);
          if (res != null) result.add(res);
        },
        onError: future.completeError,
        onDone: () {
          future.complete(result);
        },
        cancelOnError: true);
    return future.future;
  }
}
