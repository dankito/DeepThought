package net.dankito.deepthought.android.adapter

import android.widget.BaseAdapter


abstract class ListAdapter<T>() : BaseAdapter() {

    protected var list : List<T> = ArrayList<T>()


    constructor(list: List<T>) : this() {
        this.list = list
    }


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