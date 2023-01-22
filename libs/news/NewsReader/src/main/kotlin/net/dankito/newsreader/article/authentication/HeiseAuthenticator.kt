package net.dankito.newsreader.article.authentication

import net.dankito.utils.credentials.ICredentials
import net.dankito.utils.credentials.UsernamePasswordCredentials
import net.dankito.utils.web.client.Cookie
import net.dankito.utils.web.client.IWebClient
import net.dankito.utils.web.client.RequestParameters
import org.slf4j.LoggerFactory


class HeiseAuthenticator(private val webClient: IWebClient) {

    private val log = LoggerFactory.getLogger(HeiseAuthenticator::class.java)

    fun login(credentials: ICredentials): Cookie? {
        if (credentials is UsernamePasswordCredentials) {
            val loginHeaders = mapOf(
                "Content-Type" to "application/x-www-form-urlencoded"
            )
            val loginResponse = webClient.post(
                RequestParameters("https://www.heise.de/sso/login/login",
                "username=${credentials.username}&password=${credentials.password}&permanent=1&ajax=1",
                headers = loginHeaders)
            )

            if (loginResponse.isSuccessful) {
                loginResponse.getCookie("ssohls")?.let { loginCookie ->
                    log.info("Login was successful")
                    return loginCookie
                }
            }
        }

        log.info("Login to Heise failed")
        return null
    }
}