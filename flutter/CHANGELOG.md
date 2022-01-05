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