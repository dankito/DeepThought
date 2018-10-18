package net.dankito.faviconextractor

import net.dankito.utils.Size


data class Favicon(val url : String, val iconType : FaviconType, var size : Size? = null, val type : String? = null) {

}