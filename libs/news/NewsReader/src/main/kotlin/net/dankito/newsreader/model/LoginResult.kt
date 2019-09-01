package net.dankito.newsreader.model

import net.dankito.utils.web.client.Cookie


class LoginResult(val cookies: List<Cookie>) {


    override fun toString(): String {
        return "${cookies.size} cookies for login:" + cookies.map { "$it" }.joinToString("", "\n")
    }

}