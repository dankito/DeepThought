package net.dankito.util.web


interface IWebClient {

    fun get(parameters: RequestParameters): WebClientResponse
    fun getAsync(parameters: RequestParameters, callback: (response: WebClientResponse) -> Unit)

    fun post(parameters: RequestParameters): WebClientResponse
    fun postAsync(parameters: RequestParameters, callback: (response: WebClientResponse) -> Unit)

    fun head(parameters: RequestParameters): WebClientResponse
    fun headAsync(parameters: RequestParameters, callback: (response: WebClientResponse) -> Unit)

}