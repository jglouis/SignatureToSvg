package xyz.hexode.signaturetosvg

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.*
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.MotionEvent
import android.view.View
import com.flask.colorpicker.ColorPickerView
import com.flask.colorpicker.builder.ColorPickerDialogBuilder
import kotlinx.android.synthetic.main.activity_drawing.*
import java.io.File
import java.io.FileOutputStream


/**
 * Created by JGLouis on 08/03/2018.
 *
 * Drawing activity
 */
class DrawingActivity : AppCompatActivity() {

    private val mPaint: Paint = Paint()
    private val mDrawingView: DrawingView by lazy { DrawingView(this) }

    companion object {
        internal const val REQUEST_CODE_SAVE_PERMISSION = 123
        const val TOUCH_TOLERANCE = 4f
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_drawing)
        drawingLayout.addView(mDrawingView)
        mPaint.isAntiAlias = true
        mPaint.isDither = true
        mPaint.color = Color.GREEN
        mPaint.style = Paint.Style.STROKE
        mPaint.strokeJoin = Paint.Join.ROUND
        mPaint.strokeCap = Paint.Cap.ROUND
        mPaint.strokeWidth = 12f

        textViewSignature.bringToFront()

        buttonUndo.setOnClickListener {
            mDrawingView.undoLastPath()
        }

        buttonSave.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                saveDrawing()
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_CODE_SAVE_PERMISSION)
                }
            }
        }

        buttonPickColor.setOnClickListener {
            ColorPickerDialogBuilder
                    .with(this)
                    .setTitle("Choose color")
                    .initialColor(mPaint.color)
                    .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                    .density(12)
                    .setPositiveButton("ok") { _, selectedColor, _ -> mPaint.color = selectedColor }
                    .build()
                    .show()
        }

        buttonClear.setOnClickListener {
            mDrawingView.reset()
        }
    }

    private fun saveDrawing() {
        val outFile = FileOutputStream(File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "signature.svg"))
        mDrawingView.mSvg.writeXml(outFile)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_CODE_SAVE_PERMISSION -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    saveDrawing()
                } else {
                    toast("Could not save file without permission ${Manifest.permission.WRITE_EXTERNAL_STORAGE}")
                }
            }
        }
    }

    inner class DrawingView(context: Context) : View(context) {

        private var mBitmap: Bitmap? = null
        private var mCanvas: Canvas? = null
        private val mPath: Path = Path()
        internal val mSvg: Svg by lazy {
            val svg = Svg(measuredWidth, measuredHeight)
            svg.addText(textViewSignature.x,
                    textViewSignature.y,
                    textViewSignature.text.toString(),
                    textViewSignature.textSize.toInt(),
                    fillColor = textViewSignature.currentTextColor)
            svg
        }
        private val mBitmapPaint: Paint = Paint(Paint.DITHER_FLAG)

        private var mX: Float = 0f
        private var mY: Float = 0f

        override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
            super.onSizeChanged(w, h, oldw, oldh)

            mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            mCanvas = Canvas(mBitmap)
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)

            canvas.drawBitmap(mBitmap, 0f, 0f, mBitmapPaint)
            canvas.drawPath(mPath, mPaint)
        }

        private fun touchStart(x: Float, y: Float) {
            mPath.reset()
            mSvg.startPath(mPaint.color, mPaint.strokeWidth)
            mPath.moveTo(x, y)
            mSvg.moveTo(x, y)
            mX = x
            mY = y
        }

        private fun touchMove(x: Float, y: Float) {
            val dx = Math.abs(x - mX)
            val dy = Math.abs(y - mY)
            if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
                mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2)
                mSvg.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2)
                mX = x
                mY = y
            }
        }

        private fun touchUp() {
            mPath.lineTo(mX, mY)
            mSvg.lineTo(mX, mY)
            // commit the path to our offscreen
            mCanvas?.drawPath(mPath, mPaint)
            // kill this so we don't double draw
            mPath.reset()
        }

        internal fun reset() {
            mPath.reset()
            mSvg.reset()
            mCanvas?.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
            invalidate()
        }

        internal fun undoLastPath() {
            mSvg.undoLastPath()
            mCanvas?.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
            mSvg.getAndroidPathsAndPaints().forEach {
                mCanvas?.drawPath(it.first, it.second)
            }
            invalidate()
        }

        @SuppressLint("ClickableViewAccessibility")
        override fun onTouchEvent(event: MotionEvent): Boolean {
            val x = event.x
            val y = event.y

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    touchStart(x, y)
                }
                MotionEvent.ACTION_MOVE -> {
                    touchMove(x, y)
                    invalidate()
                }
                MotionEvent.ACTION_UP -> {
                    touchUp()
                    invalidate()
                }
            }
            return true
        }
    }
}
