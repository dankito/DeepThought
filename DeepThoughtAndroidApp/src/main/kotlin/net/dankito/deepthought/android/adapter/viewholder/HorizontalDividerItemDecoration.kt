package net.dankito.deepthought.android.adapter.viewholder

import android.content.Context
import android.graphics.Canvas
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.RecyclerView


class HorizontalDividerItemDecoration(context: Context) : DividerItemDecoration(context, DividerItemDecoration.VERTICAL) {

    override fun onDraw(c: Canvas?, parent: RecyclerView?, state: RecyclerView.State?) {
        super.onDraw(c, parent, state)
    }
}