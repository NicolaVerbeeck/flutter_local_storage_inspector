# Local storage inspector

This repository contains the different parts that make up the local storage inspector system.

The local storage inspector system allows you to inspect and modify the contents of local storage on
your 'app' using a websocket based API or a plugin for intelliJ IDEs.

## Framework

The framework uses a websocket based protocol to send and receive control messages between a server (app) and a client.

The full protocol still needs a description as it is still work in progress.


## UI

### IntelliJ plugin
[![plugin_badge](https://img.shields.io/jetbrains/plugin/v/18231-local-storage-inspector?color=blue&label=IntelliJ%20Plugin)](https://plugins.jetbrains.com/plugin/18231-local-storage-inspector)

A plugin that acts as a storage inspector is available for IntelliJ IDEs. The source lives in [plugin](plugin), the plugin 
is published to the [IntelliJ marketplace](https://plugins.jetbrains.com/plugin/18231-local-storage-inspector) and a custom 
plugin repository that receives faster updates.

To enable this custom repository, add: https://plugins.chimerapps.com/pluginUpload/updatePlugins.xml to Plugins > Manage Plugin Repositories 

## Libraries

### Flutter
[![pub package](https://img.shields.io/pub/v/storage_inspector.svg?color=blue)](https://pub.dev/packages/storage_inspector)
[![pub points](https://badges.bar/sentry/pub%20points)](https://pub.dev/packages/storage_inspector/score)

A flutter library is provided that implements the local storage server with built-in bindings for
shared preferences, encrypted shared preferences, file browsing, ... See [flutter](flutter) for more
details on how to use and integrate