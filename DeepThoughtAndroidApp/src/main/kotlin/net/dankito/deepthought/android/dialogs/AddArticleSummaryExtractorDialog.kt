package net.dankito.deepthought.android.dialogs

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ListView
import android.widget.TextView
import kotlinx.android.synthetic.main.dialog_add_article_summary_extractor.view.*
import net.dankito.data_access.network.webclient.extractor.AsyncResult
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.activities.ArticleSummaryActivity
import net.dankito.deepthought.android.adapter.FoundFeedAddressesAdapter
import net.dankito.deepthought.android.di.AppComponent
import net.dankito.deepthought.news.summary.config.ArticleSummaryExtractorConfigManager
import net.dankito.feedaddressextractor.FeedAddress
import net.dankito.feedaddressextractor.FeedAddressExtractor
import net.dankito.newsreader.feed.IFeedReader
import net.dankito.newsreader.model.FeedArticleSummary
import net.dankito.serializer.ISerializer
import java.net.URI
import javax.inject.Inject


class AddArticleSummaryExtractorDialog : DialogFragment() {

    companion object {
        const val TAG = "ADD_ARTICLE_SUMMARY_EXTRACTOR_DIALOG"
    }


    @Inject
    protected lateinit var extractorsConfigManager: ArticleSummaryExtractorConfigManager

    @Inject
    protected lateinit var feedReader: IFeedReader

    @Inject
    protected lateinit var feedAddressExtractor:FeedAddressExtractor

    @Inject
    protected lateinit var serializer: ISerializer

    private val feedAddressesAdapter = FoundFeedAddressesAdapter()

    private var lstFeedSearchResults: ListView? = null
    private var txtFeedSearchResultsLabel: TextView? = null


    init {
        AppComponent.component.inject(this)
    }


    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater?.inflate(R.layout.dialog_add_article_summary_extractor, container)

        view?.let { view ->
            view.btnCheckFeedOrWebsiteUrl?.setOnClickListener { checkFeedOrWebsiteUrl(view.edtxtFeedOrWebsiteUrl.text.toString()) }

            txtFeedSearchResultsLabel = view.txtFeedSearchResultsLabel

            this.lstFeedSearchResults = view.lstFeedSearchResults
            view.lstFeedSearchResults.adapter = feedAddressesAdapter
            view.lstFeedSearchResults.setOnItemClickListener { _, _, position, _ -> foundFeedAddressSelected(position) }

            view.edtxtFeedOrWebsiteUrl.setOnFocusChangeListener { _, hasFocus ->
                if(hasFocus) {
                    dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
                }
            }
        }

        return view
    }

    private fun foundFeedAddressSelected(position: Int) {
        val feedAddress = feedAddressesAdapter.getItem(position)

        feedReader.readFeedAsync(feedAddress.url) {
            it.result?.let { feedAdded(feedAddress.url, it) }
            it.error?.let { showError(feedAddress.url, it) }
        }
    }

    private fun checkFeedOrWebsiteUrl(enteredFeedOrWebsiteUrl: String) {
        val feedOrWebsiteUrl = sanitizeUrl(enteredFeedOrWebsiteUrl)

        feedReader.readFeedAsync(feedOrWebsiteUrl) {
            if(it.result != null) {
                feedAdded(feedOrWebsiteUrl, it.result as FeedArticleSummary)
            }
            else {
                feedAddressExtractor.extractFeedAddressesAsync(feedOrWebsiteUrl) { asyncResult ->
                    handleExtractFeedAddressesResult(feedOrWebsiteUrl, asyncResult)
                }
            }
        }
    }

    private fun sanitizeUrl(enteredFeedOrWebsiteUrl: String): String {
        var feedOrWebsiteUrl = enteredFeedOrWebsiteUrl

        if(feedOrWebsiteUrl.startsWith("http") == false) {
            if(feedOrWebsiteUrl.startsWith("www.") == false && feedOrWebsiteUrl.startsWith("/") == false) {
                feedOrWebsiteUrl = "www." + feedOrWebsiteUrl
            }

            val slashesToAdd = if(feedOrWebsiteUrl.startsWith("//")) ""
            else if(feedOrWebsiteUrl.startsWith("/")) "/"
            else "//"

            feedOrWebsiteUrl = "http:" + slashesToAdd + feedOrWebsiteUrl // TODO: what about https variant?
        }

        mayShowEasterEgg(feedOrWebsiteUrl)

        return feedOrWebsiteUrl
    }

    private fun mayShowEasterEgg(enteredFeedOrWebsiteUrl: String) {
        try {
            val url = URI(enteredFeedOrWebsiteUrl)
            if(url.host?.toLowerCase()?.contains("bild.de") ?: false) {
                AlertDialog.Builder(activity).setMessage(R.string.bild_easter_egg)
                        .setNegativeButton(android.R.string.ok, { dialog, which -> throw Exception("Du hast bild.de eingegeben") })
                        .create().show()
            }
        } catch(ignored: Exception) { }
    }

    private fun handleExtractFeedAddressesResult(feedOrWebsiteUrl: String, asyncResult: AsyncResult<List<FeedAddress>>) {
        if (asyncResult.result != null) {
            val feedAddresses = asyncResult.result as List<FeedAddress>
            if (feedAddresses.size == 0) {
                showNoFeedAddressesFoundError(feedOrWebsiteUrl)
            } else {
                showFoundFeedAddresses(feedAddresses)
            }
        } else {
            asyncResult.error?.let { showError(feedOrWebsiteUrl, it) }
        }
    }

    private fun feedAdded(feedUrl: String, summary: FeedArticleSummary) {
        activity.runOnUiThread {
            val askExtractorNameDialog = AskExtractorNameDialog()

            askExtractorNameDialog.askForName(activity, summary.title ?: "", false) { didSelectName, selectedName ->
                val selectedExtractorName = if(didSelectName) selectedName ?: "" else summary.title ?: ""

                feedAdded(feedUrl, summary, selectedExtractorName)
            }
        }
    }

    private fun feedAdded(feedUrl: String, summary: FeedArticleSummary, selectedExtractorName: String) {
        summary.title = selectedExtractorName

        extractorsConfigManager.addFeed(feedUrl, summary) {
            activity.runOnUiThread {
                showArticleSummaryActivity(feedUrl, summary)

                dismiss()
            }
        }
    }

    private fun showArticleSummaryActivity(feedUrl: String, summary: FeedArticleSummary) {
        val intent = Intent(activity, ArticleSummaryActivity::class.java)

        intent.putExtra(ArticleSummaryActivity.EXTRACTOR_URL_INTENT_EXTRA_NAME, feedUrl)
        intent.putExtra(ArticleSummaryActivity.LAST_LOADED_SUMMARY_INTENT_EXTRA_NAME, serializer.serializeObject(summary))

        startActivity(intent)
    }

    private fun showFoundFeedAddresses(result: List<FeedAddress>) {
        activity.runOnUiThread {
            feedAddressesAdapter.setItems(result)

            txtFeedSearchResultsLabel?.visibility = VISIBLE
            lstFeedSearchResults?.visibility = VISIBLE
        }
    }

    private fun showNoFeedAddressesFoundError(feedOrWebsiteUrl: String) {
        showErrorThreadSafe(getString(R.string.error_no_rss_or_atom_feed_found_for_url, feedOrWebsiteUrl))
    }

    private fun showError(feedOrWebsiteUrl: String, error: Exception) {
        showErrorThreadSafe(getString(R.string.error_cannot_read_feed_or_extract_feed_addresses_from_url, feedOrWebsiteUrl, error.localizedMessage))
    }

    private fun showErrorThreadSafe(error: String) {
        activity.runOnUiThread { showError(error) }
    }

    private fun showError(error: String) {
        var builder = AlertDialog.Builder(activity)

        builder.setMessage(error)

        builder.setNegativeButton(android.R.string.ok, null)

        builder.create().show()
    }

}