package net.dankito.deepthought.android.views

import android.app.Activity
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.github.clans.fab.FloatingActionButton
import net.dankito.deepthought.android.R


class PointToFloatingActionButtonArrow : View {

    companion object {
        private const val StrokeWidth = 6f // TODO: adjust stroke width to display as on 1920x1080 Moto G4 it just looks perfect whilst on 1280x720 Vernee Thor it is too thick
        private const val DashLength = 30f
        private val PaddingStart = 35f
        private val PaddingEnd = 50f
        private val LineHeadDistance = 30f
        private const val ArrowHeadLength = 30f
    }


    private val arrowPaint: Paint

    private var floatingActionButton: FloatingActionButton? = null


    init {
        arrowPaint = Paint()
        arrowPaint.style = Paint.Style.STROKE
        arrowPaint.isAntiAlias = true
        arrowPaint.strokeWidth = StrokeWidth
        arrowPaint.strokeCap = Paint.Cap.ROUND
        arrowPaint.pathEffect = DashPathEffect(floatArrayOf(DashLength, DashLength), 0f)
        arrowPaint.color = Color.parseColor("#999999")
    }


    constructor(context: Context?) : super(context)

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)


    override fun draw(canvas: Canvas) {
        super.draw(canvas)

        val arrowStartX = canvas.width / 2f
        val arrowStartY = 0f
        val arrowEndPosition = getArrowEndPosition(canvas)

        drawArrow(canvas, arrowStartX, arrowStartY, arrowEndPosition.x, arrowEndPosition.y)
    }

    private fun drawArrow(canvas: Canvas, arrowStartX: Float, arrowStartY: Float, arrowEndX: Float, arrowEndY: Float) {
        val arrowRect = RectF(arrowStartX, arrowStartY, arrowEndX, arrowEndY)

        // for drawing the arrow with drawArc() we have to create a rectangle of double the height and width which contains the start point at center left and end point at
        // center bottom position
        val arrowRectRotated = RectF(arrowStartX, arrowStartY - arrowRect.height() + PaddingStart, arrowEndX + arrowRect.width() - 2 * PaddingEnd, arrowEndY)

        canvas.drawArc(arrowRectRotated, 90f, 90f, false, arrowPaint)

        // draw arrow head
        val endX = arrowEndX - PaddingEnd + LineHeadDistance
        canvas.drawLine(endX, arrowEndY, endX - ArrowHeadLength, arrowEndY - ArrowHeadLength, arrowPaint)
        canvas.drawLine(endX, arrowEndY, endX - ArrowHeadLength, arrowEndY + ArrowHeadLength, arrowPaint)
    }

    private fun getArrowEndPosition(canvas: Canvas): PointF {
        var arrowEndX = canvas.width - 130f
        var arrowEndY = canvas.height - 55f

        getFloatingActionButton()?.let { floatingActionButton ->
            val fabLocation = IntArray(2)
            floatingActionButton.getLocationOnScreen(fabLocation)

            val ownLocation = IntArray(2)
            this.getLocationOnScreen(ownLocation)

            arrowEndX = fabLocation[0].toFloat()
            arrowEndY = fabLocation[1] - ownLocation[1] + (floatingActionButton.height / 2f)
        }

        return PointF(arrowEndX, arrowEndY)
    }

    private fun getFloatingActionButton(): FloatingActionButton? {
        if(floatingActionButton == null) {
            (context as? Activity)?.let { activity ->
                activity.findViewById(R.id.floatingActionMenu)?.let { fab ->
                    val mMenuButtonField = fab::class.java.getDeclaredField("mMenuButton")
                    mMenuButtonField.isAccessible = true

                    floatingActionButton = mMenuButtonField.get(fab) as FloatingActionButton
                }
            }
        }

        return floatingActionButton
    }

}