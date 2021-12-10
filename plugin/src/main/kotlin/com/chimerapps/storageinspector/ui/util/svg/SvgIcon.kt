package com.chimerapps.storageinspector.ui.util.svg

import com.intellij.util.ui.ImageUtil
import org.apache.batik.transcoder.TranscoderInput
import org.apache.batik.transcoder.TranscoderOutput
import org.apache.batik.transcoder.image.ImageTranscoder
import java.awt.Component
import java.awt.Graphics
import java.awt.image.BufferedImage
import java.io.StringReader
import javax.swing.Icon

/**
 * @author Nicola Verbeeck
 */
class SvgIcon(private val image: BufferedImage, private val width: Int, private val height: Int) : Icon {

    companion object {
        fun load(svg: String, w: Int, h: Int): SvgIcon? {
            val t = BufferedImageTranscoder()
            if (w != 0 && h != 0) {
                t.setDimensions(w, h)
            }
            return try {
                t.transcode(TranscoderInput(StringReader(svg)), null)

                t.bufferedImage?.let { SvgIcon(it, it.width, it.height) }
            } catch (e: Throwable) {
                null
            }
        }
    }

    override fun paintIcon(c: Component?, g: Graphics, x: Int, y: Int) {
        g.drawImage(image, x, y, null)
    }

    override fun getIconWidth(): Int = width

    override fun getIconHeight(): Int = height

}

private class BufferedImageTranscoder : ImageTranscoder() {
    var bufferedImage: BufferedImage? = null
        private set

    override fun createImage(width: Int, height: Int): BufferedImage {
        return ImageUtil.createImage(width, height, BufferedImage.TYPE_INT_ARGB)
    }

    override fun writeImage(img: BufferedImage, output: TranscoderOutput) {
        bufferedImage = img
    }

    fun setDimensions(w: Int, h: Int) {
        hints[KEY_WIDTH] = w
        hints[KEY_HEIGHT] = h
    }
}