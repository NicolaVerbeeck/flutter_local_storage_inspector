import 'package:flutter/foundation.dart';

void _dontLog(String _) {}

/// Logger callback used by the storage inspector
ValueChanged<String> storageInspectorLogger = _dontLog;
