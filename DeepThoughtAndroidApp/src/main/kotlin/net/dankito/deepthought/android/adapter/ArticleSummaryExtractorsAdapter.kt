package net.dankito.deepthought.android.adapter

import android.net.Uri
import android.os.Looper
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import kotlinx.android.synthetic.main.list_item_article_summary_extractor.view.*
import net.dankito.data_access.filesystem.AndroidFileStorageService
import net.dankito.data_access.network.webclient.OkHttpWebClient
import net.dankito.deepthought.android.R
import net.dankito.deepthought.news.summary.config.ArticleSummaryExtractorConfig
import net.dankito.serializer.JacksonJsonSerializer
import net.dankito.utils.ImageCache
import java.io.File


class ArticleSummaryExtractorsAdapter(private val activity: AppCompatActivity, extractors: List<ArticleSummaryExtractorConfig>)
    : ListAdapter<ArticleSummaryExtractorConfig>(extractors) {


    val fileStorageService = AndroidFileStorageService(activity)

    val imageCache = ImageCache(OkHttpWebClient(), JacksonJsonSerializer(), fileStorageService) // TODO: inject


    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val extractorConfig = getItem(position)

        var view = convertView ?: LayoutInflater.from(parent?.context).inflate(R.layout.list_item_article_summary_extractor, parent, false)

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
                        showIcon(imageView, iconPath, view)
                    }
                }
            }
        }
    }

    private fun showIcon(imageView: ImageView, iconPath: File, view: View) {
        if (Looper.getMainLooper().thread == Thread.currentThread()) {
            imageView.setImageURI(Uri.fromFile(iconPath))
        } else {
            activity.runOnUiThread {
                imageView.setImageURI(Uri.fromFile(iconPath))
            }
        }
    }

}