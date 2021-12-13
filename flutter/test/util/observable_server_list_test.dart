import 'package:flutter_test/flutter_test.dart';
import 'package:storage_inspector/src/util/observable_server_list.dart';

void main() {
  group('Observable list tests', () {
    test('Test add notifies', () {
      final list = ObservableList<String>();
      final listener = TestListener();
      list.addListener(listener);
      list.add('test');
      expect(1, listener.addedCalled);
      list.add('test2');
      expect(2, listener.addedCalled);
      list.removeListener(listener);
      list.add('test2');
      expect(2, listener.addedCalled);
    });
    test('Test remove notifies', () {
      final list = ObservableList<String>();
      final listener = TestListener();
      list.addListener(listener);
      list.remove('test');
      expect(1, listener.removedCalled);
      list.remove('test2');
      expect(2, listener.removedCalled);
      list.removeListener(listener);
      list.remove('test2');
      expect(2, listener.removedCalled);
    });
  });
}

class TestListener implements ObservableListObserver<String> {
  var addedCalled = 0;
  var removedCalled = 0;

  @override
  void onAdded(String added) {
    ++addedCalled;
  }

  @override
  void onRemoved(String removed) {
    ++removedCalled;
  }
}
