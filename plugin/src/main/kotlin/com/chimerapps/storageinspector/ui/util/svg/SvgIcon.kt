package com.chimerapps.storageinspector.ui.util.svg

import com.intellij.ui.scale.JBUIScale
import com.intellij.util.ui.ImageUtil
import org.apache.batik.transcoder.TranscoderInput
import org.apache.batik.transcoder.TranscoderOutput
import org.apache.batik.transcoder.TranscodingHints
import org.apache.batik.transcoder.image.ImageTranscoder
import java.awt.Component
import java.awt.Graphics
import java.awt.GraphicsConfiguration
import java.awt.image.BufferedImage
import java.io.File
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
            val cssFile = File.createTempFile("batik-default-override-", ".css")
            return try {
                val css = "svg {" +
                        "shape-rendering: geometricPrecision;" +
                        "text-rendering:  geometricPrecision;" +
                        "color-rendering: optimizeQuality;" +
                        "image-rendering: optimizeQuality;" +
                        "}"
                cssFile.writeText(css)
                t.putHint(ImageTranscoder.KEY_USER_STYLESHEET_URI, cssFile.toURI().toString());

                t.transcode(TranscoderInput(StringReader(svg)), null)

                t.bufferedImage?.let { SvgIcon(it, it.width, it.height) }
            } catch (e: Throwable) {
                null
            } finally {
                cssFile.delete()
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

    override fun writeImage(img: BufferedImage, output: TranscoderOutput?) {
        bufferedImage = img
    }

    fun setDimensions(w: Int, h: Int) {
        val scale = JBUIScale.sysScale(null as GraphicsConfiguration?)

        hints[KEY_WIDTH] = w.toFloat() * scale
        hints[KEY_HEIGHT] = h.toFloat() * scale
    }

    fun putHint(key: TranscodingHints.Key, hint: Any) {
        hints[key] = hint
    }
}