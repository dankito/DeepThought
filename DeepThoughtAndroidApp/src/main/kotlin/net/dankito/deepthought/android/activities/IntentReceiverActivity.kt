package net.dankito.deepthought.android.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import net.dankito.deepthought.android.di.AppComponent
import net.dankito.deepthought.android.service.ExtractArticleHandler
import net.dankito.deepthought.android.service.IntentHandler
import net.dankito.deepthought.ui.IRouter
import net.dankito.service.search.ISearchEngine
import net.dankito.utils.web.UrlUtil
import javax.inject.Inject


/**
 * An invisible activity that receives share intents.
 * I extracted this activity from MainActivity so that current activity stack is retained. Otherwise other open activities get destroyed.
 *
 * Invisibility is archived by setting theme in AndroidManifest to @android:style/Theme.NoDisplay
 */
// We are extending the normal Activity class here so that we can use Theme.NoDisplay, which is not supported by AppCompat activities
class IntentReceiverActivity : Activity() {


    @Inject
    protected lateinit var router: IRouter

    @Inject
    protected lateinit var urlUtil: UrlUtil

    @Inject
    protected lateinit var extractArticleHandler: ExtractArticleHandler

    @Inject
    protected lateinit var searchEngine: ISearchEngine


    init {
        AppComponent.component.inject(this)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) { // should actually almost never be called
        handleIntent(intent)

        super.onNewIntent(intent)
    }

    override fun onStart() {
        super.onStart()

        // to avoid Exception "An activity without a UI must call finish() before onResume() completes"
        finish()
    }


    private fun handleIntent(intent: Intent?) {
        if(intent == null) {
            return
        }

        IntentHandler(extractArticleHandler, searchEngine, router, urlUtil).handle(intent)
    }

}