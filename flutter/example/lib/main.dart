import 'package:file_local_storage_inspector/file_local_storage_inspector.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:path_provider/path_provider.dart';
import 'package:preferences_local_storage_inspector/preferences_local_storage_inspector.dart';
import 'package:secure_storage_local_storage_inspector/secure_storage_local_storage_inspector.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:storage_inspector/storage_inspector.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();

  final preferences = await SharedPreferences.getInstance();

  final driver =
      StorageServerDriver(bundleId: 'com.example.example', icon: 'flutter');
  final keyValueServer =
      PreferencesKeyValueServer(preferences, 'Preferences', keySuggestions: {
    const ValueWithType(StorageType.string, 'testBool'),
    const ValueWithType(StorageType.string, 'testInt'),
    const ValueWithType(StorageType.string, 'testFloat'),
  });
  driver.addKeyValueServer(keyValueServer);

  final secureKeyValueServer = SecureStorageKeyValueServer(
      const FlutterSecureStorage(), 'Secure Storage',
      keySuggestions: {
        'testBool',
        'testInt',
        'testFloat',
      });
  driver.addKeyValueServer(secureKeyValueServer);

  final fileServer =
      DefaultFileServer(await _documentsDirectory(), 'App Documents');
  driver.addFileServer(fileServer);

  // Don't wait for a connection from the instrumentation driver
  await driver.start(paused: false);

  // run app
  runApp(const MyApp());
}

Future<String> _documentsDirectory() async {
  if (kIsWeb) return '.';
  return (await getApplicationDocumentsDirectory()).path;
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Flutter Demo',
      theme: ThemeData(
        primarySwatch: Colors.blue,
      ),
      home: const MyHomePage(title: 'Flutter Demo Home Page'),
    );
  }
}

class MyHomePage extends StatefulWidget {
  const MyHomePage({super.key, required this.title});

  final String title;

  @override
  State<MyHomePage> createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  int _counter = 0;

  void _incrementCounter() {
    setState(() {
      _counter++;
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(widget.title),
      ),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: <Widget>[
            const Text(
              'You have pushed the button this many times:',
            ),
            Text(
              '$_counter',
              style: Theme.of(context).textTheme.headlineMedium,
            ),
          ],
        ),
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: _incrementCounter,
        tooltip: 'Increment',
        child: const Icon(Icons.add),
      ),
    );
  }
}
