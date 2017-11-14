package net.dankito.deepthought.android.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import kotlinx.android.synthetic.main.view_swipe_button.view.*
import net.dankito.deepthought.android.R


class SwipeButton : RelativeLayout {

    constructor(context: Context) : super(context) { initialize(context, null) }
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) { initialize(context, attrs) }
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) { initialize(context, attrs) }
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) { initialize(context, attrs) }


    private fun initialize(context: Context, attrs: AttributeSet?) {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val rootView = inflater.inflate(R.layout.view_swipe_button, this)

        rootView.imgIcon.setOnClickListener { rootView.callOnClick() } // pass click on to RelativeLayout

        applyAttributes(context, attrs, rootView)
    }

    private fun applyAttributes(context: Context, attrs: AttributeSet?, rootView: View) {
        context.theme.obtainStyledAttributes(attrs, R.styleable.SwipeButton, 0, 0)?.let { typedArray ->
            try {
                val iconResourceId = typedArray.getResourceId(R.styleable.SwipeButton_icon, 0)

                rootView.imgIcon.setImageResource(iconResourceId)
            } finally {
                typedArray.recycle()
            }
        }
    }

}