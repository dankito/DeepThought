package net.dankito.deepthought.service.clipboard

import net.dankito.deepthought.di.CommonComponent
import net.dankito.deepthought.model.Item
import net.dankito.deepthought.model.Source
import net.dankito.deepthought.model.util.ItemExtractionResult
import net.dankito.deepthought.news.article.ArticleExtractorManager
import net.dankito.deepthought.ui.IRouter
import net.dankito.utils.MimeTypeUtil
import net.dankito.utils.UrlUtil
import net.dankito.utils.localization.Localization
import net.dankito.utils.ui.IDialogService
import javax.inject.Inject


class OptionsForClipboardContentDetector(private val articleExtractorManager: ArticleExtractorManager, private val dialogService: IDialogService, private val router: IRouter) {


    @Inject
    protected lateinit var urlUtil: UrlUtil

    @Inject
    protected lateinit var mimeTypeUtil: MimeTypeUtil

    @Inject
    protected lateinit var localization: Localization


    init {
        CommonComponent.component.inject(this)
    }


    fun getOptions(clipboardContent: ClipboardContent): OptionsForClipboardContent? {
        clipboardContent.url?.let { url ->
            if(mimeTypeUtil.isHttpUrlAWebPage(url)) {
                return createOptionsForWebPage(url)
            }
        }

        return null
    }

    private fun createOptionsForWebPage(webPageUrl: String): OptionsForClipboardContent {
        return OptionsForClipboardContent(localization.getLocalizedString("clipboard.content.header.create.item.from", urlUtil.getHostName(webPageUrl) ?: ""),
                listOf(
                        ClipboardContentOption(localization.getLocalizedString("clipboard.content.option.try.to.extract.important.web.page.parts")) {
                            extractItemFromUrl(webPageUrl)
                        },
//                        ClipboardContentOption(localization.getLocalizedString("clipboard.content.option.extract.plain.text.only")) {
//                            // TODO
//                        },
                        ClipboardContentOption(localization.getLocalizedString("clipboard.content.option.show.original.page")) {
                            router.showEditEntryView(ItemExtractionResult(Item(""), Source(webPageUrl, webPageUrl)))
                        }
                )
        )
    }


    private fun extractItemFromUrl(url: String) {
        articleExtractorManager.extractArticleUserDidNotSeeBeforeAndAddDefaultDataAsync(url) {
            it.result?.let { router.showEditEntryView(it) }
            it.error?.let { showErrorMessage(it, url) }
        }
    }

    private fun showErrorMessage(error: Exception, articleUrl: String) {
        dialogService.showErrorMessage(localization.getLocalizedString("alert.message.could.not.extract.item.from.url", articleUrl), exception = error)
    }

}