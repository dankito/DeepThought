package net.dankito.deepthought.android.adapter

import android.net.Uri
import android.os.Looper
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import kotlinx.android.synthetic.main.list_item_article_summary_extractor.view.*
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.di.AppComponent
import net.dankito.deepthought.news.summary.config.ArticleSummaryExtractorConfig
import net.dankito.utils.ImageCache
import java.io.File
import javax.inject.Inject


class ArticleSummaryExtractorsAdapter(private val activity: AppCompatActivity, extractors: List<ArticleSummaryExtractorConfig>)
    : ListAdapter<ArticleSummaryExtractorConfig>(extractors) {


    @Inject
    protected lateinit var imageCache: ImageCache


    init {
        AppComponent.component.inject(this)
    }


    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val extractorConfig = getItem(position)

        val view = convertView ?: LayoutInflater.from(parent?.context).inflate(R.layout.list_item_article_summary_extractor, parent, false)

        showExtractorIcon(view, extractorConfig)

        view.txtExtractorName.text = extractorConfig.name

        view.tag = extractorConfig

        return view
    }

    private fun showExtractorIcon(view: View, extractorConfig: ArticleSummaryExtractorConfig) {
        val imageView = view.imgPreviewImage

        imageView.tag = extractorConfig.iconUrl
        imageView.setImageBitmap(null)

        extractorConfig.iconUrl?.let { iconUrl ->
            imageCache.getCachedForRetrieveIconForUrlAsync(iconUrl) { result ->
                result.result?.let { iconPath ->
                    if(iconUrl == imageView.tag) { // check if icon in imgPreviewImage still for the same iconUrl should be displayed
                        showIcon(imageView, iconPath)
                    }
                }
            }
        }
    }

    private fun showIcon(imageView: ImageView, iconPath: File) {
        if (Looper.getMainLooper().thread == Thread.currentThread()) {
            imageView.setImageURI(Uri.fromFile(iconPath))
        } else {
            activity.runOnUiThread {
                imageView.setImageURI(Uri.fromFile(iconPath))
            }
        }
    }

}