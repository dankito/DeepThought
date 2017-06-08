package net.dankito.deepthought.android.routing

import android.content.Context
import android.content.Intent
import net.dankito.deepthought.android.activities.ArticleSummaryActivity
import net.dankito.deepthought.android.activities.ViewArticleActivity
import net.dankito.deepthought.news.summary.config.ArticleSummaryExtractorConfig
import net.dankito.newsreader.model.Article
import net.dankito.serializer.ISerializer


class Router(private val context: Context, private val serializer: ISerializer) {


    fun showArticleSummaryView(extractor: ArticleSummaryExtractorConfig) {
        val articleSummaryActivityIntent = Intent(context, ArticleSummaryActivity::class.java)

        articleSummaryActivityIntent.putExtra(ArticleSummaryActivity.EXTRACTOR_URL_INTENT_EXTRA_NAME, extractor.url)

        context.startActivity(articleSummaryActivityIntent)
    }

    fun showArticleView(article: Article) {
        val serializedArticle = serializer.serializeObject(article)

        val viewArticleIntent = Intent(context, ViewArticleActivity::class.java)

        viewArticleIntent.putExtra(ViewArticleActivity.ARTICLE_INTENT_EXTRA_NAME, serializedArticle)

        context.startActivity(viewArticleIntent)
    }

}