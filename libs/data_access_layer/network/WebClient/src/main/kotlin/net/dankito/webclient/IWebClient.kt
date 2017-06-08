package net.dankito.webclient


interface IWebClient {

    fun get(parameters: RequestParameters): WebClientResponse
    fun getAsync(parameters: RequestParameters, callback: (response: WebClientResponse) -> Unit)

    fun post(parameters: RequestParameters): WebClientResponse
    fun postAsync(parameters: RequestParameters, callback: (response: WebClientResponse) -> Unit)

}