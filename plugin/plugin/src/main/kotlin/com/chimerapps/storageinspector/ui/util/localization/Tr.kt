package com.chimerapps.storageinspector.ui.util.localization

enum class Tr(val key: String) {
    ActionConnect("storageinspector.action.connect"), //Connect
    ActionConnectDescription("storageinspector.action.connect.description"), //Connect to storage inspector server
    ActionDisconnect("storageinspector.action.disconnect"), //Disconnect
    ActionDisconnectDescription("storageinspector.action.disconnect.description"), //Disconnect from storage inspector server
    ActionNewSession("storageinspector.action.new.session"), //New session
    ActionNewSessionDescription("storageinspector.action.new.session.description"), //Start a new session
    GenericRefresh("storageinspector.generic.refresh"), //Refresh
    KeyValueKey("storageinspector.key.value.key"), //Key
    KeyValueValue("storageinspector.key.value.value"), //Value
    PreferencesBrowseAdbDescription("storageinspector.preferences.browse.adb.description"), //Path to adb
    PreferencesBrowseAdbTitle("storageinspector.preferences.browse.adb.title"), //Storage inspector - adb
    PreferencesBrowseIdeviceDescription("storageinspector.preferences.browse.idevice.description"), //Path to imobiledevice folders
    PreferencesBrowseIdeviceTitle("storageinspector.preferences.browse.idevice.title"), //Storage inspector - imobiledevice
    PreferencesBrowseSdbDescription("storageinspector.preferences.browse.sdb.description"), //Path to sdb
    PreferencesBrowseSdbTitle("storageinspector.preferences.browse.sdb.title"), //Storage inspector - sdb
    PreferencesButtonTestConfiguration("storageinspector.preferences.button.test.configuration"), //Test configuration
    PreferencesOptionPathToAdb("storageinspector.preferences.option.path.to.adb"), //Path to adb:
    PreferencesOptionPathToIdevice("storageinspector.preferences.option.path.to.idevice"), //Path to idevice binaries:
    PreferencesOptionPathToSdb("storageinspector.preferences.option.path.to.sdb"), //Path to sdb:
    PreferencesSendAnalytics("storageinspector.preferences.send.analytics"), //Send anonymous usage statistics
    PreferencesSendAnalyticsInfo("storageinspector.preferences.send.analytics.info"), //More info
    PreferencesTestMessageChecking("storageinspector.preferences.test.message.checking"), //Checking %s command
    PreferencesTestMessageDebugbridgeFoundAt("storageinspector.preferences.test.message.debugbridge.found.at"), //%1$s defined at path: %2$s
    PreferencesTestMessageDebugbridgeNotFound("storageinspector.preferences.test.message.debugbridge.not.found"), //Path to %s not found
    PreferencesTestMessageDebugbridgeOk("storageinspector.preferences.test.message.debugbridge.ok"), //%s path seems ok
    PreferencesTestMessageErrorCommunicationFailed("storageinspector.preferences.test.message.error.communication.failed"), //ERROR - Failed to communicate with %s
    PreferencesTestMessageErrorFileNotExecutable("storageinspector.preferences.test.message.error.file.not.executable"), //ERROR - %s file not executable
    PreferencesTestMessageErrorFileNotFound("storageinspector.preferences.test.message.error.file.not.found"), //ERROR - %s file not found
    PreferencesTestMessageErrorIdeviceNotDirectory("storageinspector.preferences.test.message.error.idevice.not.directory"), //ERROR - iMobileDevice path is not a directory
    PreferencesTestMessageErrorIdeviceNotFound("storageinspector.preferences.test.message.error.idevice.not.found"), //ERROR - iMobileDevice folder not found
    PreferencesTestMessageErrorPathIsDir("storageinspector.preferences.test.message.error.path.is.dir"), //ERROR - Specified path is a directory
    PreferencesTestMessageFileOk("storageinspector.preferences.test.message.file.ok"), //%s seems ok
    PreferencesTestMessageFoundDevicesCount("storageinspector.preferences.test.message.found.devices.count"), //%s devices returns: %d device(s)
    PreferencesTestMessageIdevicePath("storageinspector.preferences.test.message.idevice.path"), //iMobileDevice folder defined at path: %s
    PreferencesTestMessageListingDevices("storageinspector.preferences.test.message.listing.devices"), //Listing devices
    PreferencesTestMessageStarting("storageinspector.preferences.test.message.starting"), //Starting %s server
    PreferencesTestMessageTestingDebugbridgeTitle("storageinspector.preferences.test.message.testing.debugbridge.title"), //Testing %s\n=======================================
    PreferencesTestMessageTestingIdeviceTitle("storageinspector.preferences.test.message.testing.idevice.title"), //\nTesting iDevice\n=======================================
    ServersFile("storageinspector.servers.file"), //File Servers
    ServersKeyValue("storageinspector.servers.key.value"), //Key Value Servers
    ServersSql("storageinspector.servers.sql"), //SQL Servers
    SqlCurrentQuery("storageinspector.sql.current.query"), //Showing results of %s
    SqlHistoryDescription("storageinspector.sql.history.description"), //List of previous queries
    SqlHistoryPopupTitle("storageinspector.sql.history.popup.title"), //Query history
    SqlHistoryText("storageinspector.sql.history.text"), //Query history
    SqlSelectDatabase("storageinspector.sql.select.database"), //Select a table
    StatusConnected("storageinspector.status.connected"), //Connected
    StatusConnectedTo("storageinspector.status.connected.to"), //to
    StatusDisconnected("storageinspector.status.disconnected"), //Disconnected
    StatusPaused("storageinspector.status.paused"), //(paused)
    TypeBinary("storageinspector.type.binary"), //binary
    TypeBooleanFalse("storageinspector.type.boolean.false"), //false
    TypeBooleanTrue("storageinspector.type.boolean.true"), //true
    ViewSession("storageinspector.view.session"), //Session
    ViewStartingAdb("storageinspector.view.starting.adb"); //Starting adb

    fun tr(vararg arguments: Any) : String {
        val raw = Localization.getString(key)
        if (arguments.isEmpty()) return raw
        return String.format(raw, *arguments)
    }
}