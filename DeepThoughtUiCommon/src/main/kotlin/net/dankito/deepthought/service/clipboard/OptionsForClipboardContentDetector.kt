package net.dankito.deepthought.service.clipboard

import net.dankito.data_access.network.webclient.IWebClient
import net.dankito.data_access.network.webclient.RequestParameters
import net.dankito.deepthought.di.CommonComponent
import net.dankito.deepthought.files.FileManager
import net.dankito.deepthought.files.MimeTypeService
import net.dankito.deepthought.model.FileLink
import net.dankito.deepthought.model.Item
import net.dankito.deepthought.model.Source
import net.dankito.deepthought.news.article.ArticleExtractorManager
import net.dankito.deepthought.ui.IRouter
import net.dankito.utils.IPlatformConfiguration
import net.dankito.util.UrlUtil
import net.dankito.util.localization.Localization
import net.dankito.utils.services.network.download.IFileDownloader
import net.dankito.utils.ui.IDialogService
import java.io.File
import java.util.*
import javax.inject.Inject


class OptionsForClipboardContentDetector(private val articleExtractorManager: ArticleExtractorManager, private val fileManager: FileManager, private val dialogService: IDialogService,
                                         private val mimeTypeService: MimeTypeService, private val platformConfiguration: IPlatformConfiguration, private val router: IRouter) {


    @Inject
    protected lateinit var webClient: IWebClient

    @Inject
    protected lateinit var fileDownloader: IFileDownloader

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
            if(response.isSuccessful && response.isSuccessResponse) {
                val contentType = response.getHeaderValue("Content-Type")

                if(contentType != null) {
                    val contentTypeWithoutEncoding = contentType.substringBefore(';').trim()

                    getOptionsForMimeType(url, contentTypeWithoutEncoding, callback)
                }
                else {
                    getOptionsForHttpUrl(url, callback)
                }
            }
        }
    }

    private fun getOptionsForHttpUrl(url: String, callback: (OptionsForClipboardContent) -> Unit) {
        if(mimeTypeService.isHttpUrlAWebPage(url)) {
            callback(getOptionsForWebPage(url))
        }
        else { // TODO: are there actually any files that cannot be downloaded?
            mimeTypeService.getBestMimeType(url)?.let { mimeType ->
                callback(getOptionsForDownloadableFile(url, mimeType))
            }
        }
    }

    private fun getOptionsForMimeType(url: String, mimeType: String, callback: (OptionsForClipboardContent) -> Unit) {
        if(mimeTypeService.categorizer.isHtmlFile(mimeType)) {
            callback(getOptionsForWebPage(url))
        }
        else { // TODO: are there actually any files that cannot be downloaded?
            callback(getOptionsForDownloadableFile(url, mimeType))
        }
    }

    private fun getOptionsForWebPage(webPageUrl: String): OptionsForClipboardContent {
        return OptionsForClipboardContent(localization.getLocalizedString("clipboard.content.header.create.item.from", urlUtil.getHostName(webPageUrl) ?: ""),
                createOptionsForWebPage(webPageUrl)
        )
    }

    private fun createOptionsForWebPage(webPageUrl: String): List<ClipboardContentOption> {
        return listOf(
            ClipboardContentOption(localization.getLocalizedString("clipboard.content.option.create.item.from.web.page")) {
                extractItemFromUrl(it, webPageUrl)
            }
        )
    }

    private fun getOptionsForDownloadableFile(url: String, mimeType: String): OptionsForClipboardContent {
        return OptionsForClipboardContent(localization.getLocalizedString("clipboard.content.header.clipboard.contains.file", urlUtil.getFileName(url)),
                createOptionsForDownloadableFile(url, mimeType)
        )
    }

    private fun createOptionsForDownloadableFile(url: String, mimeType: String): List<ClipboardContentOption> {
        val options = mutableListOf<ClipboardContentOption>()

        if(mimeTypeService.categorizer.isPdfFile(mimeType)) {
            options.add(ClipboardContentOption(localization.getLocalizedString("clipboard.content.option.download.and.show.file")) {
                downloadFileAndShowFile(it, url, mimeType)
            })
        }

        options.add(ClipboardContentOption(localization.getLocalizedString("clipboard.content.option.download.file.and.attach.to.item")) {
            downloadFileAndAttachToItem(it, url, mimeType)
        })

        options.add(ClipboardContentOption(localization.getLocalizedString("clipboard.content.option.download.file.and.attach.to.source")) {
            downloadFileAndAttachToSource(it, url, mimeType)
        })

        return options
    }


    private fun extractItemFromUrl(option: ClipboardContentOption, url: String) {
        option.setIndeterminateProgressState()

        articleExtractorManager.extractArticleUserDidNotSeeBeforeAndAddDefaultDataAsync(url) {
            option.setActionDone()

            it.result?.let { router.showEditItemView(it) }
            it.error?.let { showCouldNotExtractItemFromUrlErrorMessage(it, url) }
        }
    }

    private fun downloadFileAndAttachToItem(option: ClipboardContentOption, url: String, mimeType: String) {
        downloadFile(option, url, mimeType) { downloadedFile ->
            val item = Item("")

            item.addAttachedFile(downloadedFile)
            item.source = createSourceForDownloadedFile(downloadedFile, url, false)

            router.showEditItemView(item)
        }
    }

    private fun downloadFileAndAttachToSource(option: ClipboardContentOption, url: String, mimeType: String) {
        downloadFile(option, url, mimeType) { downloadedFile ->
            val source = createSourceForDownloadedFile(downloadedFile, url)

            router.showEditSourceView(source)
        }
    }

    private fun downloadFileAndShowFile(option: ClipboardContentOption, url: String, mimeType: String) {
        downloadFile(option, url, mimeType) { downloadedFile ->
            if(mimeTypeService.categorizer.isPdfFile(mimeType)) {
                router.showPdfView(downloadedFile, createSourceForDownloadedFile(downloadedFile, url))
            }
        }
    }

    private fun createSourceForDownloadedFile(downloadedFile: FileLink, url: String, attachFileToSource: Boolean = true): Source {
        val source = Source(downloadedFile.name, url)
        source.lastAccessDate = Date()

        if(attachFileToSource) {
            source.addAttachedFile(downloadedFile)
        }

        return source
    }

    private fun downloadFile(option: ClipboardContentOption, url: String, mimeType: String, callback: (downloadedFile: FileLink) -> Unit) {
        val destination = getDestinationFileForUrl(url)

        fileDownloader.downloadAsync(url, destination) { downloadState ->
            option.updateIsExecutingState(downloadState.progress)

            if(downloadState.finished && downloadState.successful) {
                val downloadedFile = fileManager.createDownloadedLocalFile(url, destination, mimeType)
                callback(downloadedFile)
            }

            downloadState.error?.let { error ->
                showCouldNotDownloadFileErrorMessage(error, url)
            }
        }
    }

    private fun getDestinationFileForUrl(url: String): File {
        val filename = urlUtil.getFileName(url)

        return platformConfiguration.getDefaultSavePathForFile(filename, mimeTypeService.getFileType(filename))
    }

    private fun showCouldNotExtractItemFromUrlErrorMessage(error: Exception, articleUrl: String) {
        dialogService.showErrorMessage(localization.getLocalizedString("alert.message.could.not.extract.item.from.url", articleUrl), exception = error)
    }

    private fun showCouldNotDownloadFileErrorMessage(error: Exception, url: String) {
        dialogService.showErrorMessage(localization.getLocalizedString("alert.message.could.not.download.file.from.url", url), exception = error)
    }

}