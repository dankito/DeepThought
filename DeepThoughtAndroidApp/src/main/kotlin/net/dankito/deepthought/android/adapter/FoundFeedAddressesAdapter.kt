package net.dankito.deepthought.android.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.list_item_found_feed_address.view.*
import net.dankito.deepthought.android.R
import net.dankito.feedaddressextractor.FeedAddress
import net.dankito.feedaddressextractor.FeedType


class FoundFeedAddressesAdapter : ListAdapter<FeedAddress>() {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val feedAddress = getItem(position)

        val view = convertView ?: LayoutInflater.from(parent?.context).inflate(R.layout.list_item_found_feed_address, parent, false)

        var feedName = feedAddress.title
        if(feedAddress.type == FeedType.Atom && count > 1) { // for Atom feeds add ' (preferred)' to title to give user a hint which feed to choose
            feedName += " " + view.context.getString(R.string.dialog_add_article_summary_extractor_preferred_feed_title_addition) // getString() removes the white space from dialog_add_article_summary_extractor_preferred_feed_title_addition
        }

        view.txtFeedName.text = feedName
        view.txtFeedUrl.text = feedAddress.url

        return view
    }

}