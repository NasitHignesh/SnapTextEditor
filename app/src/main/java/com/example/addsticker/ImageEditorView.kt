package com.example.addsticker


import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class BlurDrawingView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    private var path = Path()
    private var paint = Paint().apply {
        color = Color.TRANSPARENT
        style = Paint.Style.STROKE
        strokeWidth = 20f
        maskFilter = BlurMaskFilter(25f, BlurMaskFilter.Blur.NORMAL)
    }
    private var bitmap: Bitmap? = null
    private var canvasBitmap: Canvas? = null
    private var lastX = 0f
    private var lastY = 0f

    init {
        setLayerType(LAYER_TYPE_SOFTWARE, null) // Enable software layer for blur effect
        paint.isAntiAlias = true
        paint.isDither = true
        paint.strokeJoin = Paint.Join.ROUND
        paint.strokeCap = Paint.Cap.ROUND
    }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        canvasBitmap = Canvas(bitmap!!)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        bitmap?.let { bitmap ->
            canvas.drawBitmap(bitmap, null, RectF(0f, 0f, width.toFloat(), height.toFloat()), null)
            canvas.drawPath(path, paint)
        }
    }


    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                path.moveTo(x, y)
                lastX = x
                lastY = y
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = Math.abs(x - lastX)
                val dy = Math.abs(y - lastY)
                if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
                    path.quadTo(lastX, lastY, (x + lastX) / 2, (y + lastY) / 2)
                    lastX = x
                    lastY = y
                    drawPath()
                }
            }
            MotionEvent.ACTION_UP -> {
                path.lineTo(lastX, lastY)
                drawPath()
            }
        }

        return super.onTouchEvent(event)
    }

    private fun drawPath() {
        canvasBitmap?.drawPath(path, paint)
        invalidate()
        path.reset()
    }

    fun clearCanvas() {
        canvasBitmap?.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        invalidate()
    }

    companion object {
        private const val TOUCH_TOLERANCE = 4f
    }
}
