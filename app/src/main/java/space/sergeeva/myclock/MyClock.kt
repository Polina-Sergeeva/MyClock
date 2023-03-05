package space.sergeeva.myclock

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.graphics.withSave
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import space.sergeeva.myclock.R.*
import java.util.*

class MyClock @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attributeSet, defStyleAttr) {

    private val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val bigScalePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val smallScalePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val hourPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val minutePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val secondPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val circleStroke = 10f
    private val bigScaleStroke = 7f
    private val bigScaleLen = 40f
    private val smallScaleStroke = 4f
    private val smallScaleLen = 20f
    private val hourStroke = 25f
    private val minuteStroke = 15f
    private val secondStroke = 8f
    private val textSize = 50f

    private var circleColor = ContextCompat.getColor(getContext(), color.clock_ring_color)
    private var scaleColor = ContextCompat.getColor(getContext(), color.clock_big_scale_color)
    private var minuteHandColor =
        ContextCompat.getColor(getContext(), color.clock_minute_hand_color)
    private var secondHandColor =
        ContextCompat.getColor(getContext(), color.clock_second_hand_color)
    private var hourHandColor = ContextCompat.getColor(getContext(), color.clock_hour_hand_color)
    private var numberColor = ContextCompat.getColor(getContext(), color.clock_numbers_color)

    private var phoneWidth = 0
    private var viewHeight = 0
    private var radius = 0f
    private var marginFromDisplay = 100
    private var centerX = 0f
    private var centerY = 0f

    private val hourNumbers = Array(12) { i -> i + 1 }


    init {
        val attrsArray = context.obtainStyledAttributes(attributeSet, styleable.MyClock)
        circlePaint.color =
            attrsArray.getColor(styleable.MyClock_clock_ring_color, circleColor)
        bigScalePaint.color =
            attrsArray.getColor(styleable.MyClock_clock_big_scale_color, scaleColor)
        smallScalePaint.color =
            attrsArray.getColor(styleable.MyClock_clock_small_scale_color, scaleColor)
        hourPaint.color =
            attrsArray.getColor(styleable.MyClock_clock_hour_color, hourHandColor)
        minutePaint.color =
            attrsArray.getColor(styleable.MyClock_clock_minute_color, minuteHandColor)
        secondPaint.color =
            attrsArray.getColor(styleable.MyClock_clock_second_color, secondHandColor)
        textPaint.color = attrsArray.getColor(styleable.MyClock_clock_text_color, numberColor)

        attrsArray.recycle()

        initPaints()

        phoneWidth = context.resources.displayMetrics.widthPixels

        radius = phoneWidth / 2f - marginFromDisplay

        centerX = phoneWidth / 2f
        centerY = radius + marginFromDisplay

        viewHeight = (centerY * 2).toInt()

    }

    private fun initPaints() {
        circlePaint.apply {
            style = Paint.Style.STROKE
            strokeWidth = circleStroke
        }

        bigScalePaint.apply {
            style = Paint.Style.STROKE
            strokeWidth = bigScaleStroke
        }

        smallScalePaint.apply {
            style = Paint.Style.STROKE
            strokeWidth = smallScaleStroke
        }

        hourPaint.apply {
            style = Paint.Style.STROKE
            strokeWidth = hourStroke
            strokeCap = Paint.Cap.ROUND
        }

        minutePaint.apply {
            style = Paint.Style.STROKE
            strokeWidth = minuteStroke
            strokeCap = Paint.Cap.ROUND
        }

        secondPaint.apply {
            style = Paint.Style.STROKE
            strokeWidth = secondStroke
            strokeCap = Paint.Cap.ROUND
        }

        textPaint.textSize = textSize

    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)


        getCurrentTime()

        drawCircle(canvas)
        drawText(canvas)
        drawScale(canvas)
        drawHour(canvas)
        drawMinute(canvas)
        drawSecond(canvas)
        postInvalidateDelayed(1000)
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(phoneWidth, viewHeight)
    }

    private fun drawCircle(canvas: Canvas?) {
        canvas?.drawCircle(centerX, centerY, radius, circlePaint)
    }

    private fun drawText(canvas: Canvas?) {
        canvas?.withSave {
            rotate(360 / 12f, centerX, centerY)
            for (i in 0 until 60) {
                if (i % 5 == 0) {
                    val textWidth = textPaint.measureText(hourNumbers[i / 5].toString())
                    drawText(
                        hourNumbers[i / 5].toString(),
                        centerX - textWidth / 2,
                        marginFromDisplay + bigScaleLen + textSize,
                        textPaint
                    )
                }
                rotate(HAND_MOVE_DEGREE, centerX, centerY)
            }
        }
    }

    private fun drawScale(canvas: Canvas?) {
        canvas?.withSave {
            for (i in 0 until 60) {
                // draw 12 scales
                if (i % 5 == 0) {
                    // draw only 12, 3, 6, 8 scales
                    if (i % 3 == 0) {
                        drawLine(
                            centerX,
                            marginFromDisplay - bigScaleStroke / 2 + circleStroke,
                            centerX,
                            marginFromDisplay + bigScaleLen,
                            secondPaint
                        )
                        // draw another big scales
                    } else {
                        drawLine(
                            centerX,
                            marginFromDisplay - bigScaleStroke / 2 + circleStroke,
                            centerX,
                            marginFromDisplay + bigScaleLen,
                            bigScalePaint
                        )
                    }
                } else {
                    drawLine(
                        centerX,
                        marginFromDisplay - smallScaleStroke / 2 + circleStroke,
                        centerX,
                        marginFromDisplay + smallScaleLen,
                        smallScalePaint
                    )
                }
                rotate(HAND_MOVE_DEGREE, centerY, centerY)
            }
        }
    }

    private fun drawHour(canvas: Canvas?) {
        canvas?.withSave {
            rotate(hourDegrees, centerX, centerY)
            drawLine(centerX, centerY, centerX, radius - 100, hourPaint)
        }

    }

    private fun drawMinute(canvas: Canvas?) {
        canvas?.withSave {
            rotate(minuteDegrees, centerX, centerY)
            drawLine(centerX, centerY, centerX, radius - 180, minutePaint)
        }

    }

    private fun drawSecond(canvas: Canvas?) {
        canvas?.withSave {
            rotate(secondDegrees, centerX, centerY)
            drawLine(centerX, centerY, centerX, radius - 200, secondPaint)
        }

    }

    private fun getCurrentTime() {
        CoroutineScope(Dispatchers.IO).launch {
            val calendar = GregorianCalendar.getInstance()
            var hour = calendar.get(Calendar.HOUR)
            if (hour >= 12) {
                hour -= 12
            }
            val minute = calendar.get(Calendar.MINUTE)
            val second = calendar.get(Calendar.SECOND)

            secondDegrees = second * HAND_MOVE_DEGREE
            minuteDegrees = minute * HAND_MOVE_DEGREE
            hourDegrees = hour * HOUR_MOVE_DEGREE
        }
    }

    companion object {
        private const val HAND_MOVE_DEGREE = 6f
        private const val HOUR_MOVE_DEGREE = 30f
        var hourDegrees = 0f
        var minuteDegrees = 0f
        var secondDegrees = 0f
    }
}