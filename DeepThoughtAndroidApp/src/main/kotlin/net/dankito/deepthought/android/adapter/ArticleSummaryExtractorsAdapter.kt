package net.dankito.deepthought.android.adapter

import android.graphics.Bitmap
import android.os.Looper
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.adapter.viewholder.ArticleSummaryExtractorViewHolder
import net.dankito.deepthought.android.di.AppComponent
import net.dankito.deepthought.android.dialogs.ArticleSummaryExtractorConfigDialog
import net.dankito.deepthought.android.service.BitmapCache
import net.dankito.deepthought.model.ArticleSummaryExtractorConfig
import net.dankito.deepthought.news.summary.config.ArticleSummaryExtractorConfigManager
import net.dankito.utils.image.ImageCache
import javax.inject.Inject


class ArticleSummaryExtractorsAdapter(private val activity: AppCompatActivity, private val summaryExtractorsManager: ArticleSummaryExtractorConfigManager)
    : ListRecyclerSwipeAdapter<ArticleSummaryExtractorConfig, ArticleSummaryExtractorViewHolder>() {


    @Inject
    protected lateinit var imageCache: ImageCache

    private var bitmapCache: BitmapCache


    init {
        AppComponent.component.inject(this)

        bitmapCache = BitmapCache(imageCache)

        summaryExtractorsManager.addInitializationListener { updateConfigs() }
    }


    override fun getSwipeLayoutResourceId(position: Int) = R.id.articleSummaryExtractorSwipeLayout

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleSummaryExtractorViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.list_item_article_summary_extractor, parent, false)

        val viewHolder = ArticleSummaryExtractorViewHolder(itemView)

        viewHolderCreated(viewHolder)
        return viewHolder
    }


    override fun bindViewForNullValue(viewHolder: ArticleSummaryExtractorViewHolder) {
        super.bindViewForNullValue(viewHolder)

        viewHolder.imgPreviewImage.visibility = View.GONE
        viewHolder.txtExtractorName.visibility = View.GONE
        viewHolder.btnIsFavorite.visibility = View.GONE
        viewHolder.txtFavoriteIndex.visibility = View.GONE
    }

    override fun bindItemToView(viewHolder: ArticleSummaryExtractorViewHolder, item: ArticleSummaryExtractorConfig) {
        showExtractorIcon(viewHolder.imgPreviewImage, item)

        viewHolder.txtExtractorName.text = item.name


        if(item.isFavorite) {
            viewHolder.btnIsFavorite.setImageResource(R.drawable.ic_star_white_48dp)
        }
        else {
            viewHolder.btnIsFavorite.setImageResource(R.drawable.ic_star_border_white_48dp)
        }
        viewHolder.btnIsFavorite.setOnClickListener { summaryExtractorsManager.toggleFavoriteStatus(item) }


        viewHolder.txtFavoriteIndex.visibility = if(item.favoriteIndex != null) View.VISIBLE else View.GONE
        item.favoriteIndex?.let {
            viewHolder.txtFavoriteIndex.text = (it + 1).toString()
        }
    }

    override fun setupSwipeView(viewHolder: ArticleSummaryExtractorViewHolder, item: ArticleSummaryExtractorConfig) {
        viewHolder.btnEditArticleSummaryExtractorConfig.setOnClickListener {
            ArticleSummaryExtractorConfigDialog().editConfiguration(activity, item) { }
            closeSwipeView(viewHolder)
        }

        viewHolder.btnDeleteArticleSummaryExtractorConfig.setOnClickListener {
            summaryExtractorsManager.deleteConfig(item)
            closeSwipeView(viewHolder)
        }
    }


    private fun showExtractorIcon(imgPreviewImage: ImageView, extractorConfig: ArticleSummaryExtractorConfig) {
        imgPreviewImage.tag = extractorConfig.iconUrl
        imgPreviewImage.setImageBitmap(null)

        extractorConfig.iconUrl?.let { iconUrl ->
            bitmapCache.getBitmapForUrlAsync(iconUrl) { result ->
                result.result?.let { bitmap ->
                    if(iconUrl == imgPreviewImage.tag) { // check if icon in imgPreviewImage still for the same iconUrl should be displayed
                        showIcon(imgPreviewImage, bitmap)
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


    fun updateConfigs() {
        activity.runOnUiThread { updateConfigsOnUIThread() }
    }

    private fun updateConfigsOnUIThread() {
        items = summaryExtractorsManager.getConfigs()
    }

}