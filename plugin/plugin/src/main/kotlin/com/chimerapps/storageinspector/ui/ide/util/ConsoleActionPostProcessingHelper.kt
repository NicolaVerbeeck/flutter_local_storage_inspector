package com.chimerapps.storageinspector.ui.ide.util

import com.intellij.execution.actions.ConsoleActionsPostProcessor
import com.intellij.execution.ui.ConsoleView
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.util.Disposer
import io.flutter.run.daemon.DaemonConsoleView

class ConsoleActionPostProcessingHelper : ConsoleActionsPostProcessor() {

    private val linkedConsoles = hashSetOf<ConsoleView>()

    override fun postProcess(console: ConsoleView, actions: Array<out AnAction>): Array<AnAction> {
        if (console is DaemonConsoleView) {
            if (linkedConsoles.add(console)) {
                Disposer.register(console) {
                    linkedConsoles.remove(console)
                }
                console.addMessageFilter(StorageInspectorConnectFilter(console.project))
            }
        }
        return super.postProcess(console, actions)
    }
}