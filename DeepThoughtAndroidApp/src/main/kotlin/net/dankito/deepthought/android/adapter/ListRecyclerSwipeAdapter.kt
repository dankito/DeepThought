package net.dankito.deepthought.android.adapter

import android.support.v4.view.GestureDetectorCompat
import android.support.v7.widget.RecyclerView
import android.view.GestureDetector
import android.view.MotionEvent
import com.daimajia.swipe.SwipeLayout
import com.daimajia.swipe.adapters.RecyclerSwipeAdapter


abstract class ListRecyclerSwipeAdapter<T, THolder : RecyclerView.ViewHolder>(list: List<T> = ArrayList<T>()) : RecyclerSwipeAdapter<THolder>() {

    var itemClickListener: ((item: T) -> Unit)? = null

    var itemLongClickListener: ((item: T) -> Unit)? = null


    var items: List<T> = list
        set(value) {
            field = value

            notifyDataSetChanged()
        }

    override fun getItemCount() = items.size

    override fun getItemId(position: Int) = position.toLong()

    fun getItem(position: Int) = items[position]


    fun addItems(items: Collection<T>) {
        val newList = ArrayList(this.items)
        newList.addAll(items)

        this.items = newList
    }


    override fun onBindViewHolder(viewHolder: THolder, position: Int) {
        val item = getItem(position)

        if(item == null) {
            bindViewForNullValue(viewHolder)
        }
        else {
            bindItemToView(viewHolder, item)

            (viewHolder.itemView as? SwipeLayout)?.isSwipeEnabled = true
            setupSwipeView(viewHolder, item)

            itemBound(viewHolder, item, position)
        }
    }

    protected open fun bindViewForNullValue(viewHolder: THolder) {
        (viewHolder.itemView as? SwipeLayout)?.isSwipeEnabled = false
    }

    abstract fun bindItemToView(viewHolder: THolder, item: T)

    abstract fun setupSwipeView(viewHolder: THolder, item: T)

    protected open fun itemBound(viewHolder: RecyclerView.ViewHolder, item: T, position: Int) {
        viewHolder.itemView.isLongClickable = true // otherwise context menu won't trigger / pop up

        if(itemClickListener != null || itemLongClickListener != null) { // use a GestureDetector as item clickListener also triggers when swiping or long pressing an item
            val gestureDetector = GestureDetectorCompat(viewHolder.itemView.context, TapGestureDetector<T>(item, { itemClicked(viewHolder, item, position) },
                    { itemLongClicked(viewHolder, item, position) }))

            viewHolder.itemView.setOnTouchListener { _, event -> gestureDetector.onTouchEvent(event) }
        }
        else {
            viewHolder.itemView.setOnTouchListener(null)
        }
    }


    protected open fun itemClicked(viewHolder: RecyclerView.ViewHolder, item: T, position: Int): Boolean {
        itemClickListener?.let {
            it.invoke(item)
            return true
        }

        return false
    }

    protected open fun itemLongClicked(viewHolder: RecyclerView.ViewHolder, item: T, position: Int) {
        itemLongClickListener?.invoke(item)
    }


    protected fun closeSwipeView(viewHolder: RecyclerView.ViewHolder) {
        (viewHolder.itemView as? SwipeLayout)?.close()
    }


    class TapGestureDetector<T>(private val item: T, private val itemClickListener: (item: T) -> Boolean, private val itemLongClickListener: (item: T) -> Unit) : GestureDetector.OnGestureListener {

        override fun onSingleTapUp(e: MotionEvent?): Boolean {
            return itemClickListener.invoke(item)
        }

        override fun onLongPress(e: MotionEvent?) {
            itemLongClickListener.invoke(item)
        }

        override fun onDown(e: MotionEvent?): Boolean {
            return false
        }

        override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {
            return false
        }

        override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
            return false
        }

        override fun onShowPress(e: MotionEvent?) { }
    }

}