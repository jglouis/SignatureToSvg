package xyz.hexode.signaturetosvg

import android.content.Context
import android.graphics.*
import android.os.Bundle
import android.os.Environment
import android.support.v7.app.AppCompatActivity
import android.view.MotionEvent
import android.view.View
import kotlinx.android.synthetic.main.activity_drawing.*
import java.io.File
import java.io.FileOutputStream

const val TOUCH_TOLERANCE = 4f

/**
 * Created by JGLouis on 08/03/2018.
 *
 * Drawing activity
 */
class DrawingActivity : AppCompatActivity() {

    private var mPaint: Paint? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dv = DrawingView(this)
        setContentView(R.layout.activity_drawing)
        drawingLayout.addView(dv)
        mPaint = Paint()
        mPaint!!.isAntiAlias = true
        mPaint!!.isDither = true
        mPaint!!.color = Color.GREEN
        mPaint!!.style = Paint.Style.STROKE
        mPaint!!.strokeJoin = Paint.Join.ROUND
        mPaint!!.strokeCap = Paint.Cap.ROUND
        mPaint!!.strokeWidth = 12f

        buttonSave.setOnClickListener {
            val outFile = FileOutputStream(File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "signature.svg"))
            dv.mSvg.writeXml(outFile)
        }
    }

    inner class DrawingView(context: Context) : View(context) {

        private var mBitmap: Bitmap? = null
        private var mCanvas: Canvas? = null
        private val mPath: Path = Path()
        internal var mSvg: Svg = Svg(0, 0)
        private val mBitmapPaint: Paint = Paint(Paint.DITHER_FLAG)
        private val circlePaint: Paint = Paint()

        private var mX: Float = 0f
        private var mY: Float = 0f

        init {
            circlePaint.isAntiAlias = true
            circlePaint.color = Color.BLUE
            circlePaint.style = Paint.Style.STROKE
            circlePaint.strokeJoin = Paint.Join.MITER
            circlePaint.strokeWidth = 4f
        }

        override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
            super.onSizeChanged(w, h, oldw, oldh)

            mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            mCanvas = Canvas(mBitmap!!)

            mSvg = Svg(measuredWidth, measuredHeight)
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)

            canvas.drawBitmap(mBitmap!!, 0f, 0f, mBitmapPaint)
            canvas.drawPath(mPath, mPaint!!)
        }

        private fun touchStart(x: Float, y: Float) {
            mPath.reset()
            mSvg.startPath()
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
            mCanvas!!.drawPath(mPath, mPaint!!)
            // kill this so we don't double draw
            mPath.reset()
        }

        override fun onTouchEvent(event: MotionEvent): Boolean {
            val x = event.x
            val y = event.y

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    touchStart(x, y)
                    invalidate()
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
