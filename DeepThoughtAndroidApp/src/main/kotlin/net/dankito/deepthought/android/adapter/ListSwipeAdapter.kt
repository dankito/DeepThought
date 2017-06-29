package net.dankito.deepthought.android.adapter

import com.daimajia.swipe.adapters.BaseSwipeAdapter


abstract class ListSwipeAdapter<T>(protected var list: List<T> = ArrayList<T>()) : BaseSwipeAdapter() {


    override fun getCount() = list.size

    override fun getItem(position: Int) = list[position]

    override fun getItemId(position: Int) = position.toLong()


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