package net.dankito.deepthought.android.adapter

import android.support.v7.widget.RecyclerView
import com.daimajia.swipe.adapters.RecyclerSwipeAdapter


abstract class ListRecyclerSwipeAdapter<T, THolder : RecyclerView.ViewHolder>(protected var list: List<T> = ArrayList<T>()) : RecyclerSwipeAdapter<THolder>() {

    override fun getItemCount() = list.size

    override fun getItemId(position: Int) = position.toLong()

    fun getItem(position: Int) = list[position]


    fun setItems(items: List<T>) {
        this.list = items

        notifyDataSetChanged()
    }

    fun addItems(items: Collection<T>) {
        val newList = ArrayList(this.list)
        newList.addAll(items)

        setItems(newList)
    }

}