package net.dankito.deepthought.android.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.list_item_found_feed_address.view.*
import net.dankito.deepthought.android.R
import net.dankito.feedaddressextractor.FeedAddress


class FoundFeedAddressesAdapter : ListAdapter<FeedAddress>() {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val feedAddress = getItem(position)

        val view = convertView ?: LayoutInflater.from(parent?.context).inflate(R.layout.list_item_found_feed_address, parent, false)

        view.txtFeedName.text = feedAddress.title
        view.txtFeedUrl.text = feedAddress.url

        return view
    }

}