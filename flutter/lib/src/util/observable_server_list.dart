class ObservableList<T> {
  final _servers = <T>[];
  final _listeners = <ObservableListObserver<T>>[];

  List<T> get servers => _servers;

  void add(T server) {
    _servers.add(server);
    for (final listener in _listeners) {
      listener.onAdded(server);
    }
  }

  void remove(T server) {
    _servers.remove(server);
    for (final listener in _listeners) {
      listener.onRemoved(server);
    }
  }

  void addListener(ObservableListObserver<T> listener) {
    _listeners.add(listener);
  }

  void removeListener(ObservableListObserver<T> listener) {
    _listeners.remove(listener);
  }
}

abstract class ObservableListObserver<T> {
  void onAdded(T added);

  void onRemoved(T removed);
}
