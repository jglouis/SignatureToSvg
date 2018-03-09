package xyz.hexode.signaturetosvg

import android.graphics.Color
import java.io.OutputStream

/**
 * Created by JGLouis on 08/03/2018.
 *
 * Serializable object representing an SVG
 */
class Svg(private val width: Int, private val height: Int) {
    private val mPaths: MutableList<Path> = mutableListOf()

    fun startPath(strokeColor: Int = Color.GREEN, strokeWidth: Int = 5) {
        mPaths += Path(strokeColor, strokeWidth)
    }

    fun moveTo(x: Float, y: Float) {
        mPaths.last().mData += Path.Command(Path.CommandType.M, x, y)
    }

    fun quadTo(x1: Float, y1: Float, x2: Float, y2: Float) {
        mPaths.last().mData += Path.Command(Path.CommandType.Q, x1, y1, x2, y2)
    }

    fun lineTo(x: Float, y: Float) {
        mPaths.last().mData += Path.Command(Path.CommandType.L, x, y)
    }

    fun writeXml(out: OutputStream) {
        out.write("<svg width=\"$width\" height=\"$height\" xmlns=\"http://www.w3.org/2000/svg\">".toByteArray())
        mPaths.forEach { it.writeXml(out) }
        out.write("</svg>".toByteArray())
    }

    fun reset() {
        mPaths.clear()
    }

    class Path(private val strokeColor: Int, private val strokeWidth: Int) {
        internal val mData: MutableList<Command> = mutableListOf()

        enum class CommandType {
            M, // moveTo
            L, // lineTo
            Q, // quadratic Bezier curve
        }

        class Command(private val type: CommandType, private vararg val params: Float) {
            override fun toString(): String {
                val sb = StringBuilder()
                sb.append(type.toString())
                params.forEach {
                    sb.append(it.toString())
                    sb.append(" ")
                }
                return sb.toString()
            }
        }

        internal fun writeXml(out: OutputStream) {
            val red = Color.red(strokeColor)
            val green = Color.green(strokeColor)
            val blue = Color.blue(strokeColor)
            out.write("<path stroke=\"rgb($red,$green,$blue)\" fill=\"none\" stroke-width=\"$strokeWidth\" d=\"".toByteArray())
            mData.forEach {
                out.write(it.toString().toByteArray())
            }
            out.write("\" />".toByteArray())
        }
    }
}
