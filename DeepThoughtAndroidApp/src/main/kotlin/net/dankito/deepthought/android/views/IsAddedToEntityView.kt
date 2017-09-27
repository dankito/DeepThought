package net.dankito.deepthought.android.views

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import kotlinx.android.synthetic.main.view_is_added_to_entity.view.*
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.extensions.setTintListToEntityIsSelectedColor


class IsAddedToEntityView : RelativeLayout {

    private lateinit var vwIsAddedToEntityBorder: View

    private lateinit var imgIsAddedToEntity: ImageView

    private lateinit var txtvwEntityName: TextView


    constructor(context: Context) : super(context) {
        setupUI(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        setupUI(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        setupUI(context)
    }



    private fun setupUI(context: Context) {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val rootView = inflater.inflate(R.layout.view_is_added_to_entity, this)

        vwIsAddedToEntityBorder = rootView.vwIsAddedToEntityBorder
        imgIsAddedToEntity = rootView.imgIsAddedToEntity
        txtvwEntityName = rootView.txtvwEntityName

        imgIsAddedToEntity.setTintListToEntityIsSelectedColor()
    }


    fun showState(entityName: String, isAddedToEntity: Boolean) {
        txtvwEntityName.text = entityName

        if(isAddedToEntity) {
            imgIsAddedToEntity.setImageResource(R.drawable.ic_checkmark)
            txtvwEntityName.setTypeface(null, Typeface.BOLD)
            vwIsAddedToEntityBorder.visibility = View.VISIBLE
        }
        else {
            imgIsAddedToEntity.setImageResource(R.drawable.ic_add)
            txtvwEntityName.setTypeface(null, Typeface.NORMAL)
            vwIsAddedToEntityBorder.visibility = View.INVISIBLE
        }

        this.isActivated = isAddedToEntity // sets icon's tint and textview's text color
    }

}