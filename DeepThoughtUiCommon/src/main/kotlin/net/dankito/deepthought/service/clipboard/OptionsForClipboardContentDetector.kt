package net.dankito.deepthought.service.clipboard

import net.dankito.data_access.network.webclient.IWebClient
import net.dankito.data_access.network.webclient.RequestParameters
import net.dankito.deepthought.di.CommonComponent
import net.dankito.deepthought.files.MimeTypeService
import net.dankito.deepthought.model.Item
import net.dankito.deepthought.model.Source
import net.dankito.deepthought.model.util.ItemExtractionResult
import net.dankito.deepthought.news.article.ArticleExtractorManager
import net.dankito.deepthought.ui.IRouter
import net.dankito.utils.UrlUtil
import net.dankito.utils.localization.Localization
import net.dankito.utils.ui.IDialogService
import javax.inject.Inject


class OptionsForClipboardContentDetector(private val articleExtractorManager: ArticleExtractorManager, private val dialogService: IDialogService,
                                         private val mimeTypeService: MimeTypeService, private val router: IRouter) {


    @Inject
    protected lateinit var webClient: IWebClient

    @Inject
    protected lateinit var urlUtil: UrlUtil

    @Inject
    protected lateinit var localization: Localization


    init {
        CommonComponent.component.inject(this)
    }


    fun getOptionsAsync(clipboardContent: ClipboardContent, callback: (OptionsForClipboardContent) -> Unit) {
        clipboardContent.url?.let { url ->
            getOptionsForUrl(url, callback)
        }
    }

    private fun getOptionsForUrl(url: String, callback: (OptionsForClipboardContent) -> Unit) {
        webClient.headAsync(RequestParameters(url)) { response ->
            if(response.isSuccessful) {
                val contentType = response.getHeaderValue("Content-Type")

                if(contentType != null) {
                    val contentTypeWithoutEncoding = contentType.substringBefore(';').trim()

                    if(mimeTypeService.categorizer.isHtmlFile(contentTypeWithoutEncoding)) {
                        callback(createOptionsForWebPage(url))
                    }
                }
                else if(mimeTypeService.isHttpUrlAWebPage(url)) {
                    callback(createOptionsForWebPage(url))
                }
            }
        }
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
                    router.showEditItemView(ItemExtractionResult(Item(""), Source(webPageUrl, webPageUrl)))
                }
            )
        )
    }


    private fun extractItemFromUrl(url: String) {
        articleExtractorManager.extractArticleUserDidNotSeeBeforeAndAddDefaultDataAsync(url) {
            it.result?.let { router.showEditItemView(it) }
            it.error?.let { showErrorMessage(it, url) }
        }
    }

    private fun showErrorMessage(error: Exception, articleUrl: String) {
        dialogService.showErrorMessage(localization.getLocalizedString("alert.message.could.not.extract.item.from.url", articleUrl), exception = error)
    }

}