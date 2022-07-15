package com.chimerapps.storageinspector.ui.ide.settings

import com.chimerapps.discovery.device.adb.ADBBootstrap
import com.chimerapps.discovery.device.debugbridge.DebugBridgeBootstrap
import com.chimerapps.discovery.device.sdb.SDBBootstrap
import com.chimerapps.storageinspector.ui.ide.InspectorSessionWindow
import com.chimerapps.storageinspector.ui.ide.InspectorToolWindow
import com.chimerapps.storageinspector.ui.util.localization.Tr
import com.chimerapps.storageinspector.util.adb.ADBUtils
import com.chimerapps.storageinspector.util.analytics.batching.BatchingEventTracker
import com.chimerapps.storageinspector.util.sdb.SDBUtils
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.wm.WindowManager
import com.intellij.ui.components.JBTextField
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import javax.swing.JComponent
import javax.swing.JTextPane
import javax.swing.SwingWorker

class SettingsFormWrapper(private val project: Project?, private val driftInspectorSettings: StorageInspectorSettings) {

    private val settingsForm = StorageInspectorSettingsForm() {
        runTest(project = project)
    }

    val component: JComponent
        get() = settingsForm.rootComponent()

    val isModified: Boolean
        get() = ((settingsForm.iDeviceField.textOrNull != driftInspectorSettings.state.iDeviceBinariesPath)
                || (settingsForm.adbField.textOrNull != driftInspectorSettings.state.adbPath)
                || (settingsForm.analyticsCheckbox.isSelected != driftInspectorSettings.state.analyticsStatus))

    private var worker: VerifierWorker? = null
    private var runningTests = false

    fun save() {
        driftInspectorSettings.state.iDeviceBinariesPath = settingsForm.iDeviceField.textOrNull
        driftInspectorSettings.state.adbPath = settingsForm.adbField.textOrNull
        driftInspectorSettings.state.analyticsStatus = settingsForm.analyticsCheckbox.isSelected
        if (driftInspectorSettings.state.analyticsStatus != true) {
            driftInspectorSettings.state.analyticsUserId = null
            BatchingEventTracker.instance.clear()
        }

        val project = ProjectManager.getInstance().openProjects.firstOrNull { project ->
            val window = WindowManager.getInstance().suggestParentWindow(project)
            window != null && window.isActive
        } ?: return
        InspectorToolWindow.get(project)?.first?.redoADBBootstrapBlocking()
    }

    fun initUI() {
        settingsForm.adbField.addBrowseFolderListener(
            Tr.PreferencesBrowseAdbTitle.tr(),
            Tr.PreferencesBrowseAdbDescription.tr(),
            project,
            FileChooserDescriptor(true, false, false, false, false, false)
        )
        settingsForm.sdbField.addBrowseFolderListener(
            Tr.PreferencesBrowseSdbTitle.tr(),
            Tr.PreferencesBrowseSdbDescription.tr(),
            project,
            FileChooserDescriptor(true, false, false, false, false, false)
        )
        settingsForm.iDeviceField.addBrowseFolderListener(
            Tr.PreferencesBrowseIdeviceTitle.tr(),
            Tr.PreferencesBrowseIdeviceDescription.tr(),
            project,
            FileChooserDescriptor(false, true, false, false, false, false)
        )

        (settingsForm.iDeviceField.textField as? JBTextField)?.emptyText?.text =
            InspectorSessionWindow.DEFAULT_IDEVICE_PATH

        (settingsForm.adbField.textField as? JBTextField)?.emptyText?.text =
            ADBBootstrap(ADBUtils.guessPaths(project)).pathToDebugBridge ?: ""

        (settingsForm.sdbField.textField as? JBTextField)?.emptyText?.text =
            SDBBootstrap(SDBUtils.guessPaths(project)).pathToDebugBridge ?: ""

        reset()
    }

    fun reset() {
        settingsForm.adbField.text = driftInspectorSettings.state.adbPath ?: ""
        settingsForm.sdbField.text = driftInspectorSettings.state.sdbPath ?: ""
        settingsForm.iDeviceField.text = driftInspectorSettings.state.iDeviceBinariesPath ?: ""
        settingsForm.analyticsCheckbox.isSelected = driftInspectorSettings.state.analyticsStatus ?: false
    }

    private fun runTest(project: Project?) {
        if (runningTests) return

        runningTests = true
        worker?.cancel(true)
        settingsForm.resultsPane.text = ""

        worker = VerifierWorker(
            settingsForm.adbField.textOrNull ?: ADBBootstrap(ADBUtils.guessPaths(project)).pathToDebugBridge,
            settingsForm.sdbField.textOrNull ?: SDBBootstrap(SDBUtils.guessPaths(project)).pathToDebugBridge,
            settingsForm.iDeviceField.textOrNull ?: InspectorSessionWindow.DEFAULT_IDEVICE_PATH,
            settingsForm.resultsPane
        ) {
            runningTests = false
        }.also {
            it.execute()
        }
    }
}

private val TextFieldWithBrowseButton.textOrNull: String?
    get() {
        val textValue = text.trim()
        if (textValue.isEmpty())
            return null
        return textValue
    }

private class VerifierWorker(
    private val adbPath: String?,
    private val sdbPath: String?,
    private val iDevicePath: String,
    private val textField: JTextPane,
    private val onFinished: () -> Unit
) : SwingWorker<Boolean, String>() {

    private val builder = StringBuilder()

    override fun doInBackground(): Boolean {
        val adbResult = testDebugBridge(adbPath, "ADB") { ADBBootstrap(emptySet()) { it } }
        val sdbResult = testDebugBridge(sdbPath, "SDB") { SDBBootstrap(emptySet()) { it } }
        val iDeviceResult = testIDevice()
        return adbResult && iDeviceResult && sdbResult
    }

    override fun process(chunks: List<String>) {
        chunks.forEach { builder.append(it).append('\n') }

        textField.text = builder.toString()
        textField.invalidate()
    }

    override fun done() {
        super.done()
        onFinished()
    }

    private fun testDebugBridge(path: String?, name: String, bootstrapCreator: (path: String) -> DebugBridgeBootstrap): Boolean {
        publish(Tr.PreferencesTestMessageTestingDebugbridgeTitle.tr(name))
        var ok = false
        if (path == null) {
            publish(Tr.PreferencesTestMessageDebugbridgeNotFound.tr(name))
        } else {
            publish(Tr.PreferencesTestMessageDebugbridgeFoundAt.tr(name, path))
            val file = File(path)
            if (file.isDirectory) {
                publish(Tr.PreferencesTestMessageErrorPathIsDir.tr())
            } else if (!file.exists()) {
                publish(Tr.PreferencesTestMessageDebugbridgeNotFound.tr(name))
            } else if (!file.canExecute()) {
                publish(Tr.PreferencesTestMessageErrorFileNotExecutable.tr(name))
            } else {
                publish(Tr.PreferencesTestMessageDebugbridgeOk.tr(name))
                ok = true
            }
        }
        return ok && checkDebugBridgeExecutable(path!!, name, bootstrapCreator)
    }

    private fun testIDevice(): Boolean {
        publish(Tr.PreferencesTestMessageTestingIdeviceTitle.tr())

        publish(Tr.PreferencesTestMessageIdevicePath.tr(iDevicePath))
        val file = File(iDevicePath)
        if (!file.exists()) {
            publish(Tr.PreferencesTestMessageErrorIdeviceNotFound.tr())
        } else if (!file.isDirectory) {
            publish(Tr.PreferencesTestMessageErrorIdeviceNotDirectory.tr())
        } else {
            checkFile(File(file, "ideviceinfo"))
            checkFile(File(file, "iproxy"))
            checkFile(File(file, "idevice_id"))
        }

        return true //TODO
    }

    private fun checkFile(file: File) {
        if (!file.exists()) {
            publish(Tr.PreferencesTestMessageErrorFileNotFound.tr(file.name))
        } else if (!file.canExecute()) {
            publish(Tr.PreferencesTestMessageErrorFileNotExecutable.tr(file.name))
        } else {
            publish(Tr.PreferencesTestMessageFileOk.tr(file.name))
        }
    }

    private fun checkDebugBridgeExecutable(path: String, name: String, bootstrapCreator: (path: String) -> DebugBridgeBootstrap): Boolean {
        publish(Tr.PreferencesTestMessageChecking.tr(name))
        val bootstrap = bootstrapCreator(path)
        publish(Tr.PreferencesTestMessageStarting.tr(name))
        return try {
            val adbInterface = bootstrap.bootStrap()
            publish(Tr.PreferencesTestMessageListingDevices.tr(name))
            val devices = adbInterface.devices
            publish(Tr.PreferencesTestMessageFoundDevicesCount.tr(name, devices.size))
            true
        } catch (e: Exception) {
            publish(Tr.PreferencesTestMessageErrorCommunicationFailed.tr(name))
            val writer = StringWriter()
            val printer = PrintWriter(writer)
            e.printStackTrace(printer)
            printer.flush()
            publish(writer.toString())
            false
        }
    }

}