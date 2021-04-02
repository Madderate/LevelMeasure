package com.madderate.levelmeasure.page

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.core.content.res.ResourcesCompat
import com.madderate.levelmeasure.util.dp
import kotlin.concurrent.thread
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * 展示手机水平情况的 view
 *
 * @author      madderate
 * @date        4/2/21 9:37 PM
 */
class LevelIndicatorView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null,
    defStyleAttr: Int = 0, defStyleRes: Int = 0
) : SurfaceView(context, attrs, defStyleAttr, defStyleRes),
    SurfaceHolder.Callback,
    SensorEventListener {

    private val mSensorManager: SensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val mGravitySensor: Sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)


    // 气泡指示器坐标
    private var indicatorX: Float = 0f
    private var indicatorY: Float = 0f
    private val indicatorRadius: Float = 16f.dp
    private val maxIndicatorReachableRadius: Float
        get() = (borderRadius - borderWidth - indicatorRadius)

    // 一些背景会用到的值
    private var centerX: Float = 0f
    private var centerY: Float = 0f
    private var borderRadius: Float = 0f
    private val borderWidth: Float = 8f.dp

    // 背景色
    private val whitePaint: Paint = Paint().apply {
        color = ResourcesCompat.getColor(resources, android.R.color.white, context.theme)
        isAntiAlias = true
    }

    // 构成最外面的环
    private val defaultPaint = Paint().apply {
        color = ResourcesCompat.getColor(
            resources, android.R.color.darker_gray, context.theme
        )
        isAntiAlias = true
    }
    private val correctPaint = Paint().apply {
        color = ResourcesCompat.getColor(
            resources, android.R.color.holo_green_light, context.theme
        )
        isAntiAlias = true
    }

    private var mIsDrawing = false


    init {
        holder.addCallback(this)
        isFocusable = true
        keepScreenOn = true
        isFocusableInTouchMode = true
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        mIsDrawing = true
        mSensorManager.registerListener(this, mGravitySensor, SensorManager.SENSOR_DELAY_GAME)
        thread {
            while (mIsDrawing) {
                drawCanvas()
            }
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        mIsDrawing = false
        mSensorManager.unregisterListener(this, mGravitySensor)
    }

    private fun drawCanvas() {
        var canvas: Canvas? = null
        try {
            canvas = holder.lockCanvas()
            if (borderRadius == 0f) {
                borderRadius = (min(width, height) / 2f) - 20f.dp
            }
            if (centerX == 0f) {
                centerX = width / 2f
            }
            if (centerY == 0f) {
                centerY = height / 2f
            }
            canvas?.run {
                drawPaint(whitePaint)
                val lengthX = indicatorX - centerX
                val lengthY = indicatorY - centerY
                if (lengthX.pow(2) + lengthY.pow(2) <= MARGIN_OF_ERROR.pow(2)) {
                    drawCircle(centerX, centerY, borderRadius, correctPaint)
                    drawCircle(centerX, centerY, borderRadius - borderWidth, whitePaint)
                    drawCircle(indicatorX, indicatorY, indicatorRadius, correctPaint)
                    drawLine(
                        centerX - indicatorRadius, centerY,
                        centerX + indicatorRadius, centerY, defaultPaint
                    )
                    drawLine(
                        centerX, centerY - indicatorRadius,
                        centerX, centerY + indicatorRadius, defaultPaint
                    )
                } else {
                    drawCircle(centerX, centerY, borderRadius, defaultPaint)
                    drawCircle(centerX, centerY, borderRadius - borderWidth, whitePaint)
                    drawCircle(indicatorX, indicatorY, indicatorRadius, defaultPaint)
                    drawLine(
                        centerX - indicatorRadius, centerY,
                        centerX + indicatorRadius, centerY, defaultPaint
                    )
                    drawLine(
                        centerX, centerY - indicatorRadius,
                        centerX, centerY + indicatorRadius, defaultPaint
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            canvas?.let { holder.unlockCanvasAndPost(it) }
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.run {
            if (sensor.type == Sensor.TYPE_GRAVITY && values.size <= 3) {
                // 此时的 values 代表三轴的 gravity
                // values[0] 为 x 轴的重力分量（朝上为正，朝下为负）
                // values[1] 为 y 轴的重力分量
                // values[2] 为 z 轴的重力分量
                val gravityX = values[0]
                val gravityY = values[1]
                indicatorX = maxIndicatorReachableRadius * (gravityX / MAX_GRAVITY)
                indicatorY = maxIndicatorReachableRadius * (gravityY / MAX_GRAVITY)
                val predictRadius = sqrt(indicatorX.pow(2) + indicatorY.pow(2))
                if (predictRadius > maxIndicatorReachableRadius) {
                    indicatorX =
                        centerX + (maxIndicatorReachableRadius * indicatorX / predictRadius)
                    indicatorY =
                        centerY + (maxIndicatorReachableRadius * indicatorY / predictRadius)
                } else {
                    indicatorX += centerX
                    indicatorY += centerY
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}


    companion object {
        // 低于 7 m^2/s 就让它在边缘待着（
        private const val MAX_GRAVITY = 3f
        private const val MARGIN_OF_ERROR = 4f
    }
}