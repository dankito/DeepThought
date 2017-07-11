package net.dankito.deepthought.ui.view

import net.dankito.deepthought.model.ReadLaterArticle


interface IReadLaterArticleView {

    fun showArticles(readLaterArticles: List<ReadLaterArticle>)

}