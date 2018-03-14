package net.dankito.deepthought.android.service

import android.app.Activity
import android.os.Build
import android.view.View
import android.webkit.*
import java.util.*
import kotlin.concurrent.schedule


class WebPageLoader(private val activity: Activity) {

    companion object {
        private const val GetHtmlCodeFromWebViewJavaScriptInterfaceName = "HtmlViewer"
    }


    private val webView = WebView(activity)

    private var firstRetrievedHtmlCallback: ((String) -> Unit)? = null

    private var siteLoadedCallback: ((String) -> Unit)? = null


    init {
        setupWebView()
    }


    private fun setupWebView() {
        fixThatWebViewIsLoadingVerySlow()

        webView.setWebViewClient(WebViewClient()) // to avoid that redirects open url in browser

        val settings = webView.settings
        settings.defaultTextEncodingName = "UTF-8" // otherwise non ASCII text doesn't get displayed correctly
        settings.domStorageEnabled = true // otherwise images may not load, see https://stackoverflow.com/questions/29888395/images-not-loading-in-android-webview
        settings.javaScriptEnabled = true // so that embedded videos etc. work

        webView.addJavascriptInterface(GetHtmlCodeFromWebViewJavaScripInterface { url, html -> siteFinishedLoading(url, html) }, GetHtmlCodeFromWebViewJavaScriptInterfaceName)

        webView.setWebChromeClient(object : WebChromeClient() {
            private var hasCompletelyFinishedLoadingPage = false
            private val timerCheckIfHasCompletelyFinishedLoadingPage = Timer()

            override fun onProgressChanged(webView: WebView, newProgress: Int) {
                super.onProgressChanged(webView, newProgress)

//                firstRetrievedHtmlCallbacks.remove(webView.url)?.let { it }

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


    inner class GetHtmlCodeFromWebViewJavaScripInterface(private val retrievedHtmlCode: ((url: String, html: String) -> Unit)) {

        @JavascriptInterface
        @SuppressWarnings("unused")
        fun finishedLoadingSite(url: String, html: String) {
            retrievedHtmlCode(url, html)
        }

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
        if(webView.url != null && webView.url != "about:blank" && webView.url.startsWith("data:text/html") == false) {
            getWebViewHtml("finishedLoadingSite")
        }
    }

    private fun getWebViewHtml(callbackMethodName: String) {
        webView.loadUrl("javascript:${GetHtmlCodeFromWebViewJavaScriptInterfaceName}.$callbackMethodName" +
                "(document.URL, '<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');")
    }

    private fun retrievedWebViewCurrentHtml(url: String, html: String) {
        activity.runOnUiThread {
            webView.removeJavascriptInterface(GetHtmlCodeFromWebViewJavaScriptInterfaceName)

            siteLoadedCallback?.invoke(html)
        }
    }

    private fun siteFinishedLoading(url: String, html: String) {
        activity.runOnUiThread {
            webView.removeJavascriptInterface(GetHtmlCodeFromWebViewJavaScriptInterfaceName)

            siteLoadedCallback?.invoke(html)
        }
    }


    fun loadUrl(url: String, firstRetrievedHtmlCallback: (html: String) -> Unit, siteLoadedCallback: (html: String) -> Unit) {
        this.firstRetrievedHtmlCallback = firstRetrievedHtmlCallback
        this.siteLoadedCallback = siteLoadedCallback

        clearWebView()

        webView.loadUrl(url)
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