package xyz.hexode.signaturetosvg

import android.graphics.Color
import java.io.OutputStream

/**
 * Created by JGLouis on 08/03/2018.
 *
 * Serializable object representing an SVG
 */
class Svg(private val width: Int, private val height: Int) {
    private val mPaths = mutableListOf<Path>()
    private val mTexts = mutableListOf<Text>()

    fun startPath(strokeColor: Int = Color.GREEN, strokeWidth: Float = 5.5f, strokeCap: Path.Cap = Path.Cap.Round, strokeJoin: Path.Join = Path.Join.Round) {
        mPaths += Path(strokeColor, strokeWidth, strokeCap, strokeJoin)
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

    fun addText(x: Float,
                y: Float,
                content: String,
                fontSize: Int,
                fillColor: Int = Color.BLACK,
                anchor: Text.Anchor = Text.Anchor.Start,
                alignment: Text.Align = Text.Align.Start,
                fontFamily: String = "Tahoma",
                fontStyle: Text.FontStyle = Text.FontStyle.Normal,
                fontWeight: Int = 400) {
        mTexts += Text(x, y, content, fillColor, anchor, alignment, fontFamily, fontSize, fontStyle, fontWeight)
    }

    fun writeXml(out: OutputStream) {
        out.write("<svg width=\"$width\" height=\"$height\" xmlns=\"http://www.w3.org/2000/svg\">".toByteArray())
        mPaths.forEach { it.writeXml(out) }
        mTexts.forEach { it.writeXml(out) }
        out.write("</svg>".toByteArray())
    }

    fun reset() {
        mPaths.clear()
    }

    class Path(private val strokeColor: Int,
               private val strokeWidth: Float,
               private val strokeCap: Cap,
               private val strokeJoin: Join) {
        internal val mData: MutableList<Command> = mutableListOf()

        enum class Cap {
            Butt,
            Round,
            Square;

            override fun toString(): String {
                return super.toString().toLowerCase()
            }
        }

        enum class Join {
            Miter,
            Round,
            Bevel;

            override fun toString(): String {
                return super.toString().toLowerCase()
            }
        }

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
            val strokeColorWithoutAlpha = colorToHexadecimalString(strokeColor)
            out.write(("<path " +
                    "stroke=\"$strokeColorWithoutAlpha\" " +
                    "fill=\"none\" " +
                    "stroke-width=\"$strokeWidth\" " +
                    "stroke-linecap=\"$strokeCap\" " +
                    "stroke-linejoin=\"$strokeJoin\" " +
                    "d=\"").toByteArray())
            mData.forEach {
                out.write(it.toString().toByteArray())
            }
            out.write("\" />".toByteArray())
        }
    }

    class Text(private val x: Float,
               private val y: Float,
               private val content: String,
               private val fillColor: Int,
               private val anchor: Anchor,
               private val alignment: Align,
               private val fontFamily: String,
               private val fontSize: Int,
               private val fontStyle: FontStyle,
               private val fontWeight: Int) {
        enum class Anchor {
            Start,
            Middle,
            End;

            override fun toString(): String {
                return super.toString().toLowerCase()
            }
        }

        enum class Align {
            Start,
            End,
            Center;

            override fun toString(): String {
                return super.toString().toLowerCase()
            }
        }

        enum class FontStyle {
            Normal,
            Italique,
            Oblique;

            override fun toString(): String {
                return super.toString().toLowerCase()
            }
        }

        fun writeXml(out: OutputStream) {
            out.write(("<text " +
                    "x=\"$x\" " +
                    "y=\"$y\" " +
                    "text-anchor=\"$anchor\" " +
                    "text-align=\"$alignment\" " +
                    "font-family=\"$fontFamily\" " +
                    "fill=\"${colorToHexadecimalString(fillColor)}\" " +
                    "font-size=\"${fontSize}pt\" " +
                    "font-weight=\"$fontWeight\" " +
                    "font-style=\"$fontStyle\">" +
                    "$content</text>").toByteArray())
        }
    }
}
