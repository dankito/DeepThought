package net.dankito.deepthought.android.views

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet


class MaxHeightRecyclerView : RecyclerView {

    companion object {
        const val DisableMaxHeight = -1
    }


    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)


    var maxHeightInPixel = DisableMaxHeight


    init {
        setMaxHeightInDp(175)
    }


    fun setMaxHeightInDp(maxHeightInDp: Int) {
        val density = resources.displayMetrics.density
        maxHeightInPixel = (maxHeightInDp * density).toInt()
    }

    fun disableMaxHeight() {
        maxHeightInPixel = DisableMaxHeight
    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if(maxHeightInPixel >= 0) {
            val adjustedHeightMeasureSpec = MeasureSpec.makeMeasureSpec(maxHeightInPixel, MeasureSpec.AT_MOST)

            super.onMeasure(widthMeasureSpec, adjustedHeightMeasureSpec)
        }
        else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }
    }

}