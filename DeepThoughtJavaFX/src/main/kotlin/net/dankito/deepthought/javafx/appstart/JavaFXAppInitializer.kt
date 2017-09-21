package net.dankito.deepthought.javafx.appstart

import net.dankito.deepthought.javafx.di.AppComponent
import net.dankito.deepthought.service.data.DataManager
import net.dankito.deepthought.ui.html.HtmlEditorExtractor
import net.dankito.service.search.ISearchEngine
import net.dankito.utils.localization.UTF8ResourceBundleControl
import tornadofx.*
import java.util.*
import javax.inject.Inject


class JavaFXAppInitializer {

    // DataManager currently initializes itself, so inject DataManager here so that it start asynchronously initializing itself in parallel to creating UI and therefore
    // speeding app start up a bit.
    // That's also the reason why LuceneSearchEngine gets injected here so that as soon as DataManager is initialized it can initialize its indices

    @Inject
    protected lateinit var dataManager: DataManager

    @Inject
    protected lateinit var searchEngine: ISearchEngine

    @Inject
    protected lateinit var communicationManagerStarter: CommunicationManagerStarter // same here: just create instance, CommunicationManagerStarter initializes itself

    @Inject
    protected lateinit var htmlEditorExtractor: HtmlEditorExtractor


    fun initializeApp() {
        AppComponent.component.inject(this)

        setupMessagesResources()

        initializeHtmlEditorExtractor()
    }

    private fun setupMessagesResources() {
        ResourceBundle.clearCache() // at this point default ResourceBundles are already created and cached. In order that ResourceBundle created below takes effect cache has to be clearedbefore
        FX.messages = ResourceBundle.getBundle("Messages", UTF8ResourceBundleControl())
    }


    private fun initializeHtmlEditorExtractor() {
        // start extracting HtmlEditor only after DataManager is initialized as both to a lot of disk i/o
        dataManager.addInitializationListener {
            htmlEditorExtractor.extractHtmlEditorIfNeededAsync()

            htmlEditorExtractor.addHtmlEditorExtractedListener {
                searchEngine.addInitializationListener {
                    // TODO: preload html editors
                }
            }
        }
    }

}