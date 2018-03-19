package xyz.hexode.signaturetosvg

import android.graphics.Color
import android.graphics.Paint
import java.io.OutputStream

/**
 * Created by JGLouis on 08/03/2018.
 *
 * Serializable object representing an SVG
 */
class Svg(private val width: Int, private val height: Int) {
    private val mPaths = mutableListOf<Path>()
    private val mTexts = mutableListOf<Text>()

    fun startPath(strokeColor: Int = Color.GREEN, strokeWidth: Float = 5.5f, strokeCap: Paint.Cap = Paint.Cap.ROUND, strokeJoin: Paint.Join = Paint.Join.ROUND) {
        mPaths += Path(strokeColor, strokeWidth, strokeCap, strokeJoin)
    }

    fun undoLastPath() {
        if(mPaths.size > 0) mPaths.removeAt(mPaths.size - 1)
    }

    fun getAndroidPathsAndPaints(): List<Pair<android.graphics.Path, Paint>> {
        return List(mPaths.size, { index ->
            val paint = Paint().apply {
                this.strokeWidth = mPaths[index].strokeWidth
                this.color = mPaths[index].strokeColor
                this.strokeCap = mPaths[index].strokeCap
                this.strokeJoin = mPaths[index].strokeJoin
                this.isAntiAlias = true
                this.isDither = true
                this.style = Paint.Style.STROKE
            }
            Pair(mPaths[index].getAndroidPath(), paint)
        })
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

    class Path(internal val strokeColor: Int,
               internal val strokeWidth: Float,
               internal val strokeCap: Paint.Cap,
               internal val strokeJoin: Paint.Join) {
        internal val mData: MutableList<Command> = mutableListOf()


        enum class CommandType {
            M, // moveTo
            L, // lineTo
            Q, // quadratic Bezier curve
        }

        class Command(internal val type: CommandType, internal vararg val params: Float) {
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

        internal fun getAndroidPath(): android.graphics.Path {
            return android.graphics.Path().apply {
                mData.forEach {
                    when (it.type) {
                        Svg.Path.CommandType.M -> this.moveTo(it.params[0], it.params[1])
                        Svg.Path.CommandType.L -> this.lineTo(it.params[0], it.params[1])
                        Svg.Path.CommandType.Q -> this.quadTo(it.params[0], it.params[1], it.params[2], it.params[3])
                    }
                }
            }
        }

        internal fun writeXml(out: OutputStream) {
            val strokeColorWithoutAlpha = colorToHexadecimalString(strokeColor)
            out.write(("<path " +
                    "stroke=\"$strokeColorWithoutAlpha\" " +
                    "fill=\"none\" " +
                    "stroke-width=\"$strokeWidth\" " +
                    "stroke-linecap=\"${strokeCap.toString().toLowerCase()}\" " +
                    "stroke-linejoin=\"${strokeJoin.toString().toLowerCase()}\" " +
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
