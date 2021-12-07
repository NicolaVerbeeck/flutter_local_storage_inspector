class ObservableServerList<T> {
  final _servers = <T>[];
  final _listeners = <ObservableServerListObserver<T>>[];

  List<T> get servers => _servers;

  void addServer(T server) {
    _servers.add(server);
    for (final listener in _listeners) {
      listener.onServerAdded(server);
    }
  }

  void removeServer(T server) {
    _servers.remove(server);
    for (final listener in _listeners) {
      listener.onServerRemoved(server);
    }
  }

  void addListener(ObservableServerListObserver<T> listener) {
    _listeners.add(listener);
  }

  void removeListener(ObservableServerListObserver<T> listener) {
    _listeners.remove(listener);
  }
}

abstract class ObservableServerListObserver<T> {
  void onServerAdded(T added);

  void onServerRemoved(T removed);
}
