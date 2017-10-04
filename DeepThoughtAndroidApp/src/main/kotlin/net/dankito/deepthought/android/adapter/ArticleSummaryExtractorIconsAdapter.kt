package net.dankito.deepthought.android.adapter

import android.app.Activity
import android.graphics.Bitmap
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.di.AppComponent
import net.dankito.deepthought.android.service.BitmapCache
import net.dankito.faviconextractor.Favicon
import net.dankito.utils.ImageCache
import javax.inject.Inject


class ArticleSummaryExtractorIconsAdapter(private val activity: Activity) : ListAdapter<Favicon>() {


    @Inject
    protected lateinit var imageCache: ImageCache

    private var bitmapCache: BitmapCache


    init {
        AppComponent.component.inject(this)

        bitmapCache = BitmapCache(imageCache)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val favicon = getItem(position)

        val view = convertView ?: LayoutInflater.from(parent?.context).inflate(R.layout.list_item_article_summary_extractor_config_icon, parent, false)

        showExtractorIcon(view.findViewById(R.id.imgArticleSummaryExtractorIcon) as ImageView, favicon)

        val txtArticleSummaryExtractorIconSize = view.findViewById(R.id.txtArticleSummaryExtractorIconSize) as TextView
        txtArticleSummaryExtractorIconSize.text = favicon.size?.getDisplayText() ?: ""

        return view
    }

    private fun showExtractorIcon(imageView: ImageView, favicon: Favicon) {
        imageView.tag = favicon.url
        imageView.setImageBitmap(null)

        favicon.url?.let { iconUrl ->
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
        }
        else {
            activity.runOnUiThread {
                imageView.setImageBitmap(bitmap)
            }
        }
    }

}