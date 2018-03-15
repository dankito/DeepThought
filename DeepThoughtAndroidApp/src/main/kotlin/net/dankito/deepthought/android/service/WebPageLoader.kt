package net.dankito.deepthought.android.service

import android.app.Activity
import android.os.Build
import android.view.View
import android.webkit.*
import net.dankito.data_access.network.webclient.IWebClient
import net.dankito.data_access.network.webclient.RequestParameters
import net.dankito.deepthought.android.di.AppComponent
import java.util.*
import javax.inject.Inject
import kotlin.concurrent.schedule


class WebPageLoader(private val activity: Activity) {

    companion object {
        private const val GetHtmlCodeFromWebViewJavaScriptInterfaceName = "HtmlViewer"
    }


    @Inject
    protected lateinit var webClient: IWebClient


    private val webView = WebView(activity)

    private var siteLoadedCallback: ((String) -> Unit)? = null

    private var getHtmlCallback: ((String, String) -> Unit)? = null


    init {
        AppComponent.component.inject(this)

        setupWebView()
    }


    private fun setupWebView() {
        fixThatWebViewIsLoadingVerySlow()

        webView.setWebViewClient(WebViewClient()) // to avoid that redirects open url in browser

        val settings = webView.settings
        settings.defaultTextEncodingName = "UTF-8" // otherwise non ASCII text doesn't get displayed correctly
        settings.domStorageEnabled = true // otherwise images may not load, see https://stackoverflow.com/questions/29888395/images-not-loading-in-android-webview
        settings.javaScriptEnabled = true // so that embedded videos etc. work

        // adding JavaScriptInterface just before the call to getWebViewHtml() does not work as injected object will only appear in JavaScript next time web page is loaded
        // so i had to introduce getHtmlCallback
        webView.addJavascriptInterface(GetHtmlCodeFromWebViewJavaScripInterface { url, html -> getHtmlCallback?.invoke(url, html) }, GetHtmlCodeFromWebViewJavaScriptInterfaceName)

        webView.setWebChromeClient(object : WebChromeClient() {
            private var hasCompletelyFinishedLoadingPage = false
            private val timerCheckIfHasCompletelyFinishedLoadingPage = Timer()

            override fun onProgressChanged(webView: WebView, newProgress: Int) {
                super.onProgressChanged(webView, newProgress)

                if(newProgress < 100) {
                    hasCompletelyFinishedLoadingPage = false
                }
                else {
                    hasCompletelyFinishedLoadingPage = true

                    timerCheckIfHasCompletelyFinishedLoadingPage.schedule(1000L) { // 100 % may only means a part of the page but not the whole page is loaded -> wait some time and check if not loading another part of the page
                        if(hasCompletelyFinishedLoadingPage) {
                            webPageCompletelyLoaded()
                        }
                    }
                }
            }
        })
    }


    private fun fixThatWebViewIsLoadingVerySlow() {
        // avoid that WebView is loading very, very slow, see https://stackoverflow.com/questions/7422427/android-webview-slow
        // but actually had no effect on my side

        webView.settings.cacheMode = WebSettings.LOAD_NO_CACHE

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            webView.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        }
        else {
            webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null)

            @Suppress("DEPRECATION")
            webView.settings.setRenderPriority(WebSettings.RenderPriority.HIGH)
        }
    }


    private fun webPageCompletelyLoaded() {
        activity.runOnUiThread { webPageCompletelyLoadedOnUiThread() }
    }

    private fun webPageCompletelyLoadedOnUiThread() {
        getWebViewHtml { _, html ->
            if(html != "<html><head></head><body></body></html>") { // don't know where this is coming from but definitely filter them out
                siteLoadedCallback?.invoke(html)

                webView.removeJavascriptInterface(GetHtmlCodeFromWebViewJavaScriptInterfaceName)
            }
        }
    }


    private fun getWebViewHtml(htmlRetrieved: (url: String, html: String) -> Unit) {
        getHtmlCallback = htmlRetrieved

        webView.loadUrl("javascript:${GetHtmlCodeFromWebViewJavaScriptInterfaceName}.currentHtmlCode" +
                "(document.URL, '<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');")
    }

    inner class GetHtmlCodeFromWebViewJavaScripInterface(private val retrievedHtmlCode: ((url: String, html: String) -> Unit)) {

        @JavascriptInterface
        @SuppressWarnings("unused")
        fun currentHtmlCode(url: String, html: String) {
            activity.runOnUiThread {
                retrievedHtmlCode(url, html)
            }
        }

    }


    fun loadUrl(url: String, retrievedBaseHtmlCallback: (html: String) -> Unit, siteLoadedCallback: (html: String) -> Unit) {
        this.siteLoadedCallback = siteLoadedCallback

        loadWebSiteBaseHtml(url, retrievedBaseHtmlCallback)

        clearWebView() // TODO: remove
    }

    private fun loadWebSiteBaseHtml(url: String, retrievedBaseHtmlCallback: (html: String) -> Unit) {
        webClient.getAsync(RequestParameters(url)) { response ->
            response.body?.let { webSiteBaseHtml ->
                retrievedWebSiteBaseHtml(url, webSiteBaseHtml, retrievedBaseHtmlCallback)
            }

            response.error?.let {
                // TODO: what to do in error case?
                activity.runOnUiThread {
                    webView.loadUrl(url)
                }
            }
        }
    }

    private fun retrievedWebSiteBaseHtml(url: String, webSiteBaseHtml: String, retrievedBaseHtmlCallback: (html: String) -> Unit) {
        activity.runOnUiThread {
            retrievedBaseHtmlCallback(webSiteBaseHtml)

            webView.loadDataWithBaseURL(url, webSiteBaseHtml, "text/html; charset=UTF-8", "utf-8", null)
        }
    }

    private fun clearWebView() {
        if(Build.VERSION.SDK_INT < 18) {
            @Suppress("DEPRECATION")
            webView.clearView()
        }
        else {
            webView.loadUrl("about:blank")
        }
    }

}