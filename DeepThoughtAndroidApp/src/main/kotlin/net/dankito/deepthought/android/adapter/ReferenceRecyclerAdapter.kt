package net.dankito.deepthought.android.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.adapter.viewholder.ReferenceViewHolder
import net.dankito.deepthought.extensions.preview
import net.dankito.deepthought.model.Reference
import net.dankito.deepthought.ui.presenter.ReferencesListPresenter


class ReferenceRecyclerAdapter(private val presenter: ReferencesListPresenter): ListRecyclerSwipeAdapter<Reference, ReferenceViewHolder>() {

    override fun getSwipeLayoutResourceId(position: Int) = 0


    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ReferenceViewHolder {
        val itemView = LayoutInflater.from(parent?.context).inflate(R.layout.list_item_reference, parent, false)

        return ReferenceViewHolder(itemView)
    }

    override fun onBindViewHolder(viewHolder: ReferenceViewHolder, position: Int) {
        val reference = getItem(position)

        if(reference == null) {
            bindViewForNullValue(viewHolder)
        }
        else {
            bindTagToView(viewHolder, reference)
            itemBound(viewHolder, reference)
        }
    }

    private fun bindViewForNullValue(viewHolder: ReferenceViewHolder) {
        viewHolder.txtReferencePreview.visibility = View.INVISIBLE
    }

    private fun bindTagToView(viewHolder: ReferenceViewHolder, reference: Reference) {
        viewHolder.txtReferencePreview.visibility = View.VISIBLE

        viewHolder.txtReferencePreview.text = reference.preview

    }

}