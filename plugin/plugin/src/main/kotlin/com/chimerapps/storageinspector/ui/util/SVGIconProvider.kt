package com.chimerapps.storageinspector.ui.util

import com.chimerapps.discovery.ui.SessionIconProvider
import com.chimerapps.storageinspector.ui.util.svg.SvgIcon
import javax.swing.Icon

class SVGIconProvider(private val width: Int, private val height: Int) : SessionIconProvider {

    override fun iconForString(iconString: String): Icon? {
        if (iconString.trimStart().startsWith('<')) {
            return SvgIcon.load(iconString, width, height)
        }
        return null
    }

}
