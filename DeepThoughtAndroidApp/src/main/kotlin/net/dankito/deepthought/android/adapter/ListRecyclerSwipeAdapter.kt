package net.dankito.deepthought.android.adapter

import android.support.v4.view.GestureDetectorCompat
import android.support.v7.widget.RecyclerView
import android.view.GestureDetector
import android.view.MotionEvent
import com.daimajia.swipe.adapters.RecyclerSwipeAdapter


abstract class ListRecyclerSwipeAdapter<T, THolder : RecyclerView.ViewHolder>(list: List<T> = ArrayList<T>()) : RecyclerSwipeAdapter<THolder>() {

    var itemClickListener: ((item: T) -> Unit)? = null


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


    protected fun itemBound(viewHolder: RecyclerView.ViewHolder, item: T) {
        viewHolder.itemView.isLongClickable = true // otherwise context menu won't trigger / pop up

        itemClickListener?.let { // use a GestureDetector as item clickListener also triggers when swiping or long pressing an item
            val gestureDetector = GestureDetectorCompat(viewHolder.itemView.context, SingleTapUpGestureDetector<T>(itemClickListener, item))

            viewHolder.itemView.setOnTouchListener { _, event -> gestureDetector.onTouchEvent(event) }
        }
    }


    class SingleTapUpGestureDetector<T>(private val itemClickListener: ((item: T) -> Unit)?, private val item: T) : GestureDetector.OnGestureListener {
        override fun onShowPress(e: MotionEvent?) { }

        override fun onSingleTapUp(e: MotionEvent?): Boolean {
            itemClickListener?.let {
                it.invoke(item)
                return true
            }

            return false
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

        override fun onLongPress(e: MotionEvent?) { }
    }

}