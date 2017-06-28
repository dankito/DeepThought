package net.dankito.deepthought.android.adapter

import android.graphics.Bitmap
import android.os.Looper
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import kotlinx.android.synthetic.main.list_item_article_summary_extractor.view.*
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.di.AppComponent
import net.dankito.deepthought.android.service.utils.BitmapCache
import net.dankito.deepthought.model.ArticleSummaryExtractorConfig
import net.dankito.deepthought.news.summary.config.ArticleSummaryExtractorConfigManager
import net.dankito.utils.ImageCache
import javax.inject.Inject


class ArticleSummaryExtractorsAdapter(private val activity: AppCompatActivity, private val summaryExtractorsManager: ArticleSummaryExtractorConfigManager)
    : ListAdapter<ArticleSummaryExtractorConfig>(summaryExtractorsManager.getConfigs()) {


    @Inject
    protected lateinit var imageCache: ImageCache

    private var bitmapCache: BitmapCache


    init {
        AppComponent.component.inject(this)

        bitmapCache = BitmapCache(imageCache)
    }


    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val extractorConfig = getItem(position)

        val view = convertView ?: LayoutInflater.from(parent?.context).inflate(R.layout.list_item_article_summary_extractor, parent, false)

        showExtractorIcon(view, extractorConfig)

        view.txtExtractorName.text = extractorConfig.name


        view.chkIsFavorite.setOnCheckedChangeListener(null)

        view.chkIsFavorite.isChecked = extractorConfig.isFavorite

        view.chkIsFavorite.setOnCheckedChangeListener { _, isChecked ->
            summaryExtractorsManager.setFavoriteStatus(extractorConfig, isChecked)
        }


        view.tag = extractorConfig

        return view
    }

    private fun showExtractorIcon(view: View, extractorConfig: ArticleSummaryExtractorConfig) {
        val imageView = view.imgPreviewImage

        imageView.tag = extractorConfig.iconUrl
        imageView.setImageBitmap(null)

        extractorConfig.iconUrl?.let { iconUrl ->
            bitmapCache.getBitmapForUrlAsync(iconUrl) { result ->
                result.result?.let { bitmap ->
                    if(iconUrl == imageView.tag) { // check if icon in imgPreviewImage still for the same iconUrl should be displayed
                        showIcon(imageView, bitmap)
                    }
                }
            }
        }
    }

    private fun showIcon(imageView: ImageView, bitmap: Bitmap) {
        if (Looper.getMainLooper().thread == Thread.currentThread()) {
            imageView.setImageBitmap(bitmap)
        } else {
            activity.runOnUiThread {
                imageView.setImageBitmap(bitmap)
            }
        }
    }

}