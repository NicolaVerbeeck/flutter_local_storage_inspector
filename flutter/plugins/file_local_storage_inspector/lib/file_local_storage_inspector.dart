library file_local_storage_inspector;

export 'src/io/io_file_server.dart'
    if (dart.library.html) 'src/io/web_file_server.dart';
