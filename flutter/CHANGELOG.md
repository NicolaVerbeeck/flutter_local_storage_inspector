## 0.5.0

* Update the example app and its build tool versions
* Update flutter_lints dev dependency

## 0.4.0

* Bump required dart version to 3.0
* Support uuid 4.0.x

## 0.3.0

* Bump required dart version
* Fix analysis issues
* Added funding options

## 0.2.3

* Align version numbers

## 0.2.1

* Updated readme

## 0.2.0

* *BREAKING*: Specific implementations have been moved to their own packages and are no longer included in this package
  * files: [file_local_storage_inspector](https://pub.dev/packages/file_local_storage_inspector)
  * preferences: [preferences_local_storage_inspector](https://pub.dev/packages/preferences_local_storage_inspector)
  * secure storage: [secure_storage_local_storage_inspector](https://pub.dev/packages/secure_storage_local_storage_inspector)
* Update with vm service protocol support

## 0.1.6

* Update SQL support to allow default values to be reported

## 0.1.5

* Update SQL support to also report date time format

## 0.1.4

* Updated SQL protocol to include updating
* Updated SQL support to include schema

## 0.1.3

* Base SQL protocol

## 0.1.2

* Use more strict analysis options
* Fixed missing await
* Updated readme indicate released plugin

## 0.1.1

* Allow specifying the icon for all built-in servers

## 0.1.0

* First non-dev release

## 0.0.1-dev.12

* Support renaming in file protocol

## 0.0.1-dev.11

* Updated spec to allow empty directories to be returned by the file server
* Sanitize all passed paths to remove leading '/' in default io file server

## 0.0.1-dev.10

* Fixed bug in io file server that would prevent proper listing
* Fixed bug in io file server that would also return intermediate directories

## 0.0.1-dev.9

* Update file protocol to include file size

## 0.0.1-dev.8

* Ensure correct type conversion for ValueWithType binary and datetime variants

## 0.0.1-dev.7

* Remove print statements

## 0.0.1-dev.6

* Fixed bug when updating string lists in key value server
* Update to latest version of discovery protocol to avoid race conditions

## 0.0.1-dev.5

* As soon as the resume signal is given, remove the paused flag

## 0.0.1-dev.4

* Fixed issue where pause was not actually pausing

## 0.0.1-dev.3

* Add file server protocol

## 0.0.1-dev.2

* Ensure web "compatibility"

## 0.0.1-dev.1

* Initial release with key-value protocol proposal in-place