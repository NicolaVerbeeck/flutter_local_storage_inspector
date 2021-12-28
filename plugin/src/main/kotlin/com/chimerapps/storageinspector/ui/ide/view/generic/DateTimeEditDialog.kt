package com.chimerapps.storageinspector.ui.ide.view.generic

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.util.ui.CalendarView
import java.awt.BorderLayout
import java.awt.event.ActionEvent
import java.util.Date
import javax.swing.Action
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JSpinner
import javax.swing.SpinnerNumberModel

/**
 * @author Nicola Verbeeck
 */
class DateTimeEditDialog(project: Project, time: Date) : DialogWrapper(project, false) {

    private val calendarView = CalendarView()
    private val panel = JPanel(BorderLayout())
    private val bottomPanel = JPanel(BorderLayout())
    private val millisecondsSpinner = JSpinner(SpinnerNumberModel(1000, 0, 1000, 1))

    init {
        title = "Edit date-time (in local timezone)"
        panel.add(calendarView, BorderLayout.CENTER)
        panel.add(bottomPanel, BorderLayout.SOUTH)
        bottomPanel.add(millisecondsSpinner, BorderLayout.EAST)

        calendarView.date = time
        millisecondsSpinner.value = time.time % 1000

        init()
    }

    fun showAndReturn() : Date? {
        if (!showAndGet()) return null
        var time = calendarView.date.time
        time -= time % 1000
        time += millisecondsSpinner.value as Long
        return Date(time)
    }

    override fun createCenterPanel(): JComponent = panel

    override fun createActions(): Array<Action> {
        return arrayOf(
            ResetToNowAction(),
            okAction,
            cancelAction
        )
    }

    private inner class ResetToNowAction : DialogWrapperAction("Now") {

        override fun doAction(e: ActionEvent?) {
            val now = Date()
            calendarView.date = now
            millisecondsSpinner.value = now.time % 1000
        }
    }
}
