package net.dankito.deepthought.android.views

import android.content.Context
import android.util.AttributeSet
import android.widget.TextView
import net.dankito.deepthought.android.R
import net.dankito.utils.android.extensions.setCustomTypeface


class CustomFontTextView : TextView {


    constructor(context: Context) : super(context) { initialize(context, null) }
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) { initialize(context, attrs) }
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) { initialize(context, attrs) }
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) { initialize(context, attrs) }


    private fun initialize(context: Context, attrs: AttributeSet?) {
        context.theme.obtainStyledAttributes(attrs, R.styleable.CustomFontTextView, 0, 0)?.let { typedArray ->
            try {
                typedArray.getString(R.styleable.CustomFontTextView_customFont)?.let { customFont ->
                    setCustomTypeface(context, customFont)
                }
            } finally {
                typedArray.recycle()
            }
        }
    }

}